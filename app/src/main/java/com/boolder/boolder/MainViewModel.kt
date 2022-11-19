package com.boolder.boolder

import androidx.lifecycle.ViewModel
import com.boolder.boolder.database.entity.Problem
import com.boolder.boolder.database.repository.AreaRepository
import com.boolder.boolder.database.repository.LineRepository
import com.boolder.boolder.database.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class DataType {
    AREA, LINE, PROBLEM
}

class MainViewModel(
    private val areaRepository: AreaRepository,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository
) : ViewModel() {

    fun getAll(type: DataType) {}

    fun getProblemById(problemId: Int): Flow<List<Problem>> {
        return flow {
            val result = problemRepository.loadAllByIds(listOf(problemId))
            emit(result)
        }
    }
}