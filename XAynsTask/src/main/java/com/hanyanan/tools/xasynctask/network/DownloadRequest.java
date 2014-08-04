package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.ResponseDelivery;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/7/31.
 */
public class DownloadRequest extends NetworkRequest<String>{
    public DownloadRequest(String url, int method, HashMap<String, String> params,
                           RequestExecutor<String, NetworkRequest<?>> requestExecutor,
                           ResponseDelivery responseDelivery, Response.ErrorListener listener) {
        super(url, method, params, requestExecutor, responseDelivery, listener);
    }
}
