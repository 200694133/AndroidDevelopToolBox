package com.hanyanan.tools.cache;

import android.os.Parcel;
import android.text.TextUtils;

import java.io.InputStream;

/**
 * Created by hanyanan on 2014/7/9.
 */
public class Cache {
    public static final int DEFAULT_COUNT = 1024 * 1024;
    public static final long DEFAULT_MEM_SIZE = 4 * 1024 * 1024;//4M
    public static final long DEFAULT_DISK_SIZE = 50 * 1024 * 1024;//50M
    private final MemLruCache mMemCache;
    private final DiskLruCache mInternalDiskLruCache;
    private final DiskLruCache mExternalDiskCache;
    public Cache(long memSize, String internalPath, long internalSize,
                 String externalPath,long externalSize) {
        mMemCache = new MemLruCache(memSize);
        if(!TextUtils.isEmpty(internalPath)){
            mInternalDiskLruCache = new DiskLruCache(internalPath, 1,DEFAULT_COUNT, internalSize);
        }else{
            mInternalDiskLruCache = null;
        }
        if(!TextUtils.isEmpty(externalPath)){
            mExternalDiskCache = new DiskLruCache(externalPath, 1,DEFAULT_COUNT, externalSize);
        }else{
            mExternalDiskCache = null;
        }
    }

    public void put(String key, ICacheable cacheable){

    }

    public void remove(String key){

    }

    public void get(String key){

    }

    private ICache.IMemCacheListener mMemListener = new ICache.IMemCacheListener(){
        @Override
        public void onRemoved(String key, IMemCacheable cacheable) {

        }
    };
    private ICache.IDiskCacheListener mInternalDiskListener = new ICache.IDiskCacheListener(){
        @Override
        public void onRemoved(String key) {

        }
    };
    private ICache.IDiskCacheListener mExternalDiskListener = new ICache.IDiskCacheListener(){
        @Override
        public void onRemoved(String key) {

        }
    };
}
