package com.boolder.boolder.view.detail.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.boolder.boolder.R
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun SaveProblemDialog(
    tickStatus: TickStatus?,
    onDismissRequest: () -> Unit,
    onSaveProblem: (TickStatus) -> Unit,
    onUnsaveProblem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                when (tickStatus) {
                    null -> {
                        SaveProblemDialogActionButton(
                            iconRes = R.drawable.ic_star_outline,
                            stringRes = R.string.topo_action_project_add,
                            onClick = {
                                onSaveProblem(TickStatus.PROJECT)
                                onDismissRequest()
                            }
                        )

                        SaveProblemDialogActionButton(
                            iconRes = R.drawable.ic_done,
                            stringRes = R.string.topo_action_tick,
                            onClick = {
                                onSaveProblem(TickStatus.SUCCEEDED)
                                onDismissRequest()
                            },
                        )
                    }
                    TickStatus.PROJECT -> {
                        SaveProblemDialogActionButton(
                            iconRes = R.drawable.ic_done,
                            stringRes = R.string.topo_action_tick,
                            onClick = {
                                onSaveProblem(TickStatus.SUCCEEDED)
                                onDismissRequest()
                            },
                        )

                        SaveProblemDialogActionButton(
                            iconRes = R.drawable.ic_close,
                            stringRes = R.string.topo_action_project_remove,
                            onClick = {
                                onUnsaveProblem()
                                onDismissRequest()
                            }
                        )
                    }
                    TickStatus.SUCCEEDED -> {
                        SaveProblemDialogActionButton(
                            iconRes = R.drawable.ic_close,
                            stringRes = R.string.topo_action_untick,
                            onClick = {
                                onUnsaveProblem()
                                onDismissRequest()
                            }
                        )
                    }
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = onDismissRequest,
                    content = { Text(text = stringResource(id = R.string.cancel)) }
                )
            }
        }
    )
}

@Composable
private fun SaveProblemDialogActionButton(
    @DrawableRes iconRes: Int,
    @StringRes stringRes: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        content = {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                painter = painterResource(id = iconRes),
                contentDescription = null
            )

            Text(text = stringResource(id = stringRes))
        }
    )
}

@PreviewLightDark
@Composable
private fun SaveProblemDialogPreview(
    @PreviewParameter(SaveProblemDialogPreviewParameterProvider::class)
    tickStatus: TickStatus?
) {
    BoolderTheme {
        SaveProblemDialog(
            tickStatus = tickStatus,
            onDismissRequest = {},
            onSaveProblem = {},
            onUnsaveProblem = {}
        )
    }
}

private class SaveProblemDialogPreviewParameterProvider : PreviewParameterProvider<TickStatus?> {
    override val values = sequenceOf(
        null,
        TickStatus.PROJECT,
        TickStatus.SUCCEEDED
    )
}
