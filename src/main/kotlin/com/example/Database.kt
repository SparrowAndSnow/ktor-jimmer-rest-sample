package com.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.server.application.*
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.SqlFormatter

fun database(environment: ApplicationEnvironment): KSqlClient {
    return newKSqlClient {
        setConnectionManager {
            HikariPool(HikariConfig().apply {
                driverClassName = environment.config.property("jdbc.driver").getString()
                jdbcUrl = environment.config.property("jdbc.url").getString()
                username = environment.config.property("jdbc.username").getString()
                password = environment.config.property("jdbc.password").getString()
                maximumPoolSize = 10
                connectionTimeout = 30000
            }).connection.use {
                proceed(it)
            }
        }
        setExecutor(Executor.log())
        setSqlFormatter(SqlFormatter.PRETTY)
        setDialect(PostgresDialect())
    }
}