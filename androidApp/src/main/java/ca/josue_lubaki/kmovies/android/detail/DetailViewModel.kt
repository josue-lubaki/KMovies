package ca.josue_lubaki.kmovies.android.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.josue_lubaki.kmovies.domain.usecases.GetMovieUseCase
import kotlinx.coroutines.launch

/**
 * created by Josue Lubaki
 * date : 2023-05-02
 * version : 1.0.0
 */

class DetailViewModel(
    val getMovieUseCase: GetMovieUseCase,
    val movieId: Int
) : ViewModel() {
    var uiState by mutableStateOf(DetailState())

    init{
        loadMovie(movieId)
    }

    private fun loadMovie(movieId : Int) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true,)

            uiState = try {
                val movie = getMovieUseCase(movieId)

                uiState.copy(
                    loading = false,
                    movie = movie,
                )
            } catch (e : Throwable) {
                uiState.copy(
                    loading = false,
                    errorMessage = e.message,
                )
            }
        }
    }
}