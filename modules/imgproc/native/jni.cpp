// JNI glue for modules/imgproc. Kotlin side: src@jvmAndAndroid/.../ImgprocJni.kt
#include "opencvkt/imgproc.h"
#include "opencvkt_jni.hpp"

#define IMGPROC_JNI(ret, name) OCVKT_JNI(com_jamshedalamqaderi_kmp_opencvkt_imgproc_internal_ImgprocJni, ret, name)

using namespace ocvkt::jni;

IMGPROC_JNI(void, cvtColor)(JNIEnv* env, jclass, jlong src, jlong dst, jint code, jint dstCn) {
    throw_if_failed(env, ocvkt_imgproc_cvt_color(to_mat(src), to_mat(dst), code, dstCn));
}

IMGPROC_JNI(void, gaussianBlur)(JNIEnv* env, jclass, jlong src, jlong dst, jint kw, jint kh, jdouble sx, jdouble sy,
                                jint border) {
    throw_if_failed(env, ocvkt_imgproc_gaussian_blur(to_mat(src), to_mat(dst), kw, kh, sx, sy, border));
}

IMGPROC_JNI(void, resize)(JNIEnv* env, jclass, jlong src, jlong dst, jint w, jint h, jdouble fx, jdouble fy,
                          jint interpolation) {
    throw_if_failed(env, ocvkt_imgproc_resize(to_mat(src), to_mat(dst), w, h, fx, fy, interpolation));
}

IMGPROC_JNI(jdouble, threshold)(JNIEnv* env, jclass, jlong src, jlong dst, jdouble thresh, jdouble maxval, jint type) {
    double computed = 0;
    throw_if_failed(env, ocvkt_imgproc_threshold(to_mat(src), to_mat(dst), thresh, maxval, type, &computed));
    return computed;
}

IMGPROC_JNI(void, canny)(JNIEnv* env, jclass, jlong image, jlong edges, jdouble t1, jdouble t2, jint aperture,
                         jboolean l2) {
    throw_if_failed(env, ocvkt_imgproc_canny(to_mat(image), to_mat(edges), t1, t2, aperture, l2 ? 1 : 0));
}
