package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/14.
 */
public class DataError {
    public static final int SUCCESS = 0x00;
    public static final int FAILED = 0x01;
    private int mCode;
    private String mInfo;
    public DataError(int code, String info){
        mCode = code;
        mInfo = info;
    }

    public DataError(String info){
        mCode = FAILED;
        mInfo = info;
    }
    public int getErrorCode(){
        return mCode;
    }

    public String getErrorInfo(){
        return mInfo;
    }
}
