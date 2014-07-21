package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface IBaseExWorkTop extends IBaseWorkTop{
    public void putAsync(String key, int value, IDataResult<Integer> result);
    public void putAsync(String key, float value, IDataResult<Float> result);
    public void putAsync(String key, double value, IDataResult<Double> result);
    public void putAsync(String key, byte value, IDataResult<Byte> result);
    public void putAsync(String key, long value, IDataResult<Long> result);
    public void putAsync(String key, short value, IDataResult<Short> result);
    public void putAsync(String key, char value, IDataResult<Character> result);

    public void getInt(String key, IDataResult<Integer> result);
    public void getFloat(String key, IDataResult<Float> result);
    public void getDouble(String key, IDataResult<Double> result);
    public void getLong(String key, IDataResult<Long> result);
    public void getByte(String key, IDataResult<Byte> result);
    public void getChar(String key, IDataResult<Character> result);
    public void getShort(String key, IDataResult<Short> result);

    public void remove(String key, IDataResult result);
}
