package com.hanyanan.tools.datapersistence.db;

import com.hanyanan.tools.datapersistence.IBaseExWorkTop;
import com.hanyanan.tools.datapersistence.IDataResult;
import com.hanyanan.tools.datapersistence.IObjectWorkTop;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/21.
 */
public class DataBaseDataPersistence implements IBaseExWorkTop, IObjectWorkTop{
    @Override
    public void putAsync(String key, int value, IDataResult<Integer> result) {

    }

    @Override
    public void putAsync(String key, float value, IDataResult<Float> result) {

    }

    @Override
    public void putAsync(String key, double value, IDataResult<Double> result) {

    }

    @Override
    public void putAsync(String key, byte value, IDataResult<Byte> result) {

    }

    @Override
    public void putAsync(String key, long value, IDataResult<Long> result) {

    }

    @Override
    public void putAsync(String key, short value, IDataResult<Short> result) {

    }

    @Override
    public void putAsync(String key, char value, IDataResult<Character> result) {

    }

    @Override
    public void getInt(String key, IDataResult<Integer> result) {

    }

    @Override
    public void getFloat(String key, IDataResult<Float> result) {

    }

    @Override
    public void getDouble(String key, IDataResult<Double> result) {

    }

    @Override
    public void getLong(String key, IDataResult<Long> result) {

    }

    @Override
    public void getByte(String key, IDataResult<Byte> result) {

    }

    @Override
    public void getChar(String key, IDataResult<Character> result) {

    }

    @Override
    public void getShort(String key, IDataResult<Short> result) {

    }

    @Override
    public void remove(String key, IDataResult result) {

    }

    @Override
    public void put(String key, int value) {

    }

    @Override
    public void put(String key, float value) {

    }

    @Override
    public void put(String key, double value) {

    }

    @Override
    public void put(String key, byte value) {

    }

    @Override
    public void put(String key, long value) {

    }

    @Override
    public void put(String key, short value) {

    }

    @Override
    public void put(String key, char value) {

    }

    @Override
    public int getInt(String key) {
        return 0;
    }

    @Override
    public float getFloat(String key) {
        return 0;
    }

    @Override
    public double getDouble(String key) {
        return 0;
    }

    @Override
    public long getLong(String key) {
        return 0;
    }

    @Override
    public byte getByte(String key) {
        return 0;
    }

    @Override
    public char getChar(String key) {
        return 0;
    }

    @Override
    public short getShort(String key) {
        return 0;
    }

    @Override
    public IDataResult put(String key, Serializable value) {
        return null;
    }

    @Override
    public void putAsync(String key, Serializable value, IOnObjectResult listener) {

    }

    @Override
    public IDataResult remove(String key) {
        return null;
    }

    @Override
    public void removeAsync(String key, IOnObjectResult listener) {

    }

    @Override
    public IDataResult get(String key) {
        return null;
    }

    @Override
    public void getAsync(String key, IOnObjectResult listener) {

    }
}
