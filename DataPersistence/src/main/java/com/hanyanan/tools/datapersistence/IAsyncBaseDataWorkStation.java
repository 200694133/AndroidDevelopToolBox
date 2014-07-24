package com.hanyanan.tools.datapersistence;

import java.sql.Blob;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface IAsyncBaseDataWorkStation {
    public void putAsync(String key, final int value, final long expire, IAsyncResult<Integer> result);
    public void putAsync(String key, final float value, final long expire, IAsyncResult<Float> result);
    public void putAsync(String key, final double value, final long expire, IAsyncResult<Double> result);
    public void putAsync(String key, final byte value,final long expire,  IAsyncResult<Byte> result);
    public void putAsync(String key, final long value, final long expire, IAsyncResult<Long> result);
    public void putAsync(String key, final short value, final long expire, IAsyncResult<Short> result);
    public void putAsync(String key, final char value, final long expire, IAsyncResult<Character> result);
    public void putAsync(String key, final String value, final long expire, IAsyncResult<String> result);
    public void putAsync(String key, final byte[] value, final long expire, IAsyncResult<byte[]> result);

    public void getInt(String key, IAsyncResult<Integer> result);
    public void getFloat(String key, IAsyncResult<Float> result);
    public void getDouble(String key, IAsyncResult<Double> result);
    public void getLong(String key, IAsyncResult<Long> result);
    public void getByte(String key, IAsyncResult<Byte> result);
    public void getChar(String key, IAsyncResult<Character> result);
    public void getShort(String key, IAsyncResult<Short> result);
    public void getString(String key, IAsyncResult<String> result);
    public void getBlob(String key, IAsyncResult<byte[]> result);

    public void remove(String key, IAsyncResult result);
}
