package com.boolder.boolder.view.detail.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipBorder
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.compose.BoolderTheme
import java.util.Locale

@Composable
fun TopoFooter(
    problem: Problem,
    onBleauInfoClicked: (bleauInfoId: String?) -> Unit,
    onShareClicked: (problemId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(140.dp)
            .padding(8.dp),
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

        Steepness.fromTextValue(problem.steepness)?.let {
            TopoProblemSteepness(
                steepness = it,
                isSitStart = problem.sitStart
            )
        }

        ChipsRow(
            onBleauInfoClicked = { onBleauInfoClicked(problem.bleauInfoId) },
            onShareClicked = { onShareClicked(problem.id) }
        )
    }
}

@Composable
private fun TopoFooterTitleRow(
    problemName: String,
    grade: String
) {
    Row {
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
    steepness: Steepness,
    isSitStart: Boolean
) {
    Row(
        horizontalArrangement = spacedBy(8.dp)
    ) {
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
            text = text,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun ChipsRow(
    onBleauInfoClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    Row(
        horizontalArrangement = spacedBy(8.dp)
    ) {
        val colorPrimary = colorResource(id = R.color.primary)

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
fun TopoFooterPreview() {
    BoolderTheme {
        TopoFooter(
            modifier = Modifier.background(color = Color.White),
            problem = dummyProblem(),
            onBleauInfoClicked = {},
            onShareClicked = {}
        )
    }
}
