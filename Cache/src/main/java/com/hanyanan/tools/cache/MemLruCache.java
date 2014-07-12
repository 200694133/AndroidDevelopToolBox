package com.hanyanan.tools.cache;

import java.util.LinkedHashMap;
import java.util.Map;


public class MemLruCache {
	private static final String TAG = MemLruCache.class.getSimpleName();
	private transient LinkedHashMap<String, IMemCacheable> mCacheMap = new LinkedHashMap<String, IMemCacheable>(0, 0.75f, true);
	/** Size of this cache in units. Not necessarily the number of elements. */
    private long mCurrSize = -1;
    private long mMaxSize = -1;
	
    private int mPutCount;
    private int mEvictionCount;
    private int mHitCount;
    private int mMissCount;
    private ICache.ICacheTypeListener mCacheListener = null;
	
    /**
     * @param maxSize for caches in memory, this is the maximum sum of the sizes of the entries in this cache.
     */
    public MemLruCache(long maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        mMaxSize = maxSize;
    }
    
    public String toString(){
    	return new String("Put Count: "+mPutCount+" ,  Eviction Count"+mEvictionCount+" ,  Hit Count "+mHitCount+" ,  Miss Count"+mMissCount);
    }
    
    protected void onEntryRemove(String key, IMemCacheable data){
    	if(null != mCacheListener){
            mCacheListener.onRemoved(key);
        }
    }
    
    /**
     * Remove the eldest entries until the total of remaining entries is at or
     * below the requested size.
     *
     * @param maxSize the maximum size of the cache before returning. May be -1
     *            to evict even 0-sized elements.
     * @return
     */
    public void trimToSize(long maxSize) {
    	while (true) {
            String key = null;
            IMemCacheable value = null;
    		synchronized (this) {
    			if (mCurrSize < 0 || (mCacheMap.isEmpty() && mCurrSize != 0)) {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }

                if (mCurrSize <= maxSize || mCacheMap.isEmpty()) {
                    break;
                }
                
                Map.Entry<String, IMemCacheable> toEvict = mCacheMap.entrySet().iterator().next();
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();
                mCacheMap.remove(key);
                mCurrSize -= safeSizeOf(key, value);
                ++mEvictionCount;
        		System.out.println(TAG+" trimToSize remove "+key+" , value "+value);
    		}
    		if(null != value && null != key) onEntryRemove(key, value);
    	}
    }
    
    private long safeSizeOf(String key, IMemCacheable value) {
        long result = value.sizeOf();
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

	public IMemCacheable get(String key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

        IMemCacheable mapValue;
		synchronized (this) {
			mapValue = mCacheMap.get(key);
			if (mapValue != null) {
				mHitCount++;
				return mapValue;
			}
			mMissCount++;
		}
		return null;
	}

	public IMemCacheable put(String key, IMemCacheable value) {
		if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        IMemCacheable previous = null;
        synchronized (this) {
            mPutCount++;
            mCurrSize += safeSizeOf(key, value);
            previous = mCacheMap.put(key, value);
            if (previous != null) {
            	mCurrSize -= safeSizeOf(key, previous);
            }
        }

        trimToSize(mMaxSize);
        return previous;
	}
	
	public IMemCacheable remove(String key){
		if (key == null) {
            throw new NullPointerException("key == null");
        }

        IMemCacheable previous = null;
        synchronized (this) {
            previous = mCacheMap.remove(key);
            if (previous != null) {
                mCurrSize -= safeSizeOf(key, previous);
            }
        }

        return previous;
	}

	public void reSize(long newMaxSize) {
		if (newMaxSize <= 0) {
            throw new IllegalArgumentException("newMaxSize <= 0");
        }

        synchronized (this) {
            mMaxSize = newMaxSize;
        }
        trimToSize(newMaxSize);
	}
}
