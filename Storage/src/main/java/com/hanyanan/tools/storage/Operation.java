package com.hanyanan.tools.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hanyanan on 2014/8/3.
 */
public interface Operation {
    public Result<Integer> put(String key, int value, long expireTime);
    public Result<Float> put(String key, float value, long expireTime);
    public Result<Double> put(String key, double value, long expireTime);
    public Result<Byte> put(String key, byte value, long expireTime);
    public Result<Long> put(String key, long value, long expireTime);
    public Result<Short>  put(String key, short value, long expireTime);
    public Result<Character>  put(String key, char value, long expireTime);
    public Result<String> put(String key, String value, long expireTime);
    public Result<byte[]> put(String key, byte[] value, long expireTime);
    public Result<Serializable> put(String key, Serializable value, long expireTime);

    /**
     * put many data once.
     * @param pair a key-value pair, it must be a byte[] or a Serializable type.
     * @param expireTime
     * @return
     */
    public Result<?> put(HashMap<String,Object> pair,long expireTime);

    public int getInt(String key, int defaultValue);
    public float getFloat(String key, float defaultValue);
    public double getDouble(String key, double defaultValue);
    public long getLong(String key, long defaultValue);
    public byte getByte(String key, byte defaultValue);
    public char getChar(String key, char defaultValue);
    public short getShort(String key, short defaultValue);
    public byte[] getBlob(String key);
    public String getString(String key);
    public Serializable getSerializable(String key);
    public HashMap<String,Object> getAll(HashMap<String, Class> map);

    public Result<?> remove(String key);
    public Result<?> remove(List<String> key);
}
