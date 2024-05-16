package com.boolder.boolder.view.map

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.CircuitRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.data.userdatabase.repository.TickedProblemRepository
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.TickedProblem
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.domain.model.gradeRangeLevelDisplay
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.view.detail.TopoView
import com.boolder.boolder.view.detail.VariantSelector
import com.boolder.boolder.view.map.filter.FiltersEventHandler
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.view.ticklist.TickedProblemSaver
import com.mapbox.maps.CameraState
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
    private val tickedProblemRepository: TickedProblemRepository,
    private val topoDataAggregator: TopoDataAggregator,
    private val resources: Resources,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : ViewModel(),
    OfflineAreaDownloader,
    TopoView.TopoCallbacks,
    TickedProblemSaver,
    FiltersEventHandler {

    private val _screenStateFlow = MutableStateFlow(
        ScreenState(
            areaState = null,
            circuitState = null,
            gradeState = GradeState(
                gradeRangeButtonTitle = resources.getString(R.string.grades),
                grades = ALL_GRADES
            ),
            popularFilterState = PopularFilterState(isEnabled = false),
            projectsFilterState = ProjectsFilterState(projectIds = emptyList()),
            tickedFilterState = TickedFilterState(tickedProblemIds = emptyList()),
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

    var cameraState: CameraState? = null

    fun fetchTopo(problemId: Int, origin: TopoOrigin) {
        viewModelScope.launch {
            val currentTopo = _topoStateFlow.value

            _topoStateFlow.value = topoDataAggregator.aggregate(
                problemId = problemId,
                origin = origin,
                previousPhotoUri = currentTopo?.photoUri,
                previousPhotoWasProperlyLoaded = currentTopo?.canShowProblemStarts == true
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

    fun onAreaSelected(areaId: Int) {
        viewModelScope.launch {
            val area = areaRepository.getAreaById(areaId) ?: return@launch

            _eventFlow.emit(Event.ZoomOnArea(area))
        }
    }

    fun onCircuitSelected(circuitId: Int) {
        if (circuitId < 0) {
            _screenStateFlow.update { it.copy(circuitState = null) }
            return
        }

        val currentCircuitState = _screenStateFlow.value.circuitState

        if (circuitId == currentCircuitState?.circuitId) return

        viewModelScope.launch {
            val circuit = circuitRepository.getCircuitById(circuitId) ?: return@launch

            val newCircuitState = CircuitState(
                circuitId = circuit.id,
                color = circuit.color,
                showCircuitStartButton = true
            )

            _screenStateFlow.update { it.copy(circuitState = newCircuitState) }
            _eventFlow.emit(Event.ZoomOnCircuit(circuit))
        }
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
            val currentAreaState = _screenStateFlow.value.areaState
            val currentCircuitState = _screenStateFlow.value.circuitState

            if (currentAreaState?.area?.id == areaId) return@launch

            val area = areaRepository.getAreaById(areaId) ?: return@launch

            val offlineStatus = boolderOfflineRepository.getStatusForAreaId(areaId)

            val newAreaState = OfflineAreaItem(
                area = area,
                status = offlineStatus
            )
            val newCircuitState = if (currentAreaState?.area == null) currentCircuitState else null

            _screenStateFlow.update {
                it.copy(
                    areaState = newAreaState,
                    circuitState = newCircuitState
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

    fun onTopoUnselected() {
        _topoStateFlow.value = null
    }

    private fun updateFilterBarVisibility() {
        val shouldShowFiltersBar = lastZoomValue >= 15.0 && !isProblemTopoShown

        if (_screenStateFlow.value.shouldShowFiltersBar == shouldShowFiltersBar) return

        _screenStateFlow.update { it.copy(shouldShowFiltersBar = shouldShowFiltersBar) }
    }

    // region FiltersEventHandler

    override fun onResetFiltersButtonClicked() {
        _screenStateFlow.update {
            it.copy(
                circuitState = null,
                gradeState = GradeState(
                    gradeRangeButtonTitle = resources.getString(R.string.grades),
                    grades = ALL_GRADES
                ),
                popularFilterState = PopularFilterState(isEnabled = false),
                projectsFilterState = ProjectsFilterState(projectIds = emptyList()),
                tickedFilterState = TickedFilterState(tickedProblemIds = emptyList())
            )
        }
    }

    override fun onCircuitFilterChipClicked() {
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

    override fun onGradeFilterChipClicked() {
        viewModelScope.launch {
            _eventFlow.emit(Event.ShowGradeRanges(currentGradeRange = currentGradeRange))
        }
    }

    override fun onPopularFilterChipClicked() {
        val popularFilterValue = _screenStateFlow.value.popularFilterState.isEnabled

        _screenStateFlow.update {
            it.copy(
                popularFilterState = it.popularFilterState.copy(isEnabled = !popularFilterValue)
            )
        }
    }

    override fun onProjectsFilterChipClicked() {
        viewModelScope.launch {
            val projectIds = _screenStateFlow.value.projectsFilterState.projectIds
            val newProjectIds = if (projectIds.isNotEmpty()) {
                emptyList()
            } else {
                tickedProblemRepository.getAllProjectIds()
                    .also { if (it.isEmpty()) _eventFlow.emit(Event.WarnNoSavedProjects) }
            }

            _screenStateFlow.update {
                it.copy(
                    projectsFilterState = ProjectsFilterState(projectIds = newProjectIds),
                    tickedFilterState = if (newProjectIds.isNotEmpty()) {
                        TickedFilterState(tickedProblemIds = emptyList())
                    } else {
                        it.tickedFilterState
                    }
                )
            }
        }
    }

    override fun onTickedFilterChipClicked() {
        viewModelScope.launch {
            val tickedProblemIds = _screenStateFlow.value.tickedFilterState.tickedProblemIds
            val newTickedProblemIds = if (tickedProblemIds.isNotEmpty()) {
                emptyList()
            } else {
                tickedProblemRepository.getAllTickedProblemIds()
                    .also { if (it.isEmpty()) _eventFlow.emit(Event.WarnNoTickedProblems) }
            }

            _screenStateFlow.update {
                it.copy(
                    tickedFilterState = TickedFilterState(tickedProblemIds = newTickedProblemIds),
                    projectsFilterState = if (newTickedProblemIds.isNotEmpty()) {
                        ProjectsFilterState(projectIds = emptyList())
                    } else {
                        it.projectsFilterState
                    }
                )
            }
        }
    }

    // endregion FiltersEventHandler

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

    override fun onDeleteAreaPhotos(areaId: Int) {
        // not implemented here as no button can trigger this function in this view model
    }

    // endregion OfflineAreaDownloader

    // region TopoCallbacks

    override fun onProblemPhotoLoaded() {
        _topoStateFlow.update { it?.copy(canShowProblemStarts = true) }
    }

    override fun onProblemStartClicked(problemId: Int) {
        viewModelScope.launch { _eventFlow.emit(Event.SelectProblemOnMap(problemId = problemId)) }

        val currentTopoState = _topoStateFlow.value ?: return

        viewModelScope.launch {
            val selectedProblem = currentTopoState.otherCompleteProblems
                .find { it.problemWithLine.problem.id == problemId }
                ?: return@launch

            val otherProblems = buildList {
                currentTopoState.selectedCompleteProblem?.let(::add)
                currentTopoState.otherCompleteProblems.forEach { completeProblem ->
                    if (completeProblem != selectedProblem) add(completeProblem)
                }
            }

            val circuitInfo = topoDataAggregator.updateCircuitControlsForProblem(problemId)

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

    override fun onVariantSelected(variant: ProblemWithLine) {
        val currentTopo = _topoStateFlow.value ?: return
        val selectedProblemAsList = currentTopo.selectedCompleteProblem
            ?.let(::listOf)
            ?: emptyList()

        val completeProblems = currentTopo.otherCompleteProblems + selectedProblemAsList

        val (selectedProblem, newCompleteProblems) = VariantSelector.selectVariantInProblemStarts(
            selectedVariant = variant,
            completeProblems = completeProblems
        )

        _topoStateFlow.update {
            it?.copy(
                selectedCompleteProblem = selectedProblem,
                otherCompleteProblems = newCompleteProblems
            )
        }
    }

    override fun onCircuitProblemSelected(problemId: Int) {
        fetchTopo(problemId = problemId, origin = TopoOrigin.CIRCUIT)
    }

    override fun onShowProblemPhotoFullScreen(problemId: Int, photoUri: String) {
        viewModelScope.launch {
            _eventFlow.emit(Event.ShowProblemPhotoFullScreen(problemId, photoUri))
        }
    }

    // endregion TopoCallbacks

    // region TickedProblemSaver

    override fun onSaveProblem(problemId: Int, tickStatus: TickStatus) {
        viewModelScope.launch {
            tickedProblemRepository.deleteTickedProblemByProblemId(problemId)

            val tickedProblem = TickedProblem(
                problemId = problemId,
                tickStatus = tickStatus
            )

            tickedProblemRepository.insertTickedProblem(tickedProblem)

            updateTickStatusInCurrentTopo(problemId, tickStatus)
        }
    }

    override fun onUnsaveProblem(problemId: Int) {
        viewModelScope.launch {
            tickedProblemRepository.deleteTickedProblemByProblemId(problemId)
            updateTickStatusInCurrentTopo(problemId, null)
        }
    }

    private fun updateTickStatusInCurrentTopo(problemId: Int, tickStatus: TickStatus?) {
        val currentTopoProblem = _topoStateFlow.value
            ?.selectedCompleteProblem
            ?.problemWithLine
            ?.problem

        if (currentTopoProblem?.id != problemId) return

        val updatedTopoProblem = currentTopoProblem.copy(tickStatus = tickStatus)

        _topoStateFlow.update {
            it?.copy(
                selectedCompleteProblem = it.selectedCompleteProblem?.copy(
                    problemWithLine = it.selectedCompleteProblem.problemWithLine.copy(
                        problem = updatedTopoProblem
                    )
                )
            )
        }
    }

    // endregion TickedProblemSaver

    data class ScreenState(
        val areaState: OfflineAreaItem?,
        val circuitState: CircuitState?,
        val gradeState: GradeState,
        val popularFilterState: PopularFilterState,
        val projectsFilterState: ProjectsFilterState,
        val tickedFilterState: TickedFilterState,
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

    data class ProjectsFilterState(val projectIds: List<Int>)

    data class TickedFilterState(val tickedProblemIds: List<Int>)

    sealed interface Event {
        data class SelectProblemOnMap(val problemId: Int) : Event

        data class ShowAvailableCircuits(
            val selectedCircuit: Circuit?,
            val availableCircuits: List<Circuit>
        ) : Event

        data class ShowGradeRanges(val currentGradeRange: GradeRange) : Event

        data class ShowProblemPhotoFullScreen(
            val problemId: Int,
            val photoUri: String
        ) : Event

        data class ZoomOnCircuit(val circuit: Circuit) : Event

        data class ZoomOnCircuitStartProblem(val problemId: Int) : Event
        data class ZoomOnArea(val area: Area) : Event

        data object WarnNoSavedProjects : Event
        data object WarnNoTickedProblems : Event
    }
}
