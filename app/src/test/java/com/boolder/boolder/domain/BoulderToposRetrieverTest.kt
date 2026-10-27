package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.TopoEntity
import com.boolder.boolder.data.database.repository.BoulderTopoRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class BoulderToposRetrieverTest {

    private val boulderTopoRepository = mock<BoulderTopoRepository> {
        onBlocking { loadById(TOPO_ON_BOULDER) } doReturn topo(TOPO_ON_BOULDER, boulderId = BOULDER_ID)
        onBlocking { loadById(TOPO_WITHOUT_BOULDER) } doReturn topo(TOPO_WITHOUT_BOULDER, boulderId = null)
        onBlocking { loadAllByBoulderId(BOULDER_ID) } doReturn listOf(
            topo(30, boulderId = BOULDER_ID),
            topo(TOPO_ON_BOULDER, boulderId = BOULDER_ID),
            topo(10, boulderId = BOULDER_ID)
        )
    }

    private val lineRepository = mock<LineRepository>()

    private val problemRepository = mock<ProblemRepository>()

    private lateinit var boulderToposRetriever: BoulderToposRetriever

    @Before
    fun setUp() {
        boulderToposRetriever = BoulderToposRetriever(
            boulderTopoRepository = boulderTopoRepository,
            lineRepository = lineRepository,
            problemRepository = problemRepository
        )
    }

    @Test
    fun `getBoulderTopoIds() for a topo on a boulder should return the boulder topos in the repository order`() = runTest {
        // When
        val topoIds = boulderToposRetriever.getBoulderTopoIds(TOPO_ON_BOULDER)

        // Then
        assertEquals(listOf(30, TOPO_ON_BOULDER, 10), topoIds)
    }

    @Test
    fun `getBoulderTopoIds() for a topo without boulder should return only this topo`() = runTest {
        // When
        val topoIds = boulderToposRetriever.getBoulderTopoIds(TOPO_WITHOUT_BOULDER)

        // Then
        assertEquals(listOf(TOPO_WITHOUT_BOULDER), topoIds)
    }

    @Test
    fun `getBoulderTopoIds() for an unknown topo should return only this topo`() = runTest {
        // When
        val topoIds = boulderToposRetriever.getBoulderTopoIds(UNKNOWN_TOPO)

        // Then
        assertEquals(listOf(UNKNOWN_TOPO), topoIds)
    }

    @Test
    fun `getTopProblemId() should return the most popular problem`() = runTest {
        // Given
        givenTopoProblems(
            problem(1, popularity = 10),
            problem(2, popularity = 50),
            problem(3, popularity = null)
        )

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertEquals(2, topProblemId)
    }

    @Test
    fun `getTopProblemId() should prefer a circuit problem over a more popular one`() = runTest {
        // Given
        givenTopoProblems(
            problem(1, popularity = 500),
            problem(2, popularity = 5, circuitId = 7)
        )

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertEquals(2, topProblemId)
    }

    @Test
    fun `getTopProblemId() with equal popularity should return the highest id`() = runTest {
        // Given
        givenTopoProblems(
            problem(4, popularity = 10),
            problem(9, popularity = 10)
        )

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertEquals(9, topProblemId)
    }

    @Test
    fun `getTopProblemId() should prefer a problem with a drawn line`() = runTest {
        // Given
        givenTopoProblems(
            problem(1, popularity = 500),
            problem(2, popularity = 5),
            problemIdsWithoutLine = setOf(1)
        )

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertEquals(2, topProblemId)
    }

    @Test
    fun `getTopProblemId() without any drawn line should return the most popular problem`() = runTest {
        // Given
        givenTopoProblems(
            problem(1, popularity = 500),
            problem(2, popularity = 5),
            problemIdsWithoutLine = setOf(1, 2)
        )

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertEquals(1, topProblemId)
    }

    @Test
    fun `getTopProblemId() for a topo without problems should return null`() = runTest {
        // Given
        givenTopoProblems()

        // When
        val topProblemId = boulderToposRetriever.getTopProblemId(TOPO_ON_BOULDER)

        // Then
        assertNull(topProblemId)
    }

    private suspend fun givenTopoProblems(
        vararg problems: ProblemEntity,
        problemIdsWithoutLine: Set<Int> = emptySet()
    ) {
        val lines = problems.map {
            LineEntity(
                id = it.id,
                problemId = it.id,
                topoId = TOPO_ON_BOULDER,
                coordinates = if (it.id in problemIdsWithoutLine) "null" else """[{"x":0.5,"y":0.9},{"x":0.5,"y":0.1}]"""
            )
        }
        val problemIds = problems.map { it.id }

        whenever(lineRepository.loadAllByTopoIds(TOPO_ON_BOULDER)) doReturn lines
        whenever(problemRepository.loadAllByIds(problemIds)) doReturn problems.toList()
    }

    companion object {
        private const val BOULDER_ID = 42
        private const val TOPO_ON_BOULDER = 20
        private const val TOPO_WITHOUT_BOULDER = 50
        private const val UNKNOWN_TOPO = 99

        private fun topo(id: Int, boulderId: Int?) = TopoEntity(
            id = id,
            areaId = 1,
            boulderId = boulderId,
            position = null
        )

        private fun problem(id: Int, popularity: Int?, circuitId: Int? = null) = ProblemEntity(
            id = id,
            name = "Dummy",
            nameEn = "Dummy",
            nameSearchable = "dummy",
            grade = "4",
            latitude = 0f,
            longitude = 0f,
            circuitId = circuitId,
            circuitNumber = null,
            circuitColor = null,
            steepness = "wall",
            sitStart = false,
            areaId = 1,
            bleauInfoId = null,
            featured = false,
            popularity = popularity,
            parentId = null
        )
    }
}
