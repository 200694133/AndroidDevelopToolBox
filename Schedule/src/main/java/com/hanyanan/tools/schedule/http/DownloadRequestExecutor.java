package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/7/31.
 */
public class DownloadRequestExecutor implements RequestExecutor<String,NetworkRequest> {
    private final HttpExecutor mHttpExecutor;
    public DownloadRequestExecutor(HttpExecutor httpExecutor){
        mHttpExecutor = httpExecutor;
    }

    @Override
    public Response<String> performRequest(NetworkRequest request) throws XError {
        try {
            InputStream inputStream = mHttpExecutor.performStreamRequest(request, request.getParams());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NetworkError(e);
        }


        return null;
    }
}
