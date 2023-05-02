package ca.josue_lubaki.kmovies.android.detail

import ca.josue_lubaki.kmovies.domain.model.Movie

data class DetailState(
    var loading : Boolean = false,
    var movie : Movie? = null,
    var errorMessage : String? = null,
)
