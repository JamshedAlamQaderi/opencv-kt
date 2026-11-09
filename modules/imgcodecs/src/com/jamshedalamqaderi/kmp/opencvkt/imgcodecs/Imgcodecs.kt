package com.jamshedalamqaderi.kmp.opencvkt.imgcodecs

import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.internal.ImgcodecsBridge

/**
 * Reading and writing images from files and memory (`opencv2/imgcodecs.hpp`).
 *
 * Codecs bundled in libopencvkt (built from OpenCV's own 3rd-party sources, no system libraries needed):
 * PNG, JPEG, WebP, TIFF, JPEG 2000 (OpenJPEG), BMP, PNM/PPM/PGM, Sun Raster, PFM, HDR (Radiance).
 * Check `OpenCv.buildInformation` for the exact list of a given binary.
 *
 * Color images are loaded in **BGR** channel order.
 */
public object Imgcodecs {
    // ---------------------------------------------------------------- ImreadModes

    /** Load as-is, including alpha and 16-bit / float depths. */
    public const val IMREAD_UNCHANGED: Int = -1

    /** Load as 8-bit single-channel grayscale. */
    public const val IMREAD_GRAYSCALE: Int = 0

    /** Load as 8-bit 3-channel BGR (alpha dropped). The default. */
    public const val IMREAD_COLOR: Int = 1

    /** Keep 16-bit / 32-bit depth if the file has it; otherwise convert to 8-bit. Combine with other flags. */
    public const val IMREAD_ANYDEPTH: Int = 2

    /** Keep the file's channel layout (gray stays gray, color stays color). */
    public const val IMREAD_ANYCOLOR: Int = 4

    // ---------------------------------------------------------------- ImwriteFlags (subset)

    /** JPEG quality, 0..100 (higher is better, default 95). */
    public const val IMWRITE_JPEG_QUALITY: Int = 1

    /** PNG compression level, 0..9 (higher is smaller and slower). */
    public const val IMWRITE_PNG_COMPRESSION: Int = 16

    /** WebP quality, 1..100; values above 100 select lossless compression. */
    public const val IMWRITE_WEBP_QUALITY: Int = 64

    // ---------------------------------------------------------------- functions

    /**
     * Loads an image from a file (`cv::imread`). The codec is chosen from the file's content, not its extension.
     *
     * @param filename path to the file.
     * @param flags one of the `IMREAD_*` modes.
     * @return a new Mat owned by the caller. It is **empty** ([Mat.isEmpty]) if the file is missing, unreadable or
     *   in an unsupported format; OpenCV doesn't throw in that case.
     */
    public fun imread(filename: String, flags: Int = IMREAD_COLOR): Mat =
        Mat(ImgcodecsBridge.imread(filename, flags))

    /**
     * Saves [img] to a file (`cv::imwrite`). The format comes from the file extension (".png", ".jpg", …).
     * Supported inputs are 8-bit (and for some formats 16-bit or float) images with 1, 3 or 4 channels in BGR(A).
     *
     * @param params `(flag, value)` pairs such as `Imgcodecs.IMWRITE_JPEG_QUALITY to 90`.
     * @return `true` if the file was written; `false` if OpenCV couldn't write it (e.g. the directory is missing).
     * @throws OpenCvException for unsupported extensions or image types.
     */
    public fun imwrite(filename: String, img: Mat, vararg params: Pair<Int, Int>): Boolean =
        ImgcodecsBridge.imwrite(filename, img.nativeHandle, params.flatten())

    /**
     * Decodes an image from encoded bytes, e.g. a downloaded PNG/JPEG (`cv::imdecode`).
     *
     * @param buf the encoded file content.
     * @param flags one of the `IMREAD_*` modes.
     * @return a new Mat owned by the caller; empty if [buf] can't be decoded.
     */
    public fun imdecode(buf: ByteArray, flags: Int = IMREAD_COLOR): Mat =
        Mat(ImgcodecsBridge.imdecode(buf, flags))

    /**
     * Encodes [img] into an in-memory file (`cv::imencode`), e.g. to upload or cache it.
     *
     * @param ext codec selector including the dot: ".png", ".jpg", ".webp", ".bmp", ".tiff", ….
     * @param params `(flag, value)` pairs, as in [imwrite].
     * @return the encoded bytes.
     * @throws OpenCvException if the codec is unknown or rejects the image.
     * @throws IllegalStateException if the encoder reports failure without an exception.
     */
    public fun imencode(ext: String, img: Mat, vararg params: Pair<Int, Int>): ByteArray =
        Mat().use { out ->
            check(ImgcodecsBridge.imencode(ext, img.nativeHandle, params.flatten(), out.nativeHandle)) {
                "imencode('$ext') failed"
            }
            out.toByteArray()
        }

    /** Flattens `(flag, value)` pairs into OpenCV's `[flag, value, flag, value, …]` parameter vector. */
    private fun Array<out Pair<Int, Int>>.flatten(): IntArray =
        IntArray(size * 2) { i -> if (i % 2 == 0) this[i / 2].first else this[i / 2].second }
}
