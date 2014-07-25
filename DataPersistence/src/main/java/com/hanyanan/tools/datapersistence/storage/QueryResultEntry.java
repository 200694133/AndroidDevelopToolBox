package com.hanyanan.tools.datapersistence.storage;

/**
 * Created by hanyanan on 2014/7/25.
 */
public class QueryResultEntry {
    private String mKey;
    private long mExpireTime;
    private Object mData;
    public QueryResultEntry(String key, long et, Object ob){
        mData = ob;
        mKey = key;
        mExpireTime = et;
    }
    public void setExpireTime(long t){
        mExpireTime = t;
    }
    public void setData(Object data){
        mData = data;
    }
    public long getExpireTime(){
        return mExpireTime;
    }

    public String getKey(){
        return mKey;
    }

    public Object getData(){
        return mData;
    }
}
