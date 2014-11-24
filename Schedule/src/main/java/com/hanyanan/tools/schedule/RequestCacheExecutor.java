package com.hanyanan.tools.schedule;

import java.io.InputStream;

/**
 * Created by hanyanan on 2014/10/30.
 */
public interface RequestCacheExecutor {
    public boolean cacheable(HttpCacheExecutor request);

    public InputStream get(HttpCacheExecutor request);

    public void put(InputStream inputStream);

    public void remove(Request request, String key);
}
