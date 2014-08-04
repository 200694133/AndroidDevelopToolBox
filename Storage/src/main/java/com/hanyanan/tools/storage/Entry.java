package com.hanyanan.tools.storage;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/8/2.
 */
public final class Entry<T> {
    public Encoder<T> mEncoder;
    /** expire time */
    public long mExpireTime;
    /** Primary key of current entry */
    public String mPrimaryKey;
    /** Secondary key of current entry */
    public String mSecondaryKey;
    /** data */
    public T mData;
    /** time stamp */
    public long mTimeStamp;

    public Entry(){

    }
    public Entry(String pk, String sk, long et, T data){
        mPrimaryKey = pk;
        mSecondaryKey = sk;
        mExpireTime = et;
        mData = data;
        mTimeStamp = System.currentTimeMillis();
    }


    public void setKey(String key){
        String ss[] = key.split("_");
        mPrimaryKey = ss[0];
        mSecondaryKey = ss[1];
    }
    public String getKey(){
        return Utils.generatorKey(mPrimaryKey, mSecondaryKey);
    }
}
