package com.hanyanan.tools.storage.disk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/10/22.
 */
public class DefaultCopier implements DiskStorage.Copier {
    protected static final int DEFAULT_BUFF_SIZE = 16 * 1024;
    @Override
    public void copy(InputStream inputStream, OutputStream outputStream, long length) throws IOException {
        final byte[] bytes = new byte[DEFAULT_BUFF_SIZE];
        int read = 0;
        while((read=inputStream.read(bytes))>0){
            outputStream.write(bytes, 0, read);
        }
        outputStream.flush();
    }
}
