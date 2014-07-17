package com.hanyanan.tools.datapersistence;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Created by hanyanan on 2014/7/8.
 */
public class Utils {
    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final Charset ISO_8859_1_CHARSET = Charset.forName("ISO-8859-1");
    private Utils() {
        throw new UnsupportedOperationException("");
    }
    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     */
    public static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    public static void closeQuietly(/*Auto*/Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
    public static void throwInterruptedIoException() throws InterruptedIOException {
        // This is typically thrown in response to an
        // InterruptedException which does not leave the thread in an
        // interrupted state, so explicitly interrupt here.
        Thread.currentThread().interrupt();
        // TODO: set InterruptedIOException.bytesTransferred
        throw new InterruptedIOException();
    }
}
