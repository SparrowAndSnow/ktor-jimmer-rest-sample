package com.order

import io.ktor.server.application.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.eimsound.ktor.plugin.*
import com.eimsound.ktor.validator.exception.ValidationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.orbitz.consul.Consul
import io.ktor.client.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

@Module
@ComponentScan("com.example")
class ApplicationModule

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<ApplicationEnvironment> { environment }
            single<KSqlClient> { database(environment) }
            single<Consul>(createdAtStart = true) { consul(environment) }
            single<HttpClient> { httpClient(environment) }
            single<ObjectMapper> { ObjectMapper().apply { registerModule() } }
        })
        modules(ApplicationModule().module)
    }
    install(JimmerRest) {
        jimmerSqlClientFactory {
            inject<KSqlClient>()
        }
        pageConfiguration {
            defaultPageSize = 10
            defaultPageIndex = 0
            pageIndexParameterName = "pageIndex"
            pageSizeParameterName = "pageSize"
//            pageFactory = { rows, totalCount, source ->
//                MyPage(
//                    rows,
//                    totalCount,
//                    source.pageIndex.toLong(),
//                    source.pageSize.toLong()
//                )
//            }
        }
    }

    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(cause.httpStatusCode, cause.errors)
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.InternalServerError)
        }
    }
}

