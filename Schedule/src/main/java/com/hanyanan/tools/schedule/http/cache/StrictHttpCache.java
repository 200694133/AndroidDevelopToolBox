package com.hanyanan.tools.schedule.http.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.hanyanan.tools.schedule.XLog;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.HttpUtils;
import com.hanyanan.tools.storage.database.BasicDatabaseHelper;
import com.hanyanan.tools.storage.database.Entry;
import com.hanyanan.tools.storage.disk.DiskStorage;
import com.hanyanan.tools.storage.disk.LimitedSizeDiskStorage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/11/17.
 */
@Deprecated
public class StrictHttpCache {
    protected final DiskStorage mDiskStorage;
    protected final BasicDatabaseHelper mBasicDatabaseHelper;
    protected HttpCacheHeader mHttpCacheHeader;
    protected boolean hasReadCacheHead = false;

    public StrictHttpCache(Context context, File file, int size){
        String packageName = context.getPackageName();
        mDiskStorage = LimitedSizeDiskStorage.open(file, size);
        mBasicDatabaseHelper = new BasicDatabaseHelper(context, file.getAbsolutePath()+"/cache.db");
    }

    public StrictHttpCache(DiskStorage diskStorage, BasicDatabaseHelper db){
        mDiskStorage = diskStorage;
        mBasicDatabaseHelper = db;
    }

    public BasicHttpResponse get(String key) throws IOException{
        checkHttpCacheHeader(key);
        if(mDiskStorage.contains(key) && null != mHttpCacheHeader && !mHttpCacheHeader.isExpired()){
                //read from cache, and return the result
                return parseHttpResponse(mHttpCacheHeader, mDiskStorage.getInputStream(key));
        }
        return null;
    }


    public BasicHttpResponse put(String key, BasicHttpResponse httpResponse) throws IOException {
        checkHttpCacheHeader(key);

        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        HttpCacheHeader httpCacheHeader = parseCacheHeader(httpResponse);

        if(null != httpCacheHeader){//support cache, should store on data base
            mHttpCacheHeader = httpCacheHeader;
            //update head info
            Entry entry = new Entry();
            entry.key = key;
            entry.expire = httpCacheHeader.ttl;
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            entry.data = gson.toJson(httpCacheHeader);
            mBasicDatabaseHelper.put(entry);
        }else{//cannot cache on disk,so just return the real http response.
            return httpResponse;
        }

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
            return httpResponse;
        }

        if(statusCode>=200 && statusCode<300) {//cache content to disk cache
            //save to disk cache
             mDiskStorage.save(key, httpResponse.getEntity().getContent(), httpCacheHeader.ttl + 30 * 1000 * 60 * 60L);
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
             return httpResponse;
        }
        return httpResponse;
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
        entry.encoding = entity.getContentEncoding();
        entry.contentType = entity.getContentType();
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

    protected void checkHttpCacheHeader(String key){
        synchronized (this){
            if(hasReadCacheHead) return ;
            hasReadCacheHead = true;
            if(null == mHttpCacheHeader){
                Entry entry = mBasicDatabaseHelper.get(key);
                if(null == entry) return;
                if(System.currentTimeMillis() > entry.expire || TextUtils.isEmpty(entry.data)){//expiry the date
                    return ;
                }
                Gson gson = new Gson();
                HttpCacheHeader header = gson.fromJson(entry.data,HttpCacheHeader.class);
                mHttpCacheHeader = header;
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
        public Header encoding;
        @Expose
        public Header contentType;

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
}
