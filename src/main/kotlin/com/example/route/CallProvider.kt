package com.example.route

import io.ktor.server.routing.*

interface CallProvider {
    val call: RoutingCall
}
