package ca.josue_lubaki.kmovies.data.datasource

import ca.josue_lubaki.kmovies.data.api.MovieService
import ca.josue_lubaki.kmovies.data.model.MovieApiResponse
import ca.josue_lubaki.kmovies.data.model.MovieRemote
import ca.josue_lubaki.kmovies.util.Dispatcher
import kotlinx.coroutines.withContext

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal interface RemoteDataSource {
    suspend fun getMovies(page: Int = 1) : MovieApiResponse
    suspend fun getMovie(id: Int) : MovieRemote
}