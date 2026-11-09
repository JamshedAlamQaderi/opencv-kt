package com.jamshedalamqaderi.kmp.opencvkt.core

import com.jamshedalamqaderi.kmp.opencvkt.core.internal.CoreBridge

/**
 * `cv::Mat`: an n-channel 2D array (typically an image) that lives in **native memory**.
 *
 * ### Memory
 * The pixel data is not managed by the Kotlin garbage collector. Every Mat you create — directly, via [clone], or
 * returned by functions such as `Imgcodecs.imread` — must be released with [close], preferably through `use {}`:
 * ```kotlin
 * Mat(480, 640, CvType.CV_8UC3).use { img -> /* ... */ }   // freed here, even on exceptions
 * ```
 * [close] is idempotent; any other call on a closed Mat throws [IllegalStateException].
 *
 * ### Output parameters
 * OpenCV functions in this library write into a caller-provided destination Mat (e.g.
 * `Imgproc.cvtColor(src, dst, code)`). OpenCV (re)allocates the destination as needed, so an empty `Mat()` is fine,
 * and reusing the same destination across calls (e.g. per video frame) avoids reallocations.
 *
 * ### Threading
 * A Mat is not thread-safe. Don't use one instance from several threads at the same time.
 *
 * Platform plumbing: all calls go through [CoreBridge], implemented with JNI on JVM/Android and cinterop on
 * Kotlin/Native, on top of the C ABI in `modules/core/include/opencvkt/core.h`.
 */
public class Mat @InternalOpenCvApi constructor(handle: Long) : AutoCloseable {

    /** Native `cv::Mat*`, or 0 once closed. */
    private var handle: Long = handle

    /** Creates an empty Mat (0x0, no data). Useful as an output parameter. */
    public constructor() : this(CoreBridge.matNew())

    /**
     * Creates a Mat with [rows] x [cols] elements of [type] (see [CvType]), every element set to [fill].
     *
     * @throws OpenCvException if OpenCV rejects the arguments (e.g. negative sizes).
     */
    public constructor(rows: Int, cols: Int, type: Int, fill: Scalar = Scalar.ZERO) :
        this(CoreBridge.matNewFilled(rows, cols, type, fill.v0, fill.v1, fill.v2, fill.v3))

    /**
     * Raw `cv::Mat*` as a Long, for use by other opencv-kt modules when calling their native bridges.
     *
     * @throws IllegalStateException if the Mat is closed.
     */
    @InternalOpenCvApi
    public val nativeHandle: Long
        get() {
            check(handle != 0L) { "Mat is already closed" }
            return handle
        }

    /** `true` once [close] has been called. */
    public val isClosed: Boolean get() = handle == 0L

    /** Number of rows (the image height). */
    public val rows: Int get() = CoreBridge.matRows(nativeHandle)

    /** Number of columns (the image width). */
    public val cols: Int get() = CoreBridge.matCols(nativeHandle)

    /** Element type: depth and channel count packed together (see [CvType]). */
    public val type: Int get() = CoreBridge.matType(nativeHandle)

    /** Number of channels per element (e.g. 3 for BGR images). */
    public val channels: Int get() = CoreBridge.matChannels(nativeHandle)

    /** Per-channel element depth, e.g. [CvType.CV_8U]. */
    public val depth: Int get() = CoreBridge.matDepth(nativeHandle)

    /** `true` if the Mat has no elements (e.g. a failed `imread`). */
    public val isEmpty: Boolean get() = CoreBridge.matEmpty(nativeHandle)

    /** Number of elements (`rows * cols`); each element has [channels] values. */
    public val total: Long get() = CoreBridge.matTotal(nativeHandle)

    /** Size of one element in bytes (all channels), e.g. 3 for [CvType.CV_8UC3]. */
    public val elemSize: Long get() = CoreBridge.matElemSize(nativeHandle)

    /** Width x height of the Mat. Note the order: `Size(cols, rows)`. */
    public val size: Size get() = Size(cols, rows)

    /**
     * Deep copy: a new Mat with its own copy of the data. The caller owns (and must [close]) the result.
     */
    public fun clone(): Mat = Mat(CoreBridge.matClone(nativeHandle))

    /**
     * Copies the pixel data out into a new array of `total * elemSize` bytes, row by row with no padding,
     * channels interleaved (e.g. `B G R B G R …`). Works for non-continuous Mats (e.g. ROIs) too.
     *
     * @throws IllegalArgumentException if the data is larger than a ByteArray can hold (Kotlin/Native).
     */
    public fun toByteArray(): ByteArray = CoreBridge.matGetBytes(nativeHandle)

    /**
     * Overwrites the pixel data from [data], laid out as described in [toByteArray].
     *
     * @throws OpenCvException if `data.size` is not exactly `total * elemSize`.
     */
    public fun put(data: ByteArray) {
        CoreBridge.matPutBytes(nativeHandle, data)
    }

    /** Sets every element to [value] (`cv::Mat::setTo`). */
    public fun setTo(value: Scalar) {
        CoreBridge.matSetTo(nativeHandle, value.v0, value.v1, value.v2, value.v3)
    }

    /**
     * Converts to another depth with optional scaling, writing into [dst]: `dst = saturate(src * alpha + beta)`
     * (`cv::Mat::convertTo`). The channel count is kept.
     *
     * @param rtype target type or depth, e.g. [CvType.CV_32F]; a negative value keeps the current depth.
     * @param alpha scale factor.
     * @param beta value added after scaling.
     */
    public fun convertTo(dst: Mat, rtype: Int, alpha: Double = 1.0, beta: Double = 0.0) {
        CoreBridge.matConvertTo(nativeHandle, dst.nativeHandle, rtype, alpha, beta)
    }

    /** Releases the native memory. Safe to call more than once. */
    override fun close() {
        val h = handle
        if (h != 0L) {
            handle = 0L
            CoreBridge.matDelete(h)
        }
    }

    /** Human-readable summary, e.g. `Mat[480x640, type=64, channels=3]`; never touches pixel data. */
    override fun toString(): String =
        if (isClosed) "Mat[closed]" else "Mat[${rows}x$cols, type=$type, channels=$channels]"
}
