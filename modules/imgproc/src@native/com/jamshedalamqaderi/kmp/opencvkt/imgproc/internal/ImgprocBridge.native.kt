package com.jamshedalamqaderi.kmp.opencvkt.imgproc.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.imgproc.cinterop.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value

/** Long Mat handle → `ocvkt_mat` (`void*`). */
private fun Long.m(): COpaquePointer? = toCPointer()

/** Throws `OpenCvException` with `ocvkt_last_error()` for a non-zero status. */
private fun check(status: Int) {
    if (status != 0) throw OpenCvException(ocvkt_last_error()?.toKString() ?: "OpenCV call failed ($status)")
}

/**
 * Kotlin/Native implementation of [ImgprocBridge]: calls the `ocvkt_imgproc_*` functions through this module's
 * cinterop package and converts statuses into exceptions. See [ImgprocBridge] for docs.
 */
internal actual object ImgprocBridge {
    actual fun cvtColor(src: Long, dst: Long, code: Int, dstCn: Int): Unit =
        check(ocvkt_imgproc_cvt_color(src.m(), dst.m(), code, dstCn))

    actual fun gaussianBlur(src: Long, dst: Long, kw: Int, kh: Int, sigmaX: Double, sigmaY: Double, borderType: Int): Unit =
        check(ocvkt_imgproc_gaussian_blur(src.m(), dst.m(), kw, kh, sigmaX, sigmaY, borderType))

    actual fun resize(src: Long, dst: Long, width: Int, height: Int, fx: Double, fy: Double, interpolation: Int): Unit =
        check(ocvkt_imgproc_resize(src.m(), dst.m(), width, height, fx, fy, interpolation))

    actual fun threshold(src: Long, dst: Long, thresh: Double, maxval: Double, type: Int): Double = memScoped {
        val out = alloc<DoubleVar>()
        check(ocvkt_imgproc_threshold(src.m(), dst.m(), thresh, maxval, type, out.ptr))
        out.value
    }

    actual fun canny(image: Long, edges: Long, threshold1: Double, threshold2: Double, apertureSize: Int, l2Gradient: Boolean): Unit =
        check(ocvkt_imgproc_canny(image.m(), edges.m(), threshold1, threshold2, apertureSize, if (l2Gradient) 1 else 0))
}
