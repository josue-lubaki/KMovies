package ca.josue_lubaki.kmovies.android.common

import androidx.navigation.NavType
import androidx.navigation.navArgument
import ca.josue_lubaki.kmovies.android.common.Destination.Home.route

/**
 * created by Josue Lubaki
 * date : 2023-04-30
 * version : 1.0.0
 */

sealed class Destination(
    val title : String,
    val route : String,
    val routeWithArgs: String
){
    object Home : Destination(
        title = "Movies",
        route = "home",
        routeWithArgs = route
    )

    object MovieDetail : Destination(
        title = "Movie Details",
        route = "detail",
        routeWithArgs = "$route/{movieId}"
    )

    val arguments = listOf(
        navArgument(name= "movieId"){
            type = NavType.IntType
        }
    )
}
