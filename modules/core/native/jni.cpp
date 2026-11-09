// JNI glue for modules/core (JVM + Android). Mirrors core.h 1:1; Kotlin side: src@jvmAndAndroid/.../CoreJni.kt
#include "opencvkt/core.h"
#include "opencvkt_jni.hpp"

#define CORE_JNI(ret, name) OCVKT_JNI(com_jamshedalamqaderi_kmp_opencvkt_core_internal_CoreJni, ret, name)

using namespace ocvkt::jni;

namespace ocvkt::jni {

static bool throw_last_error(JNIEnv* env) {
    const char* msg = ocvkt_last_error();
    jclass cls = env->FindClass(OCVKT_EXCEPTION_CLASS);
    if (cls == nullptr) return true;  // NoClassDefFoundError already pending
    env->ThrowNew(cls, msg ? msg : "OpenCV call failed");
    env->DeleteLocalRef(cls);
    return true;
}

bool throw_if_failed(JNIEnv* env, ocvkt_status status) { return status != 0 && throw_last_error(env); }

bool throw_if_null(JNIEnv* env, const void* handle) { return handle == nullptr && throw_last_error(env); }

}  // namespace ocvkt::jni

CORE_JNI(jstring, versionString)(JNIEnv* env, jclass) { return env->NewStringUTF(ocvkt_version_string()); }

CORE_JNI(jstring, buildInformation)(JNIEnv* env, jclass) { return env->NewStringUTF(ocvkt_build_information()); }

CORE_JNI(jlong, matNew)(JNIEnv*, jclass) { return from_ptr(ocvkt_mat_new()); }

CORE_JNI(jlong, matNewFilled)(JNIEnv* env, jclass, jint rows, jint cols, jint type,
                              jdouble s0, jdouble s1, jdouble s2, jdouble s3) {
    ocvkt_mat m = ocvkt_mat_new_filled(rows, cols, type, s0, s1, s2, s3);
    throw_if_null(env, m);
    return from_ptr(m);
}

CORE_JNI(jlong, matClone)(JNIEnv* env, jclass, jlong h) {
    ocvkt_mat m = ocvkt_mat_clone(to_mat(h));
    throw_if_null(env, m);
    return from_ptr(m);
}

CORE_JNI(void, matDelete)(JNIEnv*, jclass, jlong h) { ocvkt_mat_delete(to_mat(h)); }

CORE_JNI(jint, matRows)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_rows(to_mat(h)); }
CORE_JNI(jint, matCols)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_cols(to_mat(h)); }
CORE_JNI(jint, matType)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_type(to_mat(h)); }
CORE_JNI(jint, matChannels)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_channels(to_mat(h)); }
CORE_JNI(jint, matDepth)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_depth(to_mat(h)); }
CORE_JNI(jboolean, matEmpty)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_empty(to_mat(h)) ? JNI_TRUE : JNI_FALSE; }
CORE_JNI(jlong, matTotal)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_total(to_mat(h)); }
CORE_JNI(jlong, matElemSize)(JNIEnv*, jclass, jlong h) { return ocvkt_mat_elem_size(to_mat(h)); }

CORE_JNI(jbyteArray, matGetBytes)(JNIEnv* env, jclass, jlong h) {
    ocvkt_mat m = to_mat(h);
    const jlong size = ocvkt_mat_total(m) * ocvkt_mat_elem_size(m);
    jbyteArray out = env->NewByteArray(static_cast<jsize>(size));
    if (out == nullptr || size == 0) return out;
    void* dst = env->GetPrimitiveArrayCritical(out, nullptr);
    ocvkt_status st = ocvkt_mat_get_bytes(m, static_cast<uint8_t*>(dst), size);
    env->ReleasePrimitiveArrayCritical(out, dst, 0);
    throw_if_failed(env, st);
    return out;
}

CORE_JNI(void, matPutBytes)(JNIEnv* env, jclass, jlong h, jbyteArray data) {
    const jsize len = env->GetArrayLength(data);
    void* src = env->GetPrimitiveArrayCritical(data, nullptr);
    ocvkt_status st = ocvkt_mat_put_bytes(to_mat(h), static_cast<const uint8_t*>(src), len);
    env->ReleasePrimitiveArrayCritical(data, src, JNI_ABORT);
    throw_if_failed(env, st);
}

CORE_JNI(void, matSetTo)(JNIEnv* env, jclass, jlong h, jdouble s0, jdouble s1, jdouble s2, jdouble s3) {
    throw_if_failed(env, ocvkt_mat_set_to(to_mat(h), s0, s1, s2, s3));
}

CORE_JNI(void, matConvertTo)(JNIEnv* env, jclass, jlong src, jlong dst, jint rtype, jdouble alpha, jdouble beta) {
    throw_if_failed(env, ocvkt_mat_convert_to(to_mat(src), to_mat(dst), rtype, alpha, beta));
}
