package com.jamshedalamqaderi.kmp.opencvkt.photo

import com.jamshedalamqaderi.kmp.opencvkt.core.CvType
import com.jamshedalamqaderi.kmp.opencvkt.core.Mat
import com.jamshedalamqaderi.kmp.opencvkt.core.Scalar
import kotlin.test.Test
import kotlin.test.assertEquals

class PhotoTest {
    @Test
    fun inpaintFillsMaskedPixel() {
        Mat(9, 9, CvType.CV_8UC1, Scalar(200.0)).use { img ->
            val px = img.toByteArray().also { it[4 * 9 + 4] = 0 }
            img.put(px)
            Mat(9, 9, CvType.CV_8UC1).use { mask ->
                mask.put(ByteArray(81).also { it[4 * 9 + 4] = 1 })
                Mat().use { out ->
                    Photo.inpaint(img, mask, out, 3.0)
                    assertEquals(200, out.toByteArray()[4 * 9 + 4].toInt() and 0xFF)
                }
            }
        }
    }
}
