package com.boolder.boolder.view.ticklist

import com.boolder.boolder.data.userdatabase.entity.TickStatus

interface TickedProblemSaver {
    fun onSaveProblem(problemId: Int, tickStatus: TickStatus)
    fun onUnsaveProblem(problemId: Int)
}
