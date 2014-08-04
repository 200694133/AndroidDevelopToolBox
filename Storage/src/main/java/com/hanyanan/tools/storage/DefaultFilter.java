package com.hanyanan.tools.storage;

/**
 * Created by hanyanan on 2014/8/2.
 */
public class DefaultFilter<T> implements Filter {
    @Override
    public boolean isValid(Entry entry) {
        return true;
    }
}
