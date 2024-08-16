package com.jetbrains.kmpapp
//for yolo


data class PoseResult(val keypoints: List<KeyPoint>, val score: Float)

data class KeyPoint(val x: Float, val y: Float, val confidence: Float)
