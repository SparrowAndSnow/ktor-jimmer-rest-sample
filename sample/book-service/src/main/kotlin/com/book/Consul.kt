package com.book
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import com.orbitz.consul.model.agent.ImmutableRegCheck
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.util.*
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.server.application.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConsulFeature(val consulUrl: String) {

    class Config {
        var consulUrl: String = "http://localhost:8500"
        fun build(): ConsulFeature = ConsulFeature(consulUrl)
    }

    companion object Feature : HttpClientPlugin<Config, ConsulFeature>, KoinComponent {
        val consulClient by inject<Consul>()

        var currentNodeIndex: Int = 0

        override val key = AttributeKey<ConsulFeature>("ConsulFeature")

        override fun prepare(block: Config.() -> Unit): ConsulFeature = Config().apply(block).build()

        //轮询策略
        override fun install(feature: ConsulFeature, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) {
//                var consulClient = Consul.builder().withUrl(feature.consulUrl).build()
                val nodes = consulClient.healthClient().getHealthyServiceInstances(context.url.host).response
                val selectedNode = nodes[currentNodeIndex]
                context.url.host = selectedNode.service.address
                context.url.port = selectedNode.service.port
                currentNodeIndex = (currentNodeIndex + 1) % nodes.size
                println("Calling ${selectedNode.service.id}: ${context.url.buildString()}")
            }
        }
    }
}

fun consul(environment: ApplicationEnvironment): Consul {
    val consulUrl = environment.config.property("consul.url").getString()
    val port = environment.config.port
    val address = environment.config.host
    val name = environment.config.property("name").getString()

    val consul = Consul.builder().withUrl(consulUrl).build()
    val service = ImmutableRegistration.builder()
        .id("${name}-${port}")
        .name(name)
        .address(address)
        .port(port)
        .check(ImmutableRegCheck.http("http://$address:$port/health",5))
        .build()
    consul.agentClient().register(service)
    println("Registered ${service.id} at ${service.address}:${service.port}")
    return consul
}


