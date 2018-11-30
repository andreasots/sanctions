package eu.qrpth.sanctions.domain

import io.ebean.annotation.DbArray
import javax.persistence.*
import javax.persistence.Entity

@Embeddable
data class EntityId(var source: String, var id: Long)

@Entity
@Table(name = "entities")
data class Entity (
        @Id var id: EntityId,
        var type: String,
        @DbArray
        var datesOfBirth: List<String>
) {
    @OneToMany
    @JoinColumns(
            JoinColumn(name = "entity_id", referencedColumnName = "id"),
            JoinColumn(name = "entity_source", referencedColumnName = "source")
    )
    var aliases: List<Alias> = listOf()
}

@Entity
@Table(name = "aliases")
data class Alias(
        @DbArray
        var names: List<String>,
        var wholeName: String,

        @ManyToOne
        @JoinColumns(
                JoinColumn(name = "entity_id", referencedColumnName = "id"),
                JoinColumn(name = "entity_source", referencedColumnName = "source")
        )
        var entity: eu.qrpth.sanctions.domain.Entity
) {
    @Id
    var id: Long = 0
}