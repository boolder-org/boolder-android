package com.boolder.boolder.data.network

import android.util.Log
import com.boolder.boolder.data.network.model.TopoRemote
import com.boolder.boolder.data.network.model.TopoUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
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
            Log.e("Ktor Client", e.message ?: "No message")
            Result.failure(e)
        }
    }

    suspend fun loadTopoPicturesForArea(areaId: Int): Result<List<TopoUrl>> {
        return try {
            val request = client.get("https://www.boolder.com/api/v1/areas/$areaId/topos.json")
            val body = request.body<List<TopoUrl>>()
            Result.success(body)
        } catch (e: Exception) {
            Log.e("Ktor Client", e.message ?: "No message")
            Result.failure(e)
        }
    }
}
