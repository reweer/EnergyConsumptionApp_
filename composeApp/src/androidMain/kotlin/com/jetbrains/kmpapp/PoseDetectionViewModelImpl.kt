package com.jetbrains.kmpapp

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class PoseDetectionViewModelImpl : ViewModel(), com.jetbrains.kmpapp.PoseDetectionViewModel {
    override val selectedSolution: MutableState<PoseDetectionSolution?> = mutableStateOf(null)

    override fun selectSolution(solution: PoseDetectionSolution) {
        selectedSolution.value = solution
    }
}
