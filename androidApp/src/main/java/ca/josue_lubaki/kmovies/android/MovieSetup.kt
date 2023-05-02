package ca.josue_lubaki.kmovies.android


import Detail
import Home
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.josue_lubaki.kmovies.android.common.MovieAppBar
import ca.josue_lubaki.kmovies.android.detail.DetailScreen
import ca.josue_lubaki.kmovies.android.detail.DetailViewModel
import ca.josue_lubaki.kmovies.android.home.HomeScreen
import ca.josue_lubaki.kmovies.android.home.HomeViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import moviesDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * created by Josue Lubaki
 * date : 2023-05-02
 * version : 1.0.0
 */

@Composable
fun MovieSetup() {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val scaffoldState = rememberScaffoldState()

    val isSystemDark= isSystemInDarkTheme()
    val statusBarColor = if (isSystemDark) {
        MaterialTheme.colors.primaryVariant
    } else {
        Color.Transparent
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isSystemDark
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = moviesDestination.find {
        backStackEntry?.destination?.route == it.route ||
                backStackEntry?.destination?.route == it.routeWithArgs
    } ?: Home

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MovieAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    ) { innerPaddings ->
        NavHost(
            navController = navController,
            startDestination = Home.routeWithArgs,
            modifier = Modifier.padding(innerPaddings)
        ){
            composable(Home.routeWithArgs){
                val homeViewModel : HomeViewModel = koinViewModel()
                HomeScreen(
                    uiState = homeViewModel.uiState,
                    loadNextMovies = {
                        homeViewModel.loadMovies(forceReload = false)
                    },
                    navigateToDetails = { movie ->
                        navController.navigate("${Detail.route}/${movie.id}")
                    }
                )
            }

            composable(Detail.routeWithArgs){
                val movieId = it.arguments?.getString("movieId") ?: "0"
                val detailViewModel : DetailViewModel = koinViewModel(
                    parameters = {
                        parametersOf(movieId.toInt())
                    }
                )
                DetailScreen(uiState = detailViewModel.uiState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieAppPreview() {
    MovieSetup()
}