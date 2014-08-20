package com.hanyanan.tools.storage.disk;

import java.util.concurrent.ExecutorService;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class SerializableDiskStorage {
    private final FixSizeDiskStorageImpl mFixSizeDiskStorage;
    private final ExecutorService mService;
    SerializableDiskStorage(FixSizeDiskStorageImpl driver, ExecutorService service){
        mService = service;
        mFixSizeDiskStorage = driver;
    }










}
