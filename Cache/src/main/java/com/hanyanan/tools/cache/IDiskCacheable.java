package com.hanyanan.tools.cache;

/**
 * Created by hanyanan on 2014/7/8.
 * The basic interface for cacheable model. Any class/object want to store in disk must implement
 * this interface.
 */
public interface IDiskCacheable extends ICacheable{
    /**
     * storage class to byte
     * @return result bytes of current object to store at disk.
     */
    public byte[] toBytes();

    /**
     * update current object by bytes.
     * @param bytes
     */
    public void setContent(byte[] bytes);
}
