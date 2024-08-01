package com.jetbrains.kmpapp

import androidx.compose.runtime.Composable


@Composable
fun MainScreen(viewModel: PoseDetectionViewModel) {
    val solution = viewModel.selectedSolution.value
    if (solution == null) {
        StartScreen(viewModel)
    } else {
        when (solution) {
            PoseDetectionSolution.MLKit -> CameraView()
            PoseDetectionSolution.MediaPipe -> CameraView()
        }
    }
}

