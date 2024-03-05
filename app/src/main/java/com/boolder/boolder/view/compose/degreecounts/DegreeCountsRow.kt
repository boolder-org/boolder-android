package com.boolder.boolder.view.compose.degreecounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun DegreeCountsRow(
    degreeCounts: Map<String, Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(2.dp)
    ) {
        degreeCounts.forEach { (degree, count) ->
            val backgroundColor = if (count >= 20) color else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color = backgroundColor, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = degree,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = TextUnit(14f, TextUnitType.Sp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GradeCountsRowPreview() {
    BoolderTheme {
        DegreeCountsRow(
            degreeCounts = mapOf(
                "1" to 3,
                "2" to 103,
                "3" to 63,
                "4" to 88,
                "5" to 60,
                "6" to 84,
                "7" to 97,
                "8" to 15,
            ),
            color = Color(red = 45, green = 161, blue = 125)
        )
    }
}
