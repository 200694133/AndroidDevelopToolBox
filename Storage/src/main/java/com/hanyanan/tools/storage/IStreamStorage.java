package com.hanyanan.tools.storage;

import com.hanyanan.tools.storage.Error.BusyInUsingError;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/8/1.
 */
public interface IStreamStorage {
    public Editor edit(String key) throws IOException,BusyInUsingError;

    public Snapshot get(String key) throws IOException;

    public boolean remove(String key) throws IOException;

    public void close() throws IOException;

    public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            // Eat all writes silently. Nom nom.
        }
    };

    public static interface Editor{
        public void commit() throws IOException;

        public void abort() throws IOException;

        public OutputStream newOutputStream();

        public InputStream newInputStream();

        public void close();

        public void setHasError(boolean error);
    }

    /** A snapshot of the values for an entry. */
    public interface Snapshot extends Closeable {
        public InputStream getInputStream();
        /** Returns the byte length of the value. */
        public long getLength();

        public void close();
    }

    public static class FaultHidingOutputStream extends FilterOutputStream {
        private Editor mEditor;
        public FaultHidingOutputStream(OutputStream out, Editor editor) {
            super(out);
            mEditor = editor;
        }

        @Override public void write(int oneByte) {
            try {
                out.write(oneByte);
            } catch (IOException e) {
                mEditor.setHasError(true);
            }
        }

        @Override public void write(byte[] buffer, int offset, int length) {
            try {
                out.write(buffer, offset, length);
            } catch (IOException e) {
                mEditor.setHasError(true);
            }
        }

        @Override public void close() {
            try {
                out.close();
            } catch (IOException e) {
                mEditor.setHasError(true);
            }
        }

        @Override public void flush() {
            try {
                out.flush();
            } catch (IOException e) {
                mEditor.setHasError(true);
            }
        }
    }
}
