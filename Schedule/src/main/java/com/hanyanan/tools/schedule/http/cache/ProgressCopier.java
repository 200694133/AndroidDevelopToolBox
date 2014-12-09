package com.hanyanan.tools.schedule.http.cache;

import com.hanyanan.tools.schedule.XLog;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.storage.disk.DefaultCopier;
import com.hanyanan.tools.storage.disk.DiskStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/12/9.
 */
public class ProgressCopier extends DefaultCopier implements DiskStorage.Copier {
    private final HttpRequest.HttpProgressListener mHttpProgressListener;

    public ProgressCopier(HttpRequest.HttpProgressListener listener){
        mHttpProgressListener = listener;
    }
    @Override
    public void copy(InputStream inputStream, OutputStream outputStream, long length) throws IOException {
        final byte[] bytes = new byte[DEFAULT_BUFF_SIZE];
        int read = 0;

        long lengthMark = length;
        long curr = 0;
        if(lengthMark<=0 || lengthMark >= Integer.MAX_VALUE) lengthMark = inputStream.available();
        while((read=inputStream.read(bytes))>0){
            outputStream.write(bytes, 0, read);


            if(lengthMark > 0 && null!=mHttpProgressListener){
                curr += read;
                XLog.d("lengthMark "+lengthMark+"\tcurr"+curr);
                mHttpProgressListener.downloadProgress(curr/(float)lengthMark);
            }
        }
        outputStream.flush();
    }
}
