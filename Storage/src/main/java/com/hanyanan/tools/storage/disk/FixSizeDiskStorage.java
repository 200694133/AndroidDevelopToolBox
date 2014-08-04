package com.hanyanan.tools.storage.disk;

import com.hanyanan.tools.storage.StorageLog;
import com.hanyanan.tools.storage.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hanyanan on 2014/7/28.
 */
public class FixSizeDiskStorage extends FlexibleDiskStorage {
    private final static long DEFAULT_MAX_SIZE = 1024 * 1024 * 10;//10M

    private final long mMaxSize;
    private long mCurrSize = 0;
    private FixSizeDiskStorage(File directory, int appVersion, long size) {
        super(directory, appVersion);
        mMaxSize = size;
    }

    private FixSizeDiskStorage(File directory, int appVersion) {
        this(directory, appVersion, DEFAULT_MAX_SIZE);
    }
    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param directory a writable directory
     * @throws java.io.IOException if reading or writing the cache directory fails
     */
    public static FixSizeDiskStorage open(File directory, int appVersion,long size)
            throws IOException {
        // If a bkp file exists, use it instead.
        File backupFile = new File(directory, JOURNAL_FILE_BACKUP);
        if (backupFile.exists()) {
            File journalFile = new File(directory, JOURNAL_FILE);
            // If journal file also exists just delete backup file.
            if (journalFile.exists()) {
                backupFile.delete();
            } else {
                renameTo(backupFile, journalFile, false);
            }
        }

        // Prefer to pick up where we left off.
        FixSizeDiskStorage cache = new FixSizeDiskStorage(directory, appVersion, size);
        if (cache.journalFile.exists()) {
            try {
                cache.readJournal();
                cache.processJournal();
                cache.journalWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(cache.journalFile, true),
                                Utils.ASCII_CHARSET));
                cache.trimToSizeIfNeed();
                return cache;
            } catch (IOException journalIsCorrupt) {
                StorageLog.e("DiskLruCache " + directory + " is corrupt: "
                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // Create a new empty cache.
        directory.mkdirs();
        cache = new FixSizeDiskStorage(directory, appVersion,size);
        cache.rebuildJournal();
        cache.trimToSizeIfNeed();
        return cache;
    }

    protected void onEntryChanged(String key, long oldLength, long currLength){
        mCurrSize = mCurrSize - oldLength + currLength;
        trimToSizeIfNeed();
    }
    protected void onEntryRemoved(String key, long length){
        mCurrSize = mCurrSize - length;
        trimToSizeIfNeed();
    }
    protected void onEntryAdded(String key, long length){
        mCurrSize = mCurrSize + length;
        trimToSizeIfNeed();
    }
    protected void onEntryClear(){
        mCurrSize = 0;
    }

    public long getCurrSize(){
        return mCurrSize;
    }
    public long getMaxSize(){
        return mMaxSize;
    }
    private void trimToSizeIfNeed(){
        synchronized (this){
            if(mCurrSize > mMaxSize){
                trimToSize();
            }
        }
    }
    private void trimToSize(){
        Iterator<Map.Entry<String, Entry>> iterator = lruEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Entry> entry = iterator.next();
            Entry e = entry.getValue();
            iterator.remove();
            try {
                remove(e.getKey());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (StorageLog.DEBUG) {
                StorageLog.v("trimToSize() remove "+e.getKey());
            }
            if (mCurrSize <= mMaxSize) {
                break;
            }
        }
    }
}
