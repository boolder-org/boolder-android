package com.boolder.boolder.view.detail.composable

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onPremiumRequested: (() -> Unit)?,
    imageModifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            modifier = imageModifier.blur(radius = 24.dp),
            painter = painter,
            contentDescription = null
        )

        // Modifier.blur has no effect before Android 12, the photo is hidden instead
        val veilAlpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) .34f else 1f

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(color = Color.Black.copy(alpha = veilAlpha))
        )

        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = Color.White.copy(alpha = .95f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.ic_premium_lock),
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Text(
                text = stringResource(id = R.string.premium_photo_locked_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = Shadow(color = Color.Black.copy(alpha = .45f), blurRadius = 8f)
                ),
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            onPremiumRequested?.let {
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = premiumPrice
                            ?.let { price -> stringResource(id = R.string.premium_photo_locked_button_with_price, price) }
                            ?: stringResource(id = R.string.premium_photo_locked_button),
                        fontWeight = FontWeight.Bold
                    )
                }
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
