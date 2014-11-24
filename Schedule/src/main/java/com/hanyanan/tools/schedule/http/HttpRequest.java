package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.HttpCacheExecutor;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.http.cache.HttpCache;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class HttpRequest extends Request<HttpRequestParam>{
    private Cache.Mode mCacheMode = Cache.Mode.Disable;
    private HttpCache mHttpCache;
    public HttpRequest(RequestQueue requestQueue,
                       RequestExecutor requestExecutor, HttpRequestParam param) {
        super(requestQueue,requestExecutor,param);
    }

    public HttpRequest(RequestQueue requestQueue,
                       RequestExecutor requestExecutor, HttpRequestParam param, Cache.Mode mode) {
        super(requestQueue,requestExecutor,param);
        mCacheMode = mode;
    }

    public HttpRequest setHttpCache(HttpCache httpCache){
        this.mHttpCache = httpCache;
        return this;
    }

    public HttpCache getHttpCache(){
        return mHttpCache;
    }
    public HttpRequest setCacheMode(Cache.Mode mode){
        mCacheMode = mode;
        return this;
    }

    public Cache.Mode getCacheMode(){
        return mCacheMode;
    }
    public String getKey(){
        return String.valueOf(Math.abs(getRequestParam().getUrl().hashCode()));
    }

    public String getUrl(){
        return getRequestParam().getUrl();
    }
}
