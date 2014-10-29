package com.hanyanan.tools.storage.disk;

/**
 * Created by hanyanan on 2014/10/22.
 * Used to check valid.
 */
public interface Authority {
    public boolean isValid(long expireTime);
}
