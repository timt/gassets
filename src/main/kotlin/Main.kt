package io.shaka

import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    embeddedServer(Netty, port = 8000) {
        routing {
             get ("/") {
                call.respondText("Hello, world!")
            }
            staticResources("/static", "static")
            singlePageApplication{
                useResources = true
                defaultPage = "main.html"
                react("spa")
            }
        }
    }.start(wait = true)
}