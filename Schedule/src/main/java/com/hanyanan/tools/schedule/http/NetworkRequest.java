package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.RequestParam;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequest extends Request<HttpRequestParam>{

    public NetworkRequest(RequestQueue requestQueue,
                          RequestExecutor  requestExecutor, HttpRequestParam param) {
        super(requestQueue,requestExecutor,param);
    }

    public String getKey(){
        return String.valueOf(Math.abs(getRequestParam().getUrl().hashCode()));
    }

    public String getUrl(){
        return getRequestParam().getUrl();
    }
}
