package com.jamshedalamqaderi.kmp.opencvkt.imgproc.internal

/**
 * Platform bridge onto `modules/imgproc/include/opencvkt/imgproc.h`. Mat arguments are `cv::Mat*` handles
 * (see `Mat.nativeHandle`). Failures throw `OpenCvException`.
 *
 * Implementations: `src@jvmAndAndroid` (JNI, [ImgprocJni]) and `src@native` (cinterop).
 */
internal expect object ImgprocBridge {
    /** `ocvkt_imgproc_cvt_color` → `cv::cvtColor`. */
    fun cvtColor(src: Long, dst: Long, code: Int, dstCn: Int)

    /** `ocvkt_imgproc_gaussian_blur` → `cv::GaussianBlur` with kernel size ([kw], [kh]). */
    fun gaussianBlur(src: Long, dst: Long, kw: Int, kh: Int, sigmaX: Double, sigmaY: Double, borderType: Int)

    /** `ocvkt_imgproc_resize` → `cv::resize` to ([width], [height]) or by ([fx], [fy]). */
    fun resize(src: Long, dst: Long, width: Int, height: Int, fx: Double, fy: Double, interpolation: Int)

    /** `ocvkt_imgproc_threshold` → `cv::threshold`; returns the threshold actually used. */
    fun threshold(src: Long, dst: Long, thresh: Double, maxval: Double, type: Int): Double

    /** `ocvkt_imgproc_canny` → `cv::Canny`. */
    fun canny(image: Long, edges: Long, threshold1: Double, threshold2: Double, apertureSize: Int, l2Gradient: Boolean)
}
