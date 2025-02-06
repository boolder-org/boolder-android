package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
                containerColor = MaterialTheme.colorScheme.surface,
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
            ProblemVariantsPillButton(
                variantsCount = displayedSize,
                onClick = { showSelector = true }
            )
        }
    }
}

@Composable
private fun ProblemVariantsPillButton(
    variantsCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(shape = CircleShape)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = pluralStringResource(
                id = R.plurals.variant,
                count = variantsCount,
                variantsCount
            ),
            color = Color.White
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun ProblemVariantItem(problem: Problem) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = problem.name.orEmpty(),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            text = problem.grade.orEmpty(),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Use the preview interactive mode in order to interact with the menu
 */
@PreviewLightDark
@Composable
private fun ProblemVariantsButtonPreview() {
    BoolderTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
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
