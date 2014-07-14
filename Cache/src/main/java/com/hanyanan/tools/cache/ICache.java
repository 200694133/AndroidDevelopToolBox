package com.hanyanan.tools.cache;

public interface ICache {
	public enum CacheType{
        MEMORY,
        DISK
    }

    public interface IMemCacheListener{
        public void onRemoved(String key, IMemCacheable cacheable);
    }
    public interface  IDiskCacheListener{
        public void onRemoved(String key);
    }
}

