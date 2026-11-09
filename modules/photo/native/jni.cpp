// JNI glue for modules/photo. Kotlin side: src@jvmAndAndroid/.../PhotoJni.kt
#include "opencvkt/photo.h"
#include "opencvkt_jni.hpp"

#define PHOTO_JNI(ret, name) OCVKT_JNI(com_jamshedalamqaderi_kmp_opencvkt_photo_internal_PhotoJni, ret, name)

using namespace ocvkt::jni;

PHOTO_JNI(void, inpaint)(JNIEnv* env, jclass, jlong src, jlong mask, jlong dst, jdouble radius, jint flags) {
    throw_if_failed(env, ocvkt_photo_inpaint(to_mat(src), to_mat(mask), to_mat(dst), radius, flags));
}

PHOTO_JNI(void, fastNlMeansDenoising)(JNIEnv* env, jclass, jlong src, jlong dst, jfloat h, jint tw, jint sw) {
    throw_if_failed(env, ocvkt_photo_fast_nl_means_denoising(to_mat(src), to_mat(dst), h, tw, sw));
}
