package ca.josue_lubaki.kmovies.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieApiResponse(
    val results: List<MovieRemote>,
)
