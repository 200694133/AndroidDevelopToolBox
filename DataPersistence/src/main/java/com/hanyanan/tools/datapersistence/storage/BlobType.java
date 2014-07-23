package com.hanyanan.tools.datapersistence.storage;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface BlobType {
    public byte[] getData();
    public long getExpireTime();
}
