package com.hanyanan.tools.storage;

/**
 * Created by hanyanan on 2014/8/2.
 */
public interface Encoder<T> {
    public Entry<T> encode(Entry<T> entry);
    public Entry<T> decode(Entry<T> entry);
}
