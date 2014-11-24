package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestParam;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

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
        DEFAULT,JASON, CHAR,STREAM,BINARY
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
    private TransactionType mTransactionType = TransactionType.DEFAULT;
    private final HashMap<String,String> mUrlParams = new HashMap<String, String>();
    private final HashMap<String,String> mHeaderMaps = new HashMap<String, String>();
    private List<UpLoadWrapper> mUpLoadList;
    private ContentRangeWrapper mDownLoadContentRangeWrapper;
    public HttpRequestParam(String url){
        mUrl = url;
    }

    public HttpRequestParam setRequestMethod(int method){
        mMethod = method;
        return this;
    }
    private boolean isCacheCookie;
    public void setCacheCookie(boolean b){
        isCacheCookie = b;
    }
    public boolean cacheCookie(){
        return isCacheCookie;
    }
    public HttpRequestParam putUrlParam(String key, String value){
        mUrlParams.put(key, value);
        return this;
    }

    public String parseUrlParam(){
        return HttpUtils.encodeParam(mUrlParams);
    }

    public SSLSocketFactory getSSLSocketFactory(){
        return null;//TODO
    }
    public HashMap<String,String> getUrlPramsMaps(){
        return mUrlParams;
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
        switch (mTransactionType){
            case DEFAULT:
                return APPLICATION_DEFAULT;
            case JASON:
                return APPLICATION_JSON;
            case STREAM:
            case BINARY:
                return APPLICATION_OCTET_STREAM;
            case CHAR:
                return APPLICATION_JSON;
        }
        return APPLICATION_DEFAULT;
    }
    public int getSocketTimeOut(){
        return mSocketTimeOut;
    }
    public int getConnectTimeOut(){
        return mConnectionTimeOut;
    }

    public ContentRangeWrapper getDownLoadContentRangeWrapper(){
        return mDownLoadContentRangeWrapper;
    }
//    public static class FileWrapper {
//        public final File file;
//        public final String contentType;
//        private long mOffset, mLength, mSize;
//        public FileWrapper(File file, String contentType, long offset, long length) {
//            mOffset = offset;
//            mLength = length;
//            mSize = file.length();
//            this.file = file;
//            this.contentType = contentType;
//        }
//        public FileWrapper(File file, String contentType) {
//            this.file = file;
//            this.contentType = contentType;
//            mOffset = 0;
//            mSize = mLength = file.length();
//        }
//    }

    public List<UpLoadWrapper> getUploadWrappers(){
        return null;
    }

    public HttpRequestParam setHeaderProperty(String key, String value){
        mHeaderMaps.put(key, value);
        return this;
    }
    public HttpRequestParam setHeaderProperty(HashMap<String,String> property){
        mHeaderMaps.putAll(property);
        return this;
    }


    public HashMap<String,String> getHttpHeader(){
        return mHeaderMaps;
    }

    public static class UpLoadWrapper {
        public final ContentRangeWrapper contentRangeWrapper;
        public final InputStream inputStream;
        public final String name;
        public final String contentType;
        public final boolean autoClose;
        public UpLoadWrapper(InputStream inputStream){
            this.contentRangeWrapper = null;
            this.autoClose = false;
            this.contentType = "application/x-www-form-urlencoded;charset=utf-8";
            this.inputStream = inputStream;
            this.name = null;
        }
        public UpLoadWrapper(InputStream inputStream, ContentRangeWrapper rangeWrapper, String name, String contentType, boolean autoClose){
            this.contentRangeWrapper = rangeWrapper;
            this.autoClose = autoClose;
            this.contentType = contentType;
            this.inputStream = inputStream;
            this.name = name;
        }
    }
    public static class ContentRangeWrapper{
        public long size;
        public long offset;
        public long length;
        public ContentRangeWrapper(int offset, int length){
            this.offset = offset;
            this.length = length;
        }
    }
//    public static class DownLoadWrapper {
//        public final InputStream inputStream;
//        public ContentRangeWrapper inputRangeWrapper;
//        public final String name;
//        public final String contentType;
//        public final boolean autoClose;
//
//        public DownLoadWrapper(InputStream inputStream,String name, String contentType, boolean autoClose) {
//            this.inputStream = inputStream;
//            this.name = name;
//            this.contentType = contentType;
//            this.autoClose = autoClose;
//        }
//
//        public static DownLoadWrapper newInstance(InputStream inputStream, String name, String contentType, boolean autoClose) {
//            return new DownLoadWrapper(
//                    inputStream,
//                    name,
//                    contentType == null ? APPLICATION_OCTET_STREAM : contentType,
//                    autoClose);
//        }
//    }
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
