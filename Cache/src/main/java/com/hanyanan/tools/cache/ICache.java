package com.hanyanan.tools.cache;

public interface ICache {
	public enum CacheType{
        MEMORY,
        DISK
    }
    /** get current cache type */
    public CacheType getType();

	public ICacheable get(String key);
	
	public ICacheable put(String key, ICacheable value);
	
	public ICacheable pull(String key);

    public void clear();

	public void reSize(long newMaxSize);

    public void addCacheTypeListener(ICacheTypeListener listener);

    public interface ICacheTypeListener{

        public void onRemoved(String key);
    }
}

