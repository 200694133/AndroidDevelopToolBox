package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.XLog;

import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2014/8/27.
 */
public class HttpConnectionImpl implements HttpInterface{
    private static final String BOUNDARY = "------o9weufv9ou4ef943pv9ikv03tiy045i9b09"; // 边界标识 随机生成
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型
    public static boolean performUploadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
        parseHead(connection, param);
        if(param.getMethod() == HttpRequestParam.Method.POST){
            connection.setDoInput(true);
        }
        connection.setDoOutput(true);
        packageHttpHeader(connection,param.getHttpHeader());
        connection.setRequestProperty("Content-type", CONTENT_TYPE+";boundary="+BOUNDARY);
        connection.setRequestProperty("Connection", "close");
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(param.useCache());
        connection.setConnectTimeout(param.getConnectTimeOut());
        connection.setReadTimeout(param.getSocketTimeOut());
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        {//write http url param
            HashMap<String,String> urls = param.getUrlPramsMaps();
            if(null != urls){
                Set<Map.Entry<String,String>> entry = urls.entrySet();
                if(null != entry){
                    for(Map.Entry<String,String> e:entry){
                        sendUrlParams(dos, e.getKey(),e.getValue());
                    }
                }
            }
        }

        {//send file to server
            List<HttpRequestParam.UploadWrapper> wrappers = param.getUploadWrappers();
            if(null != wrappers){
                for(HttpRequestParam.UploadWrapper w:wrappers){
                    sendFile(dos, w);
                }
            }
        }

        {//write the lst boundary with new line char
            byte[] end_data = (BOUNDARY + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();
        }

        {//check response
            int responseCode = connection.getResponseCode();
            if (responseCode >= 300 || responseCode<200) {
               return false;
            }
        }
        return true;
    }

    private static void sendUrlParams(DataOutputStream dataOutputStream,String key,String value) throws IOException {
        //write boundary to server
        StringBuffer sb = new StringBuffer();
        sb.append(BOUNDARY);
        sb.append(LINE_END);
        sb.append("Content-Disposition: form-data; name=\""+ key+"\"" + LINE_END);
        sb.append(LINE_END);
        sb.append(value);
        sb.append(LINE_END);
        dataOutputStream.write(sb.toString().getBytes());
    }

    private static void sendFile(DataOutputStream dataOutputStream, HttpRequestParam.UploadWrapper wrapper) throws IOException {
        //write boundary to server
        StringBuffer sb = new StringBuffer();
        sb.append(BOUNDARY);
        sb.append(LINE_END);
        /**
         * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
         * filename是文件的名字，包含后缀名的 比如:abc.png
         */
        sb.append("Content-Disposition: form-data; name=\""+ wrapper.name+"\"; filename=\""
                + wrapper.name + "\"" + LINE_END);
        sb.append("Content-Type: "+wrapper.contentType + LINE_END);
        sb.append(LINE_END);
        dataOutputStream.write(sb.toString().getBytes());
        InputStream inputStream = wrapper.inputStream;
        byte[] bytes = new byte[4098];
        int offset = 0;
        int length = Integer.MAX_VALUE;
        if(null != wrapper.contentRangeWrapper){
            offset = (int)wrapper.contentRangeWrapper.offset;
            length = (int)wrapper.contentRangeWrapper.length;
        }
        if(offset > 0) inputStream.skip(offset);
        while(true){
            int want = length>4098?4098:length;
            length -= want;
            int read = inputStream.read(bytes,0,want);
            if(read <=0) break;
            dataOutputStream.write(bytes,0, read);
            if(length <= 0) break;
        }
        dataOutputStream.write(LINE_END.getBytes());
        if(wrapper.autoClose){
            wrapper.inputStream.close();
        }
    }

    public static HttpResponseWrapper performDownloadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
        parseHead(connection, param);
        if(param.getMethod() == HttpRequestParam.Method.POST){
            connection.setDoInput(true);
        }
        connection.setDoOutput(true);
        packageHttpHeader(connection,param.getHttpHeader());
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

    @Override
    public boolean performUpLoadRequest(NetworkRequest httpRequest) throws IOException {
        HttpRequestParam httpRequestParam = httpRequest.getHttpRequestParam();
        URL url = null;
        if(httpRequestParam.getMethod() == HttpRequestParam.Method.GET){
            url = new URL(httpRequestParam.getUrl()+"?"+encodeParam(httpRequestParam.getUrlPramsMaps()));
        }else{
            url = new URL(httpRequestParam.getUrl());
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();//TODO
        return performUploadRequest(httpURLConnection, httpRequestParam);
    }

    @Override
    public BasicHttpResponse performSimpleRequest(NetworkRequest httpRequest) throws IOException, ServerError {
        HttpResponseWrapper httpResponseWrapper = performDownLoadRequest(httpRequest);
        BasicHttpResponse basicHttpResponse = httpResponseWrapper.basicHttpResponse;
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        inputStream = httpResponseWrapper.inputStream;
        entity.setContent(inputStream);
        entity.setContentLength(httpResponseWrapper.httpURLConnection.getContentLength());
        entity.setContentEncoding(httpResponseWrapper.httpURLConnection.getContentEncoding());
        entity.setContentType(httpResponseWrapper.httpURLConnection.getContentType());
        return basicHttpResponse;
    }

    @Override
    public HttpResponseWrapper performDownLoadRequest(NetworkRequest httpRequest) throws IOException {
        HttpRequestParam httpRequestParam = httpRequest.getHttpRequestParam();
        URL url = null;
        if(httpRequestParam.getMethod() == HttpRequestParam.Method.GET){
            url = new URL(httpRequestParam.getUrl()+"?"+encodeParam(httpRequestParam.getUrlPramsMaps()));
        }else{
            url = new URL(httpRequestParam.getUrl());
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();//TODO
        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && httpRequestParam.getSSLSocketFactory() != null) {
            ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(httpRequestParam.getSSLSocketFactory());
        }
        return performDownloadRequest(httpURLConnection, httpRequestParam);
    }

    private static void packageHttpHeader(HttpURLConnection httpURLConnection, HashMap<String,String> headers){
        if(null == headers) return;
        for (String headerName : headers.keySet()) {
            httpURLConnection.addRequestProperty(headerName, headers.get(headerName));
        }
    }

    private static String encodeParam(Map<String, String> params){
        String encodeing  = "UTF-8";
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), encodeing));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), encodeing));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encodeing, uee);
        }
    }


}
