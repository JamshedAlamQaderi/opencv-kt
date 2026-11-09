package com.jamshedalamqaderi.kmp.opencvkt.photo.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.internal.OpenCvNativeRuntime

/**
 * JNI entry points implemented in `modules/photo/native/jni.cpp`
 * (C symbols `Java_com_jamshedalamqaderi_kmp_opencvkt_photo_internal_PhotoJni_<name>`).
 * Each throws `OpenCvException` on failure. Loading this object makes sure libopencvkt is loaded.
 * Members stay public inside the internal object so their JVM names aren't mangled.
 */
internal object PhotoJni {
    init {
        OpenCvNativeRuntime.ensureLoaded()
    }

    /** → `ocvkt_photo_inpaint`. */
    @JvmStatic external fun inpaint(src: Long, mask: Long, dst: Long, radius: Double, flags: Int)
    /** → `ocvkt_photo_fast_nl_means_denoising`. */
    @JvmStatic external fun fastNlMeansDenoising(src: Long, dst: Long, h: Float, templateWindow: Int, searchWindow: Int)
}

/** JVM/Android implementation of [PhotoBridge]: forwards to [PhotoJni]. See [PhotoBridge] for docs. */
internal actual object PhotoBridge {
    actual fun inpaint(src: Long, mask: Long, dst: Long, radius: Double, flags: Int): Unit =
        PhotoJni.inpaint(src, mask, dst, radius, flags)
    actual fun fastNlMeansDenoising(src: Long, dst: Long, h: Float, templateWindow: Int, searchWindow: Int): Unit =
        PhotoJni.fastNlMeansDenoising(src, dst, h, templateWindow, searchWindow)
}
