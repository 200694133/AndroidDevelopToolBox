package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestParam;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/8/27.
 * Http request param. It support all type of http request, such as
 */
public class HttpRequestParam implements RequestParam{
    public final static String APPLICATION_DEFAULT="application/x-www-form-urlencoded";
    public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public final static String APPLICATION_JSON = "application/json";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ENCODING_GZIP = "gzip";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    public static enum TransactionType {
        JASON, CHAR,STREAM,BINARY
    }
    /**
     * Supported request methods.
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
    }
    private int mConnectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;
    private int mSocketTimeOut = DEFAULT_SOCKET_TIMEOUT;
    private int mMethod = Method.GET;
    private final String mUrl;
    private TransactionType mTransactionType = TransactionType.CHAR;
    private final HashMap<String,String> mUrlParams = new HashMap<String, String>();

    private FileWrapper mFileWrapper;
    private StreamWrapper mStreamWrapper;
    public HttpRequestParam(String url){
        mUrl = url;
    }

    public HttpRequestParam setRequestMethod(int method){
        mMethod = method;
        return this;
    }


    public HttpRequestParam setInputStream(String name,InputStream inputStream){
//        mStreamWrapper = StreamWrapper.newInstance(inputStream,name,APPLICATION_OCTET_STREAM,true);
        return this;
    }

    public HttpRequestParam setUploadFile(File file){
        mFileWrapper = new FileWrapper(file, APPLICATION_OCTET_STREAM);
        return this;
    }

    public HttpRequestParam putUrlParam(String key, String value){
        mUrlParams.put(key, value);
        return this;
    }

    public String parseUrlParam(){
        return "";
        //TODO
    }
    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] parseUrlRequestHeader(){
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : mUrlParams.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + "UTF-8", uee);
        }
    }

    public HttpRequestParam setTransactionType(TransactionType type){
        mTransactionType = type;
        return this;
    }

    public boolean useCache(){
        return false;//TODO
    }

    public ProxyWrapper getProxy(){
        return null;//TODO
    }

    public String getUrl(){
        return mUrl;
    }

    public int getMethod(){
        return mMethod;
    }

    public String getContentType(){
        return APPLICATION_JSON;//TODO
    }
    public int getSocketTimeOut(){
        return mSocketTimeOut;
    }
    public int getConnectTimeOut(){
        return mConnectionTimeOut;
    }
    public static class FileWrapper {
        public final File file;
        public final String contentType;
        private long mOffset, mLength, mSize;
        public FileWrapper(File file, String contentType, long offset, long length) {
            mOffset = offset;
            mLength = length;
            mSize = file.length();
            this.file = file;
            this.contentType = contentType;
        }
        public FileWrapper(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
            mOffset = 0;
            mSize = mLength = file.length();
        }
    }

    public static class ContentRangeWrapper{
        public long size;
        public long offset;
        public long length;
    }
    public static class StreamWrapper {
        public final InputStream inputStream;
        public ContentRangeWrapper inputRangeWrapper;
        public final OutputStream outputStream;
        public ContentRangeWrapper outputRangeWrapper;
        public final String name;
        public final String contentType;
        public final boolean autoClose;

        public StreamWrapper(InputStream inputStream, OutputStream outputStream, String name, String contentType, boolean autoClose) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.name = name;
            this.contentType = contentType;
            this.autoClose = autoClose;
        }

        static StreamWrapper newInstance(InputStream inputStream,OutputStream outputStream, String name, String contentType, boolean autoClose) {
            return new StreamWrapper(
                    inputStream,
                    outputStream,
                    name,
                    contentType == null ? APPLICATION_OCTET_STREAM : contentType,
                    autoClose);
        }
    }
    public static class ProxyWrapper{
        public final String address;
        public final String port;
        public final String name;
        public final String passwd;
        public ProxyWrapper(String address, String port,String name,String passwd){
            this.address = address;
            this.port = port;
            this.name = name;
            this.passwd = passwd;
        }
    }
}
