package com.hanyanan.tools.datapersistence;

/**
 * Created by Administrator on 2014/7/19.
 */
public class SimpleResult<T> implements IResult {
    private T mResult;
    private DataError mDataError;
    public SimpleResult(T result){
        mResult = result;
        mDataError = new DataError(DataError.SUCCESS, "success");
    }
    public SimpleResult(T result, DataError info){
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
