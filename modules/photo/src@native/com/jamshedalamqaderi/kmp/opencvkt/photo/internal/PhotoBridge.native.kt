package com.jamshedalamqaderi.kmp.opencvkt.photo.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.photo.cinterop.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString

/** Long Mat handle → `ocvkt_mat` (`void*`). */
private fun Long.m(): COpaquePointer? = toCPointer()

/** Throws `OpenCvException` with `ocvkt_last_error()` for a non-zero status. */
private fun check(status: Int) {
    if (status != 0) throw OpenCvException(ocvkt_last_error()?.toKString() ?: "OpenCV call failed ($status)")
}

/** Kotlin/Native implementation of [PhotoBridge] via this module's cinterop package. See [PhotoBridge] for docs. */
internal actual object PhotoBridge {
    actual fun inpaint(src: Long, mask: Long, dst: Long, radius: Double, flags: Int): Unit =
        check(ocvkt_photo_inpaint(src.m(), mask.m(), dst.m(), radius, flags))

    actual fun fastNlMeansDenoising(src: Long, dst: Long, h: Float, templateWindow: Int, searchWindow: Int): Unit =
        check(ocvkt_photo_fast_nl_means_denoising(src.m(), dst.m(), h, templateWindow, searchWindow))
}
