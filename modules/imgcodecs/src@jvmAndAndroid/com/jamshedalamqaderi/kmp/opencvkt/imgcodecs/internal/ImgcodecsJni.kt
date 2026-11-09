package com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.internal.OpenCvNativeRuntime

/**
 * JNI entry points implemented in `modules/imgcodecs/native/jni.cpp`
 * (C symbols `Java_com_jamshedalamqaderi_kmp_opencvkt_imgcodecs_internal_ImgcodecsJni_<name>`).
 * Each throws `OpenCvException` on failure. Loading this object makes sure libopencvkt is loaded.
 * Members stay public inside the internal object so their JVM names aren't mangled.
 */
internal object ImgcodecsJni {
    init {
        OpenCvNativeRuntime.ensureLoaded()
    }

    /** → `ocvkt_imgcodecs_imread`; returns a new Mat handle. */
    @JvmStatic external fun imread(filename: String, flags: Int): Long
    /** → `ocvkt_imgcodecs_imwrite`. */
    @JvmStatic external fun imwrite(filename: String, img: Long, params: IntArray): Boolean
    /** → `ocvkt_imgcodecs_imdecode`; returns a new Mat handle. */
    @JvmStatic external fun imdecode(buf: ByteArray, flags: Int): Long
    /** → `ocvkt_imgcodecs_imencode`, writing into the Mat [outBuf]. */
    @JvmStatic external fun imencode(ext: String, img: Long, params: IntArray, outBuf: Long): Boolean
}

/** JVM/Android implementation of [ImgcodecsBridge]: forwards to [ImgcodecsJni]. See [ImgcodecsBridge] for docs. */
internal actual object ImgcodecsBridge {
    actual fun imread(filename: String, flags: Int): Long = ImgcodecsJni.imread(filename, flags)
    actual fun imwrite(filename: String, img: Long, params: IntArray): Boolean = ImgcodecsJni.imwrite(filename, img, params)
    actual fun imdecode(buf: ByteArray, flags: Int): Long = ImgcodecsJni.imdecode(buf, flags)
    actual fun imencode(ext: String, img: Long, params: IntArray, outBuf: Long): Boolean =
        ImgcodecsJni.imencode(ext, img, params, outBuf)
}
