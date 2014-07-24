package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/24.
 */
public interface IAsyncResult<T> {
    public void onResult(String key, IResult<T> result);
}
