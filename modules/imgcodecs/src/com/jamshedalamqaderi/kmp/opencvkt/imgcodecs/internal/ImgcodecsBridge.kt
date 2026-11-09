package com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.internal

/**
 * Platform bridge onto `modules/imgcodecs/include/opencvkt/imgcodecs.h`. Returned Longs are new `cv::Mat*`
 * handles owned by the caller (wrap them in `Mat`). `params` are flat `[flag, value, …]` arrays.
 * Failures throw `OpenCvException`.
 *
 * Implementations: `src@jvmAndAndroid` (JNI, [ImgcodecsJni]) and `src@native` (cinterop).
 */
internal expect object ImgcodecsBridge {
    /** `ocvkt_imgcodecs_imread` → `cv::imread`; returns a new (possibly empty) Mat handle. */
    fun imread(filename: String, flags: Int): Long

    /** `ocvkt_imgcodecs_imwrite` → `cv::imwrite`; returns whether the file was written. */
    fun imwrite(filename: String, img: Long, params: IntArray): Boolean

    /** `ocvkt_imgcodecs_imdecode` → `cv::imdecode`; returns a new (possibly empty) Mat handle. */
    fun imdecode(buf: ByteArray, flags: Int): Long

    /** `ocvkt_imgcodecs_imencode` → `cv::imencode`; writes the bytes into [outBuf] as a 1 x N CV_8U Mat. */
    fun imencode(ext: String, img: Long, params: IntArray, outBuf: Long): Boolean
}
