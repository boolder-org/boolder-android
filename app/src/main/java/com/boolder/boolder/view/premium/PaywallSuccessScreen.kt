package com.boolder.boolder.view.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.premium.composable.PaywallSuccessConfetti

@Composable
internal fun PaywallSuccessScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        PaywallSuccessConfetti()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(14.dp, Alignment.CenterVertically)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .background(color = Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(id = R.drawable.ic_done),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = stringResource(id = R.string.premium_success_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.premium_success_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = .93f),
                    textAlign = TextAlign.Center
                )

                PaywallSuccessThanks()
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = onContinueClicked
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(id = R.string.premium_success_continue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PaywallSuccessThanks() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = .16f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color.White),
            painter = painterResource(id = R.drawable.premium_logo_bleau_info),
            contentDescription = null
        )

        Text(
            text = AnnotatedString.fromHtml(
                htmlString = stringResource(id = R.string.premium_success_thanks)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = .94f)
        )
    }
}

@PreviewLightDark
@Composable
private fun PaywallSuccessScreenPreview() {
    BoolderTheme {
        PaywallSuccessScreen(onContinueClicked = {})
    }
}
