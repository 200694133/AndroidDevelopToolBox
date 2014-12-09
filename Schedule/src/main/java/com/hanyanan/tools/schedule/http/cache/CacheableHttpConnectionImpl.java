package com.hanyanan.tools.schedule.http.cache;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.XLog;
import com.hanyanan.tools.schedule.http.HttpConnectionImpl;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.HttpUtils;
import com.hanyanan.tools.schedule.http.ServerError;
import com.hanyanan.tools.storage.database.BasicDatabaseHelper;
import com.hanyanan.tools.storage.database.Entry;
import com.hanyanan.tools.storage.disk.DiskStorage;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/11/14.
 */
public class CacheableHttpConnectionImpl extends HttpConnectionImpl {
    public BasicHttpResponse performUpLoadRequest(HttpRequest httpRequest)throws IOException{
        return super.performUpLoadRequest(httpRequest);
    }

    public BasicHttpResponse performSimpleRequest(HttpRequest httpRequest) throws IOException, ServerError{
        Cache.Mode mode = httpRequest.getCacheMode();
        if(mode == Cache.Mode.Disable) return super.performSimpleRequest(httpRequest);
        final HttpCache httpCache = httpRequest.getHttpCache();
        if(null == httpCache) return super.performSimpleRequest(httpRequest);

        if(mode == Cache.Mode.SimpleMode){
            try{
                BasicHttpResponse response = httpCache.get(httpRequest);
                if(null != response) return response;//read from disk cache
            }catch (HttpCache.HttpCacheDataLack httpCacheDataLack){
                XLog.e(httpRequest.getUrl()+" try get content from disk simple mode failed, exception is "+httpCacheDataLack.toString());
            }
            BasicHttpResponse response = super.performSimpleRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }else if(mode == Cache.Mode.StrictMode){
            BasicHttpResponse response = null;
            try{
                response = httpCache.get(httpRequest);
                if(null != response) return response;//read from disk cache
            }catch (HttpCache.HttpCacheDataLack httpCacheDataLack){
                XLog.e(httpRequest.getUrl()+" try get content from disk strict mode failed, exception is "+httpCacheDataLack.toString());
                response = super.performSimpleRequest(httpRequest);
                return httpCache.put(httpRequest, response);
            }

            HttpCache.HttpCacheHeader httpCacheHeader = httpCache.getHttpCacheHeader(httpRequest);
            if(null != httpCache) {
                HttpRequestParam param = httpRequest.getRequestParam();
                addCacheHeaders(param.getHttpHeader(), httpCacheHeader);
            }
            response = super.performSimpleRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }else if(mode == Cache.Mode.Refresh){
            final boolean hasHead = httpCache.containHead(httpRequest);
            final boolean hasContent = httpCache.containContent(httpRequest);
            HttpCache.HttpCacheHeader httpCacheHeader = httpCache.getHttpCacheHeader(httpRequest);
            if(hasHead && hasContent && null != httpCacheHeader){
                HttpRequestParam param = httpRequest.getRequestParam();
                addCacheHeaders(param.getHttpHeader(), httpCacheHeader);
            }
            BasicHttpResponse  response = super.performSimpleRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }else {
            //no need to cache content ,so it will not call progress listener to call invoke, it user
            // want to get the progress, please call it force.
            return super.performSimpleRequest(httpRequest);
        }
    }

    public BasicHttpResponse performDownLoadRequest(HttpRequest httpRequest)throws  IOException{
        Cache.Mode mode = httpRequest.getCacheMode();
        if(mode == Cache.Mode.Disable) return super.performDownLoadRequest(httpRequest);
        final HttpCache httpCache = httpRequest.getHttpCache();
        if(null == httpCache) return super.performDownLoadRequest(httpRequest);

        if(mode == Cache.Mode.SimpleMode){
            try{
                BasicHttpResponse response = httpCache.get(httpRequest);
                if(null != response) return response;//read from disk cache
            }catch (HttpCache.HttpCacheDataLack httpCacheDataLack){
                XLog.e(httpRequest.getUrl()+" try get content from disk simple mode failed, exception is "+httpCacheDataLack.toString());
            }
            BasicHttpResponse response = super.performDownLoadRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }else if(mode == Cache.Mode.StrictMode){
            BasicHttpResponse response = null;
            try{
                response = httpCache.get(httpRequest);
                if(null != response) return response;//read from disk cache
            }catch (HttpCache.HttpCacheDataLack httpCacheDataLack){
                XLog.e(httpRequest.getUrl()+" try get content from disk strict mode failed, exception is "+httpCacheDataLack.toString());
                response = super.performDownLoadRequest(httpRequest);
                return httpCache.put(httpRequest, response);
            }

            HttpCache.HttpCacheHeader httpCacheHeader = httpCache.getHttpCacheHeader(httpRequest);
            if(null != httpCache) {
                HttpRequestParam param = httpRequest.getRequestParam();
                addCacheHeaders(param.getHttpHeader(), httpCacheHeader);
            }
            response = super.performDownLoadRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }else if(mode == Cache.Mode.Refresh){
            final boolean hasHead = httpCache.containHead(httpRequest);
            final boolean hasContent = httpCache.containContent(httpRequest);
            HttpCache.HttpCacheHeader httpCacheHeader = httpCache.getHttpCacheHeader(httpRequest);
            if(hasHead && hasContent && null != httpCacheHeader){
                HttpRequestParam param = httpRequest.getRequestParam();
                addCacheHeaders(param.getHttpHeader(), httpCacheHeader);
                param.setHeaderProperty("Cache-Control","max-age=0");
            }
            BasicHttpResponse response = super.performDownLoadRequest(httpRequest);
            return httpCache.put(httpRequest, response);
        }

        return super.performDownLoadRequest(httpRequest);
    }

    private void addCacheHeaders(Map<String, String> headers, HttpCache.HttpCacheHeader entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return;
        }

        if (entry.eTag != null) {
            headers.put("If-None-Match", entry.eTag);
        }

        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }
}
