package eu.qrpth.sanctions.api

data class Sanctions(val data: List<Entity>)

data class Entity(
        val source: String,
        val id: Long,
        val datesOfBirth: List<String>,
        val aliases: List<Alias>
) {
    constructor(entity: eu.qrpth.sanctions.domain.Entity): this(
            source = entity.id.source,
            id = entity.id.id,
            datesOfBirth = entity.datesOfBirth,
            aliases = entity.aliases.map { Alias(it) }
    )
}

data class Alias(
        val names: List<String>,
        val wholeName: String
) {
    constructor(alias: eu.qrpth.sanctions.domain.Alias): this(names = alias.names, wholeName = alias.wholeName)
}
