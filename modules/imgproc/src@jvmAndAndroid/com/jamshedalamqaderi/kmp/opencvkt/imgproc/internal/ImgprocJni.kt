package com.jamshedalamqaderi.kmp.opencvkt.imgproc.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.internal.OpenCvNativeRuntime

/**
 * JNI entry points implemented in `modules/imgproc/native/jni.cpp`
 * (C symbols `Java_com_jamshedalamqaderi_kmp_opencvkt_imgproc_internal_ImgprocJni_<name>`).
 * Each throws `OpenCvException` on failure. Loading this object makes sure libopencvkt is loaded.
 * Members stay public inside the internal object so their JVM names aren't mangled.
 */
internal object ImgprocJni {
    init {
        OpenCvNativeRuntime.ensureLoaded()
    }

    /** → `ocvkt_imgproc_cvt_color`. */
    @JvmStatic external fun cvtColor(src: Long, dst: Long, code: Int, dstCn: Int)
    /** → `ocvkt_imgproc_gaussian_blur`. */
    @JvmStatic external fun gaussianBlur(src: Long, dst: Long, kw: Int, kh: Int, sigmaX: Double, sigmaY: Double, borderType: Int)
    /** → `ocvkt_imgproc_resize`. */
    @JvmStatic external fun resize(src: Long, dst: Long, width: Int, height: Int, fx: Double, fy: Double, interpolation: Int)
    /** → `ocvkt_imgproc_threshold`; returns the threshold used. */
    @JvmStatic external fun threshold(src: Long, dst: Long, thresh: Double, maxval: Double, type: Int): Double
    /** → `ocvkt_imgproc_canny`. */
    @JvmStatic external fun canny(image: Long, edges: Long, threshold1: Double, threshold2: Double, apertureSize: Int, l2Gradient: Boolean)
}

/** JVM/Android implementation of [ImgprocBridge]: forwards to [ImgprocJni]. See [ImgprocBridge] for docs. */
internal actual object ImgprocBridge {
    actual fun cvtColor(src: Long, dst: Long, code: Int, dstCn: Int): Unit = ImgprocJni.cvtColor(src, dst, code, dstCn)
    actual fun gaussianBlur(src: Long, dst: Long, kw: Int, kh: Int, sigmaX: Double, sigmaY: Double, borderType: Int): Unit =
        ImgprocJni.gaussianBlur(src, dst, kw, kh, sigmaX, sigmaY, borderType)
    actual fun resize(src: Long, dst: Long, width: Int, height: Int, fx: Double, fy: Double, interpolation: Int): Unit =
        ImgprocJni.resize(src, dst, width, height, fx, fy, interpolation)
    actual fun threshold(src: Long, dst: Long, thresh: Double, maxval: Double, type: Int): Double =
        ImgprocJni.threshold(src, dst, thresh, maxval, type)
    actual fun canny(image: Long, edges: Long, threshold1: Double, threshold2: Double, apertureSize: Int, l2Gradient: Boolean): Unit =
        ImgprocJni.canny(image, edges, threshold1, threshold2, apertureSize, l2Gradient)
}
