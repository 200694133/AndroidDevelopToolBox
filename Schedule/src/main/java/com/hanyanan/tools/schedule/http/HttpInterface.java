package com.hanyanan.tools.schedule.http;

import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by Administrator on 2014/8/30.
 */
public interface HttpInterface {

    public BasicHttpResponse performUpLoadRequest(HttpRequest httpRequest)throws  IOException;


    public BasicHttpResponse performSimpleRequest(HttpRequest httpRequest) throws IOException, ServerError;


    public BasicHttpResponse performDownLoadRequest(HttpRequest httpRequest)throws  IOException;



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
