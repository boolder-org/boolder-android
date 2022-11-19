package com.boolder.boolder.data.network

import com.boolder.boolder.data.network.`object`.TopoRemote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KtorClient {

    private val client = HttpClient(OkHttp) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun loadTopoPicture(topoId: Int): Result<TopoRemote> {
        return try {
            val request = client.get("https://www.boolder.com/api/v1/topos/$topoId")
            val body = request.body<TopoRemote>()
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}