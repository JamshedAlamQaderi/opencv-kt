package com.jamshedalamqaderi.kmp.opencvkt.photo.internal

/**
 * Platform bridge onto `modules/photo/include/opencvkt/photo.h`. Mat arguments are `cv::Mat*` handles.
 * Failures throw `OpenCvException`. Implementations: `src@jvmAndAndroid` (JNI, [PhotoJni]) and `src@native` (cinterop).
 */
internal expect object PhotoBridge {
    /** `ocvkt_photo_inpaint` → `cv::inpaint`. */
    fun inpaint(src: Long, mask: Long, dst: Long, radius: Double, flags: Int)
    /** `ocvkt_photo_fast_nl_means_denoising` → `cv::fastNlMeansDenoising`. */
    fun fastNlMeansDenoising(src: Long, dst: Long, h: Float, templateWindow: Int, searchWindow: Int)
}
