package com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.cinterop.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value

/** Long Mat handle → `ocvkt_mat` (`void*`). */
private fun Long.m(): COpaquePointer? = toCPointer()

/** Builds an `OpenCvException` from `ocvkt_last_error()`, or [fallback] if no message was recorded. */
private fun lastError(fallback: String) = OpenCvException(ocvkt_last_error()?.toKString() ?: fallback)

/** Throws `OpenCvException` for a non-zero status. */
private fun check(status: Int) {
    if (status != 0) throw lastError("OpenCV call failed ($status)")
}

/** Converts a returned `ocvkt_mat` into a Long handle; NULL means the C side caught an exception. */
private fun COpaquePointer?.handle(): Long = this?.toLong() ?: throw lastError("OpenCV returned a null Mat")

/**
 * Kotlin/Native implementation of [ImgcodecsBridge] via this module's cinterop package. Kotlin Strings are passed
 * as UTF-8 C strings by cinterop; ByteArrays/IntArrays are pinned for the call. See [ImgcodecsBridge] for docs.
 */
internal actual object ImgcodecsBridge {
    actual fun imread(filename: String, flags: Int): Long = ocvkt_imgcodecs_imread(filename, flags).handle()

    actual fun imwrite(filename: String, img: Long, params: IntArray): Boolean = memScoped {
        val ok = alloc<IntVar>()
        withParams(params) { p, n -> check(ocvkt_imgcodecs_imwrite(filename, img.m(), p, n, ok.ptr)) }
        ok.value != 0
    }

    actual fun imdecode(buf: ByteArray, flags: Int): Long {
        if (buf.isEmpty()) return ocvkt_imgcodecs_imdecode(null, 0, flags).handle()
        return buf.usePinned { ocvkt_imgcodecs_imdecode(it.addressOf(0).reinterpret(), buf.size.toLong(), flags) }.handle()
    }

    actual fun imencode(ext: String, img: Long, params: IntArray, outBuf: Long): Boolean = memScoped {
        val ok = alloc<IntVar>()
        withParams(params) { p, n -> check(ocvkt_imgcodecs_imencode(ext, img.m(), p, n, outBuf.m(), ok.ptr)) }
        ok.value != 0
    }

    /** Calls [block] with a pinned pointer to [params] (or NULL and 0 when empty). */
    private inline fun withParams(params: IntArray, block: (kotlinx.cinterop.CPointer<IntVar>?, Int) -> Unit) {
        if (params.isEmpty()) block(null, 0) else params.usePinned { block(it.addressOf(0), params.size) }
    }
}
