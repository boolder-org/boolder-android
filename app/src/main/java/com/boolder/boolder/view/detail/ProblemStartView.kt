package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ViewProblemStartBinding

class ProblemStartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewProblemStartBinding.inflate(LayoutInflater.from(context), this)

    private val paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true

        setShadowLayer(
            resources.getDimension(R.dimen.radius_problem_line),
            0f,
            0f,
            ContextCompat.getColor(context, R.color.problem_line_shadow)
        )
        setLayerType(LAYER_TYPE_SOFTWARE, this)
    }

    private var radius = resources.getDimension(R.dimen.size_problem_start_without_number) / 2f

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawDiscBackground()
        super.onDraw(canvas)
    }

    fun setText(text: CharSequence?) {
        val sizeRes = if (text == null) {
            R.dimen.size_problem_start_without_number
        } else {
            R.dimen.size_problem_start_with_number
        }

        val size = resources.getDimensionPixelSize(sizeRes)

        binding.background.updateLayoutParams<ViewGroup.LayoutParams> {
            width = size
            height = size
        }

        binding.textView.text = text
        radius = size / 2f
        invalidate()
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.textView.setTextColor(color)
    }

    fun setProblemColor(@ColorInt color: Int) {
        paint.color = color
        invalidate()
    }

    private fun Canvas.drawDiscBackground() {
        drawCircle(width / 2f, width / 2f, radius, paint)
    }
}
