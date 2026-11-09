package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.core.cinterop.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned

/** Converts a Long Mat handle back into the `void*` (`ocvkt_mat`) the C ABI expects. */
internal fun Long.asMat(): COpaquePointer? = toCPointer()

/**
 * Converts a `ocvkt_mat` returned by C into a Long handle.
 *
 * @throws OpenCvException with `ocvkt_last_error()` if the C function returned NULL (it caught an exception).
 */
internal fun COpaquePointer?.asHandle(): Long =
    this?.toLong() ?: throw OpenCvException(ocvkt_last_error()?.toKString() ?: "OpenCV returned a null Mat")

/**
 * Throws [OpenCvException] with `ocvkt_last_error()` if a C function returned a non-zero `ocvkt_status`.
 */
internal fun checkStatus(status: Int) {
    if (status != 0) throw OpenCvException(ocvkt_last_error()?.toKString() ?: "OpenCV call failed ($status)")
}

/**
 * Kotlin/Native implementation of [CoreBridge]: calls the `ocvkt_*` C functions directly through cinterop.
 * The C code is the static libopencvkt.a embedded in this module's cinterop klib. ByteArrays are pinned for the
 * duration of a copy, so no extra buffer is allocated. See [CoreBridge] for per-function docs.
 */
internal actual object CoreBridge {
    actual fun versionString(): String = ocvkt_version_string()!!.toKString()
    actual fun buildInformation(): String = ocvkt_build_information()!!.toKString()

    actual fun matNew(): Long = ocvkt_mat_new().asHandle()
    actual fun matNewFilled(rows: Int, cols: Int, type: Int, s0: Double, s1: Double, s2: Double, s3: Double): Long =
        ocvkt_mat_new_filled(rows, cols, type, s0, s1, s2, s3).asHandle()
    actual fun matClone(handle: Long): Long = ocvkt_mat_clone(handle.asMat()).asHandle()
    actual fun matDelete(handle: Long): Unit = ocvkt_mat_delete(handle.asMat())

    actual fun matRows(handle: Long): Int = ocvkt_mat_rows(handle.asMat())
    actual fun matCols(handle: Long): Int = ocvkt_mat_cols(handle.asMat())
    actual fun matType(handle: Long): Int = ocvkt_mat_type(handle.asMat())
    actual fun matChannels(handle: Long): Int = ocvkt_mat_channels(handle.asMat())
    actual fun matDepth(handle: Long): Int = ocvkt_mat_depth(handle.asMat())
    actual fun matEmpty(handle: Long): Boolean = ocvkt_mat_empty(handle.asMat()) != 0
    actual fun matTotal(handle: Long): Long = ocvkt_mat_total(handle.asMat())
    actual fun matElemSize(handle: Long): Long = ocvkt_mat_elem_size(handle.asMat())

    actual fun matGetBytes(handle: Long): ByteArray {
        val m = handle.asMat()
        val size = ocvkt_mat_total(m) * ocvkt_mat_elem_size(m)
        require(size <= Int.MAX_VALUE) { "Mat too large for a ByteArray: $size bytes" }
        val out = ByteArray(size.toInt())
        if (out.isEmpty()) return out
        out.usePinned { checkStatus(ocvkt_mat_get_bytes(m, it.addressOf(0).reinterpret(), size)) }
        return out
    }

    actual fun matPutBytes(handle: Long, data: ByteArray) {
        if (data.isEmpty()) {
            checkStatus(ocvkt_mat_put_bytes(handle.asMat(), null, 0))
            return
        }
        data.usePinned { checkStatus(ocvkt_mat_put_bytes(handle.asMat(), it.addressOf(0).reinterpret(), data.size.toLong())) }
    }

    actual fun matSetTo(handle: Long, s0: Double, s1: Double, s2: Double, s3: Double): Unit =
        checkStatus(ocvkt_mat_set_to(handle.asMat(), s0, s1, s2, s3))

    actual fun matConvertTo(src: Long, dst: Long, rtype: Int, alpha: Double, beta: Double): Unit =
        checkStatus(ocvkt_mat_convert_to(src.asMat(), dst.asMat(), rtype, alpha, beta))
}
