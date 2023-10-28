package com.boolder.boolder.view.map.filter.grade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.gradeRangeLevelDisplay
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.filter.grade.GradesFilterViewModel.Companion.QUICK_GRADE_RANGES

@Composable
internal fun GradesFilterLayout(
    gradeRanges: List<GradeRange>,
    selectedGradeRange: GradeRange?,
    onGradeRangeSelected: (GradeRange) -> Unit,
    onCustomLowBoundSelected: (String) -> Unit,
    onCustomHighBoundSelected: (String) -> Unit,
    onGradeRangeReset: () -> Unit,
    onGradeRangeValidated: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.grades),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        GradeRangesList(
            gradeRanges = gradeRanges,
            selectedGradeRange = selectedGradeRange,
            onGradeRangeSelected = onGradeRangeSelected
        )

        CustomRangeSelectors(
            selectedGradeRange = selectedGradeRange,
            onCustomLowBoundSelected = onCustomLowBoundSelected,
            onCustomHighBoundSelected = onCustomHighBoundSelected
        )

        BottomButtons(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            onGradeRangeReset = onGradeRangeReset,
            onGradeRangeValidated = onGradeRangeValidated
        )
    }
}

@Composable
private fun GradeRangesList(
    gradeRanges: List<GradeRange>,
    selectedGradeRange: GradeRange?,
    onGradeRangeSelected: (GradeRange) -> Unit
) {
    val resources = LocalContext.current.resources
    val shape = MaterialTheme.shapes.small

    Column(
        modifier = Modifier
            .clip(shape = shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
    ) {
        gradeRanges.forEachIndexed { index, gradeRange ->
            val title = if (gradeRange.isCustom) {
                stringResource(R.string.custom)
            } else {
                resources.gradeRangeLevelDisplay(gradeRange)
            }

            val additionalInfo = if (gradeRange == GradeRange.BEGINNER) {
                stringResource(id = R.string.beginner)
            } else {
                null
            }

            GradeRangeItem(
                title = title,
                additionalInfo = additionalInfo,
                selected = gradeRange == selectedGradeRange,
                onClick = { onGradeRangeSelected(gradeRange) }
            )

            if (index < gradeRanges.lastIndex) {
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun GradeRangeItem(
    title: String,
    additionalInfo: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium
                .copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        additionalInfo?.let {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomRangeSelectors(
    selectedGradeRange: GradeRange?,
    onCustomLowBoundSelected: (String) -> Unit,
    onCustomHighBoundSelected: (String) -> Unit
) {
    AnimatedVisibility(visible = selectedGradeRange?.isCustom == true) {
        val minGrade = selectedGradeRange?.min.orEmpty()
        val maxGrade = selectedGradeRange?.max.orEmpty()

        val maxLowBoundIndex = ALL_GRADES.indexOf(maxGrade).coerceAtLeast(0)
        val minHighBoundIndex = ALL_GRADES.indexOf(minGrade).coerceAtLeast(0)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = spacedBy(8.dp)
        ) {
            CustomRangeSelectorItem(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.grade_min),
                value = minGrade,
                grades = ALL_GRADES.subList(
                    fromIndex = 0,
                    toIndex = maxLowBoundIndex + 1
                ),
                onGradeSelected = onCustomLowBoundSelected
            )
            CustomRangeSelectorItem(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.grade_max),
                value = maxGrade,
                grades = ALL_GRADES.subList(
                    fromIndex = minHighBoundIndex,
                    toIndex = ALL_GRADES.size
                ),
                onGradeSelected = onCustomHighBoundSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeSelectorItem(
    label: String,
    value: String,
    grades: List<String>,
    onGradeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded}
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                label = { Text(text = label) },
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .1f)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                grades.forEach { grade ->
                    DropdownMenuItem(
                        text = { Text(text = grade) },
                        onClick = {
                            expanded = false
                            onGradeSelected(grade)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomButtons(
    onGradeRangeReset: () -> Unit,
    onGradeRangeValidated: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Button(
            colors = ButtonDefaults.outlinedButtonColors(),
            border = ButtonDefaults.outlinedButtonBorder,
            onClick = onGradeRangeReset
        ) {
            Text(text = stringResource(id = R.string.reset))
        }

        Button(
            onClick = onGradeRangeValidated
        ) {
            Text(text = stringResource(id = R.string.apply))
        }
    }
}

@Preview
@Composable
internal fun GradesFilterLayoutPreview() {
    BoolderTheme {
        GradesFilterLayout(
            gradeRanges = QUICK_GRADE_RANGES + GradeRange(min = "4a", max = "5a"),
            selectedGradeRange = GradeRange(min = "4a", max = "5a"),
            onGradeRangeSelected = {},
            onCustomLowBoundSelected = {},
            onCustomHighBoundSelected = {},
            onGradeRangeReset = {},
            onGradeRangeValidated = {}
        )
    }
}
