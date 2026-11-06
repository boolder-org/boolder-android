package com.boolder.boolder.view.fullscreenphoto

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.premium.model.PremiumStatus
import com.boolder.boolder.data.premium.repository.PremiumRepository
import com.boolder.boolder.domain.BoulderToposRetriever
import com.boolder.boolder.domain.TopoDataAggregator
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FullScreenPhotoViewModelTest {

    @Mock private lateinit var savedStateHandle: SavedStateHandle
    @Mock private lateinit var lineRepository: LineRepository
    @Mock private lateinit var problemRepository: ProblemRepository
    @Mock private lateinit var premiumRepository: PremiumRepository
    @Mock private lateinit var boulderToposRetriever: BoulderToposRetriever
    @Mock private lateinit var topoDataAggregator: TopoDataAggregator

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Default)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should emit a Content state`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            mockPremium(freeAreaIds = setOf(1), status = PremiumStatus.NOT_SUBSCRIBED)
        }

        // When
        viewModel.screenState.test {

            // Then
            val contentState = FullScreenPhotoViewModel.ScreenState.Content(
                photoUri = "https://www.boolder.com/en/photo/666.png",
                completeProblem = CompleteProblem(
                    problemWithLine = ProblemWithLine(
                        problem = PROBLEM,
                        line = LINE
                    ),
                    variants = emptyList()
                )
            )

            assertEquals(contentState, awaitFinalState())
        }
    }

    @Test
    fun `viewModel should emit a locked Content state for a paid area when not subscribed`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            mockPremium(freeAreaIds = setOf(2), status = PremiumStatus.NOT_SUBSCRIBED)
        }

        // When
        viewModel.screenState.test {

            // Then
            val contentState = awaitFinalState() as FullScreenPhotoViewModel.ScreenState.Content

            assertEquals(true, contentState.isPhotoLocked)
        }
    }

    @Test
    fun `viewModel should emit an unlocked Content state for a paid area when subscribed`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            mockPremium(freeAreaIds = setOf(2), status = PremiumStatus.SUBSCRIBED)
        }

        // When
        viewModel.screenState.test {

            // Then
            val contentState = awaitFinalState() as FullScreenPhotoViewModel.ScreenState.Content

            assertEquals(false, contentState.isPhotoLocked)
        }
    }

    @Test
    fun `viewModel should emit an Error state`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(null).whenever(problemRepository).problemById(666)
        }

        // When
        viewModel.screenState.test {

            // Then
            assertEquals(FullScreenPhotoViewModel.ScreenState.Error, awaitFinalState())
        }
    }

    @Test
    fun `getChangedTopo() without any selection should return null`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(123).whenever(savedStateHandle).get<Int>("topo_id")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            mockPremium(freeAreaIds = setOf(1), status = PremiumStatus.NOT_SUBSCRIBED)
        }

        viewModel.screenState.test {
            awaitFinalState()

            // When
            val changedTopo = viewModel.getChangedTopo()

            // Then
            assertNull(changedTopo)
        }
    }

    @Test
    fun `getChangedTopo() after swiping back to the shown topo should return null`() = runTest {
        // Given
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(123).whenever(savedStateHandle).get<Int>("topo_id")
            doReturn(true).whenever(savedStateHandle).get<Boolean>("is_topo_mode")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            doReturn(666).whenever(boulderToposRetriever).getTopProblemId(123)
            doReturn(topo(problemId = 666, topoId = 123)).whenever(topoDataAggregator).aggregate(666, TopoOrigin.TOPO, null, false)
            mockPremium(freeAreaIds = setOf(1), status = PremiumStatus.NOT_SUBSCRIBED)
        }

        viewModel.screenState.test {
            awaitShownTopo()

            // When
            val changedTopo = viewModel.getChangedTopo()

            // Then
            assertNull(changedTopo)
        }
    }

    @Test
    fun `getChangedTopo() after selecting another problem should return the shown topo`() = runTest {
        // Given
        val otherTopo = topo(problemId = 777, topoId = 123)
        val viewModel = viewModelWithMocking {
            doReturn(666).whenever(savedStateHandle).get<Int>("problem_id")
            doReturn("https://www.boolder.com/en/photo/666.png").whenever(savedStateHandle).get<String>("photo_uri")
            doReturn(123).whenever(savedStateHandle).get<Int>("topo_id")
            doReturn(PROBLEM_ENTITY).whenever(problemRepository).problemById(666)
            doReturn(LINE_ENTITY).whenever(lineRepository).loadByProblemId(666)
            doReturn(otherTopo).whenever(topoDataAggregator).aggregate(777, TopoOrigin.TOPO, null, false)
            mockPremium(freeAreaIds = setOf(1), status = PremiumStatus.NOT_SUBSCRIBED)
        }

        viewModel.screenState.test {
            awaitFinalState()

            // When
            viewModel.onProblemSelected(777)
            awaitShownTopo()
            val changedTopo = viewModel.getChangedTopo()

            // Then
            assertEquals(otherTopo, changedTopo)
        }
    }

    private suspend fun viewModelWithMocking(block: suspend () -> Unit): FullScreenPhotoViewModel {
        block()

        return FullScreenPhotoViewModel(
            savedStateHandle = savedStateHandle,
            lineRepository = lineRepository,
            problemRepository = problemRepository,
            premiumRepository = premiumRepository,
            boulderToposRetriever = boulderToposRetriever,
            topoDataAggregator = topoDataAggregator
        )
    }

    private suspend fun ReceiveTurbine<FullScreenPhotoViewModel.ScreenState>.awaitFinalState(): FullScreenPhotoViewModel.ScreenState =
        awaitItem().let { if (it is FullScreenPhotoViewModel.ScreenState.Loading) awaitItem() else it }

    private suspend fun ReceiveTurbine<FullScreenPhotoViewModel.ScreenState>.awaitShownTopo() {
        while ((awaitItem() as? FullScreenPhotoViewModel.ScreenState.Content)?.shownTopo == null) Unit
    }

    private fun topo(problemId: Int, topoId: Int) = Topo(
        photoUri = null,
        selectedCompleteProblem = CompleteProblem(
            problemWithLine = ProblemWithLine(problem = PROBLEM.copy(id = problemId), line = null),
            variants = emptyList()
        ),
        otherCompleteProblems = emptyList(),
        circuitInfo = null,
        origin = TopoOrigin.TOPO,
        topoId = topoId,
        isTopoMode = true
    )

    private fun mockPremium(freeAreaIds: Set<Int>, status: PremiumStatus) {
        doReturn(MutableStateFlow(freeAreaIds)).whenever(premiumRepository).freeAreaIds
        doReturn(MutableStateFlow(status)).whenever(premiumRepository).status
    }

    companion object {
        private val PROBLEM_ENTITY = ProblemEntity(
            id = 666,
            name = "Dummy",
            nameEn = "Dummy",
            nameSearchable = "dummy",
            grade = "4",
            latitude = 0f,
            longitude = 0f,
            circuitId = null,
            circuitNumber = null,
            circuitColor = null,
            steepness = "wall",
            sitStart = false,
            areaId = 1,
            bleauInfoId = null,
            featured = false,
            popularity = null,
            parentId = null
        )

        private val PROBLEM = Problem(
            id = 666,
            name = "Dummy",
            nameEn = "Dummy",
            grade = "4",
            latitude = 0f,
            longitude = 0f,
            circuitId = null,
            circuitNumber = null,
            circuitColor = null,
            steepness = "wall",
            sitStart = false,
            areaId = 1,
            bleauInfoId = null,
            featured = false,
            parentId = null,
            areaName = null,
            tickStatus = null
        )

        private val LINE_ENTITY = LineEntity(
            id = 345,
            problemId = 666,
            topoId = 123,
            coordinates = null
        )

        private val LINE = Line(
            id = 345,
            problemId = 666,
            topoId = 123,
            coordinates = null
        )
    }
}
