package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import java.io.IOException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class RawRequestExecutor implements RequestExecutor<byte[], HttpRequest> {
    public final static HttpInterface DEFAULT_HTTP = new HttpConnectionImpl();
    private final HttpInterface mHttpInterface;
    public RawRequestExecutor(HttpInterface network){
        mHttpInterface = network;
    }
    public RawRequestExecutor(){
        mHttpInterface = DEFAULT_HTTP;
    }

    @Override
    public Response<byte[]> performRequest(HttpRequest request) throws XError {
        try {
            NetworkResponse response = HttpUtils.doRequest(mHttpInterface,request);
            if(null == response) return Response.success(null);
            return Response.success(response.data);
        } catch (IOException e) {
           throw new XError(e);
        }catch (Exception e){
            throw new XError(e);
        }
    }
}
