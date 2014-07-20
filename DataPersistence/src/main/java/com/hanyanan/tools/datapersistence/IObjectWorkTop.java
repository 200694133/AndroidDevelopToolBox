package com.hanyanan.tools.datapersistence;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/18.
 */
public interface IObjectWorkTop {
    /**
     * put new or update the value, it's return the previous value
     * @param key key storage
     * @param value new content to storage
     * @return stale value, null means that it's first time to insert
     */
    public IDataResult put(String key, final Serializable value);

    /**
     * put new content to storage
     * @param key key storage
     * @param listener the listener to get the current content
     */
    public void putAsync(String key, final Serializable value, IOnObjectResult listener);

    /**
     * remove object from storage
     * @param key key storage
     * @return stable value
     */
    public IDataResult remove(String key,  Class clazz);

    /**
     *
     * @param key
     * @param listener
     */
    public void removeAsync(String key, Class clazz, IOnObjectResult listener);

    public IDataResult get(String key, Class clazz);

    public void getAsync(String key, Class clazz, IOnObjectResult listener);

    public interface IDataChangeObserver<T extends Serializable>{
        public void onDataChanged(String key, T curr, T prev);
    }

    public interface IOnObjectResult<T extends Serializable>{
        public void onResult(String key, IDataResult result);
    }
}
