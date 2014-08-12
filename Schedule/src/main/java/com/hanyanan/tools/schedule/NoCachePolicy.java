package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/8/1.
 */
public class NoCachePolicy implements CachePolicy{
    @Override
    public String getCacheKey() {
        return null;
    }

    @Override
    public boolean skipCache() {
        return false;
    }

    @Override
    public boolean shouldCache() {
        return false;
    }

    @Override
    public boolean needRefresh() {
        return true;
    }

    @Override
    public boolean canReadFromCache() {
        return false;
    }

    @Override
    public boolean missInCache() {
        return false;
    }
}
