package ca.josue_lubaki.kmovies

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform