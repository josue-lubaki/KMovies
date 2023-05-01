package ca.josue_lubaki.kmovies.domain.usecases

import ca.josue_lubaki.kmovies.domain.repository.MovieRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

class GetMoviesUseCase : KoinComponent {
    private val movieRepository by inject<MovieRepository>()

    @Throws(Exception::class)
    suspend operator fun invoke(page: Int) = movieRepository.getMovies(page)
}