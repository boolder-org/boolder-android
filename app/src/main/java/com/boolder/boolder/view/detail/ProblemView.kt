package com.boolder.boolder.view.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import coil.load
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ViewProblemBinding
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.domain.model.toProblemStart
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.detail.composable.ProblemStartsLayer
import com.boolder.boolder.view.detail.uimodel.ProblemStart
import java.util.Locale

class ProblemView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    private val binding = ViewProblemBinding.inflate(LayoutInflater.from(context), this)

    private var problemStarts: List<ProblemStart> = emptyList()

    var onProblemFromSameTopoSelected: ((problemId: String) -> Unit)? = null

    init {
        setBackgroundColor(Color.WHITE)
        isClickable = true
        isFocusable = true
    }

    fun setProblem(completeProblem: CompleteProblem) {
        setProblemStarts(
            problemStarts = emptyList(),
            selectedProblem = null
        )

        loadBoolderImage(completeProblem)
        updateLabels(completeProblem.problem)
        setupChipClick(completeProblem.problem)
    }

    private fun onProblemPictureLoaded(completeProblem: CompleteProblem) {
        val containerWidth = binding.picture.measuredWidth
        val containerHeight = binding.picture.measuredHeight

        val initialProblemStart = completeProblem.toProblemStart(
            containerWidthPx = containerWidth,
            containerHeightPx = containerHeight
        )

        val otherProblemStarts = completeProblem.otherCompleteProblem
            .mapNotNull {
                it.toProblemStart(
                    containerWidthPx = containerWidth,
                    containerHeightPx = containerHeight
                )
            }

        this.problemStarts = initialProblemStart
            ?.let { otherProblemStarts + it }
            ?: otherProblemStarts

        setProblemStarts(
            problemStarts = problemStarts,
            selectedProblem = completeProblem
        )
    }

    private fun onProblemStartClicked(completeProblem: CompleteProblem) {
        updateLabels(completeProblem.problem)
        setupChipClick(completeProblem.problem)
        setProblemStarts(
            problemStarts = problemStarts,
            selectedProblem = completeProblem
        )
        onProblemFromSameTopoSelected?.invoke(completeProblem.problem.id.toString())
    }

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

    private fun setProblemStarts(
        problemStarts: List<ProblemStart>,
        selectedProblem: CompleteProblem?
    ) {
        binding.problemStartsComposeView.setContent {
            BoolderTheme {
                ProblemStartsLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    problemStarts = problemStarts,
                    selectedProblem = selectedProblem,
                    onProblemStartClicked = ::onProblemStartClicked
                )
            }
        }
    }

    private fun Problem.nameSafe(): String =
        if (Locale.getDefault().language == "fr") {
            name.orEmpty()
        } else {
            nameEn.orEmpty()
        }
}
