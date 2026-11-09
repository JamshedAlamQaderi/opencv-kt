package com.jamshedalamqaderi.kmp.opencvkt.imgproc

import com.jamshedalamqaderi.kmp.opencvkt.core.CvType
import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.Scalar
import com.jamshedalamqaderi.kmp.opencvkt.core.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImgprocTest {
    @Test
    fun bgrToGray() {
        Mat(4, 4, CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0)).use { bgr ->
            Mat().use { gray ->
                Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY)
                assertEquals(1, gray.channels)
                assertTrue(gray.toByteArray().all { it == 255.toByte() })
            }
        }
    }

    @Test
    fun resizeAndThreshold() {
        Mat(10, 20, CvType.CV_8UC1, Scalar(100.0)).use { src ->
            Mat().use { small ->
                Imgproc.resize(src, small, Size(10, 5))
                assertEquals(Size(10, 5), small.size)
                Mat().use { bin ->
                    Imgproc.threshold(small, bin, 50.0, 255.0, Imgproc.THRESH_BINARY)
                    assertTrue(bin.toByteArray().all { it == 255.toByte() })
                }
            }
        }
    }
}
