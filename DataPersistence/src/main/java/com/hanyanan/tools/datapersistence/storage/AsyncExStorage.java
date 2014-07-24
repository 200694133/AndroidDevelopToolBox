package com.hanyanan.tools.datapersistence.storage;

import com.hanyanan.tools.datapersistence.Constant;
import com.hanyanan.tools.datapersistence.IAsyncBaseDataWorkStation;
import com.hanyanan.tools.datapersistence.IAsyncObjectWorkStation;
import com.hanyanan.tools.datapersistence.IAsyncResult;
import com.hanyanan.tools.datapersistence.IBaseDataWorkStation;
import com.hanyanan.tools.datapersistence.IObjectWorkStation;
import com.hanyanan.tools.datapersistence.SimpleResult;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * Created by hanyanan on 2014/7/23.
 */
public final class AsyncExStorage implements IAsyncBaseDataWorkStation,
        IAsyncObjectWorkStation{
    private static final String TAG = AsyncExStorage.class.getSimpleName();
    public final static ExecutorService service = Constant.service;
    private final IBaseDataWorkStation mBaseWorkStation;
    private final IObjectWorkStation mObjectWorkStation;
    AsyncExStorage(IBaseDataWorkStation baseDataWorkStation, IObjectWorkStation objectWorkStation) {
        mBaseWorkStation = baseDataWorkStation;
        mObjectWorkStation = objectWorkStation;
    }

    @Override
    public void putAsync(String key, int value, long expire, IAsyncResult<Integer> result) {
        final String k = key;
        final int v = value;
        final long e = expire;
        final IAsyncResult<Integer> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, float value, long expire, IAsyncResult<Float> result) {
        final String k = key;
        final float v = value;
        final long e = expire;
        final IAsyncResult<Float> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, double value, long expire, IAsyncResult<Double> result) {
        final String k = key;
        final double v = value;
        final long e = expire;
        final IAsyncResult<Double> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, byte value, long expire, IAsyncResult<Byte> result) {
        final String k = key;
        final byte v = value;
        final long e = expire;
        final IAsyncResult<Byte> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, long value, long expire, IAsyncResult<Long> result) {
        final String k = key;
        final long v = value;
        final long e = expire;
        final IAsyncResult<Long> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, short value, long expire, IAsyncResult<Short> result) {
        final String k = key;
        final short v = value;
        final long e = expire;
        final IAsyncResult<Short> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, char value, long expire, IAsyncResult<Character> result) {
        final String k = key;
        final char v = value;
        final long e = expire;
        final IAsyncResult<Character> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, String value, long expire, IAsyncResult<String> result) {
        final String k = key;
        final String v = value;
        final long e = expire;
        final IAsyncResult<String> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, byte[] value, long expire, IAsyncResult<byte[]> result) {
        final String k = key;
        final  byte[] v = value;
        final long e = expire;
        final IAsyncResult< byte[]> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k,mBaseWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getInt(String key, IAsyncResult<Integer> result) {
        final String k = key;
        final IAsyncResult<Integer> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Integer>(mBaseWorkStation.getInt(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getFloat(String key, IAsyncResult<Float> result) {
        final String k = key;
        final IAsyncResult<Float> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Float>(mBaseWorkStation.getFloat(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getDouble(String key, IAsyncResult<Double> result) {
        final String k = key;
        final IAsyncResult<Double> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Double>(mBaseWorkStation.getDouble(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getLong(String key, IAsyncResult<Long> result) {
        final String k = key;
        final IAsyncResult<Long> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Long>(mBaseWorkStation.getLong(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getByte(String key, IAsyncResult<Byte> result) {
        final String k = key;
        final IAsyncResult<Byte> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Byte>(mBaseWorkStation.getByte(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getChar(String key, IAsyncResult<Character> result) {
        final String k = key;
        final IAsyncResult<Character> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Character>(mBaseWorkStation.getChar(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getShort(String key, IAsyncResult<Short> result) {
        final String k = key;
        final IAsyncResult<Short> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<Short>(mBaseWorkStation.getShort(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getString(String key, IAsyncResult<String> result) {
        final String k = key;
        final IAsyncResult<String> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<String>(mBaseWorkStation.getString(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getBlob(String key, IAsyncResult<byte[]> result) {
        final String k = key;
        final IAsyncResult<byte[]> r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, new SimpleResult<byte[]>(mBaseWorkStation.getBlob(k)));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void remove(String key, IAsyncResult result) {
        final String k = key;
        final IAsyncResult r = result;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, mBaseWorkStation.remove(k));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void putAsync(String key, Serializable value, long expire,IAsyncResult listener) {
        final String k = key;
        final Serializable v = value;
        final IAsyncResult r = listener;
        final long e = expire;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, mObjectWorkStation.put(k,v,e));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void removeAsync(String key, IAsyncResult listener) {
        final String k = key;
        final IAsyncResult r = listener;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, mObjectWorkStation.get(k));
            }
        };
        service.execute(runnable);
    }

    @Override
    public void getAsync(String key, IAsyncResult listener) {
        final String k = key;
        final IAsyncResult r = listener;
        Runnable runnable = new Runnable() {
            public void run() {
                r.onResult(k, mObjectWorkStation.remove(k));
            }
        };
        service.execute(runnable);
    }
}
