package com.harrisonog.taper.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.harrisonog.taper.R
import com.harrisonog.taper.ui.TaperScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaperAppBar(
    currentScreen: TaperScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    ) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewTaperAppBar() {
    TaperAppBar(
        currentScreen = TaperScreen.Main,
        canNavigateBack = false,
        navigateUp = {},
    )
}

@Preview
@Composable
fun PreviewTaperAppBarEdit() {
    TaperAppBar(
        currentScreen = TaperScreen.Edit,
        canNavigateBack = true,
        navigateUp = {},
    )
}