package com.jetbrains.kmpapp

import android.content.res.AssetManager


class YoloV8Ncnn {
    init {
        System.loadLibrary("yolov8ncnn")
    }

    external fun loadModel(mgr: AssetManager, modelid: Int, cpugpu: Int): Boolean
   // external fun openCamera(facing: Int): Boolean
   // external fun closeCamera(): Boolean
  //  external fun setOutputWindow(surface: Surface): Boolean
    external fun detectPose(imageData: ByteArray, width: Int, height: Int, assetManager: AssetManager): Boolean
}





