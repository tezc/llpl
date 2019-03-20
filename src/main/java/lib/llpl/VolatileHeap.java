package lib.llpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

public class VolatileHeap {
    static {
        NativeLibrary.load();
    }

    //This is approximation to overhead of libmemobj, no details are known
    //just relies on observation.
    private static final int FILE_SIZE_OVERHEAD = 10 * 1024 * 1024;

    private static final Object lock = new Object();
    private final Object closeLock = new Object();

    private final Path path;
    private final long size;
    private final long poolHandle;
    private boolean open;

    private VolatileHeap(Path path, long size, long poolHandle) {
        this.path = path;
        this.size = size;
        this.poolHandle = poolHandle;
        this.open = true;
    }

    public static VolatileHeap openHeap(String path, long size) throws IOException {
        size += FILE_SIZE_OVERHEAD;

        synchronized (lock) {
            long poolHandle = nativeOpenHeap(path, size);
            if (poolHandle == 0) {
                throw new IOException("Failed to open heap at : " + path);
            }

            VolatileHeap heap = new VolatileHeap(Paths.get(path), size, poolHandle);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                heap.close();
            }));

            return heap;
        }
    }

    public void close() {
        synchronized (closeLock) {
            if (open) {
                nativeCloseHeap(poolHandle);

                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    //ignore
                }

                open = false;
            }
        }
    }

    public long allocate(long size) {
        long offset = nativeAlloc(poolHandle, size);
        return poolHandle + offset;
    }

    public long realloc(long addr, long size) {
        long offset = nativeRealloc(poolHandle, addr, size);
        return poolHandle + offset;
    }

    public void free(long addr) {
        nativeFree(addr);
    }

    private static native long nativeOpenHeap(String path, long size);
    private static native void nativeCloseHeap(long poolHandle);

    private static native long nativeAlloc(long poolHandle, long size);
    private static native long nativeRealloc(long poolHandle, long addr, long size);
    private static native int nativeFree(long addr);
}
