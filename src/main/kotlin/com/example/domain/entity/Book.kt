package com.example.domain.entity

import io.ktor.server.application.*
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.kt.KTransientResolver
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.math.BigDecimal

@Entity
interface Book : BaseEntity {
    @Key
    val name: String

    @Key
    val edition: Int

    val price: BigDecimal

    @ManyToOne
    val store: BookStore?

    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_ID"
    )
    val authors: List<Author>

    @Formula(dependencies = ["name"])
    val nameUpperCase: String?
        get() = name.uppercase()

    @Transient(ApplicationEnvironmentResolver::class)
    val env: Map<String, Any?>
}

@Single
class ApplicationEnvironmentResolver: KTransientResolver<Long, Map<String, Any?>>, KoinComponent {

    private val env by inject<ApplicationEnvironment>()

    override fun resolve(ids: Collection<Long>): Map<Long, Map<String, Any?>> {
        return ids.map { it to mapOf(
            "host" to env.config.host,
            "port" to env.config.port
        ) }.toMap()
    }
}
