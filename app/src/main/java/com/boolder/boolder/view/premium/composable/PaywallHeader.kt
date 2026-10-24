package com.boolder.boolder.view.premium.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun PaywallHeader(
    showCloseButton: Boolean,
    onCloseClicked: () -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp + topInset)
            .clipToBounds()
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(top = topInset),
        contentAlignment = Alignment.Center
    ) {
        PaywallHeaderRock(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-32).dp, y = 32.dp)
                .size(width = 160.dp, height = 104.dp)
        )
        PaywallHeaderRock(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 24.dp, y = 16.dp)
                .size(width = 128.dp, height = 84.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.premium_paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(id = R.string.premium_paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = .88f),
                textAlign = TextAlign.Center
            )
        }

        if (showCloseButton) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = .28f),
                    contentColor = Color.White
                ),
                onClick = onCloseClicked
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.premium_paywall_close)
                )
            }
        }
    }
}

@Composable
private fun PaywallHeaderRock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            color = Color.White.copy(alpha = .13f),
            shape = RoundedCornerShape(
                topStartPercent = 44,
                topEndPercent = 56,
                bottomEndPercent = 40,
                bottomStartPercent = 60
            )
        )
    )
}

@PreviewLightDark
@Composable
private fun PaywallHeaderPreview() {
    BoolderTheme {
        Surface {
            PaywallHeader(
                showCloseButton = true,
                onCloseClicked = {}
            )
        }
    }
}
