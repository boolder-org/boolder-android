package com.boolder.boolder.view.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import coil.load
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ViewProblemBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.utils.CubicCurveAlgorithm
import java.util.Locale

class ProblemView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    private val binding = ViewProblemBinding.inflate(LayoutInflater.from(context), this)

    var onProblemFromSameTopoSelected: ((problemId: String) -> Unit)? = null

    init {
        setBackgroundColor(Color.WHITE)
        isClickable = true
        isFocusable = true
    }

    fun setProblem(completeProblem: CompleteProblem) {
        binding.problemStartsContainer.removeAllViews()
        binding.lineVector.clearPath()

        loadBoolderImage(completeProblem)
        updateLabels(completeProblem.problem)
        setupChipClick(completeProblem.problem)
    }

    private fun onProblemPictureLoaded(completeProblem: CompleteProblem) {
        completeProblem.otherCompleteProblem.forEach(::drawCircuitNumberCircle)

        drawCircuitNumberCircle(completeProblem)
        drawCurves(completeProblem)
    }

    private fun onProblemStartClicked(completeProblem: CompleteProblem) {
        updateLabels(completeProblem.problem)
        setupChipClick(completeProblem.problem)
        drawCurves(completeProblem)
        onProblemFromSameTopoSelected?.invoke(completeProblem.problem.id.toString())
    }

    //region Draw
    private fun drawCurves(completeProblem: CompleteProblem) {
        binding.lineVector.clearPath()

        val points = completeProblem.line?.points()

        if (points.isNullOrEmpty()) return

        val problemColor = completeProblem.problem.getColor(context)

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
    }

    private fun drawCircuitNumberCircle(completeProblem: CompleteProblem) {
        val pointD = completeProblem.line?.points()?.firstOrNull()
        if (pointD != null) {
            val viewSizeRes = if (completeProblem.problem.circuitNumber.isNullOrBlank()) {
                R.dimen.size_problem_start_without_number
            } else {
                R.dimen.size_problem_start_with_number
            }

            val viewSize = resources.getDimensionPixelSize(viewSizeRes)
            val marginProblemStart = resources.getDimensionPixelSize(R.dimen.margin_problem_start)
            val viewWithMarginSize = viewSize + marginProblemStart * 2
            val offset = viewWithMarginSize / 2

            val textColor = when (completeProblem.problem.circuitColorSafe) {
                CircuitColor.WHITE -> Color.BLACK
                else -> Color.WHITE
            }

            val problemStartView = ProblemStartView(binding.root.context).apply {
                setText(completeProblem.problem.circuitNumber)
                setTextColor(textColor)
                setProblemColor(completeProblem.problem.getColor(context))
                translationX = (pointD.x * binding.picture.measuredWidth - offset).toFloat()
                translationY = (pointD.y * binding.picture.measuredHeight - offset).toFloat()

                setOnClickListener { onProblemStartClicked(completeProblem) }
            }

            binding.problemStartsContainer.addView(problemStartView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }
    }
    //endregion

    private fun updateLabels(problem: Problem) {
        binding.title.text = problem.nameSafe()
        binding.grade.text = problem.grade

        val steepness = Steepness.fromTextValue(problem.steepness)

        binding.typeIcon.apply {
            val steepnessDrawable = steepness.iconRes
                ?.let { ContextCompat.getDrawable(context, it) }

            setImageDrawable(steepnessDrawable)
            isVisible = steepnessDrawable != null
        }

        binding.typeText.apply {
            val steepnessText = steepness.textRes?.let(context::getString)

            val sitStartText = if (problem.sitStart) {
                resources.getString(R.string.sit_start)
            } else null

            text = listOfNotNull(steepnessText, sitStartText).joinToString(separator = " • ")
            isVisible = !text.isNullOrEmpty()
        }
    }

    private fun setupChipClick(problem: Problem) {
        binding.bleauInfo.isVisible = !problem.bleauInfoId.isNullOrEmpty()
        binding.bleauInfo.setOnClickListener {
            try {
                val bleauUrl = "https://bleau.info/a/${problem.bleauInfoId}.html"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Log.i("Bottom Sheet", "No apps can handle this kind of intent")
            }
        }

        binding.share.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://www.boolder.com/${Locale.getDefault().language}/p/${problem.id}"
                )
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

    private fun loadBoolderImage(completeProblem: CompleteProblem) {
        binding.progressCircular.isVisible = true

        if (completeProblem.topo != null) {
            binding.picture.load(completeProblem.topo.url) {
                crossfade(true)
                error(R.drawable.ic_placeholder)

                listener(
                    onSuccess = { _, _ ->
                        context?.let {
                            binding.picture.setPadding(0)
                            binding.progressCircular.isVisible = false
                            onProblemPictureLoaded(completeProblem)
                        }
                    },
                    onError = { _, _ -> loadErrorPicture() }
                )
            }
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
