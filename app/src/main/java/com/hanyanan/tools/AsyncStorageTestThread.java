package com.hanyanan.tools;

import android.content.Context;
import android.util.Log;

import com.hanyanan.tools.storage.AsyncOperation;
import com.hanyanan.tools.storage.OperationTable;
import com.hanyanan.tools.storage.Result;
import com.hanyanan.tools.storage.StorageManager;
import com.hanyanan.tools.storage.Type;

import java.util.Random;

/**
 * Created by hanyanan on 2014/8/5.
 */
public class AsyncStorageTestThread extends Thread {
    private static final String TAG = "TestStorage";
    private Context mContext;
    public AsyncStorageTestThread(Context context){
        mContext = context;
    }
    public void run(){
        Log.d(TAG, "read start");
        int tagCount = 10000;
        Log.d(TAG, "read tag count "+tagCount);
        String tag = "whosyourdaddy";
        OperationTable ot = StorageManager.getInstance().generateOperationTable(tag, Type.DATABASE,mContext);
        AsyncOperation ao = ot.getAsyncOperation(mContext);
        final int k = 9;
        //write to database
        for(int i=0;i<tagCount;++i){
                Random rd = new Random();
                final String key = ""+i;
                int nextInt= i%k;
                if(nextInt  == 0){
                    ao.putInt(OperationTable.deliveryEntry(ot, key, rd.nextInt()), new Result.OnResultListener<Integer>() {
                        public void onResult(Result<Integer> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 1){
                    ao.putLong(OperationTable.deliveryEntry(ot, key, rd.nextLong()), new Result.OnResultListener<Long>() {
                        public void onResult(Result<Long> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 2){
                    ao.putDouble(OperationTable.deliveryEntry(ot, key, rd.nextDouble()), new Result.OnResultListener<Double>() {
                        public void onResult(Result<Double> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 3){
                    ao.putFloat(OperationTable.deliveryEntry(ot, key, rd.nextFloat()), new Result.OnResultListener<Float>() {
                        public void onResult(Result<Float> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 4){
                    ao.putByte(OperationTable.deliveryEntry(ot, key, Integer.valueOf(rd.nextInt()).byteValue()), new Result.OnResultListener<Byte>() {
                        public void onResult(Result<Byte> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 5){
                    ao.putChar(OperationTable.deliveryEntry(ot, key, 'D'), new Result.OnResultListener<Character>() {
                        public void onResult(Result<Character> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 6){
                    ao.putByte(OperationTable.deliveryEntry(ot, key, Integer.valueOf(rd.nextInt()).byteValue()), new Result.OnResultListener<Byte>() {
                        public void onResult(Result<Byte> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 7){
                    ao.putShort(OperationTable.deliveryEntry(ot, key, Integer.valueOf(rd.nextInt()).shortValue()), new Result.OnResultListener<Short>() {
                        public void onResult(Result<Short> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else if(nextInt == 8){
                    ao.putString(OperationTable.deliveryEntry(ot, key, key), new Result.OnResultListener<String>() {
                        public void onResult(Result<String> result) {
                            Log.d(TAG, "put async " + key + " " + (result.mErrorInfo == null ? "Success" : "Failed"));
                        }
                    });
                }else {
                    Log.e("TestStorage","sdfsdfsfsdfsdfdsfsdfsdf");
                }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<tagCount;++i){
            final String key = ""+i;
            int nextInt= i%k;
            if(nextInt  == 0){
                ao.getInt(key,-1,new Result.OnResultListener<Integer>() {
                    @Override
                    public void onResult(Result<Integer> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 1){
                ao.getLong(key,-1,new Result.OnResultListener<Long>() {
                    @Override
                    public void onResult(Result<Long> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 2){
                ao.getDouble(key,-1,new Result.OnResultListener<Double>() {
                    @Override
                    public void onResult(Result<Double> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 3){
                ao.getFloat(key,-1,new Result.OnResultListener<Float>() {
                    @Override
                    public void onResult(Result<Float> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 4){
                ao.getByte(key,(byte)-1,new Result.OnResultListener<Byte>() {
                    @Override
                    public void onResult(Result<Byte> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 5){
                ao.getChar(key,(char)-1,new Result.OnResultListener<Character>() {
                    @Override
                    public void onResult(Result<Character> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 6){
                ao.getByte(key,(byte)-1,new Result.OnResultListener<Byte>() {
                    @Override
                    public void onResult(Result<Byte> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 7){
                ao.getShort(key,(short)-1,new Result.OnResultListener<Short>() {
                    @Override
                    public void onResult(Result<Short> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else if(nextInt == 8){
                ao.getString(key,new Result.OnResultListener<String>() {
                    @Override
                    public void onResult(Result<String> result) {
                        Log.d(TAG, "get async "+key+" = "+result.mResult.mData);
                    }
                });
            }else {
                Log.e("TestStorage","sdfsdfsfsdfsdfdsfsdfsdf");
            }
        }
    }
}
