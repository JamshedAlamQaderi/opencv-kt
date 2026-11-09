package com.jamshedalamqaderi.kmp.opencvkt.core

/**
 * Marks declarations that exist only so sibling opencv-kt modules (imgproc, imgcodecs, ...) can reach
 * native handles and the native runtime. Not part of the public API; may change without notice.
 */
@RequiresOptIn(
    message = "Internal opencv-kt API used to bridge modules. Do not use it from application code.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
public annotation class InternalOpenCvApi
