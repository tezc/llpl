/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include <libpmemobj.h>
#include <libpmem.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>

#ifndef _Included_lib_llpl_NonVolatileHeap
#define _Included_lib_llpl_NonVolatileHeap
#ifdef __cplusplus
extern "C" {
#endif

#define CHAR_TYPE_OFFSET 1017
TOID_DECLARE(char, CHAR_TYPE_OFFSET);

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeCreateHeap
  (JNIEnv *env, jobject, jstring, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeOpenHeap
  (JNIEnv*, jobject, jstring);

JNIEXPORT void JNICALL Java_lib_llpl_NonVolatileHeap_nativeCloseHeap
  (JNIEnv*, jobject, jlong);

JNIEXPORT jint JNICALL Java_lib_llpl_NonVolatileHeap_nativeSetRoot
  (JNIEnv *env, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeGetRoot
  (JNIEnv *env, jobject, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeAlloc
  (JNIEnv*, jobject, jlong, jlong);

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeRealloc
  (JNIEnv*, jobject, jlong, jlong, jlong);

JNIEXPORT void JNICALL Java_lib_llpl_NonVolatileHeap_nativeFree
  (JNIEnv*, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
