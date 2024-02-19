package com.boolder.boolder.view.ticklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.BoolderYellow
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.compose.ProblemIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TickListScreen(
    screenState: TickListViewModel.ScreenState,
    onProblemClicked: (Problem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .padding(bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar))
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.tab_tick_list))
                }
            )
        },
        content = {
            when (screenState) {
                is TickListViewModel.ScreenState.Loading -> LoadingScreen(
                    modifier = Modifier.padding(top = it.calculateTopPadding())
                )
                is TickListViewModel.ScreenState.Content -> TickListScreenContent(
                    screenState = screenState,
                    contentPadding = it,
                    onProblemClicked = onProblemClicked
                )
            }
        }
    )
}

@Composable
private fun TickListScreenContent(
    screenState: TickListViewModel.ScreenState.Content,
    contentPadding: PaddingValues,
    onProblemClicked: (Problem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (screenState.problems.isEmpty()) {
        TickListEmptyState(contentPadding = contentPadding)
        
        return
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = screenState.problems,
            key = { _, problem -> problem.id }
        ) { index, problem ->
            if (index == 0 || screenState.problems[index - 1].areaName != problem.areaName) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(bottom = 8.dp),
                    text = problem.areaName.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            ProblemItem(
                problem = problem,
                onProblemClicked = { onProblemClicked(problem) }
            )
        }
    }
}

@Composable
private fun TickListEmptyState(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.tick_list_empty_state_body),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProblemItem(
    problem: Problem,
    onProblemClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .clickable(onClick = onProblemClicked)
            .padding(16.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProblemIcon(problem = problem)

        Text(
            modifier = Modifier.weight(1f),
            text = problem.name.orEmpty()
        )

        problem.tickStatus?.let {
            val (iconRes, iconTint) = when (it) {
                TickStatus.PROJECT -> R.drawable.ic_star to Color.BoolderYellow
                TickStatus.SUCCEEDED -> R.drawable.ic_check_circle to MaterialTheme.colorScheme.primary
            }

            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint
            )
        }

        Text(text = problem.grade.orEmpty())
    }
}

@Preview
@Composable
private fun TickListScreenPreview(
    @PreviewParameter(TickListScreenPreviewParameterProvider::class)
    screenState: TickListViewModel.ScreenState
) {
    BoolderTheme {
        TickListScreen(
            screenState = screenState,
            onProblemClicked = {}
        )
    }
}

private class TickListScreenPreviewParameterProvider : PreviewParameterProvider<TickListViewModel.ScreenState> {
    override val values = sequenceOf(
        TickListViewModel.ScreenState.Loading,
        TickListViewModel.ScreenState.Content(problems = emptyList()),
        TickListViewModel.ScreenState.Content(
            problems = List(4) {
                dummyProblem(
                    id = it,
                    areaName = if (it < 2) "Sector 1" else "Sector 9",
                    tickStatus = if (it % 2 == 0) TickStatus.SUCCEEDED else TickStatus.PROJECT
                )
            }
        )
    )
}
