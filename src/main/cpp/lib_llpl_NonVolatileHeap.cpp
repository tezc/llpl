/*
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 */

#include "lib_llpl_NonVolatileHeap.h"

const char *nonvolatile_layout_name = "llpl_nonvolatile_heap";

void native_throw_OOM(JNIEnv *env)
{
    char className[50] = "java/lang/OutOfMemoryError";
    jclass exClass = env->FindClass(className);

    char errmsg[250];
    strcpy(errmsg, pmemobj_errormsg());
    env->ThrowNew(exClass, errmsg);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeCreateHeap
  (JNIEnv *env, jobject obj, jstring path, jlong size)
{
    const char* native_string = env->GetStringUTFChars(path, 0);

    PMEMobjpool *pool = pmemobj_open(native_string, nonvolatile_layout_name);
    if (pool != NULL) {
        pmemobj_close(pool);
        return 0;
    }

    pool = pmemobj_create(native_string, nonvolatile_layout_name, (size_t) size, S_IRUSR | S_IWUSR);

    env->ReleaseStringUTFChars(path, native_string);

    return (long) pool;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeOpenHeap
  (JNIEnv *env, jobject obj, jstring path)
{
    const char* native_string = env->GetStringUTFChars(path, 0);

    PMEMobjpool *pool = pmemobj_open(native_string, nonvolatile_layout_name);

    env->ReleaseStringUTFChars(path, native_string);

    return (long) pool;
}

JNIEXPORT jint JNICALL Java_lib_llpl_NonVolatileHeap_nativeSetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong val)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    int ret = 0;
    TX_BEGIN(pool) {
        long *root_address = (long *)pmemobj_direct(pmemobj_root(pool, 8));
        pmemobj_tx_add_range_direct((const void *)root_address, 8);
        *root_address = val;
    } TX_ONABORT {
        ret = -1;
    } TX_END

    return ret;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeGetRoot
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    return (jlong)*(long *)pmemobj_direct(pmemobj_root(pool, 8));
}

JNIEXPORT void JNICALL Java_lib_llpl_NonVolatileHeap_nativeCloseHeap
  (JNIEnv *env, jobject obj, jlong poolHandle)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    pmemobj_close(pool);
}

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeAlloc
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong size)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    PMEMoid bytes = OID_NULL;

    int rc = pmemobj_alloc(pool, &bytes, (size_t)size, 0, NULL, NULL);
    if (rc == -1) {
        native_throw_OOM(env);
    }

    return bytes.off;
}

JNIEXPORT jlong JNICALL Java_lib_llpl_NonVolatileHeap_nativeRealloc
  (JNIEnv *env, jobject obj, jlong poolHandle, jlong address, jlong size)
{
    PMEMobjpool *pool = (PMEMobjpool*)poolHandle;
    PMEMoid bytes = pmemobj_oid((const void*)address);

    int rc = pmemobj_realloc(pool, &bytes, (size_t)size, 0);
    if (rc == -1) {
        native_throw_OOM(env);
    }

    return bytes.off;
}

JNIEXPORT void JNICALL Java_lib_llpl_NonVolatileHeap_nativeFree
  (JNIEnv *env, jobject obj, jlong address)
{
    PMEMoid oid = pmemobj_oid((const void*)address);
    TOID(char) bytes;

    TOID_ASSIGN(bytes, oid);
    POBJ_FREE(&bytes);
}


