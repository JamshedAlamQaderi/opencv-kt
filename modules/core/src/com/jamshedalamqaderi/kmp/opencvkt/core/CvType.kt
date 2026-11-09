package com.jamshedalamqaderi.kmp.opencvkt.core

/**
 * Mat element types, matching `opencv2/core/hal/interface.h` of OpenCV 5.x.
 *
 * A type packs a *depth* (the per-channel element type, e.g. [CV_8U]) and a *channel count* into one Int:
 * `type = depth + ((channels - 1) shl CN_SHIFT)`.
 *
 * OpenCV 5 widened the depth field ([CN_SHIFT] is 5; it was 3 in OpenCV 4.x), so the numeric value of every
 * multi-channel type differs from OpenCV 4 — e.g. `CV_8UC3` is 64 here, 16 in 4.x. Always build types with
 * [makeType] or the constants below instead of hard-coding numbers.
 */
public object CvType {
    /** Bit offset of the channel count inside a type (`CV_CN_SHIFT`). */
    public const val CN_SHIFT: Int = 5

    /** Maximum number of channels a Mat element can have (`CV_CN_MAX`). */
    public const val CN_MAX: Int = 128

    /** Number of representable depths (`CV_DEPTH_MAX`). */
    public const val DEPTH_MAX: Int = 1 shl CN_SHIFT

    /** Mask that extracts the depth from a type (`CV_MAT_DEPTH_MASK`). */
    public const val DEPTH_MASK: Int = DEPTH_MAX - 1

    /** Unsigned 8-bit integer depth (0..255). The usual depth for images. */
    public const val CV_8U: Int = 0

    /** Signed 8-bit integer depth (-128..127). */
    public const val CV_8S: Int = 1

    /** Unsigned 16-bit integer depth (0..65535). */
    public const val CV_16U: Int = 2

    /** Signed 16-bit integer depth. */
    public const val CV_16S: Int = 3

    /** Signed 32-bit integer depth. */
    public const val CV_32S: Int = 4

    /** 32-bit floating point depth. */
    public const val CV_32F: Int = 5

    /** 64-bit floating point depth. */
    public const val CV_64F: Int = 6

    /** 16-bit IEEE half-precision floating point depth. */
    public const val CV_16F: Int = 7

    /** 16-bit bfloat16 depth (new in OpenCV 5). */
    public const val CV_16BF: Int = 8

    /** Boolean depth, stored as one byte per element (new in OpenCV 5). */
    public const val CV_BOOL: Int = 9

    /** Unsigned 64-bit integer depth (new in OpenCV 5). */
    public const val CV_64U: Int = 10

    /** Signed 64-bit integer depth (new in OpenCV 5). */
    public const val CV_64S: Int = 11

    /** Unsigned 32-bit integer depth (new in OpenCV 5). */
    public const val CV_32U: Int = 12

    /**
     * Combines a [depth] (one of the `CV_8U` … `CV_32U` constants) and a [channels] count into a Mat type
     * (`CV_MAKETYPE`).
     *
     * @throws IllegalArgumentException if [channels] is not in `1..CN_MAX`.
     */
    public fun makeType(depth: Int, channels: Int): Int {
        require(channels in 1..CN_MAX) { "channels must be in 1..$CN_MAX, was $channels" }
        return (depth and DEPTH_MASK) + ((channels - 1) shl CN_SHIFT)
    }

    /** Extracts the depth (e.g. [CV_8U]) from a Mat [type] (`CV_MAT_DEPTH`). */
    public fun depth(type: Int): Int = type and DEPTH_MASK

    /** Extracts the channel count from a Mat [type] (`CV_MAT_CN`). */
    public fun channels(type: Int): Int = (type shr CN_SHIFT) + 1

    /** 8-bit unsigned, 1 channel: grayscale images and masks. */
    public val CV_8UC1: Int = makeType(CV_8U, 1)

    /** 8-bit unsigned, 2 channels. */
    public val CV_8UC2: Int = makeType(CV_8U, 2)

    /** 8-bit unsigned, 3 channels: BGR color images (OpenCV's default channel order). */
    public val CV_8UC3: Int = makeType(CV_8U, 3)

    /** 8-bit unsigned, 4 channels: BGRA images. */
    public val CV_8UC4: Int = makeType(CV_8U, 4)

    /** 16-bit unsigned, 1 channel: e.g. depth maps. */
    public val CV_16UC1: Int = makeType(CV_16U, 1)

    /** 16-bit signed, 1 channel: e.g. Sobel derivatives. */
    public val CV_16SC1: Int = makeType(CV_16S, 1)

    /** 32-bit signed integer, 1 channel: e.g. labels and integral images. */
    public val CV_32SC1: Int = makeType(CV_32S, 1)

    /** 32-bit float, 1 channel. */
    public val CV_32FC1: Int = makeType(CV_32F, 1)

    /** 32-bit float, 3 channels. */
    public val CV_32FC3: Int = makeType(CV_32F, 3)

    /** 64-bit float, 1 channel. */
    public val CV_64FC1: Int = makeType(CV_64F, 1)
}
