package ca.josue_lubaki.kmovies.domain.repository

import ca.josue_lubaki.kmovies.domain.model.Movie

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal interface MovieRepository {
    suspend fun getMovies(page: Int = 1) : List<Movie>
    suspend fun getMovie(id: Int) : Movie
}