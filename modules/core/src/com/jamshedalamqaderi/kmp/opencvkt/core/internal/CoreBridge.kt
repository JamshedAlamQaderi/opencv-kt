package com.jamshedalamqaderi.kmp.opencvkt.core.internal

/**
 * Platform bridge onto the C ABI in `modules/core/include/opencvkt/core.h`. Each function maps 1:1 to the
 * `ocvkt_*` C function named in its doc.
 *
 * Contract shared by all implementations:
 * - Mat handles are `cv::Mat*` carried as Long; 0 is never returned for a valid Mat.
 * - Failures surface as [com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException] carrying OpenCV's message.
 *
 * Implementations:
 * - `src@jvmAndAndroid`: JNI ([CoreJni], C side in `modules/core/native/jni.cpp`)
 * - `src@native`: cinterop (generated `.def`, see `scripts/generate-cinterop-defs.sh`)
 */
internal expect object CoreBridge {
    /** `ocvkt_version_string`: OpenCV version, e.g. "5.1.0". */
    fun versionString(): String

    /** `ocvkt_build_information`: `cv::getBuildInformation()`. */
    fun buildInformation(): String

    /** `ocvkt_mat_new`: allocates an empty Mat. */
    fun matNew(): Long

    /** `ocvkt_mat_new_filled`: allocates a [rows] x [cols] Mat of [type] filled with (s0, s1, s2, s3). */
    fun matNewFilled(rows: Int, cols: Int, type: Int, s0: Double, s1: Double, s2: Double, s3: Double): Long

    /** `ocvkt_mat_clone`: deep copy of [handle]; returns a new handle. */
    fun matClone(handle: Long): Long

    /** `ocvkt_mat_delete`: frees [handle]. Must be called exactly once per handle. */
    fun matDelete(handle: Long)

    /** `ocvkt_mat_rows`. */
    fun matRows(handle: Long): Int

    /** `ocvkt_mat_cols`. */
    fun matCols(handle: Long): Int

    /** `ocvkt_mat_type`. */
    fun matType(handle: Long): Int

    /** `ocvkt_mat_channels`. */
    fun matChannels(handle: Long): Int

    /** `ocvkt_mat_depth`. */
    fun matDepth(handle: Long): Int

    /** `ocvkt_mat_empty`. */
    fun matEmpty(handle: Long): Boolean

    /** `ocvkt_mat_total`: number of elements. */
    fun matTotal(handle: Long): Long

    /** `ocvkt_mat_elem_size`: bytes per element (all channels). */
    fun matElemSize(handle: Long): Long

    /** `ocvkt_mat_get_bytes`: copies all pixel data out, row by row without padding. */
    fun matGetBytes(handle: Long): ByteArray

    /** `ocvkt_mat_put_bytes`: overwrites all pixel data; [data] must match the Mat's byte size exactly. */
    fun matPutBytes(handle: Long, data: ByteArray)

    /** `ocvkt_mat_set_to`: sets every element to (s0, s1, s2, s3). */
    fun matSetTo(handle: Long, s0: Double, s1: Double, s2: Double, s3: Double)

    /** `ocvkt_mat_convert_to`: `dst = saturate(src * alpha + beta)` converted to [rtype]. */
    fun matConvertTo(src: Long, dst: Long, rtype: Int, alpha: Double, beta: Double)
}
