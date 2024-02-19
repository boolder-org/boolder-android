package com.boolder.boolder.view.detail.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipBorder
import androidx.compose.material3.ChipColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.BoolderYellow
import java.util.Locale

@Composable
fun TopoFooter(
    problem: Problem,
    onBleauInfoClicked: (bleauInfoId: String?) -> Unit,
    onShareClicked: (problemId: Int) -> Unit,
    onSaveProblem: (problemId: Int, tickStatus: TickStatus) -> Unit,
    onUnsaveProblem: (problemId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(140.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        TopoFooterTitleRow(
            problemName = if (Locale.getDefault().language == "fr") {
                problem.name.orEmpty()
            } else {
                problem.nameEn.orEmpty()
            },
            grade = problem.grade.orEmpty()
        )

        TopoProblemSteepness(
            steepness = Steepness.fromTextValue(problem.steepness),
            isSitStart = problem.sitStart,
            tickStatus = problem.tickStatus
        )

        ChipsRow(
            tickStatus = problem.tickStatus,
            onBleauInfoClicked = { onBleauInfoClicked(problem.bleauInfoId) },
            onShareClicked = { onShareClicked(problem.id) },
            onSaveProblem = { onSaveProblem(problem.id, it) },
            onUnsaveProblem = { onUnsaveProblem(problem.id) }
        )
    }
}

@Composable
private fun TopoFooterTitleRow(
    problemName: String,
    grade: String
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = problemName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = grade,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TopoProblemSteepness(
    steepness: Steepness?,
    isSitStart: Boolean,
    tickStatus: TickStatus?
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (steepness != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = steepness.iconRes),
                contentDescription = null
            )

            val steepnessText = stringResource(id = steepness.textRes)
            val text = if (isSitStart) {
                listOf(steepnessText, stringResource(id = R.string.sit_start))
                    .joinToString(separator = " • ")
            } else {
                steepnessText
            }

            Text(
                modifier = Modifier.weight(1f),
                text = text,
                fontSize = 18.sp
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        tickStatus?.let {
            val (iconRes, tintColor) = when (it) {
                TickStatus.PROJECT -> R.drawable.ic_star to Color.BoolderYellow
                TickStatus.SUCCEEDED -> R.drawable.ic_check_circle to MaterialTheme.colorScheme.primary
            }

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = tintColor
            )
        }

        if (steepness == null && tickStatus == null) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChipsRow(
    tickStatus: TickStatus?,
    onBleauInfoClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onSaveProblem: (TickStatus) -> Unit,
    onUnsaveProblem: () -> Unit
) {
    val colorPrimary = colorResource(id = R.color.primary)
    var showSaveDialog by remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        item {
            ChipButton(
                labelRes = R.string.bleau_info,
                iconRes = R.drawable.ic_outline_info,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colorPrimary,
                    labelColor = Color.White,
                    leadingIconContentColor = Color.White
                ),
                border = AssistChipDefaults.assistChipBorder(borderWidth = 0.dp),
                onClick = { onBleauInfoClicked() }
            )
        }

        item {
            val (stringRes, iconRes) = if (tickStatus == null) {
                R.string.save to R.drawable.ic_bookmark_border
            } else {
                R.string.saved to R.drawable.ic_bookmark
            }

            ChipButton(
                labelRes = stringRes,
                iconRes = iconRes,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Transparent,
                    labelColor = colorPrimary,
                    leadingIconContentColor = colorPrimary
                ),
                border = AssistChipDefaults.assistChipBorder(borderColor = colorPrimary),
                onClick = { showSaveDialog = true }
            )
        }

        item {
            ChipButton(
                labelRes = R.string.share,
                iconRes = R.drawable.ic_share,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Transparent,
                    labelColor = colorPrimary,
                    leadingIconContentColor = colorPrimary
                ),
                border = AssistChipDefaults.assistChipBorder(borderColor = colorPrimary),
                onClick = onShareClicked
            )
        }
    }

    if (showSaveDialog) {
        SaveProblemDialog(
            tickStatus = tickStatus,
            onDismissRequest = { showSaveDialog = false },
            onSaveProblem = onSaveProblem,
            onUnsaveProblem = onUnsaveProblem
        )
    }
}

@Composable
private fun ChipButton(
    @StringRes labelRes: Int,
    @DrawableRes iconRes: Int,
    colors: ChipColors,
    border: ChipBorder,
    onClick: () -> Unit
) {
    AssistChip(
        label = {
            Text(
                text = stringResource(id = labelRes),
                fontSize = 18.sp
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        },
        shape = CircleShape,
        colors = colors,
        border = border,
        onClick = onClick
    )
}

@Preview
@Composable
fun TopoFooterPreview(
    @PreviewParameter(TopoFooterPreviewParameterProvider::class)
    problem: Problem
) {
    BoolderTheme {
        TopoFooter(
            modifier = Modifier.background(color = Color.White),
            problem = problem,
            onBleauInfoClicked = {},
            onShareClicked = {},
            onSaveProblem = { _, _ -> },
            onUnsaveProblem = {}
        )
    }
}

private class TopoFooterPreviewParameterProvider : PreviewParameterProvider<Problem> {
    override val values = sequenceOf(
        dummyProblem(tickStatus = null),
        dummyProblem(tickStatus = TickStatus.PROJECT),
        dummyProblem(tickStatus = TickStatus.SUCCEEDED),
        dummyProblem(
            steepness = "",
            sitStart = false,
            tickStatus = null
        ),
        dummyProblem(
            steepness = "",
            sitStart = false,
            tickStatus = TickStatus.PROJECT
        ),
        dummyProblem(
            steepness = "",
            sitStart = false,
            tickStatus = TickStatus.SUCCEEDED
        )
    )
}
