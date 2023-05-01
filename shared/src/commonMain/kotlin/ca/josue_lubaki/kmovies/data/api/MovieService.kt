package ca.josue_lubaki.kmovies.data.api

import ca.josue_lubaki.kmovies.data.model.MovieApiResponse
import ca.josue_lubaki.kmovies.data.model.MovieRemote
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal class MovieService : KtorApi() {
    suspend fun getMovies(page: Int = 1) : MovieApiResponse = client.get {
        pathUrl("movie/popular")
        parameter("page", page)
    }.body()

    suspend fun getMovie(id: Int) : MovieRemote = client.get {
        pathUrl("movie/$id")
    }.body()
}