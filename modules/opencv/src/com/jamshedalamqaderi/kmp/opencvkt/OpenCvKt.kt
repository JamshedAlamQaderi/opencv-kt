package com.jamshedalamqaderi.kmp.opencvkt

/**
 * Marker for the umbrella `opencvkt` artifact. Depending on it pulls in every opencv-kt module as an `api`
 * dependency, so all of their packages (`com.jamshedalamqaderi.kmp.opencvkt.core`, `….imgproc`, …) are available.
 * The umbrella itself contains no API.
 */
public object OpenCvKt {
    /** Comma-separated names of the OpenCV modules included in the umbrella artifact. */
    public const val MODULES: String =
        "core,flann,geometry,imgproc,imgcodecs,videoio,highgui,features,stereo,objdetect,calib,dnn,photo,video,ptcloud,stitching"
}
