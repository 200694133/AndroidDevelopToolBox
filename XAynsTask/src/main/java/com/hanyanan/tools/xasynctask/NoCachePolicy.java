package com.hanyanan.tools.xasynctask;

/**
 * Created by hanyanan on 2014/8/1.
 */
public class NoCachePolicy implements CachePolicy{
    @Override
    public boolean shouldCache() {
        return false;
    }

    @Override
    public boolean needRefresh() {
        return true;
    }

    @Override
    public boolean cacheEnable() {
        return false;
    }
}
