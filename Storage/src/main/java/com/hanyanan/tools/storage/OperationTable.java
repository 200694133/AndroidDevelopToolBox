package com.hanyanan.tools.storage;

import java.lang.ref.WeakReference;

/**
 * Created by hanyanan on 2014/8/2.
 */
public class OperationTable {
    private final String mTag;
    private Filter mFilter;
    private final Type mType;
    private WeakReference<StorageManager> mWeakRef;
    OperationTable(Type type,String tag){
        mType = type;
        mTag = tag;
    }

    public void setStorageManager(StorageManager sm){
        mWeakRef = new WeakReference<StorageManager>(sm);
    }
    public StorageManager getStorageManager(){
        return mWeakRef.get();
    }

    public Operation getOperation(){
        return null;//TODO
    }

    public AsyncOperation getAsyncOperation(){
        return null;//TODO
    }

    public static <T> Entry<T> deliveryEntry(OperationTable table, String key, T data, long et,Encoder<T> encoder){
        Entry<T> entry = new Entry<T>(table.mTag,key,et,data);
        entry.mEncoder = encoder;
        return entry;
    }
    public static <T> Entry<T> deliveryEntry(OperationTable table, String key, T data, long et){
        return deliveryEntry(table,key,data, et, null);
    }
    public static <T> Entry<T> deliveryEntry(OperationTable table, String key, T data){
        return deliveryEntry(table,key,data, Long.MAX_VALUE, null);
    }
    public Type getType(){
        return mType;
    }
    public void setFilter(Filter filter){
        mFilter = filter;
    }
    public Filter getFilter(){ return mFilter; }
}
