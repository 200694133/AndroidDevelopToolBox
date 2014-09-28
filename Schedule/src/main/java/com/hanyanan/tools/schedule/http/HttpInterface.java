package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.XLog;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2014/8/30.
 */
public interface HttpInterface {

    public BasicHttpResponse performUpLoadRequest(NetworkRequest httpRequest)throws  IOException;


    public BasicHttpResponse performSimpleRequest(NetworkRequest httpRequest) throws IOException, ServerError;


    public BasicHttpResponse performDownLoadRequest(NetworkRequest httpRequest)throws  IOException;



    public static class HttpResponseWrapper{
        public InputStream inputStream;
        public BasicHttpResponse basicHttpResponse;
        public HttpURLConnection httpURLConnection;
        public HttpResponseWrapper(BasicHttpResponse basicHttpResponse, InputStream inputStream,
                                   HttpURLConnection httpURLConnection){
            this.basicHttpResponse = basicHttpResponse;
            this.inputStream = inputStream;
            this.httpURLConnection = httpURLConnection;
        }
    }
}
