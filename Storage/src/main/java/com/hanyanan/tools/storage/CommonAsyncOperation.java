package com.hanyanan.tools.storage;

import com.hanyanan.tools.storage.AsyncOperation;
import com.hanyanan.tools.storage.Entry;
import com.hanyanan.tools.storage.Operation;
import com.hanyanan.tools.storage.Result;
import com.hanyanan.tools.storage.Utils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by hanyanan on 2014/8/4.
 */
public class CommonAsyncOperation implements AsyncOperation{
    private final ExecutorService executorService = Utils.executorService;
    private final Operation mOperation;
    public CommonAsyncOperation(Operation operation){
        mOperation = operation;
    }

    @Override
    public void putInt(final Entry<Integer> entry, final Result.OnResultListener<Integer> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Integer> res = mOperation.put(entry.getKey(), entry.mData.intValue(), entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putFloat(final Entry<Float> entry, final Result.OnResultListener<Float> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Float> res = mOperation.put(entry.getKey(), entry.mData.floatValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putDouble(final Entry<Double> entry, final Result.OnResultListener<Double> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Double> res = mOperation.put(entry.getKey(), entry.mData.doubleValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putByte(final Entry<Byte> entry, final Result.OnResultListener<Byte> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Byte> res = mOperation.put(entry.getKey(), entry.mData.byteValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putLong(final Entry<Long> entry, final Result.OnResultListener<Long> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Long> res = mOperation.put(entry.getKey(), entry.mData.longValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putShort(final Entry<Short> entry, final Result.OnResultListener<Short> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Short> res = mOperation.put(entry.getKey(), entry.mData.shortValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putChar(final Entry<Character> entry, final Result.OnResultListener<Character> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Character> res = mOperation.put(entry.getKey(), entry.mData.charValue(),entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putString(final Entry<String> entry,final Result.OnResultListener<String> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<String> res = mOperation.put(entry.getKey(), entry.mData,entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putBytes(final Entry<byte[]> entry, final Result.OnResultListener<byte[]> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<byte[]> res = mOperation.put(entry.getKey(), entry.mData,entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void putSerialize(final Entry<Serializable> entry, final Result.OnResultListener<Serializable> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result<Serializable> res = mOperation.put(entry.getKey(), entry.mData,entry.mExpireTime);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void put(final HashMap<String, Object> pair, final Result.OnResultListener<?> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result res = mOperation.put(pair,0);
                if(null != listener){
                    listener.onResult(res);
                }
            }
        });
    }

    @Override
    public void getInt(final String key, final int defaultValue, final Result.OnResultListener<Integer> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                int res = mOperation.getInt(key, defaultValue);
                if(null != listener){
                    Entry<Integer> entry = new Entry<Integer>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getFloat(final String key,final float defaultValue, final Result.OnResultListener<Float> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                float res = mOperation.getFloat(key, defaultValue);
                if(null != listener){
                    Entry<Float> entry = new Entry<Float>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getDouble(final String key, final double defaultValue, final Result.OnResultListener<Double> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                double res = mOperation.getDouble(key, defaultValue);
                if(null != listener){
                    Entry<Double> entry = new Entry<Double>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getLong(final String key, final long defaultValue, final Result.OnResultListener<Long> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                long res = mOperation.getLong(key, defaultValue);
                if(null != listener){
                    Entry<Long> entry = new Entry<Long>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getByte(final String key, final byte defaultValue, final Result.OnResultListener<Byte> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                byte res = mOperation.getByte(key, defaultValue);
                if(null != listener){
                    Entry<Byte> entry = new Entry<Byte>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getChar(final String key, final char defaultValue, final Result.OnResultListener<Character> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                char res = mOperation.getChar(key, defaultValue);
                if(null != listener){
                    Entry<Character> entry = new Entry<Character>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getShort(final String key, final short defaultValue, final Result.OnResultListener<Short> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                short res = mOperation.getShort(key, defaultValue);
                if(null != listener){
                    Entry<Short> entry = new Entry<Short>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getBlob(final String key, final Result.OnResultListener<byte[]> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                byte[] res = mOperation.getBlob(key);
                if(null != listener){
                    Entry<byte[]> entry = new Entry<byte[]>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getString(final String key, final Result.OnResultListener<String> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                String res = mOperation.getString(key);
                if(null != listener){
                    Entry<String> entry = new Entry<String>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getSerializable(final String key, final Result.OnResultListener<Serializable> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Serializable res = mOperation.getSerializable(key);
                if(null != listener){
                    Entry<Serializable> entry = new Entry<Serializable>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void getAll(final HashMap<String, Class> map, final Result.OnResultListener<HashMap<String, Object>> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                HashMap<String,Object> res = mOperation.getAll(map);
                if(null != listener){
                    Entry<HashMap<String,Object>> entry = new Entry<HashMap<String,Object>>();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void remove(final String key, final Result.OnResultListener<?> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result res = mOperation.remove(key);
                if(null != listener){
                    Entry entry = new Entry();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }

    @Override
    public void remove(final List<String> key, final Result.OnResultListener<?> listener) {
        executorService.execute(new Runnable() {
            public void run() {
                Result res = mOperation.remove(key);
                if(null != listener){
                    Entry entry = new Entry();
                    entry.mData = res;
                    listener.onResult(Result.success(entry));
                }
            }
        });
    }
}
