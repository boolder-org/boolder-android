package com.boolder.boolder.view.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.entity.TickEntity
import com.boolder.boolder.data.database.repository.TickRepository
import com.boolder.boolder.databinding.BottomSheetBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import java.util.Locale
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

    private val bleauUrl
        get() = "https://bleau.info/a/${selectedProblem.bleauInfoId}.html"
    private val shareUrl
        get() = "https://www.boolder.com/${Locale.getDefault().language}/p/${selectedProblem.id}"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedLine = completeProblem.line
        selectedProblem = completeProblem.problem
        hideBleauButton(view)
        loadBoolderImage()
        updateLabels()
        setupChipClick()
    }

    private fun markParentAsSelected() {
        selectedLine = completeProblem.line
        selectedProblem = completeProblem.problem
        drawCurves()
        drawCircuitNumberCircle()
        updateLabels()
    }

    private fun hideBleauButton(view: View) {
        if(selectedProblem.bleauInfoId.isNullOrEmpty()) {
            val bleauInfo = view.findViewById<Button>(R.id.bleau_info)
            bleauInfo.visibility = View.GONE
        }
    }

    //region Draw
    private fun drawCurves() {
        val points = selectedLine?.points()
        if (!points.isNullOrEmpty()) {

            val segment = curveAlgorithm.controlPointsFromPoints(points)

            val ctrl1 = segment.map { PointD(it.controlPoint1.x, it.controlPoint1.y) }
            val ctrl2 = segment.map { PointD(it.controlPoint2.x, it.controlPoint2.y) }

            binding.lineVector.apply {
                addDataPoints(
                    data = points,
                    point1 = ctrl1,
                    point2 = ctrl2,
                    drawColor = selectedProblem.getColor(requireContext())
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
            val viewSizeRes = if (selectedProblem.circuitNumber.isNullOrBlank()) {
                R.dimen.size_problem_start_without_number
            } else {
                R.dimen.size_problem_start_with_number
            }

            val viewSize = resources.getDimensionPixelSize(viewSizeRes)
            val marginProblemStart = resources.getDimensionPixelSize(R.dimen.margin_problem_start)
            val viewWithMarginSize = viewSize + marginProblemStart * 2
            val offset = viewWithMarginSize / 2

            val textColor = when (selectedProblem.circuitColorSafe) {
                CircuitColor.WHITE -> Color.BLACK
                else -> Color.WHITE
            }

            val problemStartView = ProblemStartView(binding.root.context).apply {
                setText(selectedProblem.circuitNumber)
                setTextColor(textColor)
                setProblemColor(selectedProblem.getColor(context))
                translationX = (pointD.x * binding.picture.measuredWidth - offset).toFloat()
                translationY = (pointD.y * binding.picture.measuredHeight - offset).toFloat()
            }

            binding.root.addView(problemStartView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }
    }
    //endregion

    private fun updateLabels() {
        binding.title.text = selectedProblem.nameSafe()
        binding.grade.text = selectedProblem.grade

        val steepness = Steepness.fromTextValue(selectedProblem.steepness)

        binding.typeIcon.apply {
            val steepnessDrawable = steepness.iconRes
                ?.let { ContextCompat.getDrawable(context, it) }

            setImageDrawable(steepnessDrawable)
            isVisible = steepnessDrawable != null
        }

        binding.typeText.apply {
            val steepnessText = steepness.textRes?.let(::getString)

            val sitStartText = if (selectedProblem.sitStart) {
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

        binding.tick.setOnClickListener{
            val tickRepository: TickRepository = get()
            val idToWrite = selectedProblem.id

            lifecycleScope.launch {
                val existingTick = tickRepository.loadById(idToWrite)
                if (existingTick == null) {
                    val tick = TickEntity(idToWrite)
                    tickRepository.insertTick(tick)
                }
            }
        }

      /*  binding.reportIssue.setOnClickListener {
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
        }*/

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
        if(Locale.getDefault().language == "fr") {
            return name ?: ""
        }
        else {
            return nameEn ?: ""
        }
    }

    private fun String.localize(): String {
        return CircuitColor.valueOf(this.uppercase()).localize(requireContext())
    }
    //endregion
}

data class PointD(val x: Double, val y: Double)

fun List<PointD>.toF() = map { PointF(it.x.toFloat(), it.y.toFloat()) }
