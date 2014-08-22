package com.hanyanan.tools.storage.disk;

/**
 * Created by hanyanan on 2014/8/22.
 */
public interface Abortable {
    public void abort()throws java.io.IOException;
}
