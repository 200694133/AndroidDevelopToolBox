package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.Map;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class RawRequestExecutor implements RequestExecutor<byte[], NetworkRequest> {
    private final HttpInterface mHttpInterface;
    public RawRequestExecutor(HttpInterface network){
        mHttpInterface = network;
    }
    @Override
    public Response<byte[]> performRequest(NetworkRequest request) throws XError {
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
