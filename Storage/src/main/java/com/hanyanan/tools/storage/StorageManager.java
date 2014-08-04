package com.hanyanan.tools.storage;

import android.content.Context;

import com.hanyanan.tools.storage.db.DatabaseHelper;
import com.hanyanan.tools.storage.db.DatabaseOperation;

import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2014/8/4.
 */
public class StorageManager {
    private static StorageManager sInstance = null;
    private DatabaseHelper mDatabaseHelper = null;
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
    public synchronized AsyncOperation getAsyncOperation(Type type,Context context){
        if(type == Type.DATABASE){
            if(null == mDatabaseHelper){
                mDatabaseHelper = new DatabaseHelper(context);
            }
            if(null == mDatabaseOperation){
                mDatabaseOperation  = new DatabaseOperation(mDatabaseHelper);
            }
        }else{
            //TODO
        }
    }




    private synchronized DatabaseHelper getDatabaseHelper(Context context){
        if(null != mDatabaseHelper){
            mDatabaseHelper = new DatabaseHelper(context);
        }
        return mDatabaseHelper;
    }
    private synchronized DatabaseOperation getDatabaseOperation(Context context){
        DatabaseHelper dh = getDatabaseHelper(context);
        return mDatabaseHelper;
    }
}
