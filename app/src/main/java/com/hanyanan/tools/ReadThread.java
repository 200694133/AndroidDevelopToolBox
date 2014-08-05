package com.hanyanan.tools;

import android.content.Context;
import android.util.Log;

import com.hanyanan.tools.storage.OperationTable;
import com.hanyanan.tools.storage.StorageManager;
import com.hanyanan.tools.storage.Type;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/5.
 */
public class ReadThread extends Thread{
    private static final String TAG = "TestStorage";
    Context mContext;
    public ReadThread(Context context){
        mContext = context;
    }
    public void run(){
        Log.d(TAG, "read start");
        int tagCount = 100;
        Log.d(TAG, "read tag count "+tagCount);
        final HashMap<String, OperationTable> tableMap = new HashMap<String, OperationTable>();
        for(int i =0;i<tagCount;++i){
            String t = ""+i;
            OperationTable ot = StorageManager.getInstance().generateOperationTable(t, Type.DATABASE,mContext);
            tableMap.put(t,ot);
            HashMap<String, Class> clazzMap = new HashMap<String, Class>();

            for(int m=0;m<tagCount;++m){
                String key = ""+m;
                clazzMap.put(key, Serializable.class);
            }
            HashMap<String,Object> data = ot.getOperation(mContext).getAll(clazzMap);
            for(int m=0;m<tagCount;++m){
                String key = ""+m;
                Log.d(TAG, t +"_"+key+" = "+data.get(key).toString()+" class "+data.get(key).getClass());
            }
        }

        Log.d(TAG, "read end");




    }
}
