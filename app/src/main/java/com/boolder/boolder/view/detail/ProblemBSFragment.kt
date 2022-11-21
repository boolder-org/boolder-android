package com.boolder.boolder.view.detail

import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.boolder.boolder.BuildConfig
import com.boolder.boolder.R
import com.boolder.boolder.R.layout
import com.boolder.boolder.R.string
import com.boolder.boolder.databinding.BottomSheetBinding
import com.boolder.boolder.domain.model.*
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject


class ProblemBSFragment : BottomSheetDialogFragment() {

    companion object {
        private const val PROBLEM = "PROBLEM"
        private const val TOPO = "TOPO"
        private const val LINE = "LINE"
        fun newInstance(problem: Problem, topo: Topo, line: Line) = ProblemBSFragment().apply {
            arguments = bundleOf(PROBLEM to problem, TOPO to topo, LINE to line)
        }
    }

    private val binding: BottomSheetBinding by viewBinding(BottomSheetBinding::bind)

    private val curveAlgorithm: CubicCurveAlgorithm by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val problem = requireArguments().getParcelable<Problem>(PROBLEM) ?: error("No Problem in arguments")
        val topo = requireArguments().getParcelable<Topo>(TOPO) ?: error("No Topo in arguments")
        val line = requireArguments().getParcelable<Line>(LINE) ?: error("No Line in arguments")

        Glide.with(requireContext())
            .load(topo.url)
            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .into(binding.picture)

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

        val bleauUrl = "https://bleau.info/a/${problem.bleauInfoId}.html"

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
                    Problem #${problem.id} - ${problem.name ?: problem.defaultName()}
                    Boolder v.${BuildConfig.VERSION_NAME} (build n°${BuildConfig.VERSION_CODE})
                    Android SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})
                    """
                )
            }.run { startActivity(this) }
        }

        drawCurves(line.coordinates)
    }

    private fun drawCurves(stringCoordinates: String?) {
        //[{"x":0.4325,"y":0.805}]
        if (stringCoordinates.isNullOrBlank()) return
        val coordinates = Json.decodeFromString(stringCoordinates) as List<Coordinates>
        if (coordinates.isEmpty()) return
        val points = coordinates.map { PointF(it.x, it.y) }
//        val cubicSegment = curveAlgorithm.controlPointsFromPoints(points)

//        binding.curveChart.setup(points, cubicSegment)

//        val a = cubicSegment.map { PointF(it.controlPoint1.x, it.controlPoint1.y) }
//        val b = cubicSegment.map { PointF(it.controlPoint2.x, it.controlPoint2.y) }

//        binding.curveChart2.addDataPoints(points, a, b)

//        binding.curveChart2.addDataPoints(List(2) { DataPoint(it.toFloat()) } )
//        binding.curveChart2.addDataPoints(points, a, b)


        val aaa = curveAlgorithm.controlPointsFromPoints(listOf(PointF(1f, 1f), PointF(0.2f, 2f), PointF(3f, 3f)))
        println("AAAAA $aaa")
    }

    private fun Problem.defaultName(): String {
        return if (!circuitColor.isNullOrBlank() && !circuitNumber.isNullOrBlank()) {
            "${circuitColor.localize()} $circuitNumber"
        } else "No name"
    }

    private fun String.localize(): Int {
        return CircuitColor.valueOf(this).localize(requireContext())
    }
}