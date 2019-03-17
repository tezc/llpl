package lib.llpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VolatileHeap {
    static {
        NativeLibrary.load();
    }

    private static final Object lock = new Object();

    private final String path;
    private final long size;
    private final long poolHandle;

    public VolatileHeap(String path, long size) throws IOException {
        this.path = path;
        this.size = size;
        this.poolHandle = openHeap(path, size);
    }

    private static long openHeap(String path, long size) throws IOException {
        Files.deleteIfExists(Paths.get(path));

        synchronized (lock) {
            long poolHandle = nativeOpenHeap(path, size);
            if (poolHandle == 0) {
                throw new IOException("Failed to open heap at : " + path);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(Paths.get(path));
                }
                catch (IOException e) {
                    //ignore
                }
            }));

            return poolHandle;
        }
    }

    public void close() {
        if (poolHandle != 0) {
            nativeCloseHeap(poolHandle);

            try {
                Files.deleteIfExists(Paths.get(path));
            }
            catch (IOException e) {
                //ignore
            }
        }
    }

    public long allocate(long size) {
        long offset = nativeAlloc(poolHandle, size);
        if (offset == 0) {
            throw new OutOfMemoryError("Failed to allocate memory at : " + path);
        }

        return poolHandle + offset;
    }

    public long realloc(long addr, long size) {
        long offset = nativeRealloc(poolHandle, addr, size);
        if (offset == 0) {
            throw new OutOfMemoryError("Failed to realloc memory at : " + path);
        }

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
