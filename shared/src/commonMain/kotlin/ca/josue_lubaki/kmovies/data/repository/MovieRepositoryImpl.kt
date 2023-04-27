package ca.josue_lubaki.kmovies.data.repository

import ca.josue_lubaki.kmovies.data.datasource.RemoteDataSource
import ca.josue_lubaki.kmovies.data.mapper.toDomain
import ca.josue_lubaki.kmovies.domain.model.Movie
import ca.josue_lubaki.kmovies.domain.repository.MovieRepository

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal class MovieRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : MovieRepository {
    override suspend fun getMovies(page: Int): List<Movie> {
        return remoteDataSource.getMovies(page).results.map {
            it.toDomain()
        }
    }

    override suspend fun getMovie(id: Int): Movie {
        return remoteDataSource.getMovie(id).toDomain()
    }
}