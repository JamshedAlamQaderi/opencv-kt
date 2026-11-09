/*
 * Minimal Windows jni_md.h for cross-compiling the JNI library (mingwX64) from a non-Windows host,
 * whose JDK only ships the jni_md.h of its own OS. Matches the ABI of the JDK's win32/jni_md.h.
 */
#ifndef OPENCVKT_JNI_MD_WIN32_H
#define OPENCVKT_JNI_MD_WIN32_H

#ifndef JNIEXPORT
#define JNIEXPORT __declspec(dllexport)
#endif
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall

typedef int jint;
typedef long long jlong;
typedef signed char jbyte;

#endif
