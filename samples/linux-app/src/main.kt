package com.jamshedalamqaderi.kmp.opencvkt.samples

import com.jamshedalamqaderi.kmp.opencvkt.core.CvType
import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.OpenCv
import com.jamshedalamqaderi.kmp.opencvkt.core.Scalar
import com.jamshedalamqaderi.kmp.opencvkt.core.Size
import com.jamshedalamqaderi.kmp.opencvkt.imgcodecs.Imgcodecs
import com.jamshedalamqaderi.kmp.opencvkt.imgproc.Imgproc

/**
 * opencv-kt sample: loads an image (or generates one), converts it to grayscale, blurs it, detects edges with Canny
 * and writes `gray.png` / `edges.png` to the working directory.
 *
 * Usage: `./kotlin run -m <sample> [-- path/to/image]`
 */
fun main(args: Array<String>) {
    println("OpenCV ${OpenCv.version}")

    val input = args.firstOrNull()
    val src = if (input != null) Imgcodecs.imread(input) else syntheticImage()
    require(!src.isEmpty) { "could not read $input" }

    src.use {
        Mat().use { gray ->
            Mat().use { blurred ->
                Mat().use { edges ->
                    Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
                    Imgproc.gaussianBlur(gray, blurred, Size(5, 5), 1.5)
                    Imgproc.canny(blurred, edges, 50.0, 150.0)
                    check(Imgcodecs.imwrite("gray.png", gray))
                    check(Imgcodecs.imwrite("edges.png", edges))
                    println("${src.cols}x${src.rows} -> wrote gray.png and edges.png (${Imgcodecs.imencode(".png", edges).size} bytes)")
                }
            }
        }
    }
}

/** 200x200 BGR image with a white square, so the sample runs without an input file. */
private fun syntheticImage(): Mat {
    val m = Mat(200, 200, CvType.CV_8UC3, Scalar(40.0, 40.0, 40.0))
    val bytes = m.toByteArray()
    for (y in 50 until 150) for (x in 50 until 150) for (c in 0 until 3) bytes[(y * 200 + x) * 3 + c] = 0xFF.toByte()
    m.put(bytes)
    return m
}
