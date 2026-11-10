package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.view.compose.ProblemItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopoProblemsBottomSheet(
    problems: List<Problem>,
    onProblemSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = pluralStringResource(id = R.plurals.topo_problems, count = problems.size, problems.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            items(problems, key = { it.id }) { problem ->
                ProblemItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProblemSelected(problem.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    problem = problem,
                    showFeatured = true
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
