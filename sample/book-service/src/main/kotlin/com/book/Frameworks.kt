package com.book

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
import com.orbitz.consul.Consul
import io.ktor.server.plugins.statuspages.StatusPages
import com.eimsound.ktor.validator.jimmerRestErrors
import tools.jackson.databind.json.JsonMapper

@Module
@ComponentScan("com.example")
class ApplicationModule

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<ApplicationEnvironment> { environment }
            single<KSqlClient> { database(environment) }
//            single<Consul>(createdAtStart = true) { consul(environment) }
//            single<HttpClient> { httpClient(environment) }
            single<JsonMapper> { JsonMapper().apply { registeredModules() } }
        })
        modules(ApplicationModule().module)
    }
    install(JimmerRest) {
        jimmerSqlClientFactory {
            inject<KSqlClient>()
        }
        parser {
            register<IntRange> {
                val split = split("-")
                IntRange(split[0].toInt(), split[1].toInt())
            }
        }

        pager {
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
        jimmerRestErrors()
    }
}
