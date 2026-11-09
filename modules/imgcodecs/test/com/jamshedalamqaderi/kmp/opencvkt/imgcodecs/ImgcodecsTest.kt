package com.jamshedalamqaderi.kmp.opencvkt.imgcodecs

import com.jamshedalamqaderi.kmp.opencvkt.core.CvType
import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.Scalar
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImgcodecsTest {
    @Test
    fun pngRoundTripInMemory() {
        Mat(8, 6, CvType.CV_8UC3, Scalar(10.0, 20.0, 30.0)).use { img ->
            val png = Imgcodecs.imencode(".png", img)
            assertTrue(png.size > 8)
            assertContentEquals(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte()), png.copyOf(4))

            Imgcodecs.imdecode(png).use { decoded ->
                assertEquals(8, decoded.rows)
                assertEquals(6, decoded.cols)
                assertContentEquals(img.toByteArray(), decoded.toByteArray())
            }
        }
    }

    @Test
    fun missingFileGivesEmptyMat() {
        Imgcodecs.imread("/definitely/not/here.png").use { assertTrue(it.isEmpty) }
    }
}
