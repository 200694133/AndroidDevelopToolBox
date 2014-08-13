package com.hanyanan.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.hanyanan.tools.magicbox.view.NetworkImageView;
import com.hanyanan.tools.storage.OperationTable;
import com.hanyanan.tools.storage.StorageManager;
import com.hanyanan.tools.storage.Type;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

/**
 * Created by hanyanan on 2014/8/5.
 */
public class MainActivity extends Activity{
    public void onCreate(Bundle data){
        super.onCreate(data);
        setContentView(R.layout.main_activity);
        NetworkImageView imageView = (NetworkImageView) this.findViewById(R.id.iv);
        imageView.setUrl("http://file21.mafengwo.net/M00/A5/E7/wKgB21A2RZfh_kgvAAq7E33m80s94.jpeg");
//        writeThread.start();
//        new ReadThread(this).start();
//        new AsyncStorageTestThread(this).start();
//        new TestDiskCacheThread(this).start();
    }

    private Thread writeThread = new Thread(){
        private static final String TAG = "TestStorage";
        public void run(){
            Log.d(TAG, "start");
            int tagCount = 100;
            Log.d(TAG, "init tag count "+tagCount);
            final Context context = MainActivity.this;
            final HashMap<String, HashMap<String, Object>> pairs = new HashMap<String, HashMap<String, Object>>();
            final HashMap<String, OperationTable> tableMap = createOperationTable(tagCount,pairs);
            Log.d(TAG, "create tableMap "+tableMap.size());
            Set<String> tags = tableMap.keySet();
            for(String tag:tags){
                long t = System.currentTimeMillis();
                OperationTable ot = tableMap.get(tag);
                HashMap<String, Object> map = pairs.get(tag);
                ot.getOperation(context).put(map,System.currentTimeMillis()+134454000);
                long t1 = System.currentTimeMillis();
                Log.d(TAG, "put "+map.size()+" cost "+(t1-t));
            }






            Log.d(TAG, "end");
        }
    };


    private HashMap<String, OperationTable> createOperationTable(int count,HashMap<String, HashMap<String, Object>> pairs){
        final HashMap<String, OperationTable> tableMap = new HashMap<String, OperationTable>();
        for(int i =0;i<count;++i){
            String t = ""+i;
            OperationTable ot = StorageManager.getInstance().generateOperationTable(t, Type.DATABASE,this);
            tableMap.put(t,ot);
            HashMap<String, Object> dm = createData(ot,100);
            pairs.put(t,dm);
        }
        return tableMap;
    }

    private HashMap<String, Object> createData(OperationTable ot,int count){
        final HashMap<String, Object> tableMap = new HashMap<String, Object>();
        final int k = 9;
        Random rd = new Random();
        for(int i =0;i<count;++i){
            String key = ""+i;
            int nextInt= rd.nextInt(k);
            if(nextInt % k == 0){
                tableMap.put(key,rd.nextInt());
            }else if(nextInt % k == 1){
                tableMap.put(key,rd.nextLong());
            }else if(nextInt % k == 2){
                tableMap.put(key,rd.nextDouble());
            }else if(nextInt % k == 3){
                tableMap.put(key,rd.nextFloat());
            }else if(nextInt % k == 4){
                tableMap.put(key,key.getBytes());
            }else if(nextInt % k == 5){
                tableMap.put(key,'D');
            }else if(nextInt % k == 6){
                tableMap.put(key,Integer.valueOf(rd.nextInt()).byteValue());
            }else if(nextInt % k == 7){
                tableMap.put(key,Integer.valueOf(rd.nextInt()).shortValue());
            }else if(nextInt % k == 8){
                tableMap.put(key,key);
            }else {
                Log.e("TestStorage","sdfsdfsfsdfsdfdsfsdfsdf");
            }
        }
        return tableMap;
    }
}
