package ca.josue_lubaki.kmovies.data.api

import ca.josue_lubaki.kmovies.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal abstract class KtorApi {
    protected val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    fun HttpRequestBuilder.pathUrl(pathString: String) {
        url {
            takeFrom(BuildKonfig.BASE_URL)
            path("3", pathString)
        }
        parameter("api_key", BuildKonfig.API_KEY)
    }
}