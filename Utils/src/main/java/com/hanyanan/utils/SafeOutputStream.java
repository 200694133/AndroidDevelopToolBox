package com.hanyanan.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by hanyanan on 2014/9/26.
 */
public abstract class SafeOutputStream extends OutputStream {
    private final OutputStream mOutputStream;
    public SafeOutputStream(OutputStream outputStream){
        mOutputStream = outputStream;
    }

    public void close() throws IOException {
        mOutputStream.close();
        closeCallback();
    }

    public void flush() throws IOException {
        mOutputStream.flush();
    }

    public abstract void closeCallback();

    public void write(byte[] buffer) throws IOException {
        mOutputStream.write(buffer);
    }


    public void write(byte[] buffer, int offset, int count) throws IOException {
        mOutputStream.write(buffer,offset,count);
    }

    public void write(int oneByte) throws IOException{
        mOutputStream.write(oneByte);
    }
}
