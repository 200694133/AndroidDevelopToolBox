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
    public void putAsync(String key, final Serializable value, IOnObjectResult listener);

    /**
     *
     * @param key
     * @param listener
     */
    public void removeAsync(String key, IOnObjectResult listener);

    public void getAsync(String key, IOnObjectResult listener);


    public interface IOnObjectResult<T extends Serializable>{
        public void onResult(String key, IResult result);
    }
}
