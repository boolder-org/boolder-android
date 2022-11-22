package com.boolder.boolder.view.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.boolder.boolder.BuildConfig
import com.boolder.boolder.R
import com.boolder.boolder.R.layout
import com.boolder.boolder.R.string
import com.boolder.boolder.databinding.BottomSheetBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CircuitColor.WHITE
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.get


interface BottomSheetListener {
    fun onProblemSelected(problem: Problem)
}

class ProblemBSFragment(private val listener: BottomSheetListener) : BottomSheetDialogFragment() {

    companion object {
        private const val COMPLETE_PROBLEM = "COMPLETE_PROBLEM"
        fun newInstance(problem: CompleteProblem, listener: BottomSheetListener) = ProblemBSFragment(listener).apply {
            arguments = bundleOf(COMPLETE_PROBLEM to problem)
        }
    }

    private val binding: BottomSheetBinding by viewBinding(BottomSheetBinding::bind)
    private val curveAlgorithm: CubicCurveAlgorithm
        get() = get()

    private val completeProblem
        get() = requireArguments().getParcelable<CompleteProblem>(COMPLETE_PROBLEM)
            ?: error("No Problem in arguments")

    private lateinit var selectedProblem: Problem
    private val bleauUrl
        get() = "https://bleau.info/a/${selectedProblem.bleauInfoId}.html"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedProblem = completeProblem.problem

        Glide.with(requireContext())
            .load(completeProblem.topo?.url)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.picture)

        setupClickEvent()
        setupViewFor(completeProblem.problem)

        drawCurves(completeProblem.line.points(), completeProblem.problem)
        drawCircuitNumberCircle(completeProblem.line, completeProblem.problem)

        completeProblem.otherCompleteProblem.map {
            drawCircuitNumberCircle(it.line, it.problem)
        }
    }

    private fun drawCurves(points: List<PointD>, problem: Problem) {
        if (points.isNotEmpty()) {

            val segment = curveAlgorithm.controlPointsFromPoints(points)

            val ctrl1 = segment.map { PointD(it.controlPoint1.x, it.controlPoint1.y) }
            val ctrl2 = segment.map { PointD(it.controlPoint2.x, it.controlPoint2.y) }

            //TODO Switch back to PointF, avoid home made object in custom views (re-usability)
            binding.lineVector.addDataPoints(
                points,
                ctrl1,
                ctrl2,
                problem.drawColor()
            )
        }
    }

    private fun drawCircuitNumberCircle(line: Line, problem: Problem) {
        val pointD = line.points().firstOrNull()
        if (pointD != null) {
            val match = ViewGroup.LayoutParams.MATCH_PARENT
            val cardSize = 80
            val offset = cardSize / 2
            val cardParams = RelativeLayout.LayoutParams(cardSize, cardSize)

            val text = TextView(requireContext()).apply {
                val textColor = if (problem.circuitColorSafe == WHITE) {
                    ColorStateList.valueOf(Color.BLACK)
                } else ColorStateList.valueOf(Color.WHITE)

                setTextColor(textColor)
                setAutoSizeTextTypeUniformWithPresetSizes(intArrayOf(8, 10, 12), TypedValue.COMPLEX_UNIT_SP)
                text = problem.circuitNumber
                gravity = Gravity.CENTER
            }

            val card = CardView(requireContext())

            card.apply {
                backgroundTintList = ColorStateList.valueOf(problem.drawColor())
                setOnClickListener { onNewProblemSelected(line, problem) }
                addView(text, RelativeLayout.LayoutParams(match, match))
                radius = 40f
                translationX = pointD.x.toFloat() - offset
                translationY = pointD.y.toFloat() - offset
            }

            binding.root.addView(card, cardParams)
        }
    }

    private fun onNewProblemSelected(line: Line, problem: Problem) {
        selectedProblem = problem
        setupViewFor(problem)
        drawCurves(line.points(), problem)
    }

    private fun setupViewFor(problem: Problem) {
        binding.title.text = problem.name
        binding.grade.text = problem.grade

        val steepnessDrawable = when (problem.steepness) {
            "slab" -> R.drawable.ic_steepness_slab
            "overhang" -> R.drawable.ic_steepness_overhang
            "roof" -> R.drawable.ic_steepness_roof
            "wall" -> R.drawable.ic_steepness_wall
            "traverse" -> R.drawable.ic_steepness_traverse_left_right
            else -> null
        }?.let {
            ContextCompat.getDrawable(requireContext(), it)
        }
        binding.typeIcon.setImageDrawable(steepnessDrawable)
        binding.typeText.text = problem.steepness.replaceFirstChar { it.uppercaseChar() }
    }

    private fun setupClickEvent() {
        binding.bleauInfo.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
            startActivity(browserIntent)
        }

        binding.share.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, bleauUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.reportIssue.setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, listOf(getString(string.contact_mail)).toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                putExtra(
                    Intent.EXTRA_TEXT, """
                    ----
                    Problem #${selectedProblem.id} - ${selectedProblem.name ?: selectedProblem.defaultName()}
                    Boolder v.${BuildConfig.VERSION_NAME} (build n°${BuildConfig.VERSION_CODE})
                    Android SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})
                    """
                )
            }.run { startActivity(this) }
        }

    }

    private fun Problem.drawColor(): Int = circuitColorSafe.getColor(requireContext())

    private fun Problem.defaultName(): String {
        return if (!circuitColor.isNullOrBlank() && !circuitNumber.isNullOrBlank()) {
            "${circuitColor.localize()} $circuitNumber"
        } else "No name"
    }

    private fun String.localize(): String {
        return CircuitColor.valueOf(this).localize(requireContext())
    }
}

data class PointD(val x: Double, val y: Double)

fun List<PointD>.toF() = map { PointF(it.x.toFloat(), it.y.toFloat()) }