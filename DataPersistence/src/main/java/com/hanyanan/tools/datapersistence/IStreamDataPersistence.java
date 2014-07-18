package com.hanyanan.tools.datapersistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/7/17.
 */
public interface IStreamDataPersistence {
    public InputStream getInputStreamImmediately(String key) throws IOException;
    public void getInputStreamAsync(String key, IOnInputStreamListener listener);

    public OutputStream getOutputStreamImmediately(String key) throws IOException;
    public void getOutputStreamAsync(String key, IOnOutputStreamListener listener);

    public interface IOnInputStreamListener{
        public void onReady(InputStream inputStream);
        public void onError(String errInfo);
    }
    public interface IOnOutputStreamListener{
        public void onReady(OutputStream outputStream);
        public void onError(String errInfo);
    }
}
