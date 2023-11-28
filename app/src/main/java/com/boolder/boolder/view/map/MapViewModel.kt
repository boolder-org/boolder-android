package com.boolder.boolder.view.map

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.CircuitRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.domain.model.gradeRangeLevelDisplay
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val areaRepository: AreaRepository,
    private val circuitRepository: CircuitRepository,
    private val problemRepository: ProblemRepository,
    private val topoDataAggregator: TopoDataAggregator,
    private val resources: Resources,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : ViewModel(), OfflineAreaDownloader {

    private val _screenStateFlow = MutableStateFlow(
        ScreenState(
            areaState = null,
            circuitState = null,
            gradeState = GradeState(
                gradeRangeButtonTitle = resources.getString(R.string.grades),
                grades = ALL_GRADES
            ),
            popularFilterState = PopularFilterState(isEnabled = false),
            shouldShowFiltersBar = false
        )
    )
    val screenStateFlow = _screenStateFlow.asStateFlow()

    private val _topoStateFlow = MutableStateFlow<Topo?>(null)
    val topoStateFlow = _topoStateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentGradeRange = GradeRange(
        min = ALL_GRADES.first(),
        max = ALL_GRADES.last(),
        isCustom = false
    )

    private var lastZoomValue = 0.0
    private var isProblemTopoShown = false

    fun fetchTopo(problemId: Int, origin: TopoOrigin) {
        viewModelScope.launch {
            _topoStateFlow.value = topoDataAggregator.aggregate(
                problemId = problemId,
                origin = origin
            )

            if (origin != TopoOrigin.CIRCUIT) {
                _screenStateFlow.update {
                    it.copy(
                        circuitState = it.circuitState?.copy(showCircuitStartButton = false)
                    )
                }

                return@launch
            }

            val circuit = circuitRepository.getCircuitFromProblemId(problemId) ?: return@launch

            _screenStateFlow.update {
                it.copy(
                    circuitState = CircuitState(
                        circuitId = circuit.id,
                        color = circuit.color,
                        showCircuitStartButton = false
                    )
                )
            }
        }
    }

    fun updateCircuitControlsForProblem(problemId: String) {
        val currentTopoState = _topoStateFlow.value ?: return

        val intProblemId = try {
            problemId.toInt()
        } catch (e: NumberFormatException) {
            return
        }

        viewModelScope.launch {
            val selectedProblem = currentTopoState.otherCompleteProblems
                .find { it.problemWithLine.problem.id == intProblemId }
                ?: return@launch

            val otherProblems = buildList {
                currentTopoState.selectedCompleteProblem?.let(::add)
                currentTopoState.otherCompleteProblems.forEach { completeProblem ->
                    if (completeProblem != selectedProblem) add(completeProblem)
                }
            }

            val circuitInfo = topoDataAggregator.updateCircuitControlsForProblem(intProblemId)

            _topoStateFlow.update {
                currentTopoState.copy(
                    selectedCompleteProblem = selectedProblem,
                    otherCompleteProblems = otherProblems,
                    circuitInfo = circuitInfo,
                    origin = TopoOrigin.TOPO
                )
            }
        }
    }

    fun onCircuitSelected(circuit: Circuit?) {
        val currentCircuitState = _screenStateFlow.value.circuitState
        val newCircuitState = circuit?.let {
            CircuitState(
                circuitId = it.id,
                color = it.color,
                showCircuitStartButton = true
            )
        }

        if (newCircuitState == currentCircuitState) return

        _screenStateFlow.update { it.copy(circuitState = newCircuitState) }

        if (circuit == null) return

        viewModelScope.launch { _eventFlow.emit(Event.ZoomOnCircuit(circuit)) }
    }

    fun onGradeRangeSelected(gradeRange: GradeRange) {
        currentGradeRange = gradeRange

        val grades = with(ALL_GRADES) {
            subList(
                fromIndex = indexOf(gradeRange.min),
                toIndex = indexOf(gradeRange.max) + 1
            )
        }

        val gradeRangeButtonTitle = if (gradeRange == GradeRange.LARGEST) {
            resources.getString(R.string.grades)
        } else {
            resources.gradeRangeLevelDisplay(gradeRange)
        }

        val newGradeState = GradeState(
            gradeRangeButtonTitle = gradeRangeButtonTitle,
            grades = grades
        )

        _screenStateFlow.update { it.copy(gradeState = newGradeState) }
    }

    fun onAreaVisited(areaId: Int) {
        viewModelScope.launch {
            val currentState = _screenStateFlow.value.areaState

            if (currentState?.area?.id == areaId) return@launch

            val area = areaRepository.getAreaById(areaId).convert()
            val offlineStatus = boolderOfflineRepository.getStatusForAreaId(areaId)

            val newAreaState = OfflineAreaItem(
                area = area,
                status = offlineStatus
            )

            _screenStateFlow.update {
                it.copy(
                    areaState = newAreaState,
                    circuitState = null
                )
            }
        }
    }

    fun onAreaLeft() {
        _screenStateFlow.value.areaState ?: return

        _screenStateFlow.update {
            it.copy(
                areaState = null,
                circuitState = null
            )
        }
    }

    fun onZoomLevelChanged(zoomLevel: Double) {
        lastZoomValue = zoomLevel
        updateFilterBarVisibility()
    }

    fun onProblemTopoVisibilityChanged(isVisible: Boolean) {
        isProblemTopoShown = isVisible
        updateFilterBarVisibility()
    }

    private fun updateFilterBarVisibility() {
        val shouldShowFiltersBar = lastZoomValue >= 15.0 && !isProblemTopoShown

        if (_screenStateFlow.value.shouldShowFiltersBar == shouldShowFiltersBar) return

        _screenStateFlow.update { it.copy(shouldShowFiltersBar = shouldShowFiltersBar) }
    }

    fun onResetFiltersButtonClicked() {
        _screenStateFlow.update {
            it.copy(
                circuitState = null,
                gradeState = GradeState(
                    gradeRangeButtonTitle = resources.getString(R.string.grades),
                    grades = ALL_GRADES
                ),
                popularFilterState = PopularFilterState(isEnabled = false)
            )
        }
    }

    fun onCircuitFilterChipClicked() {
        viewModelScope.launch {
            val areaState = _screenStateFlow.value.areaState ?: return@launch

            val availableCircuits = circuitRepository.getAvailableCircuits(areaState.area.id)

            val event = Event.ShowAvailableCircuits(
                selectedCircuit = null,
                availableCircuits = availableCircuits
            )

            _eventFlow.emit(event)
        }
    }

    fun onGradeFilterChipClicked() {
        viewModelScope.launch {
            _eventFlow.emit(Event.ShowGradeRanges(currentGradeRange = currentGradeRange))
        }
    }

    fun onPopularFilterChipClicked() {
        val popularFilterValue = _screenStateFlow.value.popularFilterState.isEnabled

        _screenStateFlow.update {
            it.copy(
                popularFilterState = it.popularFilterState.copy(isEnabled = !popularFilterValue)
            )
        }
    }

    fun onCircuitDepartureButtonClicked() {
        viewModelScope.launch {
            val circuitId = _screenStateFlow.value.circuitState?.circuitId ?: return@launch

            val problemId = problemRepository.problemIdByCircuitAndNumber(
                circuitId = circuitId,
                circuitProblemNumber = 1
            ) ?: return@launch

            _eventFlow.emit(Event.ZoomOnCircuitStartProblem(problemId))
        }
    }

    // region OfflineAreaDownloader

    override fun onDownloadArea(areaId: Int) {
        val currentAreaState = _screenStateFlow.value.areaState ?: return

        if (currentAreaState.area.id != areaId) return

        _screenStateFlow.update {
            it.copy(
                areaState = currentAreaState.copy(
                    status = OfflineAreaItemStatus.Downloading(areaId)
                )
            )
        }

        boolderOfflineRepository.downloadArea(areaId)
    }

    override fun onCancelAreaDownload(areaId: Int) {
        val currentAreaState = _screenStateFlow.value.areaState ?: return

        if (currentAreaState.area.id != areaId) return

        boolderOfflineRepository.cancelAreaDownload(areaId)
        boolderOfflineRepository.deleteArea(areaId)

        _screenStateFlow.update {
            it.copy(
                areaState = currentAreaState.copy(
                    status = OfflineAreaItemStatus.NotDownloaded
                )
            )
        }
    }

    override fun onAreaDownloadTerminated(areaId: Int) {
        val currentAreaState = _screenStateFlow.value.areaState ?: return

        if (currentAreaState.area.id != areaId) return

        _screenStateFlow.update {
            it.copy(
                areaState = currentAreaState.copy(
                    status = OfflineAreaItemStatus.Downloaded(folderSize = "")
                )
            )
        }
    }

    // endregion OfflineAreaDownloader

    data class ScreenState(
        val areaState: OfflineAreaItem?,
        val circuitState: CircuitState?,
        val gradeState: GradeState,
        val popularFilterState: PopularFilterState,
        val shouldShowFiltersBar: Boolean
    )

    data class CircuitState(
        val circuitId: Int,
        val color: CircuitColor,
        val showCircuitStartButton: Boolean
    )

    data class GradeState(
        val gradeRangeButtonTitle: String,
        val grades: List<String>
    )

    data class PopularFilterState(val isEnabled: Boolean)

    sealed interface Event {
        data class ShowAvailableCircuits(
            val selectedCircuit: Circuit?,
            val availableCircuits: List<Circuit>
        ) : Event

        data class ShowGradeRanges(val currentGradeRange: GradeRange) : Event

        data class ZoomOnCircuit(val circuit: Circuit) : Event

        data class ZoomOnCircuitStartProblem(val problemId: Int) : Event
    }
}
