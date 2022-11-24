package com.boolder.boolder.view.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.get
import java.util.*


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
    private lateinit var selectedLine: Line
    private var isVariantSelected = false
    private val variantsCount
        get() = completeProblem.otherCompleteProblem.count { it.problem.parentId == completeProblem.problem.id }

    private val bleauUrl
        get() = "https://bleau.info/a/${selectedProblem.bleauInfoId}.html"
    private val shareUrl
        get() = "https://www.boolder.com/${Locale.getDefault().language}/p/${selectedProblem.id}"
    private var isVariantSelectorOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.bottom_sheet, container, false)
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
            .filter { it.problem.parentId == selectedProblem.id }
            .map { variants ->
                buildVariantText(variants.problem) { onVariantSelected(variants.line, variants.problem) }
            }.forEach { binding.variantSelector.addView(it) }

        // Add parent in variant only if a variant is currently displayed
        if (isVariantSelected) {
            binding.variantSelector.addView(buildVariantText(completeProblem.problem) { markParentAsSelected() }, 0)
        }
    }

    private fun buildVariantText(problem: Problem, selectVariant: () -> Unit): TextView {
        return TextView(requireContext()).apply {
            text = problem.name
            tag = problem.id
            setTextColor(ColorStateList.valueOf(Color.BLACK))
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

    private fun onVariantSelected(line: Line, problem: Problem) {
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
        val points = selectedLine.points()
        if (points.isNotEmpty()) {

            val segment = curveAlgorithm.controlPointsFromPoints(points)

            val ctrl1 = segment.map { PointD(it.controlPoint1.x, it.controlPoint1.y) }
            val ctrl2 = segment.map { PointD(it.controlPoint2.x, it.controlPoint2.y) }

            //TODO Switch back to PointF, avoid home made object in custom views (re-usability)
            binding.lineVector.addDataPoints(
                points,
                ctrl1,
                ctrl2,
                selectedProblem.drawColor(requireContext())
            )
        }
    }

    private fun drawCircuitNumberCircle() {
        val pointD = selectedLine.points().firstOrNull()
        if (pointD != null) {
            val match = ViewGroup.LayoutParams.MATCH_PARENT
            val cardSize = 80
            val offset = cardSize / 2
            val cardParams = RelativeLayout.LayoutParams(cardSize, cardSize)

            val text = TextView(requireContext()).apply {
                val textColor = if (selectedProblem.circuitColorSafe == WHITE) {
                    ColorStateList.valueOf(Color.BLACK)
                } else ColorStateList.valueOf(Color.WHITE)

                setTextColor(textColor)
                setAutoSizeTextTypeUniformWithPresetSizes(intArrayOf(8, 10, 12), TypedValue.COMPLEX_UNIT_SP)
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
        binding.title.text = "${selectedProblem.name} $sitStartText"
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
        binding.typeText.text = selectedProblem.steepness.replaceFirstChar { it.uppercaseChar() }
    }

    private fun setupChipClick() {
        binding.bleauInfo.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
            startActivity(browserIntent)
        }

        binding.share.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
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

    private fun loadBoolderImage() {
        Glide.with(requireContext())
            .load(completeProblem.topo?.url)
            .fallback(R.drawable.ic_placeholder)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressCircular.visibility = View.GONE
                    markParentAsSelected()
                    return false
                }

            })
            .into(binding.picture)
    }
    //endregion

    //region Extensions
    private fun Problem.defaultName(): String {
        return if (!circuitColor.isNullOrBlank() && !circuitNumber.isNullOrBlank()) {
            "${circuitColor.localize()} $circuitNumber"
        } else "No name"
    }

    private fun String.localize(): String {
        return CircuitColor.valueOf(this).localize(requireContext())
    }
    //endregion
}

data class PointD(val x: Double, val y: Double)

fun List<PointD>.toF() = map { PointF(it.x.toFloat(), it.y.toFloat()) }