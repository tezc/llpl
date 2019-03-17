/* 
 * Copyright (C) 2018 Intel Corporation
 *
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 */

package examples.string_store;

import lib.llpl.AnyHeap;
import lib.llpl.Heap;
import lib.llpl.MemoryBlock;
import lib.llpl.VolatileHeap;

import java.io.Console;
import java.io.IOException;

public class Writer2 {
    public static void main(String[] args) throws IOException {

        VolatileHeap heap = new VolatileHeap("/mnt/mem/volatile_pool", 10000000L );

        long l1 = heap.allocate(2148000000L);
        AnyHeap.UNSAFE.putLong(l1, 500);
        System.out.println(AnyHeap.UNSAFE.getLong(l1));

        long l2 = heap.allocate(1000);
        System.out.println(AnyHeap.UNSAFE.getLong(l2));
        AnyHeap.UNSAFE.putLong(l2, 555);
        System.out.println(AnyHeap.UNSAFE.getLong(l2));

        long l3 = heap.realloc(l1, 2000);
        AnyHeap.UNSAFE.putLong(l3, 500);
        System.out.println(AnyHeap.UNSAFE.getLong(l3));

        heap.free(l3);
        AnyHeap.UNSAFE.putLong(l3, 5);
        AnyHeap.UNSAFE.putLong(l3+5, 5);
        AnyHeap.UNSAFE.putLong(l3+111, 5);
        AnyHeap.UNSAFE.putLong(l3+222, 5);
        AnyHeap.UNSAFE.putLong(l3+211, 5);


    }
}
