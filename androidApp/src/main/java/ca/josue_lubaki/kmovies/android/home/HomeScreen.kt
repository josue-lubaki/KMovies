package ca.josue_lubaki.kmovies.android.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.josue_lubaki.kmovies.android.components.MovieListItem
import ca.josue_lubaki.kmovies.android.ui.theme.Colors
import ca.josue_lubaki.kmovies.domain.model.Movie

/**
 * created by Josue Lubaki
 * date : 2023-04-30
 * version : 1.0.0
 */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeState,
    loadNextMovies: (Boolean) -> Unit,
    navigateToDetails: (Movie) -> Unit
) {

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.refreshing,
        onRefresh = { loadNextMovies(true) }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .pullRefresh(state = pullRefreshState)
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            itemsIndexed(
                items = uiState.movies,
                key = { _, movie -> movie.id }
            ) { index, movie ->
                MovieListItem(
                    movie = movie,
                    onClick = { navigateToDetails(movie) },
                )

                if (
                    index >= uiState.movies.size - 1
                    && !uiState.loading
                    && !uiState.loadFinished
                ) {
                    LaunchedEffect(Unit) {
                        loadNextMovies(false)
                    }
                }
            }

            if(uiState.loading && uiState.movies.isNotEmpty()){
                item(span = { GridItemSpan(2) }){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        CircularProgressIndicator(
                            color = Colors.Red
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        modifier = Modifier,
        uiState = HomeState(),
        loadNextMovies = { true },
        navigateToDetails = { }
    )
}