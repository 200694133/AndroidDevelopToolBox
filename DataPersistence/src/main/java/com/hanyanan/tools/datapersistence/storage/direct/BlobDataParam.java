package com.hanyanan.tools.datapersistence.storage.direct;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface BlobDataParam {
    public byte[] getData();
    public long getExpireTime();
}
