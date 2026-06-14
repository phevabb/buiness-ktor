package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.jdbc.Database

suspend fun Application.configureExposed() {
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/ktor_business",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "postgres"
    )

    val userService = ExposedUserService(database).also {
        it.createSchema()
    }

    routing {
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toUInt()
                ?: throw IllegalArgumentException("Invalid ID")

            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/users/{id}") {
            val id = call.parameters["id"]?.toUInt()
                ?: throw IllegalArgumentException("Invalid ID")

            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/users/{id}") {
            val id = call.parameters["id"]?.toUInt()
                ?: throw IllegalArgumentException("Invalid ID")

            userService.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
