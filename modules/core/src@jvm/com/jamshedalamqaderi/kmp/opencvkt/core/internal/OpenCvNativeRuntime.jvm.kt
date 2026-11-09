package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.InternalOpenCvApi

/**
 * Desktop JVM: loads libopencvkt once, using [NativeLibraryLoader] with the current OS/architecture
 * (e.g. `linux-x64`). See [NativeLibraryLoader] for the lookup order.
 */
@InternalOpenCvApi
public actual object OpenCvNativeRuntime {
    private val loaded: Unit by lazy {
        NativeLibraryLoader.load(listOf(NativeLibraryLoader.desktopPlatformId()))
    }

    /** Loads libopencvkt on first call; later calls return immediately. Throws `UnsatisfiedLinkError` if not found. */
    public actual fun ensureLoaded() {
        loaded
    }
}
