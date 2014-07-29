package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.Request;
import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.ResponseDelivery;
import com.hanyanan.tools.xasynctask.RetryPolicy;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequest extends Request<byte[]>{
    public NetworkRequest(RequestExecutor<byte[], NetworkRequest> requestExecutor,
                          ResponseDelivery responseDelivery, RetryPolicy retryPolicy,
                          Response.ErrorListener listener) {
        super(requestExecutor, responseDelivery, retryPolicy, listener);
    }
}
