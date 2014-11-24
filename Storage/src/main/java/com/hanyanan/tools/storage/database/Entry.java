package com.hanyanan.tools.storage.database;

/**
 * Created by hanyanan on 2014/11/6.
 */
public class Entry {
    public String key;

    public long expire;

    public String data;

    public String getKey(){
        return key;
    }

    public long getExpire(){
        return expire;
    }

    public String getData(){
        return data;
    }
}
