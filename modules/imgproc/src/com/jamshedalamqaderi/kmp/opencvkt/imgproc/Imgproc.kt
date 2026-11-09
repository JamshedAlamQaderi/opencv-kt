package com.jamshedalamqaderi.kmp.opencvkt.imgproc

import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.core.Size
import com.jamshedalamqaderi.kmp.opencvkt.imgproc.internal.ImgprocBridge

/**
 * Image processing functions from `opencv2/imgproc.hpp`.
 *
 * Every function writes into a caller-provided `dst` Mat. OpenCV (re)allocates it to the right size and type, so
 * an empty `Mat()` works; reusing the same `dst` across calls (e.g. per video frame) avoids reallocation.
 * Unless stated otherwise, `src` and `dst` may be the same Mat (in-place).
 *
 * All functions throw [OpenCvException] when OpenCV rejects the input (wrong depth, channel count, sizes, …).
 */
public object Imgproc {
    // ---------------------------------------------------------------- ColorConversionCodes (subset)

    /** BGR → BGRA (adds an opaque alpha channel). */
    public const val COLOR_BGR2BGRA: Int = 0

    /** BGRA → BGR (drops alpha). */
    public const val COLOR_BGRA2BGR: Int = 1

    /** RGBA → BGR (drops alpha and swaps red/blue). */
    public const val COLOR_RGBA2BGR: Int = 3

    /** BGR ↔ RGB (swaps red and blue). Also valid as RGB2BGR. */
    public const val COLOR_BGR2RGB: Int = 4

    /** BGR → 1-channel grayscale. Use this for images from `Imgcodecs.imread`. */
    public const val COLOR_BGR2GRAY: Int = 6

    /** RGB → 1-channel grayscale. */
    public const val COLOR_RGB2GRAY: Int = 7

    /** 1-channel grayscale → BGR (gray value copied to all three channels). */
    public const val COLOR_GRAY2BGR: Int = 8

    /** BGR → HSV. For 8-bit images hue is in 0..180 (degrees / 2), saturation and value in 0..255. */
    public const val COLOR_BGR2HSV: Int = 40

    /** HSV → BGR (inverse of [COLOR_BGR2HSV]). */
    public const val COLOR_HSV2BGR: Int = 54

    // ---------------------------------------------------------------- InterpolationFlags

    /** Nearest-neighbour: fastest, blocky. Keeps exact values (good for label/mask images). */
    public const val INTER_NEAREST: Int = 0

    /** Bilinear: the default; good general-purpose quality. */
    public const val INTER_LINEAR: Int = 1

    /** Bicubic: smoother when enlarging, slower. */
    public const val INTER_CUBIC: Int = 2

    /** Pixel-area resampling: best choice for shrinking (avoids moiré). */
    public const val INTER_AREA: Int = 3

    /** Lanczos over an 8x8 neighbourhood: highest quality, slowest. */
    public const val INTER_LANCZOS4: Int = 4

    // ---------------------------------------------------------------- ThresholdTypes

    /** `dst = src > thresh ? maxval : 0`. */
    public const val THRESH_BINARY: Int = 0

    /** `dst = src > thresh ? 0 : maxval`. */
    public const val THRESH_BINARY_INV: Int = 1

    /** `dst = src > thresh ? thresh : src`. */
    public const val THRESH_TRUNC: Int = 2

    /** `dst = src > thresh ? src : 0`. */
    public const val THRESH_TOZERO: Int = 3

    /** `dst = src > thresh ? 0 : src`. */
    public const val THRESH_TOZERO_INV: Int = 4

    /**
     * Flag (combine with one of the types above using `or`): pick the threshold automatically with Otsu's method.
     * 8-bit single-channel input only; the chosen value is returned by [threshold].
     */
    public const val THRESH_OTSU: Int = 8

    /** Flag like [THRESH_OTSU], using the triangle algorithm (good for histograms with one dominant peak). */
    public const val THRESH_TRIANGLE: Int = 16

    // ---------------------------------------------------------------- BorderTypes

    /** Pads with a constant value (0): `000000|abcdefgh|000000`. */
    public const val BORDER_CONSTANT: Int = 0

    /** Repeats the edge pixel: `aaaaaa|abcdefgh|hhhhhhh`. */
    public const val BORDER_REPLICATE: Int = 1

    /** Mirrors including the edge pixel: `fedcba|abcdefgh|hgfedcb`. */
    public const val BORDER_REFLECT: Int = 2

    /** Mirrors excluding the edge pixel: `gfedcb|abcdefgh|gfedcba`. */
    public const val BORDER_REFLECT_101: Int = 4

    /** OpenCV's default border mode, same as [BORDER_REFLECT_101]. */
    public const val BORDER_DEFAULT: Int = BORDER_REFLECT_101

    // ---------------------------------------------------------------- functions

    /**
     * Converts [src] from one color space to another (`cv::cvtColor`).
     *
     * @param src input image (8-bit, 16-bit or 32-bit float, depending on [code]).
     * @param dst output image; same depth as [src], channel count given by [code] (or [dstCn]).
     * @param code conversion code, e.g. [COLOR_BGR2GRAY].
     * @param dstCn number of output channels; 0 derives it from [code].
     */
    public fun cvtColor(src: Mat, dst: Mat, code: Int, dstCn: Int = 0) {
        ImgprocBridge.cvtColor(src.nativeHandle, dst.nativeHandle, code, dstCn)
    }

    /**
     * Blurs [src] with a Gaussian kernel (`cv::GaussianBlur`). Commonly used to reduce noise before edge detection.
     *
     * @param ksize kernel size; width and height must be positive and odd, or zero to compute them from the sigmas.
     * @param sigmaX standard deviation in X; 0 computes it from [ksize].
     * @param sigmaY standard deviation in Y; 0 means "same as [sigmaX]".
     * @param borderType how pixels outside the image are extrapolated, e.g. [BORDER_DEFAULT].
     */
    public fun gaussianBlur(
        src: Mat,
        dst: Mat,
        ksize: Size,
        sigmaX: Double,
        sigmaY: Double = 0.0,
        borderType: Int = BORDER_DEFAULT,
    ) {
        ImgprocBridge.gaussianBlur(src.nativeHandle, dst.nativeHandle, ksize.width, ksize.height, sigmaX, sigmaY, borderType)
    }

    /**
     * Resizes [src] (`cv::resize`). Pass either an explicit [dsize], or `Size(0, 0)` plus scale factors [fx]/[fy].
     * [src] and [dst] must be different Mats.
     *
     * @param dsize output size (width, height); `Size(0, 0)` means "compute from [fx] and [fy]".
     * @param fx horizontal scale factor, used when [dsize] is `Size(0, 0)`.
     * @param fy vertical scale factor, used when [dsize] is `Size(0, 0)`.
     * @param interpolation e.g. [INTER_AREA] for shrinking, [INTER_LINEAR] or [INTER_CUBIC] for enlarging.
     */
    public fun resize(
        src: Mat,
        dst: Mat,
        dsize: Size,
        fx: Double = 0.0,
        fy: Double = 0.0,
        interpolation: Int = INTER_LINEAR,
    ) {
        ImgprocBridge.resize(src.nativeHandle, dst.nativeHandle, dsize.width, dsize.height, fx, fy, interpolation)
    }

    /**
     * Applies a fixed-level threshold to each element (`cv::threshold`).
     *
     * @param thresh threshold value (ignored when [type] includes [THRESH_OTSU] or [THRESH_TRIANGLE]).
     * @param maxval value written for "on" pixels with [THRESH_BINARY] / [THRESH_BINARY_INV].
     * @param type one of the `THRESH_*` types, optionally `or`-ed with [THRESH_OTSU] / [THRESH_TRIANGLE].
     * @return the threshold actually used: [thresh], or the automatically computed value with Otsu / Triangle.
     */
    public fun threshold(src: Mat, dst: Mat, thresh: Double, maxval: Double, type: Int): Double =
        ImgprocBridge.threshold(src.nativeHandle, dst.nativeHandle, thresh, maxval, type)

    /**
     * Finds edges with the Canny algorithm (`cv::Canny`). [edges] becomes an 8-bit single-channel image where edge
     * pixels are 255.
     *
     * @param image 8-bit input image (usually grayscale, ideally blurred first).
     * @param threshold1 lower hysteresis threshold.
     * @param threshold2 upper hysteresis threshold (a 1:2 or 1:3 ratio to [threshold1] is typical).
     * @param apertureSize Sobel kernel size: 3, 5 or 7.
     * @param l2Gradient use the more accurate L2 norm for gradient magnitude instead of L1.
     */
    public fun canny(
        image: Mat,
        edges: Mat,
        threshold1: Double,
        threshold2: Double,
        apertureSize: Int = 3,
        l2Gradient: Boolean = false,
    ) {
        ImgprocBridge.canny(image.nativeHandle, edges.nativeHandle, threshold1, threshold2, apertureSize, l2Gradient)
    }
}
