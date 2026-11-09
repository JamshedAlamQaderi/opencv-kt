package com.jamshedalamqaderi.kmp.opencvkt.photo

import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCvException
import com.jamshedalamqaderi.kmp.opencvkt.photo.internal.PhotoBridge

/**
 * Computational photography from `opencv2/photo.hpp`: inpainting (removing scratches/objects) and denoising.
 *
 * Functions write into a caller-provided `dst` Mat (allocated by OpenCV as needed) and throw [OpenCvException]
 * when OpenCV rejects the input.
 */
public object Photo {
    /** Inpainting method based on Navier-Stokes fluid dynamics. */
    public const val INPAINT_NS: Int = 0

    /** Inpainting method by Alexandru Telea (fast marching). The usual default. */
    public const val INPAINT_TELEA: Int = 1

    /**
     * Reconstructs the region of [src] marked in [mask] from the surrounding pixels (`cv::inpaint`).
     *
     * @param src 8-bit 1- or 3-channel image (or 16-bit / float single-channel).
     * @param mask 8-bit single-channel Mat of the same size; non-zero pixels mark the area to restore.
     * @param dst output image, same size and type as [src].
     * @param inpaintRadius neighbourhood radius in pixels considered for each restored pixel (3 is typical).
     * @param flags [INPAINT_TELEA] or [INPAINT_NS].
     */
    public fun inpaint(src: Mat, mask: Mat, dst: Mat, inpaintRadius: Double, flags: Int = INPAINT_TELEA) {
        PhotoBridge.inpaint(src.nativeHandle, mask.nativeHandle, dst.nativeHandle, inpaintRadius, flags)
    }

    /**
     * Removes Gaussian-like noise with the Non-local Means algorithm (`cv::fastNlMeansDenoising`).
     * Works on grayscale images (for color images, OpenCV's `fastNlMeansDenoisingColored` is better; not bound yet).
     *
     * @param src 8-bit 1-, 2- or 3-channel image.
     * @param h filter strength: higher removes more noise but also more detail (3 is OpenCV's default).
     * @param templateWindowSize patch size used to compare pixels; odd, 7 recommended.
     * @param searchWindowSize area searched for similar patches; odd, 21 recommended. Larger is slower.
     */
    public fun fastNlMeansDenoising(
        src: Mat,
        dst: Mat,
        h: Float = 3f,
        templateWindowSize: Int = 7,
        searchWindowSize: Int = 21,
    ) {
        PhotoBridge.fastNlMeansDenoising(src.nativeHandle, dst.nativeHandle, h, templateWindowSize, searchWindowSize)
    }
}
