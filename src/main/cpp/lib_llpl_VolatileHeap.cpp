/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include "lib_llpl_VolatileHeap.h"

const char *layout_name = "llpl_unmanaged_heap";

void throw_OOM(JNIEnv *env)
{
    char className[50] = "java/lang/OutOfMemoryError";
    jclass exClass = env->FindClass(className);

    char errmsg[250];
    strcpy(errmsg, pmemobj_errormsg());
    env->ThrowNew(exClass, errmsg);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_VolatileHeap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size)
{
    const char* native_string = env->GetStringUTFChars(path, 0);

    PMEMobjpool *pool = pmemobj_open(native_string, layout_name);
    if (pool == NULL) {
        pool = pmemobj_create(native_string, layout_name, (size_t) size, S_IRUSR | S_IWUSR);
    }

    if (pool != NULL) {
        pmemobj_root(pool, 16);
    }

    env->ReleaseStringUTFChars(path, native_string);

    return (long) pool;
}

JNIEXPORT void JNICALL Java_lib_llpl_VolatileHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    pmemobj_close(pool);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_VolatileHeap_nativeAlloc
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    PMEMoid bytes = OID_NULL;

    int rc = pmemobj_alloc(pool, &bytes, (size_t)size, 0, NULL, NULL);
    if (rc == -1) {
        throw_OOM(env);
    }

    return bytes.off;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_VolatileHeap_nativeRealloc
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    PMEMoid bytes = pmemobj_oid((const void*)address);

    int rc = pmemobj_realloc(pool, &bytes, (size_t)size, 0);
    if (rc == -1) {
        throw_OOM(env);
    }

    return bytes.off;
}

JNIEXPORT void JNICALL Java_lib_llpl_VolatileHeap_nativeFree
  (JNIEnv *env, jobject obj, jlong address)
{
    PMEMoid oid = pmemobj_oid((const void*)address);
    TOID(char) bytes;

    TOID_ASSIGN(bytes, oid);
    POBJ_FREE(&bytes);
}


