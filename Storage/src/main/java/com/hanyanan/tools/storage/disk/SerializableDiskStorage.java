package com.hanyanan.tools.storage.disk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class SerializableDiskStorage {
    private final FixSizeDiskStorage mFixSizeDiskStorage;
    private final ExecutorService mService;
    SerializableDiskStorage(FixSizeDiskStorage driver, ExecutorService service){
        mService = service;
        mFixSizeDiskStorage = driver;
    }










}
