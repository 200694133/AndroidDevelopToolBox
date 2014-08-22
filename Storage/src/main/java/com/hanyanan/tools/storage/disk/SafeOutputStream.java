package com.hanyanan.tools.storage.disk;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class SafeOutputStream extends OutputStream implements Abortable{
    private final IStreamStorage.Editor mEditor;
    private final OutputStream mOutputStream;
    SafeOutputStream(IStreamStorage.Editor editor,OutputStream outputStream){
        mEditor = editor;
        mOutputStream = outputStream;
    }

    public void abort()throws java.io.IOException{
        mOutputStream.close();
        mEditor.abort();
    }
    public void close() throws java.io.IOException {
        mOutputStream.close();
        mEditor.commit();
    }

    public void flush() throws java.io.IOException {
        mOutputStream.flush();
    }

    public void write(byte[] buffer) throws java.io.IOException {
        mOutputStream.write(buffer);
    }

    public void write(byte[] buffer, int offset, int count) throws java.io.IOException {
        mOutputStream.write(buffer,offset,count);
    }

    public void write(int i) throws java.io.IOException{
        mOutputStream.write(i);
    }
}
