/*
 * Shared definitions for every opencv-kt C header.
 *
 * Every module header (modules/<m>/include/opencvkt/<m>.h) includes it as "opencvkt/prelude.h".
 * native/include is on the include path of the CMake build and of every generated cinterop .def.
 */
#ifndef OPENCVKT_PRELUDE_H
#define OPENCVKT_PRELUDE_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
#  define OCVKT_BEGIN_DECLS extern "C" {
#  define OCVKT_END_DECLS }
#else
#  define OCVKT_BEGIN_DECLS
#  define OCVKT_END_DECLS
#endif

#if defined(_WIN32) && defined(OCVKT_BUILDING_SHARED)
#  define OCVKT_API __declspec(dllexport)
#elif defined(__GNUC__) || defined(__clang__)
#  define OCVKT_API __attribute__((visibility("default")))
#else
#  define OCVKT_API
#endif

/** Opaque handle to a heap-allocated cv::Mat. Represented as Long on the Kotlin side. */
typedef void* ocvkt_mat;

/** 0 = success; non-zero = a C++ exception was caught (see ocvkt_last_error()). */
typedef int32_t ocvkt_status;

OCVKT_BEGIN_DECLS

/**
 * Message of the last error raised on the calling thread by any opencv-kt function, or NULL.
 * Valid until the next opencv-kt call on that thread. Implemented in modules/core/native/shim.cpp.
 * Declared here so every module's cinterop package can read it without depending on core's.
 */
OCVKT_API const char* ocvkt_last_error(void);

OCVKT_END_DECLS

#endif /* OPENCVKT_PRELUDE_H */
