package ca.josue_lubaki.kmovies.data.datasourceimpl

import ca.josue_lubaki.kmovies.data.api.MovieService
import ca.josue_lubaki.kmovies.data.datasource.RemoteDataSource
import ca.josue_lubaki.kmovies.data.model.MovieApiResponse
import ca.josue_lubaki.kmovies.data.model.MovieRemote
import ca.josue_lubaki.kmovies.util.Dispatcher
import kotlinx.coroutines.withContext

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal class RemoteDataSourceImpl(
    private val movieService: MovieService,
    private val dispatcher: Dispatcher
) : RemoteDataSource  {
    override suspend fun getMovie(id: Int): MovieRemote {
        return withContext(dispatcher.io) {
            movieService.getMovie(id)
        }
    }

    override suspend fun getMovies(page: Int): MovieApiResponse {
        return withContext(dispatcher.io) {
            movieService.getMovies(page)
        }
    }
}