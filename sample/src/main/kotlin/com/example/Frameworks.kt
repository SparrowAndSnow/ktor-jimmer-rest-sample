package com.example

import io.ktor.server.application.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.eimsound.ktor.jimmer.rest.plugin.*

@Module
@ComponentScan("com.example")
class ApplicationModule

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<ApplicationEnvironment> { environment }
            single<KSqlClient> { database(environment) }
        })
        modules(ApplicationModule().module)
    }
    install(JimmerRest){
        jimmerSqlClient = inject<KSqlClient>().value
    }
}

