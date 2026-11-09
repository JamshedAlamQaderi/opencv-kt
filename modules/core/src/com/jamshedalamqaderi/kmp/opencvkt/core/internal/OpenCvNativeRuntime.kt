package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.InternalOpenCvApi

/**
 * Makes sure libopencvkt is loaded before any native call.
 *
 * - JVM: resolves the shared library (system property / env var / classpath resource / java.library.path),
 *   see `src@jvm/.../JvmNativeLoader.kt`.
 * - Android: `System.loadLibrary("opencvkt")` from the APK's jniLibs.
 * - Kotlin/Native: no-op, the static library is linked into the binary through cinterop.
 *
 * Every module's JNI object calls [ensureLoaded] from its initializer.
 */
@InternalOpenCvApi
public expect object OpenCvNativeRuntime {
    /**
     * Loads libopencvkt if it isn't loaded yet; cheap to call repeatedly.
     *
     * @throws UnsatisfiedLinkError on JVM/Android when no matching library can be found.
     */
    public fun ensureLoaded()
}
