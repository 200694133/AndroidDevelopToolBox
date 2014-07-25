package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/21.
 */
public interface IBaseDataWorkStation {
    public IResult<Integer> put(String key, int value, long expireTime);
    public IResult<Float> put(String key, float value, long expireTime);
    public IResult<Double> put(String key, double value, long expireTime);
    public IResult<Byte> put(String key, byte value, long expireTime);
    public IResult<Long> put(String key, long value, long expireTime);
    public IResult<Short>  put(String key, short value, long expireTime);
    public IResult<Character>  put(String key, char value, long expireTime);
    public IResult<String> put(String key, String value, long expireTime);
    public IResult<byte[]> put(String key, byte[] value, long expireTime);

    public int getInt(String key, int defaultValue);
    public float getFloat(String key, float defaultValue);
    public double getDouble(String key, double defaultValue);
    public long getLong(String key, long defaultValue);
    public byte getByte(String key, byte defaultValue);
    public char getChar(String key, char defaultValue);
    public short getShort(String key, short defaultValue);
    public byte[] getBlob(String key);
    public String getString(String key);

    public IResult remove(String key);
}
