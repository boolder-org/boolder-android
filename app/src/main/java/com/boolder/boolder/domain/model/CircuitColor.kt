package com.boolder.boolder.domain.model

import android.content.Context
import androidx.core.content.ContextCompat
import com.boolder.boolder.R
import com.mapbox.maps.extension.style.expressions.generated.Expression.ExpressionBuilder

enum class CircuitColor(name: String) {
    YELLOW("yellow"),
    PURPLE("purple"),
    ORANGE("orange"),
    GREEN("green"),
    BLUE("blue"),
    SKY_BLUE("skyblue"),
    SALMON("salmon"),
    RED("red"),
    WHITE("white"),
    WHITE_KIDS("whiteForKids"),
    BLACK("black"),
    OFF_CIRCUIT("offCircuit");

    fun rgb(expressionBuilder: ExpressionBuilder) = when (this) {
        YELLOW -> expressionBuilder.rgb(255.0, 204.0, 2.0)
        PURPLE -> expressionBuilder.rgb(215.0, 131.0, 255.0)
        ORANGE -> expressionBuilder.rgb(255.0, 149.0, 0.0)
        GREEN -> expressionBuilder.rgb(255.0, 149.0, 0.0)
        BLUE -> expressionBuilder.rgb(1.0, 122.0, 255.0)
        SKY_BLUE -> expressionBuilder.rgb(90.0, 199.0, 250.0)
        SALMON -> expressionBuilder.rgb(253.0, 175.0, 138.0)
        RED -> expressionBuilder.rgb(255.0, 59.0, 47.0)
        WHITE -> expressionBuilder.rgb(255.0, 255.0, 255.0)
        WHITE_KIDS -> expressionBuilder.rgb(255.0, 255.0, 255.0) // TODO
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
            SKY_BLUE -> R.string.circuit_short_name_skyblue
            SALMON -> R.string.circuit_short_name_salmon
            RED -> R.string.circuit_short_name_red
            WHITE -> R.string.circuit_short_name_white
            WHITE_KIDS -> R.string.circuit_short_name_white_for_kids
            BLACK -> R.string.circuit_short_name_black
            OFF_CIRCUIT -> R.string.circuit_short_name_off_circuit
        }
        return context.getString(id)
    }

    fun getColor(context: Context): Int {
        val id = when (this) {
            YELLOW -> R.color.circuit_color_yellow
            PURPLE -> R.color.circuit_color_purple
            ORANGE -> R.color.circuit_color_orange
            GREEN -> R.color.circuit_color_green
            BLUE -> R.color.circuit_color_blue
            SKY_BLUE -> R.color.circuit_color_skyblue
            SALMON -> R.color.circuit_color_salmon
            RED -> R.color.circuit_color_red
            WHITE -> R.color.circuit_color_white
            WHITE_KIDS -> R.color.circuit_color_white_for_kids
            BLACK -> R.color.circuit_color_black
            OFF_CIRCUIT -> R.color.circuit_color_off_circuit
        }

        return ContextCompat.getColor(context, id)
    }
}