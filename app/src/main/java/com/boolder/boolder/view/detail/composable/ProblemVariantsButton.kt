package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.utils.previewgenerator.dummyProblemWithLine
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun ProblemVariantsButton(
    variants: List<ProblemWithLine>,
    onVariantSelected: (ProblemWithLine) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedSize = variants.size - 1

    if (displayedSize <= 0) return

    Box(
        modifier = modifier.padding(8.dp)
    ) {
        var selectedVariant by remember(key1 = variants) { mutableStateOf(variants.first()) }
        var showSelector by remember(key1 = variants) { mutableStateOf(false) }

        if (showSelector) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showSelector = false },
                content = {
                    variants.forEach {
                        if (it.problem.id == selectedVariant.problem.id) return@forEach

                        DropdownMenuItem(
                            text = { ProblemVariantItem(problem = it.problem) },
                            onClick = {
                                showSelector = false
                                selectedVariant = it
                                onVariantSelected(it)
                            }
                        )
                    }
                }
            )
        } else {
            Text(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .background(color = Color.Gray.copy(alpha = .8f), shape = CircleShape)
                    .clickable { showSelector = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = pluralStringResource(
                    id = R.plurals.variant,
                    count = displayedSize,
                    displayedSize
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProblemVariantItem(problem: Problem) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = problem.name.orEmpty(),
            color = Color.Black
        )

        Text(
            modifier = Modifier
                .background(color = Color.Black, shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            text = problem.grade.orEmpty(),
            color = Color.White
        )
    }
}

/**
 * Use the preview interactive mode in order to interact with the menu
 */
@Preview
@Composable
private fun ProblemVariantsButtonPreview() {
    BoolderTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            ProblemVariantsButton(
                modifier = Modifier.align(Alignment.TopEnd),
                variants = List(3) {
                    dummyProblemWithLine(
                        id = it,
                        name = "Problem $it"
                    )
                },
                onVariantSelected = {}
            )
        }
    }
}
