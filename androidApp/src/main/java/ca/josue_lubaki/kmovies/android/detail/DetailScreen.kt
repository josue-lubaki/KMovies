package ca.josue_lubaki.kmovies.android.detail


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.josue_lubaki.kmovies.android.R
import ca.josue_lubaki.kmovies.android.ui.theme.Colors
import ca.josue_lubaki.kmovies.domain.model.Movie
import coil.compose.AsyncImage

/**
 * created by Josue Lubaki
 * date : 2023-05-02
 * version : 1.0.0
 */

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    uiState : DetailState
) {
    Box(
        contentAlignment = Alignment.Center,
    ){
        uiState.movie?.let { movie ->
            DetailContent(
                movie = movie,
                modifier = modifier,
            )

            if(uiState.loading){
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ){
                    CircularProgressIndicator(
                        color = Colors.Red
                    )
                }
            }
        }

    }
}

@Composable
fun DetailContent(
    movie: Movie,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {

        AsyncImage(
            model = movie.posterImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp)
        ){
            Text (
                text = movie.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Colors.Red,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play_button),
                    contentDescription = null,
                    tint = Color.White,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = "Start watching now")

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Released in ${movie.releaseDate}".uppercase(),
                style = MaterialTheme.typography.overline,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = movie.overview,
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    DetailScreen(
        modifier = Modifier.fillMaxSize(),
        uiState = DetailState(
            movie = Movie(
                id = 1,
                title = "Movie Title",
                posterImage = "https://image.tmdb.org/t/p/w780/8/8b/8b8b8b8b8.jpg",
                releaseDate = "1980-01-01",
                overview = "Movie Overview",
            )
        )
    )
}