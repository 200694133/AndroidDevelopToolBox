package com.hanyanan.tools.datapersistence;

import java.sql.Blob;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface IAsyncBaseDataWorkStation {
    public void putAsync(String key, int value, IResult<Integer> result);
    public void putAsync(String key, float value, IResult<Float> result);
    public void putAsync(String key, double value, IResult<Double> result);
    public void putAsync(String key, byte value, IResult<Byte> result);
    public void putAsync(String key, long value, IResult<Long> result);
    public void putAsync(String key, short value, IResult<Short> result);
    public void putAsync(String key, char value, IResult<Character> result);
    public void putAsync(String key, String value, IResult<String> result);
    public void putAsync(String key, byte[] value, IResult<byte[]> result);

    public void getInt(String key, IResult<Integer> result);
    public void getFloat(String key, IResult<Float> result);
    public void getDouble(String key, IResult<Double> result);
    public void getLong(String key, IResult<Long> result);
    public void getByte(String key, IResult<Byte> result);
    public void getChar(String key, IResult<Character> result);
    public void getShort(String key, IResult<Short> result);
    public void getString(String key, IResult<String> result);
    public void getBlob(String key, IResult<byte[]> result);

    public void remove(String key, IResult result);
}
