package com.hanyanan.tools.cache;

public interface ICache {
	public enum CacheType{
        MEMORY,
        DISK
    }
    /** get current cache type */
    public CacheType getType();

	public IDiskCacheable get(String key);
	
	public IDiskCacheable put(String key, ICacheable value);
	
	public IDiskCacheable remove(String key);

    public void clear();

	public void reSize(long newMaxSize);

    public void addCacheTypeListener(ICacheTypeListener listener);

    public interface ICacheTypeListener{

        public void onRemovedForever(String key);

        public void onCacheTypeChanged(String key, CacheType prevType, CacheType newType);
    }
}

