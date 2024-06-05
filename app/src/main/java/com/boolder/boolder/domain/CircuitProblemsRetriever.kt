package com.boolder.boolder.domain

import androidx.annotation.VisibleForTesting
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.ProblemRepository

class CircuitProblemsRetriever(
    private val problemRepository: ProblemRepository
) {

    suspend fun getCircuitStartProblemId(circuitId: Int): Int? =
        problemRepository.problemIdByCircuitAndNumber(
            circuitId = circuitId,
            circuitProblemNumber = "D"
        ) ?: problemRepository.problemIdByCircuitAndNumber(
            circuitId = circuitId,
            circuitProblemNumber = "1"
        )

    suspend fun getCircuitPreviousAndNextProblemIds(currentProblem: ProblemEntity): AdjacentCircuitProblemIds {
        val circuitId = currentProblem.circuitId
            ?: return AdjacentCircuitProblemIds(null, null)

        val adjacentCircuitNumbers = getPreviousAndNextCircuitNumbers(currentProblem.circuitNumber)

        val previousProblemId = adjacentCircuitNumbers.previous?.let {
            problemRepository.problemIdByCircuitAndNumber(
                circuitId = circuitId,
                circuitProblemNumber = it
            )
        }

        val nextProblemId = adjacentCircuitNumbers.next?.let {
            problemRepository.problemIdByCircuitAndNumber(
                circuitId = circuitId,
                circuitProblemNumber = it
            )
        }

        return AdjacentCircuitProblemIds(
            previous = previousProblemId,
            next = nextProblemId
        )
    }

    private fun getPreviousAndNextCircuitNumbers(circuitNumber: String?): AdjacentCircuitNumbers =
        when (circuitNumber) {
            null -> AdjacentCircuitNumbers(null, null)
            "D" -> AdjacentCircuitNumbers(null, "1")
            else -> {
                try {
                    val intCircuitNumber = circuitNumber.toInt()
                    val nextNumber = (intCircuitNumber + 1).toString()

                    if (intCircuitNumber == 1) {
                        AdjacentCircuitNumbers(
                            previous = "D",
                            next = nextNumber
                        )
                    } else {
                        AdjacentCircuitNumbers(
                            previous = (intCircuitNumber - 1).toString(),
                            next = nextNumber
                        )
                    }
                } catch (e: Exception) {
                    AdjacentCircuitNumbers(null, null)
                }
            }
        }

    @VisibleForTesting
    data class AdjacentCircuitNumbers(
        val previous: String?,
        val next: String?
    )

    data class AdjacentCircuitProblemIds(
        val previous: Int?,
        val next: Int?
    )
}
