package com.jamshedalamqaderi.kmp.opencvkt.core

import com.jamshedalamqaderi.kmp.opencvkt.core.internal.CoreBridge

/** Information about the OpenCV build bundled in libopencvkt. Accessing it loads the native library if needed. */
public object OpenCv {
    /** Version of the OpenCV sources libopencvkt was compiled from (the `opencv/` submodule), e.g. `"5.1.0"`. */
    public val version: String get() = CoreBridge.versionString()

    /**
     * Output of `cv::getBuildInformation()`: compiler, enabled modules, codecs, video backends, CPU optimizations.
     * Handy for checking which features a given binary actually supports.
     */
    public val buildInformation: String get() = CoreBridge.buildInformation()
}
