package com.hanyanan.tools.storage;

import android.content.Context;

import com.hanyanan.tools.storage.db.DatabaseHelper;
import com.hanyanan.tools.storage.db.DatabaseOperation;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2014/8/4.
 */
public class StorageManager {
    private static StorageManager sInstance = null;
    private DatabaseOperation mDatabaseOperation = null;
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

    public synchronized FixSizeDiskStorage getFixSizeDiskStorage(File root, long size) throws IOException {
        return FixSizeDiskStorage.open(root,1,size);
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
        DatabaseOperation dop = new DatabaseOperation(dh,ot);
        return dop;
    }

    public void dispose(){
        //TODO
    }
}
