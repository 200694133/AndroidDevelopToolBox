package com.hanyanan.tools.datapersistence;

import android.provider.ContactsContract;

/**
 * Created by Administrator on 2014/7/19.
 */
public class SimpleDataResult<T> implements IDataResult {
    private T mResult;
    private DataError mDataError;
    public SimpleDataResult(T result){
        mResult = result;
        mDataError = new DataError(DataError.SUCCESS, "success");
    }
    public SimpleDataResult(T result, DataError info){
        mResult = result;
        mDataError = info;
    }
    @Override
    public T getData() {
        return mResult;
    }

    @Override
    public DataError getDataError() {
        return mDataError;
    }
}
