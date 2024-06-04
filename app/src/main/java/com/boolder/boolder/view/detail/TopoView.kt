package com.boolder.boolder.view.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.utils.getLanguage
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.detail.composable.TopoLayout
import com.boolder.boolder.view.ticklist.TickedProblemSaver

class TopoView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs) {

    private val composeView = ComposeView(context).apply {
        setContent {
            BoolderTheme {
                TopoLayout(
                    topo = null,
                    onProblemPhotoLoaded = {},
                    onProblemStartClicked = {},
                    onShowPhotoFullScreen = { _, _ -> },
                    onVariantSelected = {},
                    onCircuitProblemSelected = {},
                    onBleauInfoClicked = {},
                    onShareClicked = {},
                    onSaveProblem = { _, _ -> },
                    onUnsaveProblem = {}
                )
            }
        }
    }

    var topoCallbacks: TopoCallbacks? = null
    var tickedProblemSaver: TickedProblemSaver? = null

    init {
        addView(composeView)
    }

    fun setTopo(topo: Topo) {
        composeView.setContent {
            BoolderTheme {
                TopoLayout(
                    topo = topo,
                    onProblemPhotoLoaded = { topoCallbacks?.onProblemPhotoLoaded() },
                    onProblemStartClicked = { topoCallbacks?.onProblemStartClicked(it) },
                    onShowPhotoFullScreen = { problemId, photoUri ->
                        topoCallbacks?.onShowProblemPhotoFullScreen(problemId, photoUri)
                    },
                    onVariantSelected = { topoCallbacks?.onVariantSelected(it) },
                    onCircuitProblemSelected = { topoCallbacks?.onCircuitProblemSelected(it) },
                    onBleauInfoClicked = ::onBleauInfoClicked,
                    onShareClicked = ::onShareClicked,
                    onSaveProblem = { problemId, tickStatus ->
                        tickedProblemSaver?.onSaveProblem(problemId, tickStatus)
                    },
                    onUnsaveProblem = { tickedProblemSaver?.onUnsaveProblem(it) }
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
                "https://www.boolder.com/${getLanguage()}/p/$problemId"
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

    interface TopoCallbacks {
        fun onProblemPhotoLoaded()
        fun onProblemStartClicked(problemId: Int)
        fun onVariantSelected(variant: ProblemWithLine)
        fun onCircuitProblemSelected(problemId: Int)
        fun onShowProblemPhotoFullScreen(problemId: Int, photoUri: String)
    }
}
