package com.jamshedalamqaderi.kmp.opencvkt.core

/**
 * `cv::Scalar`: up to four per-channel values, e.g. a fill color.
 *
 * Channels follow the Mat's channel order, which is **BGR(A)** for images loaded by OpenCV:
 * `Scalar(255.0, 0.0, 0.0)` is blue. Unused channels are ignored.
 *
 * @property v0 first channel (blue, or the gray value for 1-channel Mats)
 * @property v1 second channel (green)
 * @property v2 third channel (red)
 * @property v3 fourth channel (alpha)
 */
public data class Scalar(
    val v0: Double,
    val v1: Double = 0.0,
    val v2: Double = 0.0,
    val v3: Double = 0.0,
) {
    public companion object {
        /** All four channels zero (black / transparent). */
        public val ZERO: Scalar = Scalar(0.0)

        /** A scalar with every channel set to [v] (`cv::Scalar::all`). */
        public fun all(v: Double): Scalar = Scalar(v, v, v, v)
    }
}

/**
 * `cv::Size`: a width/height pair. Note the order: OpenCV sizes are **width first**, while Mat constructors take
 * **rows (height) first**.
 *
 * @property width number of columns
 * @property height number of rows
 */
public data class Size(val width: Int, val height: Int)

/**
 * `cv::Point`: integer pixel coordinates.
 *
 * @property x column (0 = left edge)
 * @property y row (0 = top edge)
 */
public data class Point(val x: Int, val y: Int)
