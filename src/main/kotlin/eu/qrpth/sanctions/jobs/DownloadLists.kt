package eu.qrpth.sanctions.jobs

import eu.qrpth.sanctions.domain.Alias
import eu.qrpth.sanctions.domain.Entity
import eu.qrpth.sanctions.domain.EntityId
import eu.qrpth.sanctions.xsd.eu.ExportType
import eu.qrpth.sanctions.xsd.eu.SubjectTypeClassificationCodeType
import eu.qrpth.sanctions.xsd.ofac.SdnList
import eu.qrpth.sanctions.xsd.un.CONSOLIDATEDLIST
import io.ebean.EbeanServer
import io.micronaut.context.annotation.Prototype
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.xml.bind.JAXBContext
import javax.xml.transform.stream.StreamSource

const val UN_LIST_URL: String = "https://scsanctions.un.org/resources/xml/en/consolidated.xml"
const val EU_LIST_URL: String = "https://webgate.ec.europa.eu/europeaid/fsd/fsf/public/files/xmlFullSanctionsList_1_1/content?token=n002ew13"
const val OFAC_LIST_URL: String = "https://www.treasury.gov/ofac/downloads/sdn.xml"

@Prototype
class DownloadLists(
        @Inject
        @Client("https://scsanctions.un.org/")
        private val httpClient: RxStreamingHttpClient,

        @Inject
        private val ebeanServer: EbeanServer
) : Job {
    override fun execute(ctx: JobExecutionContext?) {
        val (entities, aliases) = Single.concatArray(fetchEuList(), fetchUnList(), fetchOfacList())
                .collectInto(Pair(mutableListOf<Entity>(), mutableListOf<Alias>()), { result, list ->
                    result.first.addAll(list.first)
                    result.second.addAll(list.second)
                })
                .blockingGet()

        ebeanServer.beginTransaction().use {
            ebeanServer.createQuery(Alias::class.java).delete()
            ebeanServer.createQuery(Entity::class.java).delete()

            ebeanServer.insertAll(entities)
            ebeanServer.insertAll(aliases.toSet())

            it.commit()
        }
    }

    private fun fetchEuList(): Single<Pair<List<Entity>, List<Alias>>> = httpClient.dataStream(HttpRequest.GET<Void>(EU_LIST_URL))
            .collectInto(ByteArrayOutputStream(), { buffer, incoming -> buffer.write(incoming.toByteArray()) })
            .map {
                val ctx = JAXBContext.newInstance("eu.qrpth.sanctions.xsd.eu")
                val unmarshaller = ctx.createUnmarshaller()
                val list = unmarshaller.unmarshal(StreamSource(ByteArrayInputStream(it.toByteArray())), ExportType::class.java)

                // EU sanctions list contains multiple entries for the same entity and those entries share a `logicalId`.
                val entities = mutableMapOf<EntityId, Entity>()
                val aliases = mutableListOf<Alias>()

                for (entry in list.value.sanctionEntity) {
                    val type = when (entry.subjectType.classificationCode!!) {
                        SubjectTypeClassificationCodeType.E -> "entity"
                        SubjectTypeClassificationCodeType.P -> "person"
                    }

                    val id = EntityId("EU", entry.logicalId)
                    val entity = entities[id] ?: Entity(id = id, type = type, datesOfBirth = listOf())

                    val datesOfBirth = mutableSetOf(*entity.datesOfBirth.toTypedArray())
                    for (dateOfBirth in entry.birthdate) {
                        var str = ""

                        if (dateOfBirth.isCirca) {
                            str += "~"
                        }

                        if (dateOfBirth.birthdate != null) {
                            str += "%04d-%02d-%02d".format(dateOfBirth.birthdate.year, dateOfBirth.birthdate.month, dateOfBirth.birthdate.day)
                        } else {
                            if (dateOfBirth.year == null && dateOfBirth.monthOfYear == null && dateOfBirth.dayOfMonth == null) {
                                continue
                            } else if (dateOfBirth.year != null && dateOfBirth.monthOfYear == null && dateOfBirth.dayOfMonth == null) {
                                str += "%04d".format(dateOfBirth.year)
                            } else if (dateOfBirth.year == null && dateOfBirth.monthOfYear != null && dateOfBirth.dayOfMonth != null) {
                                str += "--%02d-%02d".format(dateOfBirth.monthOfYear, dateOfBirth.dayOfMonth)
                            } else {
                                val year = dateOfBirth.year ?: 0
                                val month = dateOfBirth.monthOfYear ?: 0
                                val day = dateOfBirth.dayOfMonth ?: 0
                                str += "%04d-%02d-%02d".format(year, month, day)
                            }
                        }

                        datesOfBirth.add(str)
                    }

                    entity.datesOfBirth = datesOfBirth.toList()
                    entities.put(id, entity)

                    entry.nameAlias.mapTo(aliases) { alias ->
                        Alias(
                                names = listOf(alias.firstName, alias.middleName, alias.lastName).filter { !it.isBlank() },
                                wholeName = alias.wholeName,
                                entity = entity
                        )
                    }
                }

                Pair(entities.values.toList(), aliases)
            }

    private fun fetchUnList(): Single<Pair<List<Entity>, List<Alias>>> = httpClient.dataStream(HttpRequest.GET<Void>(UN_LIST_URL))
            .collectInto(ByteArrayOutputStream(), { buffer, incoming -> buffer.write(incoming.toByteArray()) })
            .map {
                val ctx = JAXBContext.newInstance("eu.qrpth.sanctions.xsd.un")
                val unmarshaller = ctx.createUnmarshaller()
                val list = unmarshaller.unmarshal(StreamSource(ByteArrayInputStream(it.toByteArray())), CONSOLIDATEDLIST::class.java)

                val entities = mutableListOf<Entity>()
                val aliases = mutableListOf<Alias>()

                for (entry in list.value.individuals.individual) {
                    val datesOfBirth = mutableSetOf<String>()
                    loop@ for (dateOfBirth in entry.individualdateofbirth) {
                        datesOfBirth.add(when {
                            dateOfBirth.date != null -> "%04d-%02d-%02d".format(dateOfBirth.date.year, dateOfBirth.date.month, dateOfBirth.date.day)
                            dateOfBirth.year != null -> "%04d".format(dateOfBirth.year)
                            else -> {
                                if (dateOfBirth.fromyear == null && dateOfBirth.toyear == null) {
                                    continue@loop
                                }
                                "%04d - %04d".format(dateOfBirth.fromyear, dateOfBirth.toyear)
                            }
                        })
                    }

                    for (alias in entry.individualalias) {
                        if (alias.dateofbirth != null) {
                            datesOfBirth.add(alias.dateofbirth)
                        }
                    }

                    val entity = Entity(id = EntityId("UN", entry.dataid.longValueExact()), type = "person", datesOfBirth = datesOfBirth.sorted())
                    entities.add(entity)

                    val names = listOf(entry.firstname, entry.secondname, entry.thirdname, entry.fourthname).filter { it != null && !it.isBlank() }
                    aliases.add(Alias(
                            names = names,
                            wholeName = names.joinToString(" "),
                            entity = entity
                    ))

                    for (alias in entry.individualalias) {
                        aliases.add(Alias(
                                names = listOf(),
                                wholeName = alias.aliasname,
                                entity = entity
                        ))
                    }
                }

                for (entry in list.value.entities.entity) {
                    val entity = Entity(id = EntityId("UN", entry.dataid.longValueExact()), type = "entity", datesOfBirth = listOf())
                    entities.add(entity)

                    aliases.add(Alias(
                            names = listOf(),
                            wholeName = entry.firstname,
                            entity = entity
                    ))
                    if (entry.nameoriginalscript != null && !entry.nameoriginalscript.isBlank()) {
                        aliases.add(Alias(
                                names = listOf(),
                                wholeName = entry.nameoriginalscript,
                                entity = entity
                        ))
                    }
                }

                Pair(entities, aliases)
            }

    private fun fetchOfacList(): Single<Pair<List<Entity>, List<Alias>>> = httpClient.dataStream(HttpRequest.GET<Void>(OFAC_LIST_URL))
            .collectInto(ByteArrayOutputStream(), { buffer, incoming -> buffer.write(incoming.toByteArray()) })
            .map {
                val ctx = JAXBContext.newInstance("eu.qrpth.sanctions.xsd.ofac")
                val unmarshaller = ctx.createUnmarshaller()
                val list = unmarshaller.unmarshal(StreamSource(ByteArrayInputStream(it.toByteArray())), SdnList::class.java)

                val entities = mutableListOf<Entity>()
                val aliases = mutableListOf<Alias>()

                loop@ for (entry in list.value.sdnEntry) {
                    val type = when (entry.sdnType) {
                        "Individual" -> "person"
                        "Entity" -> "entity"
                        else -> continue@loop
                    }
                    val rawDatesOfBirth = entry.dateOfBirthList?.dateOfBirthItem ?: listOf()
                    val datesOfBirth = rawDatesOfBirth.map { it.dateOfBirth }
                    val entity = Entity(id = EntityId("OFAC", entry.uid.toLong()), type = type, datesOfBirth = datesOfBirth)
                    entities.add(entity)

                    val names = listOfNotNull(entry.firstName, entry.lastName).filter { !it.isBlank() }
                    aliases.add(Alias(
                            names = names,
                            wholeName = names.joinToString(" "),
                            entity = entity
                    ))

                    val rawAliases = entry.akaList?.aka ?: listOf()
                    rawAliases
                            .map { alias -> listOfNotNull(alias.firstName, alias.lastName).filter { !it.isBlank() } }
                            .mapTo(aliases) {
                                Alias(
                                        names = it,
                                        wholeName = it.joinToString(" "),
                                        entity = entity
                                )
                            }
                }

                Pair(entities, aliases)
            }
}
