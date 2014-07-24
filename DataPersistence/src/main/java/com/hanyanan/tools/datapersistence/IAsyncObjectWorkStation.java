package com.hanyanan.tools.datapersistence;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/23.
 */
public interface IAsyncObjectWorkStation {
    /**
     * put new content to storage
     * @param key key storage
     * @param listener the listener to get the current content
     */
    public void putAsync(String key, final Serializable value, long expireTime, IAsyncResult listener);

    /**
     *
     * @param key
     * @param listener
     */
    public void removeAsync(String key, IAsyncResult listener);

    public void getAsync(String key, IAsyncResult listener);
}
