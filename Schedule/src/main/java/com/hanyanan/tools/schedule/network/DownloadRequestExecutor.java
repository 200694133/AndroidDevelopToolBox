package com.hanyanan.tools.schedule.network;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/7/31.
 */
public class DownloadRequestExecutor implements RequestExecutor<String,NetworkRequest<String>> {
    private final HttpStack mHttpStack;
    public DownloadRequestExecutor(HttpStack httpStack){
        mHttpStack = httpStack;
    }
    @Override
    public Response<String> performRequest(NetworkRequest<String> request) throws XError {
        try {
            InputStream inputStream = mHttpStack.performStreamRequest(request, request.getParams());
        } catch (IOException e) {
            e.printStackTrace();
            throw new NetworkError(e);
        }


        return null;
    }
}
