package lib.llpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NonVolatileHeap {
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

    private NonVolatileHeap(Path path, long poolHandle) throws IOException {
        this.path = path;
        this.size = Files.size(path);
        this.poolHandle = poolHandle;
        this.open = true;
    }

    public static NonVolatileHeap openHeap(String path) throws IOException {
        synchronized (lock) {
            long poolHandle = nativeOpenHeap(path);
            if (poolHandle == 0) {
                throw new IOException("Failed to open heap at : " + path);
            }

            return new NonVolatileHeap(Paths.get(path), poolHandle);
        }
    }

    public static NonVolatileHeap createHeap(String path, long size) throws IOException {
        synchronized (lock) {
            long poolHandle = nativeCreateHeap(path, size + FILE_SIZE_OVERHEAD);
            if (poolHandle == 0) {
                throw new IOException("Cannot create heap at " + path);
            }

            return new NonVolatileHeap(Paths.get(path), poolHandle);
        }
    }
    public void close() {
        synchronized (closeLock) {
            if (open) {
                nativeCloseHeap(poolHandle);
                open = false;
            }
        }
    }

    public long toAddress(long handle) {
        if (handle == 0) {
            return 0;
        }

        return poolHandle + handle;
    }
    public long toHandle(long addr) {
        if (addr == 0) {
            return 0;
        }

        long handle = addr - poolHandle;
        if (handle < 0) {
            throw new IllegalArgumentException("addr is " + addr + " poolHandle is " + poolHandle);
        }
        return addr - poolHandle;
    }

    public long allocate(long size) {
        return toAddress(nativeAlloc(poolHandle, size));
    }

    public long realloc(long addr, long size) {
        return toAddress(nativeRealloc(poolHandle, toHandle(addr), size));
    }

    public void free(long addr) {
        nativeFree(toHandle(addr));
    }

    public int setRoot(long addr) {
        return nativeSetRoot(poolHandle, addr);
    }

    public long getRoot() {
        return nativeGetRoot(poolHandle);
    }

    private static native long nativeCreateHeap(String path, long size);
    private static native long nativeOpenHeap(String path);
    private static native void nativeCloseHeap(long poolHandle);

    private static native int nativeSetRoot(long poolHandle, long address);
    private static native long nativeGetRoot(long poolHandle);

    private static native long nativeAlloc(long poolHandle, long size);
    private static native long nativeRealloc(long poolHandle, long addr, long size);
    private static native int nativeFree(long addr);
}
