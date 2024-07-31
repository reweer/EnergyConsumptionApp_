package com.jetbrains.kmpapp

import androidx.compose.runtime.MutableState

interface PoseDetectionViewModel {
    val selectedSolution: MutableState<PoseDetectionSolution?>
    fun selectSolution(solution: PoseDetectionSolution)
}
