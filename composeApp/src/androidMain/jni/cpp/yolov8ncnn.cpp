#include <jni.h>
#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

#include "yolov8pose.h"
#include "ndkcamera.h"

#if __ARM_NEON
#include <arm_neon.h>
#endif

static Inference* g_inference = 0;
static ncnn::Mutex lock;

class MyNdkCamera : public NdkCameraWindow {
public:
    virtual void on_image_render(cv::Mat& rgb) const;
};

void MyNdkCamera::on_image_render(cv::Mat& rgb) const {
    ncnn::MutexLockGuard g(lock);

    if (g_inference) {
        std::vector<Pose> objects = g_inference->runInference(rgb);
        g_inference->draw(rgb, objects);
    }
}

static MyNdkCamera* g_camera = 0;

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
__android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
g_camera = new MyNdkCamera;
return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");
    {
        ncnn::MutexLockGuard g(lock);
        delete g_inference;
        g_inference = 0;
    }
    delete g_camera;
    g_camera = 0;
}

JNIEXPORT jboolean JNICALL Java_com_jetbrains_kmpapp_YoloV8Ncnn_loadModel(JNIEnv* env, jobject thiz, jobject assetManager, jint modelid, jint cpugpu) {
    if (modelid < 0 || modelid > 6 || cpugpu < 0 || cpugpu > 1) {
        return JNI_FALSE;
    }

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    const char* modeltypes[] = {"n", "s"};
    const int target_sizes[] = {640, 640};
    const float mean_vals[][3] = {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}};
    const float norm_vals[][3] = {{1 / 255.f, 1 / 255.f, 1 / 255.f}, {1 / 255.f, 1 / 255.f, 1 / 255.f}};

    const char* modeltype = modeltypes[(int)modelid];
    int target_size = target_sizes[(int)modelid];
    bool use_gpu = (int)cpugpu == 1;

    ncnn::MutexLockGuard g(lock);

    if (use_gpu && ncnn::get_gpu_count() == 0) {
        delete g_inference;
        g_inference = 0;
    } else {
        if (!g_inference) {
            g_inference = new Inference();
            g_inference->loadNcnnNetwork(mgr, modeltype, target_size, mean_vals[(int)modelid], norm_vals[(int)modelid], use_gpu);
        }
    }

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_jetbrains_kmpapp_YoloV8Ncnn_openCamera(JNIEnv* env, jobject thiz, jint facing) {
    if (facing < 0 || facing > 1) return JNI_FALSE;
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);
    g_camera->open((int)facing);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_jetbrains_kmpapp_YoloV8Ncnn_closeCamera(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");
    g_camera->close();
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_jetbrains_kmpapp_YoloV8Ncnn_setOutputWindow(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* win = ANativeWindow_fromSurface(env, surface);
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);
    g_camera->set_window(win);
    return JNI_TRUE;
}

}
