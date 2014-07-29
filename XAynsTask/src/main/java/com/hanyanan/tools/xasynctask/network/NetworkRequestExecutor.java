package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.XError;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequestExecutor implements RequestExecutor<byte[], NetworkRequest>{
    @Override
    public Response<byte[]> performRequest(NetworkRequest request) throws XError {
        return null;
    }

}
