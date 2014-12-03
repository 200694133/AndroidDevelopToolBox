package com.hanyanan.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/9/26.
 */
public abstract class SafeInputStream extends InputStream {
    private final InputStream mInputStream;
    public SafeInputStream(InputStream inputStream) {
        mInputStream = inputStream;
    }

    public int available() throws IOException {
        return mInputStream.available();
    }

    public void close() throws IOException {
        mInputStream.close();
        closeCallback();
    }

    public abstract void closeCallback();

    public void mark(int readlimit) {
        mInputStream.mark(readlimit);
    }

    public boolean markSupported() {
        return mInputStream.markSupported();
    }

    public int read(byte[] buffer) throws IOException {
        return mInputStream.read(buffer);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return mInputStream.read(buffer,offset,length);
    }

    public synchronized void reset() throws IOException {
        mInputStream.reset();
    }

    public long skip(long byteCount) throws IOException {
        return mInputStream.skip(byteCount);
    }

    @Override
    public int read() throws IOException {
        return mInputStream.read();
    }
}
