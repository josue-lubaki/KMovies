package ca.josue_lubaki.kmovies.android.common

import Destination
import Home
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * created by Josue Lubaki
 * date : 2023-04-30
 * version : 1.0.0
 */

@Composable
fun MovieAppBar(
    modifier : Modifier = Modifier,
    canNavigateBack : Boolean,
    currentScreen : Destination,
    onNavigateBack : () -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = 4.dp,
        color = MaterialTheme.colors.primary
    ){

        Row(
            modifier = Modifier
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedVisibility(
                visible = canNavigateBack,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
            }

            Text(
                text = currentScreen.title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Preview
@Composable
fun MovieAppBarPreview() {
    MovieAppBar(
        canNavigateBack = false,
        currentScreen = Home,
        onNavigateBack = {}
    )
}