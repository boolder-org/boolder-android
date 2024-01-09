package com.boolder.boolder.view.areadetails.areaproblems

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.ProblemItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AreaProblemsScreen(
    screenState: AreaProblemsViewModel.ScreenState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onProblemClicked: (Problem) -> Unit
) {
    var isInSearchMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (screenState !is AreaProblemsViewModel.ScreenState.Content) return@TopAppBar

                    if (isInSearchMode) {
                        val focusRequester = remember { FocusRequester() }
                        var searchQuery by remember { mutableStateOf("") }

                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 48.dp)
                                .focusRequester(focusRequester),
                            value = searchQuery,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            onValueChange = {
                                searchQuery = it
                                onSearchQueryChanged(it)
                            },
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.padding(0.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isBlank()) {
                                        Text(
                                            text = "Search",
                                            color = Color.LightGray
                                        )
                                    }

                                    innerTextField()
                                }
                            }
                        )

                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    } else {
                        Text(text = screenState.areaName)
                    }
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
                },
                actions = {
                    IconButton(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(shape = CircleShape)
                            .clickable(onClick = onBackPressed),
                        onClick = {
                            isInSearchMode = !isInSearchMode

                            if (!isInSearchMode) onSearchQueryChanged("")
                        },
                        content = {
                            val iconRes = if (isInSearchMode) {
                                R.drawable.ic_close
                            } else {
                                R.drawable.ic_search
                            }

                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    )
                }
            )
        },
        content = {
            when (screenState) {
                is AreaProblemsViewModel.ScreenState.Loading -> LoadingContent()
                is AreaProblemsViewModel.ScreenState.Content -> AreaProblemsScreenContent(
                    problems = screenState.problems,
                    popularProblems = screenState.popularProblems,
                    contentPadding = it,
                    onProblemClicked = onProblemClicked
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
private fun AreaProblemsScreenContent(
    problems: List<Problem>,
    popularProblems: List<Problem>,
    contentPadding: PaddingValues,
    onProblemClicked: (Problem) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    ) {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text =  { Text(stringResource(id = R.string.area_problems_tab_all)) }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text =  { Text(stringResource(id = R.string.area_problems_tab_popular)) }
            )
        }

        val bottomInset = max(
            WindowInsets.ime.asPaddingValues().calculateBottomPadding(),
            contentPadding.calculateBottomPadding()
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = bottomInset + 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
        ) {
            val problemsToDisplay = if (selectedTabIndex == 0) problems else popularProblems

            itemsIndexed(
                items = problemsToDisplay,
                key = { _, item -> "problem-${item.id}" }
            ) { index, problem ->
                val shape = RoundedCornerShape(
                    topStart = if (index == 0) 16.dp else 0.dp,
                    topEnd = if (index == 0) 16.dp else 0.dp,
                    bottomStart = if (index == problemsToDisplay.lastIndex) 16.dp else 0.dp,
                    bottomEnd = if (index == problemsToDisplay.lastIndex) 16.dp else 0.dp
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
    }
}

@Preview
@Composable
private fun AreaProblemsScreenPreview(
    @PreviewParameter(AreaProblemsScreenPreviewParameterProvider::class)
    screenState: AreaProblemsViewModel.ScreenState
) {
    BoolderTheme {
        AreaProblemsScreen(
            screenState = screenState,
            onBackPressed = {},
            onSearchQueryChanged = {},
            onProblemClicked = {}
        )
    }
}

private class AreaProblemsScreenPreviewParameterProvider : PreviewParameterProvider<AreaProblemsViewModel.ScreenState> {
    override val values = sequenceOf(
        AreaProblemsViewModel.ScreenState.Loading,
        AreaProblemsViewModel.ScreenState.Content(
            areaName = "Rocher Canon",
            problems = List(15) {
                dummyProblem(
                    id = it,
                    featured = it < 3
                )
            },
            popularProblems = List(3) {
                dummyProblem(
                    id = it,
                    featured = true
                )
            }
        )
    )

}
