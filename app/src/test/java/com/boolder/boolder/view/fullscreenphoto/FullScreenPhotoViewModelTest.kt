package com.boolder.boolder.view.fullscreenphoto

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.ProblemWithLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FullScreenPhotoViewModelTest {

    @Mock private lateinit var savedStateHandle: SavedStateHandle
    @Mock private lateinit var lineRepository: LineRepository
    @Mock private lateinit var problemRepository: ProblemRepository

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

            assertEquals(FullScreenPhotoViewModel.ScreenState.Loading, awaitItem())
            assertEquals(contentState, awaitItem())
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
            assertEquals(FullScreenPhotoViewModel.ScreenState.Error, awaitItem())
        }
    }

    private suspend fun viewModelWithMocking(block: suspend () -> Unit): FullScreenPhotoViewModel {
        block()

        return FullScreenPhotoViewModel(
            savedStateHandle = savedStateHandle,
            lineRepository = lineRepository,
            problemRepository = problemRepository
        )
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
