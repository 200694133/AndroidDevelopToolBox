package com.hanyanan.tools.schedule;

import com.hanyanan.tools.schedule.http.HttpRequest;

import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;

/**
 * Created by hanyanan on 2014/8/1.
 * Cache policy for a request.
 */
public interface HttpCacheExecutor {
    public String getCacheKey();
    /** Whether or not responses to this request should be cached. */
    public  boolean shouldCache();
    /** Get currently cache mode. */
    public Cache.Mode getCacheMode();
    /** Whether or not need refresh current request. */
    public boolean needRefresh(HttpRequest request);
    /** Whether or not need post current cache. */
    public boolean needPostOutdatedData(HttpRequest request);
    /** Is current request miss in cache */
    public boolean missInCache(HttpRequest request);
    /** get input stream from cache. */
    public BasicHttpResponse readFromCache(HttpRequest request);

    public boolean isExpired(HttpRequest request);

    /**
     * save current input stream to cache.
     * @param httpResponse http response
     * @throws XError
     */
    public BasicHttpResponse cache(HttpRequest request, BasicHttpResponse httpResponse) throws IOException;
    /** Remove current from cache. */
    public void clear(HttpRequest request);

    public void invalidate(HttpRequest request);

    /** Close current executor. */
    public void close();
}
