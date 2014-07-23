package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface IBaseDataWorkStation {
    public void put(String key, int value);
    public void put(String key, float value);
    public void put(String key, double value);
    public void put(String key, byte value);
    public void put(String key, long value);
    public void put(String key, short value);
    public void put(String key, char value);
    public void put(String key, String value);
    public void put(String key, byte[] value);

    public int getInt(String key);
    public float getFloat(String key);
    public double getDouble(String key);
    public long getLong(String key);
    public byte getByte(String key);
    public char getChar(String key);
    public short getShort(String key);
    public byte[] getBlob(String key);

    public IResult remove(String key);
}
