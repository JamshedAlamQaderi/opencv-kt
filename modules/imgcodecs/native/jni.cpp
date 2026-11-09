// JNI glue for modules/imgcodecs. Kotlin side: src@jvmAndAndroid/.../ImgcodecsJni.kt
#include <vector>

#include "opencvkt/imgcodecs.h"
#include "opencvkt_jni.hpp"

#define IMGCODECS_JNI(ret, name) \
    OCVKT_JNI(com_jamshedalamqaderi_kmp_opencvkt_imgcodecs_internal_ImgcodecsJni, ret, name)

using namespace ocvkt::jni;

namespace {
std::vector<int32_t> read_params(JNIEnv* env, jintArray params) {
    std::vector<int32_t> out;
    if (params == nullptr) return out;
    out.resize(env->GetArrayLength(params));
    if (!out.empty()) env->GetIntArrayRegion(params, 0, static_cast<jsize>(out.size()), out.data());
    return out;
}
}  // namespace

IMGCODECS_JNI(jlong, imread)(JNIEnv* env, jclass, jstring filename, jint flags) {
    Utf path(env, filename);
    ocvkt_mat m = ocvkt_imgcodecs_imread(path.get(), flags);
    throw_if_null(env, m);
    return from_ptr(m);
}

IMGCODECS_JNI(jboolean, imwrite)(JNIEnv* env, jclass, jstring filename, jlong img, jintArray params) {
    Utf path(env, filename);
    auto p = read_params(env, params);
    int32_t ok = 0;
    throw_if_failed(env, ocvkt_imgcodecs_imwrite(path.get(), to_mat(img), p.data(), static_cast<int32_t>(p.size()), &ok));
    return ok ? JNI_TRUE : JNI_FALSE;
}

IMGCODECS_JNI(jlong, imdecode)(JNIEnv* env, jclass, jbyteArray buf, jint flags) {
    const jsize len = env->GetArrayLength(buf);
    void* data = env->GetPrimitiveArrayCritical(buf, nullptr);
    ocvkt_mat m = ocvkt_imgcodecs_imdecode(static_cast<const uint8_t*>(data), len, flags);
    env->ReleasePrimitiveArrayCritical(buf, data, JNI_ABORT);
    throw_if_null(env, m);
    return from_ptr(m);
}

IMGCODECS_JNI(jboolean, imencode)(JNIEnv* env, jclass, jstring ext, jlong img, jintArray params, jlong outBuf) {
    Utf e(env, ext);
    auto p = read_params(env, params);
    int32_t ok = 0;
    throw_if_failed(env, ocvkt_imgcodecs_imencode(e.get(), to_mat(img), p.data(), static_cast<int32_t>(p.size()),
                                                  to_mat(outBuf), &ok));
    return ok ? JNI_TRUE : JNI_FALSE;
}
