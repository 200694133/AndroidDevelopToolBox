package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequestExecutor implements RequestExecutor<byte[], NetworkRequest>{
    private final Network mNetwork;
    public NetworkRequestExecutor(Network network){
        mNetwork = network;
    }
    @Override
    public Response<byte[]> performRequest(NetworkRequest request) throws XError {
        NetworkResponse res = mNetwork.performRequest(request);
        return Response.success(res.data);
    }
}
