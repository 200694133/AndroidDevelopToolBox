package com.hanyanan.tools.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hanyanan on 2014/8/4.
 */
public interface AsyncOperation {
    public void putInt(final Entry<Integer> entry,final Result.OnResultListener<Integer> listener);
    public void putFloat(final Entry<Float> entry,final Result.OnResultListener<Float> listener);
    public void putDouble(final Entry<Double> entry, final Result.OnResultListener<Double> listener);
    public void putByte(final Entry<Byte> entry,final Result.OnResultListener<Byte> listener);
    public void putLong(final Entry<Long> entry,final Result.OnResultListener<Long> listener);
    public void putShort(final Entry<Short> entry, final Result.OnResultListener<Short> listener);
    public void putChar(final Entry<Character> entry, final Result.OnResultListener<Character> listener);
    public void putString(final Entry<String> entry, final Result.OnResultListener<String> listener);
    public void putBytes(final Entry<byte[]> entry, final Result.OnResultListener<byte[]> listener);
    public void putSerialize(final Entry<Serializable> entry, final Result.OnResultListener<Serializable> listener);
    public void put(HashMap<String,Object> pair,final Result.OnResultListener<?> listener);

    public void getInt(final String key, final int defaultValue,final Result.OnResultListener<Integer> listener);
    public void getFloat(String key, float defaultValue,final Result.OnResultListener<Float> listener);
    public void getDouble(String key, double defaultValue,final Result.OnResultListener<Double> listener);
    public void getLong(String key, long defaultValue,final Result.OnResultListener<Long> listener);
    public void getByte(String key, byte defaultValue,final Result.OnResultListener<Byte> listener);
    public void getChar(String key, char defaultValue,final Result.OnResultListener<Character> listener);
    public void getShort(String key, short defaultValue,final Result.OnResultListener<Short> listener);
    public void getBlob(String key,final Result.OnResultListener<byte[]> listener);
    public void getString(String key,final Result.OnResultListener<String> listener);
    public void getSerializable(String key,final Result.OnResultListener<Serializable> listener);
    public void getAll(HashMap<String,Class> map,final Result.OnResultListener<HashMap<String,Object>> listener);

    public void remove(String key,final Result.OnResultListener<?> listener);
    public void remove(List<String> key,final Result.OnResultListener<?> listener);
}
