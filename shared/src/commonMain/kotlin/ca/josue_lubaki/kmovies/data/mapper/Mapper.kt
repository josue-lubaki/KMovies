package ca.josue_lubaki.kmovies.data.mapper

import ca.josue_lubaki.kmovies.data.model.MovieRemote
import ca.josue_lubaki.kmovies.domain.model.Movie

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal fun MovieRemote.toDomain() : Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterImage = getImageUrl(posterImage),
        releaseDate = releaseDate
    )
}

private fun getImageUrl(posterImage: String): String = "https://image.tmdb.org/t/p/w500/$posterImage"
