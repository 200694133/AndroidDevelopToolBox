package com.hanyanan.tools.xasynctask;

/**
 * Created by hanyanan on 2014/8/1.
 * Cache policy for a request.
 */
public interface CachePolicy {
    /**
     * Get the cache key for cache operation.
     * @return cache key
     */
    public String getCacheKey();
    /** skip cache model, do not read and write current response to cache. */
    public boolean skipCache();
    /** Whether or not responses to this request should be cached. */
    public boolean shouldCache();
    /** Whether or not need refresh current request. */
    public boolean needRefresh();
    /** Whether or not could read data from cache pool. */
    public boolean canReadFromCache();
    /** Is current request miss in cache */
    public boolean missInCache();
}
