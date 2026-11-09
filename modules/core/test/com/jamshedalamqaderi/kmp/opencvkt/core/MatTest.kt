package com.jamshedalamqaderi.kmp.opencvkt.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MatTest {
    @Test
    fun cvTypeMatchesOpenCv5Encoding() {
        // OpenCV 5: CV_CN_SHIFT = 5
        assertEquals(64, CvType.CV_8UC3)
        assertEquals(3, CvType.channels(CvType.CV_8UC3))
        assertEquals(CvType.CV_8U, CvType.depth(CvType.CV_8UC3))
    }

    @Test
    fun versionComesFromSubmodule() {
        assertTrue(OpenCv.version.startsWith("5."), "unexpected version ${OpenCv.version}")
    }

    @Test
    fun createFillAndReadBack() {
        Mat(2, 3, CvType.CV_8UC1, Scalar(7.0)).use { m ->
            assertEquals(2, m.rows)
            assertEquals(3, m.cols)
            assertEquals(1, m.channels)
            assertContentEquals(ByteArray(6) { 7 }, m.toByteArray())

            m.put(ByteArray(6) { it.toByte() })
            m.clone().use { c -> assertContentEquals(ByteArray(6) { it.toByte() }, c.toByteArray()) }
        }
    }

    @Test
    fun nativeErrorsBecomeOpenCvException() {
        Mat(2, 2, CvType.CV_8UC1).use { m ->
            assertFailsWith<OpenCvException> { m.put(ByteArray(3)) }
        }
    }
}
