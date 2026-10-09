package com.boolder.boolder.view.premium.composable

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun PaywallActions(
    premiumPrice: String?,
    onSubscribeClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = premiumPrice != null,
            onClick = onSubscribeClicked
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = premiumPrice
                    ?.let { stringResource(id = R.string.premium_paywall_subscribe_with_price, it) }
                    ?: stringResource(id = R.string.premium_paywall_subscribe),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(id = R.string.premium_paywall_legal),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        PaywallLinks(
            onRestoreClicked = onRestoreClicked,
            onTermsClicked = onTermsClicked,
            onPrivacyClicked = onPrivacyClicked
        )
    }
}

@Composable
private fun PaywallLinks(
    onRestoreClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit
) {
    val links = listOf(
        stringResource(id = R.string.premium_paywall_terms) to onTermsClicked,
        stringResource(id = R.string.premium_paywall_privacy) to onPrivacyClicked,
        stringResource(id = R.string.premium_paywall_restore) to onRestoreClicked
    )

    val linkStyles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))

    Text(
        text = buildAnnotatedString {
            links.forEachIndexed { index, (text, onClick) ->
                if (index > 0) append(" · ")

                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = text,
                        styles = linkStyles,
                        linkInteractionListener = { onClick() }
                    )
                ) {
                    append(text)
                }
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@PreviewLightDark
@Composable
private fun PaywallActionsPreview() {
    BoolderTheme {
        Surface {
            PaywallActions(
                premiumPrice = "10,00 €",
                onSubscribeClicked = {},
                onRestoreClicked = {},
                onTermsClicked = {},
                onPrivacyClicked = {}
            )
        }
    }
}
