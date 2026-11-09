package com.jamshedalamqaderi.kmp.opencvkt.core

/**
 * Raised when an OpenCV call fails (a `cv::Exception` or other C++ exception was caught by the native shim).
 *
 * The JNI glue instantiates this class by name through its `(String)` constructor
 * (see native/include/opencvkt_jni.hpp), so keep the package, name and constructor stable.
 */
public class OpenCvException(message: String) : RuntimeException(message)
