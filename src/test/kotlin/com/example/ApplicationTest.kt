package com.example

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        environment {
            config = config.mergeWith(
                MapApplicationConfig(
                    "datasource.name" to "mysql",
                    "datasource.driver" to "com.mysql.cj.jdbc.Driver",
                    "datasource.url" to "jdbc:mysql://localhost:3306/ktor_jimmer",
                    "datasource.username" to "root",
                    "datasource.password" to "123456",
                )
            )
        }
        application {
            module()
        }
        client.get("/book").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
