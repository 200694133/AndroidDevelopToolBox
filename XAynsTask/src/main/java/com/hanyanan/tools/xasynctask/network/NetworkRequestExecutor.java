package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.XError;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequestExecutor implements RequestExecutor<byte[], NetworkRequest<byte[]>>{
    private final Network mNetwork;
    public NetworkRequestExecutor(Network network){
        mNetwork = network;
    }
    @Override
    public Response<byte[]> performRequest(NetworkRequest<byte[]> request) throws XError {
        NetworkResponse res = mNetwork.performRequest(request);
        return Response.success(res.data);
    }
}
