package com.boolder.boolder.domain.model

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.boolder.boolder.R
import com.mapbox.maps.extension.style.expressions.generated.Expression.ExpressionBuilder

enum class CircuitColor {
    YELLOW,
    PURPLE,
    ORANGE,
    GREEN,
    BLUE,
    SKYBLUE,
    SALMON,
    RED,
    WHITE,
    WHITEFORKIDS,
    BLACK,
    OFF_CIRCUIT;

    fun rgb(expressionBuilder: ExpressionBuilder) = when (this) {
        YELLOW -> expressionBuilder.rgb(255.0, 204.0, 2.0)
        PURPLE -> expressionBuilder.rgb(215.0, 131.0, 255.0)
        ORANGE -> expressionBuilder.rgb(255.0, 149.0, 0.0)
        GREEN -> expressionBuilder.rgb(255.0, 149.0, 0.0)
        BLUE -> expressionBuilder.rgb(1.0, 122.0, 255.0)
        SKYBLUE -> expressionBuilder.rgb(90.0, 199.0, 250.0)
        SALMON -> expressionBuilder.rgb(253.0, 175.0, 138.0)
        RED -> expressionBuilder.rgb(255.0, 59.0, 47.0)
        WHITE -> expressionBuilder.rgb(255.0, 255.0, 255.0)
        WHITEFORKIDS -> expressionBuilder.rgb(255.0, 255.0, 255.0)
        BLACK -> expressionBuilder.rgb(0.0, 0.0, 0.0)
        OFF_CIRCUIT -> expressionBuilder.rgb(135.0, 138.0, 141.0)
    }

    fun localize(context: Context): String {
        val id = when (this) {
            YELLOW -> R.string.circuit_short_name_yellow
            PURPLE -> R.string.circuit_short_name_purple
            ORANGE -> R.string.circuit_short_name_orange
            GREEN -> R.string.circuit_short_name_green
            BLUE -> R.string.circuit_short_name_blue
            SKYBLUE -> R.string.circuit_short_name_skyblue
            SALMON -> R.string.circuit_short_name_salmon
            RED -> R.string.circuit_short_name_red
            WHITE -> R.string.circuit_short_name_white
            WHITEFORKIDS -> R.string.circuit_short_name_white_for_kids
            BLACK -> R.string.circuit_short_name_black
            OFF_CIRCUIT -> R.string.circuit_short_name_off_circuit
        }
        return context.getString(id)
    }

    @ColorInt
    fun getColor(context: Context): Int {
        val id = when (this) {
            YELLOW -> R.color.circuit_color_yellow
            PURPLE -> R.color.circuit_color_purple
            ORANGE -> R.color.circuit_color_orange
            GREEN -> R.color.circuit_color_green
            BLUE -> R.color.circuit_color_blue
            SKYBLUE -> R.color.circuit_color_skyblue
            SALMON -> R.color.circuit_color_salmon
            RED -> R.color.circuit_color_red
            WHITE -> R.color.circuit_color_white
            WHITEFORKIDS -> R.color.circuit_color_white_for_kids
            BLACK -> R.color.circuit_color_black
            OFF_CIRCUIT -> R.color.circuit_color_off_circuit
        }

        return ContextCompat.getColor(context, id)
    }
}
