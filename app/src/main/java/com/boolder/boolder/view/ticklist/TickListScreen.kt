package com.boolder.boolder.view.ticklist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
    onFilterSelected: (TickListViewModel.TickListFilter) -> Unit,
    onExportTickListClicked: () -> Unit,
    onImportTickListClicked: () -> Unit,
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
                },
                actions = {
                    var showOverflowActions by remember { mutableStateOf(false) }

                    IconButton(
                        modifier = Modifier.clip(CircleShape),
                        onClick = { showOverflowActions = !showOverflowActions },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more_vert),
                                contentDescription = stringResource(id = R.string.tick_list_action_export),
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                            if (showOverflowActions) {
                                DropdownMenu(
                                    expanded = true,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    onDismissRequest = { showOverflowActions = false },
                                    content = {
                                        TickListOverflowActions(
                                            onExportTickListClicked = {
                                                showOverflowActions = false
                                                onExportTickListClicked()
                                            },
                                            onImportTickListClicked = {
                                                showOverflowActions = false
                                                onImportTickListClicked()
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    )
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
                    onProblemClicked = onProblemClicked,
                    onFilterSelected = onFilterSelected
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
    onFilterSelected: (TickListViewModel.TickListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!screenState.hasSavedProblems) {
        TickListEmptyState(contentPadding = contentPadding)
        
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        TickListFilterRow(
            selectedFilter = screenState.selectedFilter,
            onFilterSelected = onFilterSelected,
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding() + 8.dp)
                .padding(horizontal = 16.dp)
        )

        if (screenState.problems.isEmpty()) {
            TickListFilteredEmptyState(selectedFilter = screenState.selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
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
                            color = MaterialTheme.colorScheme.onSurface,
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
    }
}

@Composable
private fun TickListFilterRow(
    selectedFilter: TickListViewModel.TickListFilter,
    onFilterSelected: (TickListViewModel.TickListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = TickListViewModel.TickListFilter.values()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            val filter = filters[index]

            TickListFilterChip(
                selected = selectedFilter == filter,
                filter = filter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun TickListFilterChip(
    selected: Boolean,
    filter: TickListViewModel.TickListFilter,
    onClick: () -> Unit
) {
    ElevatedFilterChip(
        selected = selected,
        shape = CircleShape,
        colors = FilterChipDefaults.elevatedFilterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        ),
        label = { Text(text = stringResource(id = filter.labelRes())) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = filter.iconRes()),
                contentDescription = null
            )
        },
        onClick = onClick
    )
}

@Composable
private fun TickListFilteredEmptyState(
    selectedFilter: TickListViewModel.TickListFilter
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = selectedFilter.emptyStateRes()),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@DrawableRes
private fun TickListViewModel.TickListFilter.iconRes(): Int =
    when (this) {
        TickListViewModel.TickListFilter.BOTH -> R.drawable.ic_bookmark
        TickListViewModel.TickListFilter.FAVORITES -> R.drawable.ic_star
        TickListViewModel.TickListFilter.TICKED -> R.drawable.ic_check_circle
    }

private fun TickListViewModel.TickListFilter.labelRes(): Int =
    when (this) {
        TickListViewModel.TickListFilter.BOTH -> R.string.tick_list_filter_both
        TickListViewModel.TickListFilter.FAVORITES -> R.string.tick_list_filter_favorites
        TickListViewModel.TickListFilter.TICKED -> R.string.tick_list_filter_ticked
    }

private fun TickListViewModel.TickListFilter.emptyStateRes(): Int =
    when (this) {
        TickListViewModel.TickListFilter.BOTH -> R.string.tick_list_empty_state_body
        TickListViewModel.TickListFilter.FAVORITES -> R.string.tick_list_empty_filter_favorites
        TickListViewModel.TickListFilter.TICKED -> R.string.tick_list_empty_filter_ticked
    }

@Composable
private fun ProblemItem(
    problem: Problem,
    onProblemClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = onProblemClicked)
            .padding(16.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProblemIcon(problem = problem)

        Text(
            modifier = Modifier.weight(1f),
            text = problem.name.orEmpty(),
            color = MaterialTheme.colorScheme.onSurface
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

        Text(
            text = problem.grade.orEmpty(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TickListOverflowActions(
    onExportTickListClicked: () -> Unit,
    onImportTickListClicked: () -> Unit
) {
    Column {
        TickListOverflowActionItem(
            text = stringResource(id = R.string.tick_list_action_export),
            iconRes = R.drawable.ic_cloud_upload,
            onClick = onExportTickListClicked
        )

        TickListOverflowActionItem(
            text = stringResource(id = R.string.tick_list_action_import),
            iconRes = R.drawable.ic_install_mobile,
            onClick = onImportTickListClicked
        )
    }
}

@Composable
private fun TickListOverflowActionItem(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
private fun TickListScreenPreview(
    @PreviewParameter(TickListScreenPreviewParameterProvider::class)
    screenState: TickListViewModel.ScreenState
) {
    BoolderTheme {
        TickListScreen(
            screenState = screenState,
            onProblemClicked = {},
            onFilterSelected = {},
            onExportTickListClicked = {},
            onImportTickListClicked = {}
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
