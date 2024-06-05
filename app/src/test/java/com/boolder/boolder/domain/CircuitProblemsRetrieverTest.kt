package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.ProblemRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class CircuitProblemsRetrieverTest {

    private val problemRepository = mock<ProblemRepository> {
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_D, "D") } doReturn(100)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_D, "1") } doReturn(101)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_D, "2") } doReturn(102)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_D, "3") } doReturn(null)

        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_1, "D") } doReturn(null)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_1, "1") } doReturn(201)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_1, "2") } doReturn(202)
        onBlocking { problemIdByCircuitAndNumber(CIRCUIT_ID_WITH_START_1, "3") } doReturn(null)
    }

    private lateinit var circuitProblemsRetriever: CircuitProblemsRetriever

    @Before
    fun setUp() {
        circuitProblemsRetriever = CircuitProblemsRetriever(problemRepository)
    }

    @Test
    fun `getCircuitStartProblemId() for circuit with start number D should return 100`() = runTest {
        // Given
        val circuitId = CIRCUIT_ID_WITH_START_D

        // When
        val circuitStartProblemId = circuitProblemsRetriever.getCircuitStartProblemId(circuitId)

        // Then
        assertEquals(100, circuitStartProblemId)
    }

    @Test
    fun `getCircuitStartProblemId() for circuit with start number 1 should return 201`() = runTest {
        // Given
        val circuitId = CIRCUIT_ID_WITH_START_1

        // When
        val circuitStartProblemId = circuitProblemsRetriever.getCircuitStartProblemId(circuitId)

        // Then
        assertEquals(201, circuitStartProblemId)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() should return null and null as it does not belong to any circuit`() = runTest {
        // Given
        val problem = problemEntity(circuitId = null, circuitNumber = null)

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = null,
            next = null
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() should return null and null as its circuit number is null`() = runTest {
        // Given
        val problem = problemEntity(circuitId = 666, circuitNumber = null)

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = null,
            next = null
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() should return null and null as its circuit number is not valid`() = runTest {
        // Given
        val problem = problemEntity(circuitId = 666, circuitNumber = "INVALID")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = null,
            next = null
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() for circuit with start number D should return null and 101`() = runTest {
        // Given
        val problem = problemEntity(circuitId = CIRCUIT_ID_WITH_START_D, circuitNumber = "D")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = null,
            next = 101
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() for circuit with start number D should return 100 and 102`() = runTest {
        // Given
        val problem = problemEntity(circuitId = CIRCUIT_ID_WITH_START_D, circuitNumber = "1")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = 100,
            next = 102
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() for circuit with start number D should return 101 and null`() = runTest {
        // Given
        val problem = problemEntity(circuitId = CIRCUIT_ID_WITH_START_D, circuitNumber = "2")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = 101,
            next = null
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() for circuit with start number 1 should return null and 202`() = runTest {
        // Given
        val problem = problemEntity(circuitId = CIRCUIT_ID_WITH_START_1, circuitNumber = "1")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = null,
            next = 202
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    @Test
    fun `getCircuitPreviousAndNextProblemIds() for circuit with start number 1 should return 201 and null`() = runTest {
        // Given
        val problem = problemEntity(circuitId = CIRCUIT_ID_WITH_START_1, circuitNumber = "2")

        // When
        val adjacentCircuitProblemIds = circuitProblemsRetriever.getCircuitPreviousAndNextProblemIds(problem)

        // Then
        val expectedAdjacentCircuitProblemIds = CircuitProblemsRetriever.AdjacentCircuitProblemIds(
            previous = 201,
            next = null
        )

        assertEquals(expectedAdjacentCircuitProblemIds, adjacentCircuitProblemIds)
    }

    private fun problemEntity(
        circuitId: Int?,
        circuitNumber: String?,
    ) = ProblemEntity(
        id = 0,
        name = null,
        nameEn = null,
        nameSearchable = null,
        grade = null,
        latitude = 0f,
        longitude = 0f,
        circuitId = circuitId,
        circuitColor = null,
        circuitNumber = circuitNumber,
        steepness = "wall",
        sitStart = false,
        areaId = 0,
        bleauInfoId = null,
        featured = false,
        popularity = null,
        parentId = null
    )

    companion object {
        private const val CIRCUIT_ID_WITH_START_D = 0
        private const val CIRCUIT_ID_WITH_START_1 = 1
    }
}
