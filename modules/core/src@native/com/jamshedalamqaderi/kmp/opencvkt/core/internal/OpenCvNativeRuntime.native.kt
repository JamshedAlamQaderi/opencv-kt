package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import com.jamshedalamqaderi.kmp.opencvkt.core.InternalOpenCvApi

/** Kotlin/Native links libopencvkt.a statically (embedded in core's cinterop klib): nothing to load at runtime. */
@InternalOpenCvApi
public actual object OpenCvNativeRuntime {
    /** No-op on Kotlin/Native. */
    public actual fun ensureLoaded() {}
}
