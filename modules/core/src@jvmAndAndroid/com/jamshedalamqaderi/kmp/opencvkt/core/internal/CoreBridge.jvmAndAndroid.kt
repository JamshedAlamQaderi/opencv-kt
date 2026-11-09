package com.jamshedalamqaderi.kmp.opencvkt.core.internal

/**
 * JVM/Android implementation of [CoreBridge]: forwards every call to [CoreJni]. The JNI layer throws
 * `OpenCvException` itself, so no status checking is needed here. See [CoreBridge] for per-function docs.
 */
internal actual object CoreBridge {
    actual fun versionString(): String = CoreJni.versionString()
    actual fun buildInformation(): String = CoreJni.buildInformation()

    actual fun matNew(): Long = CoreJni.matNew()
    actual fun matNewFilled(rows: Int, cols: Int, type: Int, s0: Double, s1: Double, s2: Double, s3: Double): Long =
        CoreJni.matNewFilled(rows, cols, type, s0, s1, s2, s3)
    actual fun matClone(handle: Long): Long = CoreJni.matClone(handle)
    actual fun matDelete(handle: Long): Unit = CoreJni.matDelete(handle)

    actual fun matRows(handle: Long): Int = CoreJni.matRows(handle)
    actual fun matCols(handle: Long): Int = CoreJni.matCols(handle)
    actual fun matType(handle: Long): Int = CoreJni.matType(handle)
    actual fun matChannels(handle: Long): Int = CoreJni.matChannels(handle)
    actual fun matDepth(handle: Long): Int = CoreJni.matDepth(handle)
    actual fun matEmpty(handle: Long): Boolean = CoreJni.matEmpty(handle)
    actual fun matTotal(handle: Long): Long = CoreJni.matTotal(handle)
    actual fun matElemSize(handle: Long): Long = CoreJni.matElemSize(handle)

    actual fun matGetBytes(handle: Long): ByteArray = CoreJni.matGetBytes(handle)
    actual fun matPutBytes(handle: Long, data: ByteArray): Unit = CoreJni.matPutBytes(handle, data)
    actual fun matSetTo(handle: Long, s0: Double, s1: Double, s2: Double, s3: Double): Unit =
        CoreJni.matSetTo(handle, s0, s1, s2, s3)
    actual fun matConvertTo(src: Long, dst: Long, rtype: Int, alpha: Double, beta: Double): Unit =
        CoreJni.matConvertTo(src, dst, rtype, alpha, beta)
}
