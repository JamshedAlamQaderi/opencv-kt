package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import android.os.Build
import com.jamshedalamqaderi.kmp.opencvkt.core.InternalOpenCvApi

/**
 * Android: `System.loadLibrary("opencvkt")` first (the app's own `jniLibs/<abi>/libopencvkt.so`, e.g. from the
 * GitHub release), then the `opencvkt-natives-android` artifact, which carries the .so files as classpath resources
 * `opencvkt/natives/android-<abi>/` (Kotlin Toolchain library AARs can't carry `jni/` folders) and is extracted to
 * the app's cache directory.
 */
@InternalOpenCvApi
public actual object OpenCvNativeRuntime {
    private val loaded: Unit by lazy {
        NativeLibraryLoader.load(
            platformIds = Build.SUPPORTED_ABIS.map { "android-$it" },
            preferSystemLibrary = true,
        )
    }

    /** Loads libopencvkt on first call; later calls return immediately. Throws `UnsatisfiedLinkError` if not found. */
    public actual fun ensureLoaded() {
        loaded
    }
}
