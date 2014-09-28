package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.RequestParam;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;
import com.hanyanan.tools.schedule.XError;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
