package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2014/8/27.
 */
public class HttpConnectionRequestExecutor implements RequestExecutor<byte[], NetworkRequest> {
    private static final String BOUNDARY = "-----------------------------";
    private static void performUploadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
        parseHead(connection, param);
        if(param.getMethod() == HttpRequestParam.Method.POST){
            connection.setDoInput(true);
        }
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-type", param.getContentType()+"; boundary="+BOUNDARY);
        connection.setRequestProperty("Connection", "close");
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(param.useCache());
        byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
    }


    private static HttpResponseWrapper performDownloadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
        parseHead(connection, param);
        if(param.getMethod() == HttpRequestParam.Method.POST){
            connection.setDoInput(true);
        }
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-type", param.getContentType());
        connection.setRequestProperty("Connection", "close");
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(param.useCache());
        HttpRequestParam.ContentRangeWrapper contentRangeWrapper = param.getContentRangeWrapper();
        if(null != contentRangeWrapper){
            connection.setRequestProperty("Range", "bytes="+contentRangeWrapper.offset+"-"+(contentRangeWrapper.offset+contentRangeWrapper.length));
        }
        connection.connect();
        {//Set Connection Parameters For Request in post mode
            if (param.getMethod() == HttpRequestParam.Method.POST) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(param.parseUrlParam().getBytes());
                out.close();
            }
        }

        BasicHttpResponse httpResponse = getResponseHeader(connection);
        InputStream inputStream = connection.getInputStream();
        return new HttpResponseWrapper(httpResponse, inputStream, connection);
    }
    private static void parseHead(HttpURLConnection connection, HttpRequestParam param) throws ProtocolException {
        int method = param.getMethod();
        switch (method){
            case HttpRequestParam.Method.GET:
                connection.setRequestMethod("GET");
                break;
            case HttpRequestParam.Method.POST:
                connection.setRequestMethod("POST");
                break;
            case HttpRequestParam.Method.PUT:
                connection.setRequestMethod("PUT");
                break;
            case HttpRequestParam.Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            default:
                //TODO
        }
    }

    private static BasicHttpResponse getResponseHeader(HttpURLConnection connection) throws IOException {
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                connection.getResponseCode(), connection.getResponseMessage());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        return response;
    }

    private static class HttpResponseWrapper{
        public InputStream inputStream;
        public BasicHttpResponse basicHttpResponse;
        public HttpURLConnection httpURLConnection;
        private HttpResponseWrapper(BasicHttpResponse basicHttpResponse, InputStream inputStream,
                                    HttpURLConnection httpURLConnection){
            this.basicHttpResponse = basicHttpResponse;
            this.inputStream = inputStream;
            this.httpURLConnection = httpURLConnection;
        }
    }

    @Override
    public Response<byte[]> performRequest(NetworkRequest request) throws XError {
        return null;
    }
}
