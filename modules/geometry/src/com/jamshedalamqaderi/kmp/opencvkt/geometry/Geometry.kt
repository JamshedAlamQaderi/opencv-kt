package com.jamshedalamqaderi.kmp.opencvkt.geometry

/**
 * Kotlin bindings for `opencv_geometry`.
 *
 * **Not implemented yet.** The native code for this module is already compiled into libopencvkt (see native/cmake/opencv-options.cmake),
 * but no functions are bound yet. To add bindings, follow modules/imgproc:
 *   1. declare C functions in   include/opencvkt/geometry.h
 *   2. implement them in        native/shim.cpp        (wrap every body in ocvkt::guard)
 *   3. expose them to JNI in    native/jni.cpp
 *   4. add `GeometryBridge` (common expect) + `src@jvmAndAndroid` / `src@native` actuals
 *   5. run scripts/build-native.sh <target> (it also regenerates the cinterop .def files)
 */
public object Geometry {
    /** Name of the OpenCV module these bindings cover. */
    public const val OPENCV_MODULE: String = "geometry"
}
