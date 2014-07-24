package com.hanyanan.tools.datapersistence;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/23.
 */
public interface IObjectWorkStation {
    /**
     * put new or update the value, it's return the previous value
     * @param key key storage
     * @param value new content to storage
     * @param expireTime expire time
     * @return stale value, null means that it's first time to insert
     */
    public IResult put(String key, final Serializable value, final long expireTime);

    /**
     * remove object from storage
     * @param key key storage
     * @return stable value
     */
    public IResult remove(String key);

    public IResult get(String key);
}
