package com.boolder.boolder.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.boolder.boolder.domain.model.CircuitColor

@Composable
fun CircuitColor.composeColor(): Color =
    colorResource(id = colorRes)
