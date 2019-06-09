package hudson.plugins.dimensionsscm;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility method to read a file into a byte array.
 */
final class FileUtils {
    private FileUtils() {
        /* prevent instantiation. */
    }

    /**
     * Utility routine to read file into memory.
     */
    static byte[] readAllBytes(final File file) throws IOException {
        long len = file.length();
        if (len > (long) Integer.MAX_VALUE) {
            throw new IOException("File is too long to read entirely");
        }
        byte[] bytes = new byte[(int) len];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int readSoFar = 0;
        try {
            while (readSoFar < (int) len) {
                int readThisTime = bis.read(bytes, readSoFar, (int) len - readSoFar);
                if (readThisTime < 0) {
                    throw new EOFException("File ended before reading expected length");
                }
                readSoFar += readThisTime;
            }
        } finally {
            bis.close();
        }
        return bytes;
    }
}
