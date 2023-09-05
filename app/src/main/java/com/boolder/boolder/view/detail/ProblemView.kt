package com.boolder.boolder.view.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ViewProblemBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import java.util.Locale
import java.util.concurrent.TimeUnit.SECONDS

class ProblemView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    private val binding = ViewProblemBinding.inflate(LayoutInflater.from(context), this)

    private var completeProblem: CompleteProblem? = null

    private var selectedProblem: Problem? = null
    private var selectedLine: Line? = null

    private val bleauUrl
        get() = selectedProblem?.bleauInfoId?.let { "https://bleau.info/a/$it.html" }
    private val shareUrl
        get() = selectedProblem?.id?.let { "https://www.boolder.com/${Locale.getDefault().language}/p/$it" }

    init {
        setBackgroundColor(Color.WHITE)
    }

    fun setProblem(problem: CompleteProblem) {
        completeProblem = problem
        selectedLine = completeProblem?.line
        selectedProblem = completeProblem?.problem

        children.filterIsInstance<ProblemStartView>()
            .forEach(::removeView)

        binding.lineVector.clearPath()

        hideBleauButton()
        loadBoolderImage()
        updateLabels()
        setupChipClick()
    }

    private fun markParentAsSelected() {
        selectedLine = completeProblem?.line
        selectedProblem = completeProblem?.problem
        drawCurves()
        drawCircuitNumberCircle()
        updateLabels()
    }

    private fun hideBleauButton() {
        if (selectedProblem?.bleauInfoId.isNullOrEmpty()) {
            binding.bleauInfo.visibility = View.GONE
        }
    }

    //region Draw
    private fun drawCurves() {
        val points = selectedLine?.points()

        if (!points.isNullOrEmpty()) {
            val problemColor = selectedProblem?.getColor(context) ?: return

            val segment = CubicCurveAlgorithm().controlPointsFromPoints(points)

            val ctrl1 = segment.map { PointD(it.controlPoint1.x, it.controlPoint1.y) }
            val ctrl2 = segment.map { PointD(it.controlPoint2.x, it.controlPoint2.y) }

            binding.lineVector.apply {
                addDataPoints(
                    data = points,
                    point1 = ctrl1,
                    point2 = ctrl2,
                    drawColor = problemColor
                )
                animatePath()
            }
        } else {
            binding.lineVector.clearPath()
        }
    }

    private fun drawCircuitNumberCircle() {
        val pointD = selectedLine?.points()?.firstOrNull()
        if (pointD != null) {
            val viewSizeRes = if (selectedProblem?.circuitNumber.isNullOrBlank()) {
                R.dimen.size_problem_start_without_number
            } else {
                R.dimen.size_problem_start_with_number
            }

            val viewSize = resources.getDimensionPixelSize(viewSizeRes)
            val marginProblemStart = resources.getDimensionPixelSize(R.dimen.margin_problem_start)
            val viewWithMarginSize = viewSize + marginProblemStart * 2
            val offset = viewWithMarginSize / 2

            val textColor = when (selectedProblem?.circuitColorSafe) {
                CircuitColor.WHITE -> Color.BLACK
                else -> Color.WHITE
            }

            val problemStartView = ProblemStartView(binding.root.context).apply {
                setText(selectedProblem?.circuitNumber)
                setTextColor(textColor)
                selectedProblem?.getColor(context)?.let(::setProblemColor)
                translationX = (pointD.x * binding.picture.measuredWidth - offset).toFloat()
                translationY = (pointD.y * binding.picture.measuredHeight - offset).toFloat()
            }

            addView(problemStartView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }
    }
    //endregion

    private fun updateLabels() {
        binding.title.text = selectedProblem?.nameSafe()
        binding.grade.text = selectedProblem?.grade

        val steepness = selectedProblem?.steepness?.let(Steepness::fromTextValue)

        binding.typeIcon.apply {
            val steepnessDrawable = steepness
                ?.iconRes
                ?.let { ContextCompat.getDrawable(context, it) }

            setImageDrawable(steepnessDrawable)
            isVisible = steepnessDrawable != null
        }

        binding.typeText.apply {
            val steepnessText = steepness?.textRes?.let(context::getString)

            val sitStartText = if (selectedProblem?.sitStart == true) {
                resources.getString(R.string.sit_start)
            } else null

            text = listOfNotNull(steepnessText, sitStartText).joinToString(separator = " • ")
            isVisible = !text.isNullOrEmpty()
        }
    }

    private fun setupChipClick() {
        binding.bleauInfo.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Log.i("Bottom Sheet", "No apps can handle this kind of intent")
            }

        }

        binding.share.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            try {
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                Log.i("Bottom Sheet", "No apps can handle this kind of intent")
            }
        }
    }

    private fun loadBoolderImage() {
        binding.progressCircular.isVisible = true

        if (completeProblem?.topo != null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, SECONDS)
                .build()

            Picasso.Builder(context)
                .downloader(OkHttp3Downloader(okHttpClient))
                .build()
                .load(completeProblem?.topo?.url)
                .error(R.drawable.ic_placeholder)
                .into(binding.picture, object : Callback {
                    override fun onSuccess() {
                        context?.let {
                            binding.picture.setPadding(0)
                            binding.progressCircular.isVisible = false
                            markParentAsSelected()
                        }
                    }

                    override fun onError(e: java.lang.Exception?) {
                        loadErrorPicture()
                    }
                })
        } else loadErrorPicture()
    }

    private fun loadErrorPicture() {
        binding.picture.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_placeholder)
        )
        binding.picture.setPadding(200)
        binding.progressCircular.isVisible = false
    }
    //endregion

    //region Extensions

    private fun Problem.nameSafe(): String =
        if (Locale.getDefault().language == "fr") {
            name.orEmpty()
        } else {
            nameEn.orEmpty()
        }

    //endregion
}
