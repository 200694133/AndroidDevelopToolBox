package com.hanyanan.tools.datapersistence.db;

/**
 * Created by hanyanan on 2014/7/21.
 */
interface BlobType {
    public byte[] getData();
    public long getExpireTime();
}
