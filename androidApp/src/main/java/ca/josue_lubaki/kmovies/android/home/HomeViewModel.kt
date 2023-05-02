package ca.josue_lubaki.kmovies.android.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.josue_lubaki.kmovies.domain.usecases.GetMoviesUseCase
import kotlinx.coroutines.launch

/**
 * created by Josue Lubaki
 * date : 2023-04-30
 * version : 1.0.0
 */

class HomeViewModel(
    val getMoviesUseCase: GetMoviesUseCase,
) : ViewModel() {
    var uiState by mutableStateOf(HomeState())
    private var currentPage = 1

    init {
        loadMovies(forceReload = false)
    }

    fun loadMovies(forceReload : Boolean = false){
        if(uiState.loading) return
        if(forceReload) currentPage = 1
        if(currentPage == 1) uiState = uiState.copy(refreshing = true)

        viewModelScope.launch {
            uiState = uiState.copy(
                loading = true,
            )

            try {
                val resultMovies = getMoviesUseCase(currentPage)
                val movies = if(currentPage == 1) resultMovies else uiState.movies + resultMovies

                currentPage += 1

                uiState = uiState.copy(
                    loading = false,
                    refreshing = false,
                    loadFinished = resultMovies.isEmpty(),
                    movies = movies,
                )

            } catch (e : Throwable) {
                uiState = uiState.copy(
                    loading = false,
                    refreshing = false,
                    loadFinished = true,
                    errorMessage = e.message,
                )
            }
        }
    }
}