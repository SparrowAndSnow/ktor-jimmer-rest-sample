package com.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.server.application.*
import org.babyfish.jimmer.sql.dialect.MySqlDialect
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.SqlFormatter

fun database(environment: ApplicationEnvironment): KSqlClient {
    return newKSqlClient {
        setConnectionManager {
            HikariPool(HikariConfig().apply {
                driverClassName = environment.config.property("datasource.driver").getString()
                jdbcUrl = environment.config.property("datasource.url").getString()
                username = environment.config.property("datasource.username").getString()
                password = environment.config.property("datasource.password").getString()
                maximumPoolSize = 10
                connectionTimeout = 30000
            }).connection.use {
                proceed(it)
            }
        }
        setExecutor(Executor.log())
        setSqlFormatter(SqlFormatter.PRETTY)

        when (environment.config.property("datasource.name").getString()) {
            "mysql" -> setDialect(MySqlDialect())
            "postgres" -> setDialect(PostgresDialect())
        }
    }
}
