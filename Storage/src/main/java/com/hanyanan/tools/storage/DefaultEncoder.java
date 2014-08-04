package com.hanyanan.tools.storage;

/**
 * Created by hanyanan on 2014/8/2.
 */
public class DefaultEncoder<T> implements Encoder {
    @Override
    public Entry encode(Entry entry) {
        return entry;
    }

    @Override
    public Entry decode(Entry entry) {
        return entry;
    }
}
