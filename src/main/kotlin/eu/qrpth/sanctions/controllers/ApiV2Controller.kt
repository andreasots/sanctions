package eu.qrpth.sanctions.controllers

import eu.qrpth.sanctions.api.Sanctions
import eu.qrpth.sanctions.domain.Entity
import io.ebean.EbeanServer
import io.ebean.Expr
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.annotation.Nullable
import javax.inject.Inject

@Controller("/api/v2")
class ApiV2Controller(@Inject val ebeanServer: EbeanServer) {
    @Get("sanctions{?name,type:person}")
    fun sanctions(@Nullable name: String?, @Nullable type: String?): Sanctions {
        val type = type ?: "person"
        if (name == null) {
            return Sanctions(listOf())
        }

        val query = ebeanServer.find(Entity::class.java)
                .fetch("aliases")
        val where = query.where()
                .add(Expr.eq("type", type))
        for (fragment in name.split(Regex("\\s+"))) {
            where.add(Expr.ilike("aliases.wholeName", "%$fragment%"))
        }

        return Sanctions(query.findList().map { eu.qrpth.sanctions.api.Entity(it) })
    }
}
