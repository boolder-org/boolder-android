package com.boolder.boolder.view.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import coil.load
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ViewTopoBinding
import com.boolder.boolder.domain.model.CircuitInfo
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.toUiProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.detail.composable.CircuitControls
import com.boolder.boolder.view.detail.composable.ProblemStartsLayer
import com.boolder.boolder.view.detail.composable.TopoFooter
import com.boolder.boolder.view.detail.uimodel.UiProblem
import java.util.Locale

class TopoView(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    private val binding = ViewTopoBinding.inflate(LayoutInflater.from(context), this)

    private var uiProblems: List<UiProblem> = emptyList()

    var onSelectProblemOnMap: ((problemId: String) -> Unit)? = null
    var onCircuitProblemSelected: ((problemId: Int) -> Unit)? = null

    init {
        // Immediately set a footer content to avoid a bug on the bottom sheet not fully expanding
        // on the first time it is expanded
        binding.footerLayout.setContent {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(color = Color.White)
            )
        }
    }

    fun setTopo(topo: Topo) {
        setUiProblems(
            uiProblems = emptyList(),
            selectedProblem = null
        )

        loadTopoImage(topo)

        updateCircuitControls(circuitInfo = topo.circuitInfo)

        topo.selectedCompleteProblem?.let { updateFooter(it.problemWithLine.problem) }
    }

    fun applyInsets(insets: Insets) {
        binding.bottomInsetSpace.updateLayoutParams { height = insets.bottom }
    }

    private fun onProblemPictureLoaded(topo: Topo) {
        val selectedProblem = topo.selectedCompleteProblem ?: return

        val containerWidth = binding.picture.measuredWidth
        val containerHeight = binding.picture.measuredHeight

        val initialUiProblem = selectedProblem.toUiProblem(
            containerWidthPx = containerWidth,
            containerHeightPx = containerHeight
        )

        val otherUiProblems = topo.otherCompleteProblems.map {
            it.toUiProblem(
                containerWidthPx = containerWidth,
                containerHeightPx = containerHeight
            )
        }

        this.uiProblems = otherUiProblems + initialUiProblem

        setUiProblems(
            uiProblems = uiProblems,
            selectedProblem = selectedProblem
        )
    }

    private fun onProblemStartClicked(completeProblem: CompleteProblem) {
        val problem = completeProblem.problemWithLine.problem

        updateFooter(problem)
        setUiProblems(
            uiProblems = uiProblems,
            selectedProblem = completeProblem
        )
        onSelectProblemOnMap?.invoke(problem.id.toString())
    }

    private fun onVariantSelected(variant: ProblemWithLine) {
        val (selectedProblem, newUiProblems) = VariantSelector.selectVariantInProblemStarts(
            selectedVariant = variant,
            uiProblems = uiProblems,
            containerWidth = binding.picture.measuredWidth,
            containerHeight = binding.picture.measuredHeight
        )

        uiProblems = newUiProblems

        selectedProblem?.let { updateFooter(it.problemWithLine.problem) }

        setUiProblems(
            uiProblems = uiProblems,
            selectedProblem = selectedProblem
        )

        selectedProblem?.problemWithLine?.problem?.id?.toString()?.let { selectedProblemId ->
            onSelectProblemOnMap?.invoke(selectedProblemId)
        }
    }

    private fun updateCircuitControls(circuitInfo: CircuitInfo?) {
        binding.circuitControlsComposeView.setContent {
            circuitInfo ?: return@setContent

            BoolderTheme {
                CircuitControls(
                    circuitInfo = circuitInfo,
                    onPreviousProblemClicked = {
                        circuitInfo.previousProblemId?.let { onCircuitProblemSelected?.invoke(it) }
                    },
                    onNextProblemClicked = {
                        circuitInfo.nextProblemId?.let { onCircuitProblemSelected?.invoke(it) }
                    }
                )
            }
        }
    }

    private fun updateFooter(problem: Problem) {
        binding.footerLayout.setContent {
            BoolderTheme {
                TopoFooter(
                    problem = problem,
                    onBleauInfoClicked = ::onBleauInfoClicked,
                    onShareClicked = ::onShareClicked
                )
            }
        }
    }

    private fun loadTopoImage(topo: Topo) {
        binding.progressCircular.isVisible = true

        val imageData = topo.imageFile ?: topo.pictureUrl

        binding.picture.load(imageData) {
            crossfade(true)
            error(R.drawable.ic_placeholder)

            listener(
                onSuccess = { _, _ ->
                    binding.picture.setPadding(0)
                    binding.progressCircular.isVisible = false
                    onProblemPictureLoaded(topo)
                },
                onError = { _, _ -> loadErrorPicture() }
            )
        }
    }

    private fun loadErrorPicture() {
        binding.picture.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_placeholder)
        )
        binding.picture.setPadding(200)
        binding.progressCircular.isVisible = false
    }

    private fun setUiProblems(
        uiProblems: List<UiProblem>,
        selectedProblem: CompleteProblem?
    ) {
        binding.problemStartsComposeView.setContent {
            BoolderTheme {
                ProblemStartsLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    uiProblems = uiProblems,
                    selectedProblem = selectedProblem,
                    onProblemStartClicked = ::onProblemStartClicked,
                    onVariantSelected = ::onVariantSelected
                )
            }
        }
    }

    private fun onBleauInfoClicked(bleauInfoId: String?) {
        try {
            val bleauUrl = "https://bleau.info/a/$bleauInfoId.html"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bleauUrl))
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Log.i("Bottom Sheet", "No apps can handle this kind of intent")
        }
    }

    private fun onShareClicked(problemId: Int) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "https://www.boolder.com/${Locale.getDefault().language}/p/$problemId"
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
