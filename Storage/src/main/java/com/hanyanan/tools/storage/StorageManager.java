package com.hanyanan.tools.storage;

import android.content.Context;
import com.hanyanan.tools.storage.db.DatabaseHelper;
import com.hanyanan.tools.storage.db.DatabaseOperation;
import com.hanyanan.tools.storage.disk.DiskStorage;
import com.hanyanan.tools.storage.disk.LimitedSizeDiskStorage;
import java.io.File;
import java.io.IOException;

/**
 * Created by hanyanan on 2014/8/4.
 */
public class StorageManager {
    private static StorageManager sInstance = null;
    private DiskStorage mDiskStorage;
    public static synchronized StorageManager getInstance(){
        if(null == sInstance) sInstance = new StorageManager();
        return sInstance;
    }
    private StorageManager(){
        //do nothing
    }


    public synchronized OperationTable generateOperationTable(String tag, Type type,Context context){
        OperationTable ot = new OperationTable(type,tag);
        ot.setStorageManager(this);
        return ot;
    }

    public synchronized DiskStorage getLimitedSizeDiskStorage(File root, long size) throws IOException {
        if(null != mDiskStorage) return mDiskStorage;
        mDiskStorage = LimitedSizeDiskStorage.open(root,size);
        return mDiskStorage;
    }

    public synchronized AsyncOperation getAsyncOperation(Context context, OperationTable ot){
        if(ot.getType() == Type.DATABASE){
            return new CommonAsyncOperation(ot.getOperation(context));
        }else{
            return null;//TODO
        }
    }

    public synchronized Operation getOperation(Context context, OperationTable ot){
        if(ot.getType() == Type.DATABASE){
            return getDatabaseOperation(context, ot);
        }else{
            return null;//TODO
        }
    }

    private synchronized DatabaseOperation getDatabaseOperation(Context context, OperationTable ot){
        DatabaseHelper dh = new DatabaseHelper(context);
        DatabaseOperation dod= new DatabaseOperation(dh,ot);
        return dod;
    }

    public void dispose() {
        if(null != mDiskStorage){
                mDiskStorage.close();
        }
        mDiskStorage = null;
        //TODO
    }
}
