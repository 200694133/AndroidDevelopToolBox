package com.hanyanan.tools.schedule.http;

import android.os.SystemClock;

import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.XLog;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2014/8/31.
 */
public class HttpUtils {
    public static String doStringRequest(HttpInterface httpInterface, NetworkRequest networkRequest)throws XError,IOException{
        NetworkResponse networkResponse = doRequest(httpInterface,networkRequest);
        if(null == networkResponse) return null;
        byte[] data = networkResponse.data;
        if(null == data || data.length <=0) return null;
        String parsed = null;
        try {
            parsed = new String(data, parseCharset(networkResponse.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(data);
        }
        return parsed;
    }
    public static NetworkResponse doRequest(HttpInterface httpInterface, NetworkRequest networkRequest) throws XError,IOException {
        long requestStart = SystemClock.elapsedRealtime();
        HttpResponse httpResponse = null;
        byte[] responseContents = null;
        Map<String, String> responseHeaders = null;
        try {
            httpResponse = httpInterface.performSimpleRequest(networkRequest);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            responseHeaders = convertHeaders(httpResponse.getAllHeaders());
//                // Handle cache validation.
//                if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
//                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
//                            request.getCacheEntry() == null ? null : request.getCacheEntry().data,
//                            responseHeaders, true);
//                }

            // Some responses such as 204s do not have content.  We must check.
            if (httpResponse.getEntity() != null) {
                responseContents = entityToBytes(httpResponse.getEntity());
            } else {
                // Add 0 byte response as a way of honestly representing a
                // no-content request.
                responseContents = new byte[0];
            }

            // if the request is slow, log it.
            long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
//          logSlowRequests(requestLifetime, request, responseContents, statusLine);
//TODO
            if (statusCode < 200 || statusCode > 299) {
                throw new IOException();
            }
            return new NetworkResponse(statusCode, responseContents, responseHeaders, false);
        } catch (SocketTimeoutException e) {
            throw new XError(e);
        } catch (ConnectTimeoutException e) {
            throw new XError(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad URL " + networkRequest.getRequestParam().getUrl(), e);
        } catch (IOException e) {
            int statusCode = 0;
            NetworkResponse networkResponse = null;
            if (httpResponse != null) {
                statusCode = httpResponse.getStatusLine().getStatusCode();
            } else {
                throw new XError(new NoConnectionError(e));
            }
            XLog.e("Unexpected response code %d for %s", statusCode, networkRequest.getRequestParam().getUrl());
            if (responseContents != null) {
                networkResponse = new NetworkResponse(statusCode, responseContents, responseHeaders, false);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                        statusCode == HttpStatus.SC_FORBIDDEN) {
//                        attemptRetryOnException("auth",
//                                request, new AuthFailureError(networkResponse));
                    throw new XError(new AuthFailureError("auth error " + networkRequest.getRequestParam().getUrl()));
                } else {
                    // TODO: Only throw ServerError for 5xx status codes.
                    throw new XError(new ServerError(networkResponse));
                }
            } else {
                throw new XError(new NetworkError(networkResponse));
            }
        } catch (ServerError serverError) {
            throw new XError(serverError);
        }finally {
            if(null != httpResponse && null!= httpResponse.getEntity()) httpResponse.getEntity().consumeContent();
        }
    }


    /** Reads the contents of HttpEntity into a byte[]. */
    public static byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
//        PoolingByteArrayOutputStream bytes =
//                new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
//        byte[] buffer = new byte[(int) entity.getContentLength()];
        List<Byte> buffList = new ArrayList<Byte>();
        byte[] buffer = new byte[4*1024];
        try {
            InputStream in = entity.getContent();
            while(true){
                int length = in.read(buffer);
                if(length <= 0) break;
                for(int i =0 ;i <length;++i){
                    buffList.add(buffer[i]);
                }
            }
//            InputStream in = entity.getContent();
//            if (in == null) {
//                throw new ServerError();
//            }
//            in.read(buffer);
//            buffer = mPool.getBuf(1024);
//            int count;
//            while ((count = in.read(buffer)) != -1) {
//                bytes.write(buffer, 0, count);
//            }
//            return bytes.toByteArray();
            byte[] res= new byte[buffList.size()];
            for(int i =0;i<buffList.size();++i){
                res[i] = buffList.get(i);
            }
            return res;
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                XLog.v("Error occurred when calling consumingContent");
            }
        }
    }
    /**
     * Converts Headers[] to Map<String, String>.
     */
    public static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String, String> headers) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return "UTF-8";
    }

    public static String encodeParam(Map<String, String> params){
        String encodeing  = "UTF-8";
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), encodeing));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), encodeing));
                encodedParams.append('&');
            }
            if(encodedParams.length() > 0){
                if(encodedParams.charAt(encodedParams.length()-1) == '&'){
                    return encodedParams.substring(0, encodedParams.length()-1);
                }
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encodeing, uee);
        }
    }
}
