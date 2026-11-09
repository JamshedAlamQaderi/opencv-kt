package com.jamshedalamqaderi.kmp.opencvkt.core.internal

/**
 * JNI entry points implemented in `modules/core/native/jni.cpp`
 * (C symbols `Java_com_jamshedalamqaderi_kmp_opencvkt_core_internal_CoreJni_<name>`).
 *
 * Each function wraps the `ocvkt_*` C function of the same meaning (see [CoreBridge]) and throws
 * `OpenCvException` when it fails. Loading this object loads libopencvkt via [OpenCvNativeRuntime].
 *
 * Members are deliberately public inside an `internal` object: Kotlin mangles the JVM names of `internal`
 * members, which would break the JNI symbol lookup. Renaming a function or this class requires renaming the
 * C symbol too.
 */
internal object CoreJni {
    init {
        OpenCvNativeRuntime.ensureLoaded()
    }

    /** → `ocvkt_version_string`. */
    @JvmStatic external fun versionString(): String

    /** → `ocvkt_build_information`. */
    @JvmStatic external fun buildInformation(): String

    /** → `ocvkt_mat_new`. */
    @JvmStatic external fun matNew(): Long

    /** → `ocvkt_mat_new_filled`. */
    @JvmStatic external fun matNewFilled(rows: Int, cols: Int, type: Int, s0: Double, s1: Double, s2: Double, s3: Double): Long

    /** → `ocvkt_mat_clone`. */
    @JvmStatic external fun matClone(handle: Long): Long

    /** → `ocvkt_mat_delete`. */
    @JvmStatic external fun matDelete(handle: Long)

    /** → `ocvkt_mat_rows`. */
    @JvmStatic external fun matRows(handle: Long): Int

    /** → `ocvkt_mat_cols`. */
    @JvmStatic external fun matCols(handle: Long): Int

    /** → `ocvkt_mat_type`. */
    @JvmStatic external fun matType(handle: Long): Int

    /** → `ocvkt_mat_channels`. */
    @JvmStatic external fun matChannels(handle: Long): Int

    /** → `ocvkt_mat_depth`. */
    @JvmStatic external fun matDepth(handle: Long): Int

    /** → `ocvkt_mat_empty`. */
    @JvmStatic external fun matEmpty(handle: Long): Boolean

    /** → `ocvkt_mat_total`. */
    @JvmStatic external fun matTotal(handle: Long): Long

    /** → `ocvkt_mat_elem_size`. */
    @JvmStatic external fun matElemSize(handle: Long): Long

    /** → `ocvkt_mat_get_bytes` into a newly allocated Java byte array. */
    @JvmStatic external fun matGetBytes(handle: Long): ByteArray

    /** → `ocvkt_mat_put_bytes`. */
    @JvmStatic external fun matPutBytes(handle: Long, data: ByteArray)

    /** → `ocvkt_mat_set_to`. */
    @JvmStatic external fun matSetTo(handle: Long, s0: Double, s1: Double, s2: Double, s3: Double)

    /** → `ocvkt_mat_convert_to`. */
    @JvmStatic external fun matConvertTo(src: Long, dst: Long, rtype: Int, alpha: Double, beta: Double)
}
