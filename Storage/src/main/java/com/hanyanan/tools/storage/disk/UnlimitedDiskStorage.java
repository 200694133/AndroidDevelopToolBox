package com.hanyanan.tools.storage.disk;

import java.io.File;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class UnlimitedDiskStorage extends BasicDiskStorage {
    private UnlimitedDiskStorage(File rootDirectory) {
        super(rootDirectory);
    }

    public synchronized UnlimitedDiskStorage open(File rootDirectory){
        return new UnlimitedDiskStorage(rootDirectory);
    }
}
