cmake_minimum_required(VERSION 3.10)
project(yolov8ncnn)


set(CMAKE_CXX_STANDARD 11)

set(ncnn_DIR "${CMAKE_SOURCE_DIR}/jni/ncnn-20240410-android-vulkan")
set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/jni/opencv-mobile-2.4.13.7-android")

# katalogi do ścieżek
include_directories(${OpenCV_DIR}/include)
include_directories(${ncnn_DIR}/include)

find_package(OpenCV REQUIRED core imgproc)
find_package(ncnn REQUIRED)

# dodanie biblioteki C++
add_library(yolov8ncnn SHARED yolov8_pose.cpp yolo.cpp ndkcamera.cpp yolov8pose.cpp)

# laczenie bibliotek NCNN i OpenCV z biblioteką yolov8
target_link_libraries(yolov8ncnn ncnn ${OpenCV_LIBS} camera2ndk mediandk log)
