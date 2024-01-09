package com.boolder.boolder.view.areadetails.areacircuit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.areadetails.composable.SectionContainer
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.Orange
import com.boolder.boolder.view.compose.ProblemItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AreaCircuitScreen(
    screenState: AreaCircuitViewModel.ScreenState,
    onBackPressed: () -> Unit,
    onProblemClicked: (Problem) -> Unit,
    onSeeOnMapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (screenState !is AreaCircuitViewModel.ScreenState.Content) return@TopAppBar

                    Text(
                        text = stringResource(
                            id = R.string.circuit,
                            screenState.circuitColor.localizedName()
                        )
                    )
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(shape = CircleShape)
                            .clickable(onClick = onBackPressed)
                            .padding(8.dp),
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null
                    )
                }
            )
        },
        content = {
            when (screenState) {
                is AreaCircuitViewModel.ScreenState.Loading -> LoadingContent()
                is AreaCircuitViewModel.ScreenState.Content -> AreaCircuitScreenContent(
                    modifier = Modifier.padding(it),
                    screenState = screenState,
                    onProblemClicked = onProblemClicked,
                    onSeeOnMapClicked = onSeeOnMapClicked
                )
            }
        }
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun AreaCircuitScreenContent(
    screenState: AreaCircuitViewModel.ScreenState.Content,
    onProblemClicked: (Problem) -> Unit,
    onSeeOnMapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.LightGray.copy(alpha = .25f)),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 80.dp
            )
        ) {
            if (screenState.isBeginnerFriendly) {
                infoItem(
                    iconRes = R.drawable.ic_sentiment_satisfied_alt,
                    textRes = R.string.area_circuit_beginner_friendly,
                    tint = primaryColor
                )
            }

            if (screenState.isDangerous) {
                infoItem(
                    iconRes = R.drawable.ic_error_outline,
                    textRes = R.string.area_circuit_dangerous,
                    tint = Color.Orange
                )
            }

            itemsIndexed(
                items = screenState.problems,
                key = { _, item -> item.id }
            ) { index, problem ->
                val shape = RoundedCornerShape(
                    topStart = if (index == 0) 16.dp else 0.dp,
                    topEnd = if (index == 0) 16.dp else 0.dp,
                    bottomStart = if (index == screenState.problems.lastIndex) 16.dp else 0.dp,
                    bottomEnd = if (index == screenState.problems.lastIndex) 16.dp else 0.dp
                )

                ProblemItem(
                    modifier = Modifier
                        .clip(shape = shape)
                        .background(color = Color.White)
                        .clickable { onProblemClicked(problem) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    problem = problem,
                    showFeatured = true
                )
            }
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onClick = onSeeOnMapClicked
        ) {
            Text(text = stringResource(id = R.string.area_circuit_see_on_map))
        }
    }
}

private fun LazyListScope.infoItem(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    tint: Color
) {
    item {
        SectionContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = tint
                )

                Text(
                    text = stringResource(id = textRes),
                    color = tint
                )
            }
        }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
}

@Preview
@Composable
private fun AreaCircuitScreenPreview(
    @PreviewParameter(AreaCircuitScreenPreviewParameterProvider::class)
    screenState: AreaCircuitViewModel.ScreenState
) {
    BoolderTheme {
        AreaCircuitScreen(
            screenState = screenState,
            onBackPressed = {},
            onProblemClicked = {},
            onSeeOnMapClicked = {}
        )
    }
}

private class AreaCircuitScreenPreviewParameterProvider : PreviewParameterProvider<AreaCircuitViewModel.ScreenState> {
    override val values = sequenceOf(
        AreaCircuitViewModel.ScreenState.Loading,
        AreaCircuitViewModel.ScreenState.Content(
            circuitColor = CircuitColor.ORANGE,
            isBeginnerFriendly = true,
            isDangerous = true,
            problems = List(15) { dummyProblem(id = it) }
        )
    )
}
