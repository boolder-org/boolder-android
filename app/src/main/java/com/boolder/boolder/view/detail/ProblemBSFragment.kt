package com.boolder.boolder.view.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import com.boolder.boolder.BuildConfig
import com.boolder.boolder.R
import com.boolder.boolder.databinding.BottomSheetBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CircuitColor.OFF_CIRCUIT
import com.boolder.boolder.domain.model.CircuitColor.WHITE
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS


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
    private var selectedLine: Line? = null
    private var isVariantSelected = false
    private val variantsCount
        get() = completeProblem.otherCompleteProblem.count { it.problem.parentId == completeProblem.problem.id }

    private val bleauUrl
        get() = "https://bleau.info/a/${selectedProblem.bleauInfoId}.html"
    private val shareUrl
        get() = "https://www.boolder.com/${Locale.getDefault().language}/p/${selectedProblem.id}"
    private var isVariantSelectorOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedLine = completeProblem.line
        selectedProblem = completeProblem.problem
        loadBoolderImage()
        updateLabels()
        setupChipClick()
        setupVariant()
        binding.root.setOnClickListener {
            if (isVariantSelectorOpen) {
                closeVariantSelector()
            }
        }
    }

    private fun markParentAsSelected() {
        selectedLine = completeProblem.line
        selectedProblem = completeProblem.problem
        isVariantSelected = false
        closeVariantSelector()
        drawCurves()
        drawCircuitNumberCircle()
        updateLabels()
    }

    //region Variant(s)
    private fun buildVariantsView() {
        binding.variantSelector.removeAllViews()

        completeProblem.otherCompleteProblem
            .filter { it.problem.parentId == completeProblem.problem.id }
            .filter { it.problem.id != selectedProblem.id }
            .map { variants ->
                buildVariantText(variants.problem) { onVariantSelected(variants.line, variants.problem) }
            }.forEach { binding.variantSelector.addView(it) }

        // Add parent in variant only if a variant is currently displayed
        if (isVariantSelected) {
            binding.variantSelector
                .addView(buildVariantText(completeProblem.problem) {
                    listener.onProblemSelected(completeProblem.problem)
                    markParentAsSelected()
                }, 0)
        }
    }

    private fun buildVariantText(problem: Problem, selectVariant: () -> Unit): TextView {
        return TextView(requireContext()).apply {
            text = "${problem.name} ${problem.grade}"
            tag = problem.id
            setTextColor(ColorStateList.valueOf(Color.WHITE))
            setPadding(32, 16, 32, 16)
            setOnClickListener { selectVariant() }
        }
    }

    private fun openVariantSelection() {
        isVariantSelectorOpen = true
        buildVariantsView()
        binding.variant.visibility = View.INVISIBLE
        binding.variantSelector.visibility = View.VISIBLE
    }

    private fun closeVariantSelector() {
        isVariantSelectorOpen = false
        binding.variant.visibility = if (variantsCount > 0) View.VISIBLE else View.INVISIBLE
        binding.variantSelector.visibility = View.GONE
    }

    private fun onVariantSelected(line: Line?, problem: Problem) {
        listener.onProblemSelected(problem)
        isVariantSelected = true
        selectedLine = line
        selectedProblem = problem
        closeVariantSelector()
        drawCurves()
        drawCircuitNumberCircle()
        updateLabels()
    }
    //endregion

    //region Draw
    private fun drawCurves() {
        val points = selectedLine?.points()
        if (points != null && points.isNotEmpty()) {

            val segment = curveAlgorithm.controlPointsFromPoints(points)

            val ctrl1 = segment.map { PointD(it.controlPoint1.x, it.controlPoint1.y) }
            val ctrl2 = segment.map { PointD(it.controlPoint2.x, it.controlPoint2.y) }

            binding.lineVector.addDataPoints(
                points,
                ctrl1,
                ctrl2,
                selectedProblem.drawColor(requireContext())
            )
        } else {
            binding.lineVector.clearPath()
        }
    }

    private fun drawCircuitNumberCircle() {
        val pointD = selectedLine?.points()?.firstOrNull()
        if (pointD != null) {
            val match = ViewGroup.LayoutParams.MATCH_PARENT
            val cardSize = if (selectedProblem.circuitColorSafe == OFF_CIRCUIT) 30 else 48
            val offset = cardSize / 2
            val cardParams = RelativeLayout.LayoutParams(cardSize, cardSize)

            val text = TextView(requireContext()).apply {
                val textColor = if (selectedProblem.circuitColorSafe == WHITE) {
                    ColorStateList.valueOf(Color.BLACK)
                } else ColorStateList.valueOf(Color.WHITE)

                setTextColor(textColor)
                textSize = 16f
                text = selectedProblem.circuitNumber
                gravity = Gravity.CENTER
            }

            val card = CardView(requireContext())

            card.apply {
                backgroundTintList = ColorStateList.valueOf(selectedProblem.drawColor(requireContext()))
                addView(text, RelativeLayout.LayoutParams(match, match))
                radius = 40f
                translationX = ((pointD.x * binding.picture.measuredWidth) - offset).toFloat()
                translationY = ((pointD.y * binding.picture.measuredHeight) - offset).toFloat()
            }

            binding.root.addView(card, cardParams)
        }
    }
    //endregion

    // region View setup
    private fun setupVariant() {
        binding.variant.let { chip ->
            if (variantsCount == 0) chip.visibility = View.GONE
            else {
                chip.text = requireContext().resources.getQuantityString(
                    R.plurals.variant,
                    variantsCount,
                    variantsCount
                )
                chip.visibility = View.VISIBLE
                chip.setOnClickListener {
                    openVariantSelection()
                    isVariantSelectorOpen = true
                }
            }
        }
    }

    private fun updateLabels() {
        val sitStartText = if (selectedProblem.sitStart) {
            requireContext().getString(R.string.sit_start)
        } else ""
        binding.title.text = selectedProblem.nameSafe()
        binding.grade.text = selectedProblem.grade

        val steepnessDrawable = when (selectedProblem.steepness) {
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

        when (selectedProblem.steepness) {
            "slab" -> R.string.stepness_slab
            "overhang" -> R.string.stepness_overhang
            "roof" -> R.string.stepness_roof
            "wall" -> R.string.stepness_wall
            "traverse" -> R.string.stepness_traverse
            else -> null
        }?.let {
            binding.typeText.text = getString(it) + " • $sitStartText"
        }

        if (steepnessDrawable == null && selectedProblem.steepness.contains(
                "other",
                true
            ) && !selectedProblem.sitStart
        ) {
            binding.typeIcon.visibility = View.GONE
            binding.typeText.visibility = View.GONE
        }
    }

    private fun setupChipClick() {
        binding.bleauInfo.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
                startActivity(browserIntent)
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
                startActivity(shareIntent)
            } catch (e: Exception) {
                Log.i("Bottom Sheet", "No apps can handle this kind of intent")
            }
        }

        binding.reportIssue.setOnClickListener {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, listOf(getString(R.string.contact_mail)).toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                putExtra(
                    Intent.EXTRA_TEXT, """
                    =====
                    Problem #${selectedProblem.id} - ${selectedProblem.nameSafe()}
                    Boolder v.${BuildConfig.VERSION_NAME} (build n°${BuildConfig.VERSION_CODE})
                    Android SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})
                    =====
                    """.trimIndent()
                )
            }.run {
                try {
                    startActivity(this)
                } catch (e: Exception) {
                    Log.i("Bottom Sheet", "No apps can handle this kind of intent")
                }
            }
        }

    }


    private fun loadBoolderImage() {
        if (completeProblem.topo != null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, SECONDS)
                .build()

            Picasso.Builder(requireContext())
                .downloader(OkHttp3Downloader(okHttpClient))
                .build()
                .load(completeProblem.topo?.url)
                .error(R.drawable.ic_placeholder)
                .into(binding.picture, object : Callback {
                    override fun onSuccess() {
                        context?.let {
                            binding.picture.setPadding(0)
                            binding.progressCircular.visibility = View.GONE
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
        context?.let {
            binding.picture.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_placeholder))
            binding.picture.setPadding(200)
            binding.progressCircular.visibility = View.GONE
        }
    }
    //endregion

    //region Extensions
    private fun Problem.nameSafe(): String {
        return if (name.isNullOrBlank() || name.contains("null", true)) {
            if (!circuitColor.isNullOrBlank() && !circuitNumber.isNullOrBlank()) {
                "${circuitColor.localize()} $circuitNumber"
            } else getString(R.string.no_name)
        } else name
    }

    private fun String.localize(): String {
        return CircuitColor.valueOf(this.uppercase()).localize(requireContext())
    }
    //endregion
}

data class PointD(val x: Double, val y: Double)

fun List<PointD>.toF() = map { PointF(it.x.toFloat(), it.y.toFloat()) }