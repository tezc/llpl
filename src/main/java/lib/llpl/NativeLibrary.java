package lib.llpl;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLibrary {

    private static final String NATIVE_LIBRARY_DIR = "lib/";

    private enum OS {
        UNIX(".so"),
        MAC(".dylib"),
        WINDOWS(".dll");

        private final String fileExtension;

        OS(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }
    }


    private NativeLibrary() {
    }

    public static void load() {
        System.load(extractBundledLib());
    }

    private static String extractBundledLib() {

        OS os = getOS();
        String path = NATIVE_LIBRARY_DIR + "libllpl-" + getArchitecture() + os.getFileExtension();

        try (InputStream src = NativeLibrary.class.getClassLoader().getResourceAsStream(path)){
            if (src == null) {
                throw new RuntimeException("Cannot find native libray at : " + path);
            }

            File file = File.createTempFile("libllpl", os.getFileExtension());
            file.deleteOnExit();

            Path nativeLibPath = file.toPath();
            Files.copy(src, nativeLibPath, StandardCopyOption.REPLACE_EXISTING);

            return nativeLibPath.toAbsolutePath().toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            return OS.MAC;
        }
        else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OS.UNIX;
        }
        else if (osName.contains("win")) {
            return OS.WINDOWS;
        }
        else {
            throw new IllegalStateException("LLPL is not supported on : " + osName);
        }
    }

    private static String getArchitecture() {
        String arch = System.getProperty("sun.arch.data.model");
        if (arch != null && arch.equals("32")) {
            return "x86";
        }

        return "x86_64";
    }
}
