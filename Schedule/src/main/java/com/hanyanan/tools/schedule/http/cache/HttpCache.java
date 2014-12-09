package com.hanyanan.tools.schedule.http.cache;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.HttpUtils;
import com.hanyanan.tools.storage.database.BasicDatabaseHelper;
import com.hanyanan.tools.storage.database.Entry;
import com.hanyanan.tools.storage.disk.DiskStorage;
import com.hanyanan.tools.storage.disk.LimitedSizeDiskStorage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2014/11/18.
 */
public class HttpCache {
    protected final DiskStorage mDiskStorage;
    protected final BasicDatabaseHelper mBasicDatabaseHelper;
    private final WeakHashMap<HttpRequest, HttpCacheHeader> mHttpCacheHeaderMap = new WeakHashMap<HttpRequest, HttpCacheHeader>();

    public HttpCache(Context context, File file, int size){
        mDiskStorage = LimitedSizeDiskStorage.open(file, size);
        mBasicDatabaseHelper = new BasicDatabaseHelper(context, file.getAbsolutePath()+"/cache.db");
    }


    public InputStream getContentInputStream(HttpRequest httpRequest){
        String key = httpRequest.getKey();
        return mDiskStorage.getInputStream(key);
    }

    public BasicHttpResponse get(HttpRequest httpRequest) throws HttpCacheDataLack, IOException {
        Cache.Mode mode = httpRequest.getCacheMode();
        if(mode == Cache.Mode.Disable){
            return null;
        }

        String key = httpRequest.getKey();
        checkHttpCacheHeader(httpRequest);
        if(mode == Cache.Mode.SimpleMode){
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) return null;
            HttpCacheHeader header = mHttpCacheHeaderMap.get(httpRequest);
            if(header == null){
                return createBaseHttpResponse(inputStream,inputStream.available(),"utf-8","*/*");
            }else {
                return createBaseHttpResponse(inputStream,header.contentLength,header.encoding, header.contentType);
            }
        }else if(mode == Cache.Mode.StrictMode){
            HttpCacheHeader header = mHttpCacheHeaderMap.get(httpRequest);
            if(null == header) throw new HttpCacheDataLack("Lack of http head info.");
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) return null;
            return createBaseHttpResponse(inputStream,header.contentLength,header.encoding, header.contentType);
        }else if(mode == Cache.Mode.Refresh){
            return null;
        }
        return null;
    }

    public HttpCacheHeader getHttpCacheHeader(HttpRequest httpRequest){
        String key = httpRequest.getKey();
        checkHttpCacheHeader(httpRequest);
        return mHttpCacheHeaderMap.get(httpRequest);
    }

    public boolean containHead(HttpRequest httpRequest){
        checkHttpCacheHeader(httpRequest);
        return null != mHttpCacheHeaderMap.get(httpRequest);
    }

    public boolean containContent(HttpRequest httpRequest){
        return mDiskStorage.contains(httpRequest.getKey());
    }

    public BasicHttpResponse put(HttpRequest httpRequest, BasicHttpResponse httpResponse) throws IOException{
        HttpRequest.HttpProgressListener httpProgressListener = httpRequest.getHttpProgressListener();

        Cache.Mode mode = httpRequest.getCacheMode();
        if(mode == Cache.Mode.Disable) return httpResponse;

        String key = httpRequest.getKey();
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        final HttpCacheHeader httpCacheHeader = parseCacheHeader(httpResponse);
        if(null == httpCacheHeader && mode == Cache.Mode.StrictMode){//cannot cache it.
            return httpResponse;
        }

        if(mode == Cache.Mode.StrictMode || mode == Cache.Mode.Refresh){//storage http head info
            Entry entry = new Entry();
            entry.key = key;
            entry.expire = httpCacheHeader.ttl;
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            entry.data = gson.toJson(httpCacheHeader);
            mBasicDatabaseHelper.put(entry);
        }
        mHttpCacheHeaderMap.put(httpRequest, httpCacheHeader);


        if(statusCode == HttpStatus.SC_NOT_MODIFIED){//just update head, and red from disk cache
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) {
                //TODO，可以设为默认的流
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            BasicHttpEntity res = new BasicHttpEntity();
            res.setContent(inputStream);
            res.setContentLength(entity.getContentLength());
            res.setContentEncoding(entity.getContentEncoding());
            res.setContentType(entity.getContentType());
            httpResponse.setEntity(res);
            entity.consumeContent();
            if(null != httpProgressListener) httpProgressListener.downloadProgress(1.0F);//update download progress
            return httpResponse;
        }

        if(statusCode>=200 && statusCode<300) {//cache content to disk cache
            //save to disk cache
            mDiskStorage.save(key,  httpResponse.getEntity().getContent(),
                    new ProgressCopier(httpRequest.getHttpProgressListener()),
                    httpResponse.getEntity().getContentLength()<=0?Integer.MAX_VALUE:httpResponse.getEntity().getContentLength(),
                    httpCacheHeader.ttl + 30 * 1000 * 60 * 60L);
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) {
                //TODO，可以设为默认的流
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            BasicHttpEntity res = new BasicHttpEntity();
            res.setContent(inputStream);
            res.setContentLength(entity.getContentLength());
            res.setContentEncoding(entity.getContentEncoding());
            res.setContentType(entity.getContentType());
            httpResponse.setEntity(res);
            entity.consumeContent();

            if(null != httpProgressListener) httpProgressListener.downloadProgress(1.0F);//update download progress
            return httpResponse;
        }

        return httpResponse;
    }


    public static BasicHttpResponse createBaseHttpResponse(InputStream inputStream, long length,
                                                           String encoding, String contentType){
        if(null == inputStream) return null;
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        BasicHttpResponse response = new BasicHttpResponse(protocolVersion, 200, "success");
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(inputStream);
        entity.setContentLength(length);
        entity.setContentEncoding(encoding);
        entity.setContentType(contentType);
        response.setEntity(entity);
        return response;
    }

    public static HttpCacheHeader parseCacheHeader(BasicHttpResponse response){
        Map<String, String> headers = HttpUtils.convertHeaders(response.getAllHeaders());
        long now = System.currentTimeMillis();

        long serverDate = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long maxAge = 0;
        boolean hasCacheControl = false;
        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        if (hasCacheControl) {
            softExpire = now + maxAge * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is softExpire.
            softExpire = now + (serverExpires - serverDate);
        }

        HttpCacheHeader entry = new HttpCacheHeader();
        HttpEntity entity = response.getEntity();
        entry.contentLength = entity.getContentLength();
        if(null != entity.getContentEncoding()) entry.encoding = entity.getContentEncoding().getValue();
        if(null != entity.getContentType()) entry.contentType = entity.getContentType().getValue();
        entry.eTag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = entry.softTtl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;
    }
    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    protected static BasicHttpResponse parseHttpResponse(final HttpCacheHeader httpCacheHeader, final InputStream inputStream){
        if(null == inputStream || httpCacheHeader == null) return null;

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        BasicHttpResponse response = new BasicHttpResponse(protocolVersion, 200, "success");
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(inputStream);
        entity.setContentLength(httpCacheHeader.contentLength);
        entity.setContentEncoding(httpCacheHeader.encoding);
        entity.setContentType(httpCacheHeader.contentType);

        if (null != httpCacheHeader.responseHeaders) {
            for (Map.Entry<String, String> header : httpCacheHeader.responseHeaders.entrySet()) {
                if (header.getKey() != null) {
                    Header h = new BasicHeader(header.getKey(), header.getValue());
                    response.addHeader(h);
                }
            }
        }
        response.setEntity(entity);

        return response;
    }

    protected void checkHttpCacheHeader(HttpRequest request){
        synchronized (this){
            HttpCacheHeader header = mHttpCacheHeaderMap.get(request);
            String key = request.getKey();
            if(null == header){
                Entry entry = mBasicDatabaseHelper.get(key);
                if(null == entry) return;
                if(System.currentTimeMillis() > entry.expire || TextUtils.isEmpty(entry.data)){//expiry the date
                    return ;
                }
                Gson gson = new Gson();
                header = gson.fromJson(entry.data,HttpCacheHeader.class);
                mHttpCacheHeaderMap.put(request, header);
            }
        }
    }
    /**
     * Data and metadata for an entry returned by the cache.
     */
    public static class HttpCacheHeader {
        @Expose
        public long contentLength;
        @Expose
        public String encoding;
        @Expose
        public String contentType;

        /** ETag for cache coherency. */
        @Expose public String eTag;

        /** Date of this response as reported by the server. */
        @Expose public long serverDate;

        /** TTL for this record. */
        @Expose public long ttl;

        /** Soft TTL for this record. */
        @Expose public long softTtl;

        /** Immutable response headers as received from server; must be non-null. */
        @Expose public Map<String,String> responseHeaders = new HashMap<String, String>();

        /** True if the entry is expired. */
        public boolean isExpired() {
            return this.ttl < System.currentTimeMillis();
        }

        /** True if a refresh is needed from the original data source. */
        public boolean refreshNeeded() {
            return this.softTtl < System.currentTimeMillis();
        }
    }

    public static class HttpCacheDataLack extends RuntimeException {
        public HttpCacheDataLack() {
            super();
        }

        public HttpCacheDataLack(java.lang.String detailMessage) {
            super(detailMessage);
        }

        public HttpCacheDataLack(java.lang.String detailMessage, java.lang.Throwable throwable) {
            super(detailMessage, throwable);
        }

        public HttpCacheDataLack(java.lang.Throwable throwable) {
            super(throwable);
        }
    }
}
