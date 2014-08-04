package com.hanyanan.tools.storage;

/**
 * Created by hanyanan on 2014/8/2.
 * The interface used to filter the invalid entry.
 */
public interface Filter{
    /**
     * Check if the current entry is valid.
     * @param entry entry need checking
     * @return true means that it's valid, other wise it's invalid.
     */
    public boolean isValid(Entry entry);
}
