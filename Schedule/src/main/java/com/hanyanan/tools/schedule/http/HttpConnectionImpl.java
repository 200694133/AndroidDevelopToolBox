package com.hanyanan.tools.schedule.http;

import android.text.TextUtils;

import com.hanyanan.tools.schedule.XError;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Administrator on 2014/8/27.
 */
public class HttpConnectionImpl implements HttpInterface{
    private static SSLSocketFactory TRUSTED_FACTORY;

    private static HostnameVerifier TRUSTED_VERIFIER;

    private SSLSocketFactory mSSLSocketFactory = null;
    private HostnameVerifier mHostnameVerifier;
    private static SSLSocketFactory getTrustedFactory() throws XError{
        if (TRUSTED_FACTORY == null) {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }
            } };
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new XError(e);
            }
        }

        return TRUSTED_FACTORY;
    }

    private static HostnameVerifier getTrustedVerifier() {
        if (TRUSTED_VERIFIER == null)
            TRUSTED_VERIFIER = new HostnameVerifier() {

                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

        return TRUSTED_VERIFIER;
    }

    private static final String BOUNDARY = "------o9weufv9ou4ef943pv9ikv03tiy045i9b09"; // 边界标识 随机生成
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型

    public static BasicHttpResponse performFullRequest(NetworkRequest request,HttpRequestParam param) throws IOException {
        if(param.getMethod() == HttpRequestParam.Method.POST && param.getUploadWrappers() !=null){
            return performMultiPartRequest(request,param);
        }else{
            return performSimpleRequest(request,param);
        }
    }
    //multipart 用于post模式下上传文件用的
    public static BasicHttpResponse performMultiPartRequest(NetworkRequest request,HttpRequestParam param) throws IOException {
        String urlPath = param.getUrl();
        {//在get模式下，封装参数到url
            if (param.getMethod() == HttpRequestParam.Method.GET && param.getUrlPramsMaps() != null) {
                if (urlPath.contains("?")) {
                    urlPath = urlPath + "&" + HttpUtils.encodeParam(param.getUrlPramsMaps());
                } else {
                    urlPath = urlPath + "?" + HttpUtils.encodeParam(param.getUrlPramsMaps());
                }
            }
        }
        URL url = new URL(urlPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//TODO
        {//https 验证
            if (HttpsURLConnection.class.isInstance(httpURLConnection)) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;//TODO
//                if (mHostnameVerifier == null) {
//                    httpsURLConnection.setHostnameVerifier(paramgetTrustedVerifier());
//                } else {
//                    httpsURLConnection.setHostnameVerifier(mHostnameVerifier);
//                }
            }
        }
        parseHead(httpURLConnection, param);//set request method
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        packageHttpHeader(httpURLConnection, param.getHttpHeader());//set/add request property
        httpURLConnection.setRequestProperty("Content-type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
        httpURLConnection.setRequestProperty("Connection", "close");
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setUseCaches(param.useCache());
        httpURLConnection.setConnectTimeout(param.getConnectTimeOut());
        httpURLConnection.setReadTimeout(param.getSocketTimeOut());
        if (null != param.getDownLoadContentRangeWrapper()) {
            httpURLConnection.setRequestProperty("Range", "bytes=" + param.getDownLoadContentRangeWrapper().offset + "-" + (param.getDownLoadContentRangeWrapper().offset + param.getDownLoadContentRangeWrapper().length));
        }

        DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
        {//write http url param
            if (param.getMethod() == HttpRequestParam.Method.POST && param.getUrlPramsMaps() != null) {
                HashMap<String, String> urls = param.getUrlPramsMaps();
                if (null != urls) {
                    Set<Map.Entry<String, String>> entry = urls.entrySet();
                    if (null != entry) {
                        for (Map.Entry<String, String> e : entry) {
                            sendUrlParams(dos, e.getKey(), e.getValue());
                        }
                    }
                }
            }
        }
        {//send file to server
            List<HttpRequestParam.UpLoadWrapper> wrappers = param.getUploadWrappers();
            if (null != wrappers) {
                for (HttpRequestParam.UpLoadWrapper w : wrappers) {
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
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 300 || responseCode < 200) {
                return null;
            }
        }

        BasicHttpResponse httpResponse = getResponseHeader(httpURLConnection);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(httpURLConnection.getInputStream());
        entity.setContentLength(httpURLConnection.getContentLength());
        entity.setContentEncoding(httpURLConnection.getContentEncoding());
        entity.setContentType(httpURLConnection.getContentType());
        httpResponse.setEntity(entity);
        return httpResponse;
    }


    public static BasicHttpResponse performSimpleRequest(NetworkRequest request,HttpRequestParam param) throws IOException {
        String urlPath = param.getUrl();
        {//在get模式下，封装参数到url
            if (param.getMethod() == HttpRequestParam.Method.GET && param.getUrlPramsMaps() != null && param.getUrlPramsMaps().size()>0) {
                if (urlPath.contains("?")) {
                    urlPath = urlPath + "&" + HttpUtils.encodeParam(param.getUrlPramsMaps());
                } else {
                    urlPath = urlPath + "?" + HttpUtils.encodeParam(param.getUrlPramsMaps());
                }
            }
        }
        URL url = new URL(urlPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//TODO
        {//https 验证
            if (HttpsURLConnection.class.isInstance(httpURLConnection)) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;//TODO
//                if (mHostnameVerifier == null) {
//                    httpsURLConnection.setHostnameVerifier(paramgetTrustedVerifier());
//                } else {
//                    httpsURLConnection.setHostnameVerifier(mHostnameVerifier);
//                }
            }
        }
        parseHead(httpURLConnection, param);//set request method
        if(param.getMethod() == HttpRequestParam.Method.GET){
            httpURLConnection.setDoOutput(false);
        }else {
            httpURLConnection.setDoOutput(true);
        }
        httpURLConnection.setDoInput(true);
        packageHttpHeader(httpURLConnection, param.getHttpHeader());//set/add request property
        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        httpURLConnection.setRequestProperty("Connection", "close");
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setUseCaches(param.useCache());
        httpURLConnection.setConnectTimeout(param.getConnectTimeOut());
        httpURLConnection.setReadTimeout(param.getSocketTimeOut());
        if (null != param.getDownLoadContentRangeWrapper()) {
            httpURLConnection.setRequestProperty("Range", "bytes=" + param.getDownLoadContentRangeWrapper().offset + "-" + (param.getDownLoadContentRangeWrapper().offset + param.getDownLoadContentRangeWrapper().length));
        }

        if(param.getMethod() == HttpRequestParam.Method.POST){
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            {//write http url param
                if (param.getUrlPramsMaps() != null && param.getUrlPramsMaps().size() > 0) {
                    HashMap<String, String> params = param.getUrlPramsMaps();
                    String s = HttpUtils.encodeParam(params);
                    dos.write(s.getBytes());
                }
            }
//            byte[] end_data = ( LINE_END).getBytes();
//            dos.write(end_data);
            dos.flush();
            dos.close();
        }

        {//check response
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 300 || responseCode < 200) {
                return null;
            }
        }

        BasicHttpResponse httpResponse = getResponseHeader(httpURLConnection);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(httpURLConnection.getInputStream());
        entity.setContentLength(httpURLConnection.getContentLength());
        entity.setContentEncoding(httpURLConnection.getContentEncoding());
        entity.setContentType(httpURLConnection.getContentType());
        httpResponse.setEntity(entity);
        return httpResponse;
    }
//    public static HttpResponseWrapper performUploadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
//        parseHead(connection, param);
//        connection.setDoInput(true);
//        connection.setDoOutput(true);
//        packageHttpHeader(connection,param.getHttpHeader());
//        connection.setRequestProperty("Content-type", CONTENT_TYPE+";boundary="+BOUNDARY);
//        connection.setRequestProperty("Connection", "close");
//        connection.setInstanceFollowRedirects(true);
//        connection.setUseCaches(param.useCache());
//        connection.setConnectTimeout(param.getConnectTimeOut());
//        connection.setReadTimeout(param.getSocketTimeOut());
//        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
//        {//write http url param
//            HashMap<String,String> urls = param.getUrlPramsMaps();
//            if(null != urls){
//                Set<Map.Entry<String,String>> entry = urls.entrySet();
//                if(null != entry){
//                    for(Map.Entry<String,String> e:entry){
//                        sendUrlParams(dos, e.getKey(),e.getValue());
//                    }
//                }
//            }
//        }
//
//        {//send file to server
//            List<HttpRequestParam.UpLoadWrapper> wrappers = param.getUploadWrappers();
//            if(null != wrappers){
//                for(HttpRequestParam.UpLoadWrapper w:wrappers){
//                    sendFile(dos, w);
//                }
//            }
//        }
//
//        {//write the lst boundary with new line char
//            byte[] end_data = (BOUNDARY + LINE_END).getBytes();
//            dos.write(end_data);
//            dos.flush();
//            dos.close();
//        }
//
//        {//check response
//            int responseCode = connection.getResponseCode();
//            if (responseCode >= 300 || responseCode<200) {
//               return null;
//            }
//        }
//        return true;
//    }

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

    private static void sendFile(DataOutputStream dataOutputStream, HttpRequestParam.UpLoadWrapper wrapper) throws IOException {
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


//    public static HttpResponseWrapper performDownloadRequest(HttpURLConnection connection, HttpRequestParam param) throws IOException {
//        parseHead(connection, param);
//        if(param.getMethod() == HttpRequestParam.Method.POST){
//            connection.setDoOutput(true);
//        }
//        connection.setDoInput(true);
//        packageHttpHeader(connection,param.getHttpHeader());
//        connection.setRequestProperty("Content-type", param.getContentType());
//        connection.setRequestProperty("Connection", "close");
//        connection.setInstanceFollowRedirects(true);
//        connection.setUseCaches(param.useCache());
//        HttpRequestParam.ContentRangeWrapper contentRangeWrapper = param.getContentRangeWrapper();
//        if(null != contentRangeWrapper){
//            connection.setRequestProperty("Range", "bytes="+contentRangeWrapper.offset+"-"+(contentRangeWrapper.offset+contentRangeWrapper.length));
//        }
//        connection.connect();
//        {//Set Connection Parameters For Request in post mode
//            if (param.getMethod() == HttpRequestParam.Method.POST) {
//                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//                out.write(param.parseUrlParam().getBytes());
//                out.close();
//            }
//        }
//
//        BasicHttpResponse httpResponse = getResponseHeader(connection);
//        InputStream inputStream = connection.getInputStream();
//        return new HttpResponseWrapper(httpResponse, inputStream, connection);
//    }
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

//    @Override
//    public boolean performUpLoadRequest(NetworkRequest httpRequest) throws IOException {
//        HttpRequestParam httpRequestParam = httpRequest.getRequestParam();
//        URL url = null;
//        if(httpRequestParam.getMethod() == HttpRequestParam.Method.GET){
//            url = new URL(httpRequestParam.getUrl()+"?"+HttpUtils.encodeParam(httpRequestParam.getUrlPramsMaps()));
//        }else{
//            url = new URL(httpRequestParam.getUrl());
//        }
//
//        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();//TODO
//        if(null != httpURLConnection && !TextUtils.isEmpty(httpRequestParam.getUrl()) && "https://".startsWith(httpRequestParam.getUrl().toLowerCase())){
//            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)httpURLConnection;
//            if(mHostnameVerifier == null){
//                httpsURLConnection.setHostnameVerifier(getTrustedVerifier());
//            }else {
//                httpsURLConnection.setHostnameVerifier(mHostnameVerifier);
//            }
//        }
//        return performUploadRequest(httpURLConnection, httpRequestParam);
//    }

    @Override
    public BasicHttpResponse performUpLoadRequest(NetworkRequest httpRequest) throws IOException {
        HttpRequestParam httpRequestParam = httpRequest.getRequestParam();
        return performFullRequest(httpRequest, httpRequestParam);
    }
    @Override
    public BasicHttpResponse performSimpleRequest(NetworkRequest httpRequest) throws IOException, ServerError {
        HttpRequestParam httpRequestParam = httpRequest.getRequestParam();
        return performFullRequest(httpRequest, httpRequestParam);
//        HttpResponseWrapper httpResponseWrapper = performDownLoadRequest(httpRequest);
//        BasicHttpResponse basicHttpResponse = httpResponseWrapper.basicHttpResponse;
//        BasicHttpEntity entity = new BasicHttpEntity();
//        entity.setContent(httpResponseWrapper.inputStream);
//        entity.setContentLength(httpResponseWrapper.httpURLConnection.getContentLength());
//        entity.setContentEncoding(httpResponseWrapper.httpURLConnection.getContentEncoding());
//        entity.setContentType(httpResponseWrapper.httpURLConnection.getContentType());
//        basicHttpResponse.setEntity(entity);
//        return basicHttpResponse;
    }

    @Override
    public BasicHttpResponse performDownLoadRequest(NetworkRequest httpRequest) throws IOException {
        HttpRequestParam httpRequestParam = httpRequest.getRequestParam();
        return performFullRequest(httpRequest, httpRequestParam);
//        HttpRequestParam httpRequestParam = httpRequest.getRequestParam();
//        URL url = null;
//        if(httpRequestParam.getMethod() == HttpRequestParam.Method.GET){
//            if(httpRequestParam.getUrlPramsMaps()!=null && httpRequestParam.getUrlPramsMaps().size() > 0) {
//                url = new URL(httpRequestParam.getUrl() + "?" + HttpUtils.encodeParam(httpRequestParam.getUrlPramsMaps()));
//            }else{
//                url = new URL(httpRequestParam.getUrl());
//            }
//        }else{
//            url = new URL(httpRequestParam.getUrl());
//        }
//        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
//        if(null != httpURLConnection && !TextUtils.isEmpty(httpRequestParam.getUrl()) && "https://".startsWith(httpRequestParam.getUrl().toLowerCase())){
//            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)httpURLConnection;
//            if(mHostnameVerifier == null){
//                httpsURLConnection.setHostnameVerifier(getTrustedVerifier());
//            }else {
//                httpsURLConnection.setHostnameVerifier(mHostnameVerifier);
//            }
//        }
//        // use caller-provided custom SslSocketFactory, if any, for HTTPS
//        if ("https".equals(url.getProtocol()) && httpRequestParam.getSSLSocketFactory() != null) {
//            ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(httpRequestParam.getSSLSocketFactory());
//        }
//        return performDownloadRequest(httpURLConnection, httpRequestParam);
    }

    private static void packageHttpHeader(HttpURLConnection httpURLConnection, HashMap<String,String> headers){
        if(null == headers) return;
        for (String headerName : headers.keySet()) {
            httpURLConnection.addRequestProperty(headerName, headers.get(headerName));
        }
    }

    /**
     * Configure HTTPS connection to trust all certificates
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     * @throws XError
     */
    public HttpConnectionImpl trustAllCerts() throws XError {
        mHostnameVerifier = getTrustedVerifier();
//        final HttpURLConnection connection = getConnection();
//        if (connection instanceof HttpsURLConnection)
//            ((HttpsURLConnection) connection)
//                    .setSSLSocketFactory(getTrustedFactory());
        return this;
    }

    /**
     * Configure HTTPS connection to trust all hosts using a custom
     * {@link HostnameVerifier} that always returns <code>true</code> for each
     * host verified
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     */
    public HttpConnectionImpl trustAllHosts() throws XError {
        mSSLSocketFactory = getTrustedFactory();
//        final HttpURLConnection connection = getConnection();
//        if (connection instanceof HttpsURLConnection)
//            ((HttpsURLConnection) connection)
//                    .setHostnameVerifier(getTrustedVerifier());
        return this;
    }

}
