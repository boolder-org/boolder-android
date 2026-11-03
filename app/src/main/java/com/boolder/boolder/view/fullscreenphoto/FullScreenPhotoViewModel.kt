package com.boolder.boolder.view.fullscreenphoto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.premium.model.isPhotoLocked
import com.boolder.boolder.data.premium.repository.PremiumRepository
import com.boolder.boolder.domain.BoulderToposRetriever
import com.boolder.boolder.domain.TopoDataAggregator
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.BoulderTopo
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullScreenPhotoViewModel(
    savedStateHandle: SavedStateHandle,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository,
    private val premiumRepository: PremiumRepository,
    private val boulderToposRetriever: BoulderToposRetriever,
    private val topoDataAggregator: TopoDataAggregator
) : ViewModel() {

    private val problemId = requireNotNull(savedStateHandle.get<Int>("problem_id"))
    private val photoUri = requireNotNull(savedStateHandle.get<String>("photo_uri"))
    private val topoId = savedStateHandle.get<Int>("topo_id")?.takeIf { it >= 0 }
    private val isTopoMode = savedStateHandle.get<Boolean>("is_topo_mode") == true
    private val boulderTopos = run {
        val topoIds = savedStateHandle.get<IntArray>("topo_ids") ?: intArrayOf()
        val photoUris = savedStateHandle.get<Array<String>>("photo_uris") ?: emptyArray()

        topoIds.zip(photoUris) { topoId, photoUri -> BoulderTopo(topoId = topoId, photoUri = photoUri) }
    }

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    // only the latest selection should end up in the screen
    private var loadTopoJob: Job? = null

    init {
        viewModelScope.launch {
            val problem = problemRepository.problemById(problemId)
                ?.convert()
                ?: run {
                    _screenState.update { ScreenState.Error }
                    return@launch
                }

            val line = lineRepository.loadByProblemId(problemId)
                ?.convert()

            val completeProblem = CompleteProblem(
                problemWithLine = ProblemWithLine(
                    problem = problem,
                    line = line
                ),
                variants = emptyList()
            )

            _screenState.update {
                ScreenState.Content(
                    photoUri = photoUri,
                    completeProblem = completeProblem,
                    topoId = topoId,
                    boulderTopos = boulderTopos,
                    isPhotoLocked = isPhotoLocked(
                        areaId = problem.areaId,
                        freeAreaIds = premiumRepository.freeAreaIds.value,
                        status = premiumRepository.status.value
                    )
                )
            }

            // the bottom sheet was showing the whole picture
            if (isTopoMode && topoId != null) onTopoSwiped(topoId)
        }
    }

    fun onTopoSwiped(topoId: Int) {
        loadTopo {
            val topProblemId = boulderToposRetriever.getTopProblemId(topoId) ?: return@loadTopo null

            aggregate(topProblemId).copy(isTopoMode = true)
        }
    }

    fun onProblemSelected(problemId: Int) {
        loadTopo { aggregate(problemId) }
    }

    private fun loadTopo(block: suspend () -> Topo?) {
        loadTopoJob?.cancel()
        loadTopoJob = viewModelScope.launch {
            val shownTopo = block() ?: return@launch

            _screenState.update {
                (it as? ScreenState.Content)?.copy(shownTopo = shownTopo) ?: it
            }
        }
    }

    private suspend fun aggregate(problemId: Int) = topoDataAggregator.aggregate(
        problemId = problemId,
        origin = TopoOrigin.TOPO,
        previousPhotoUri = null,
        previousPhotoWasProperlyLoaded = false
    )

    sealed interface ScreenState {
        data object Loading : ScreenState

        data class Content(
            val photoUri: String,
            val completeProblem: CompleteProblem,
            val topoId: Int? = null,
            val boulderTopos: List<BoulderTopo> = emptyList(),
            val isPhotoLocked: Boolean = false,
            val shownTopo: Topo? = null
        ) : ScreenState

        data object Error : ScreenState
    }
}
