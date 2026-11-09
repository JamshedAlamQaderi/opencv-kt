// Internal helpers shared by every module's JNI glue (JVM desktop + Android only).
#pragma once

#include <jni.h>
#include <cstdint>

#include "opencvkt/prelude.h"

// Fully-qualified Kotlin class every JNI exception is mapped to (see modules/core/src/OpenCvException.kt).
#define OCVKT_EXCEPTION_CLASS "com/jamshedalamqaderi/kmp/opencvkt/core/OpenCvException"

// Declares a JNI entry point for `external fun` declared in a Kotlin `object` with @JvmStatic.
// Usage: OCVKT_JNI(com_jamshedalamqaderi_kmp_opencvkt_core_internal_CoreJni, jlong, matNew)(JNIEnv* env, jclass)
#define OCVKT_JNI(klass, ret, name) extern "C" JNIEXPORT ret JNICALL Java_##klass##_##name

namespace ocvkt::jni {

// Throws OpenCvException with ocvkt_last_error() if status != 0. Returns true when an exception is pending.
bool throw_if_failed(JNIEnv* env, ocvkt_status status);

// Throws OpenCvException with ocvkt_last_error() if handle == NULL.
bool throw_if_null(JNIEnv* env, const void* handle);

inline ocvkt_mat to_mat(jlong handle) { return reinterpret_cast<ocvkt_mat>(static_cast<intptr_t>(handle)); }
inline jlong from_ptr(const void* ptr) { return static_cast<jlong>(reinterpret_cast<intptr_t>(ptr)); }

// RAII view over a Java String as modified UTF-8.
class Utf {
public:
    Utf(JNIEnv* env, jstring s) : env_(env), s_(s), chars_(s ? env->GetStringUTFChars(s, nullptr) : nullptr) {}
    ~Utf() { if (chars_) env_->ReleaseStringUTFChars(s_, chars_); }
    Utf(const Utf&) = delete;
    Utf& operator=(const Utf&) = delete;
    const char* get() const { return chars_; }

private:
    JNIEnv* env_;
    jstring s_;
    const char* chars_;
};

}  // namespace ocvkt::jni
