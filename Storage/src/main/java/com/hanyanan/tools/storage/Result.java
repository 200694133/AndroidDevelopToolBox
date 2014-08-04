package com.hanyanan.tools.storage;

import com.hanyanan.tools.storage.Error.Error;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/8/3.
 */
public class Result<T> {
    public interface OnResultListener<D>{
        public void onResult(Result<D> result);
    }
    public Entry<T> mResult;
    public Error mErrorInfo;
    public Result(Entry<T> data){
        mResult = data;
        mErrorInfo = null;
    }
    public Result(Error error){
        mErrorInfo = error;
        mResult = null;
    }

    public static <T> Result<T> success(Entry<T> data){
        return new Result<T>(data);
    }

    public static Result failed(Error error){
        return new Result(error);
    }
}
