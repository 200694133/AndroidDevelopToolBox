package com.hanyanan.tools.storage.disk;

import com.hanyanan.tools.storage.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by hanyanan on 2014/8/22.
 */
public class LimitedSizeDiskStorage extends BasicDiskStorage {
    private LimitedSizeDiskStorage(IStreamStorage streamStorage) {
        super(streamStorage);
    }

    public synchronized static LimitedSizeDiskStorage open(File rootDirector){
        return open(rootDirector, Utils.DEFAULT_DISK_SIZE);
    }

    public synchronized static LimitedSizeDiskStorage open(File rootDirector, long size){
        try {
            FixSizeDiskStorageImpl fixSizeDiskStorage = FixSizeDiskStorageImpl.open(rootDirector,VERSION,size);
            return new LimitedSizeDiskStorage(fixSizeDiskStorage);
        } catch (IOException e) {
            return null;
        }
    }
}
