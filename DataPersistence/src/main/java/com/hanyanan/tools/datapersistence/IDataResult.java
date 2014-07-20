package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/14.
 */
public interface IDataResult<T> {
    public T getData();

    public DataError getDataError();
}
