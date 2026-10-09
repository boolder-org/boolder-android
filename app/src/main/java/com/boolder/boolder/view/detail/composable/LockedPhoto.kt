package com.boolder.boolder.view.detail.composable

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun LockedPhoto(
    painter: Painter,
    premiumPrice: String?,
    onPremiumRequested: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier.blur(radius = 24.dp),
            painter = painter,
            contentDescription = null
        )

        // Modifier.blur has no effect before Android 12, the photo is hidden instead
        val veilColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Color.Black.copy(alpha = .4f)
        } else {
            MaterialTheme.colorScheme.background
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(color = veilColor)
        )

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.premium_photo_locked_title),
                style = MaterialTheme.typography.titleMedium,
                color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                textAlign = TextAlign.Center
            )

            Button(onClick = onPremiumRequested) {
                Text(
                    text = premiumPrice
                        ?.let { stringResource(id = R.string.premium_photo_locked_button_with_price, it) }
                        ?: stringResource(id = R.string.premium_photo_locked_button)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LockedPhotoPreview(
    @PreviewParameter(LockedPhotoPreviewParameterProvider::class)
    premiumPrice: String?
) {
    BoolderTheme {
        LockedPhoto(
            painter = painterResource(id = R.drawable.area_cover_1),
            premiumPrice = premiumPrice,
            onPremiumRequested = {}
        )
    }
}

private class LockedPhotoPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "10,00 €",
        null
    )
}
