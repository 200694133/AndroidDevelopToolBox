package com.hanyanan.tools.storage;

import android.content.Context;

import com.hanyanan.tools.storage.Error.TypeNotSupportError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/8.
 */
public class Utils {
    public static final int M = 1024 * 1024;
    public static final int K = 1024;
    public static final int DEFAULT_BUFF_SIZE = 16 * K;
    public static final int DEFAULT_DISK_SIZE = 20 * M;


    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final Charset ISO_8859_1_CHARSET = Charset.forName("ISO-8859-1");
    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();


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

    public File getInternalStorageDirector(Context context, String dir){
        return context.getDir(dir, 0);
    }

    public static byte[] serialize(Object content) throws TypeNotSupportError {

        try {
            ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(mem_out);

            out.writeObject(content);

            out.close();
            mem_out.close();

            byte[] bytes =  mem_out.toByteArray();
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    public static Serializable deSerialize(byte[] bytes) throws ClassNotFoundException{
        try {
            ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(mem_in);

            Serializable content = (Serializable)in.readObject();

            in.close();
            mem_in.close();

            return content;
        } catch (StreamCorruptedException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static String generatorKey(String primaryKey, String secondaryKey){
        return primaryKey+"_"+secondaryKey;
    }
    public static void copy(InputStream in, OutputStream out, int buffSize)throws IOException{
        copy(in,out,Integer.MAX_VALUE, buffSize);
    }

    public static void copy(InputStream in, OutputStream out)throws IOException{
        copy(in,out,Integer.MAX_VALUE, DEFAULT_BUFF_SIZE);
    }
    /**
     * Copies stream, from input stream to output stream.
     * @param in input stream
     * @param out output stream
     * @param length length to copy
     * @param buffSize buffer size
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out, int length, int buffSize) throws IOException{
        final byte[] bytes = new byte[buffSize];
        int read = 0;
        while((read=in.read(bytes))>0){
            out.write(bytes, 0, read);
        }
        out.flush();
    }
}
