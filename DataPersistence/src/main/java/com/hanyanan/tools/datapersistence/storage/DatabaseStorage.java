package com.hanyanan.tools.datapersistence.storage;

import android.text.TextUtils;

import com.hanyanan.tools.datapersistence.DataError;
import com.hanyanan.tools.datapersistence.IBaseDataWorkStation;
import com.hanyanan.tools.datapersistence.IObjectWorkStation;
import com.hanyanan.tools.datapersistence.IResult;
import com.hanyanan.tools.datapersistence.SimpleResult;
import com.hanyanan.tools.datapersistence.Utils;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/21.
 */
public class DatabaseStorage implements IBaseDataWorkStation, IObjectWorkStation {
    private static final String TAG = DatabaseStorage.class.getSimpleName();
    private final DatabaseStorageDriver mDBDriver;
    DatabaseStorage(DatabaseStorageDriver db){
        mDBDriver = db;
    }
    @Override
    public IResult put(String key, String value, long expireTime){
        long l = 0;
        if(isExists(key)){
            l = mDBDriver.update(key, Utils.createBaseParam(value, expireTime));
        }else{
            l = mDBDriver.insert(key, Utils.createBaseParam(value, expireTime));
        }
        if(l<=0){
            return new SimpleResult(null, new DataError("Operation error."));
        }
        return new SimpleResult(null);
    }

    @Override
    public IResult put(String key, int value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, float value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, double value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, byte value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, long value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, short value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, char value, long expireTime) {
        return put(key, String.valueOf(value), expireTime);
    }

    @Override
    public IResult put(String key, final byte[] value, long expireTime){
        long i  = 0;
            byte[] data = getBlob(key);
            if(isExists(key)){
                i = mDBDriver.update(key, Utils.createBlobParam(value, expireTime));
            }else{
                i = mDBDriver.insert(key, Utils.createBlobParam(value, expireTime));
            }
        if(i<=0){
            return new SimpleResult(null, new DataError("Operation error."));
        }
        return new SimpleResult(null);
    }

    @Override
    public int getInt(String key) {
        String data = mDBDriver.getText(key);
        return Integer.parseInt(data);
    }

    @Override
    public float getFloat(String key) {
        String data = mDBDriver.getText(key);
        return Float.parseFloat(data);
    }

    @Override
    public double getDouble(String key) {
        String data = mDBDriver.getText(key);
        return Double.parseDouble(data);
    }

    @Override
    public long getLong(String key) {
        String data = mDBDriver.getText(key);
        return Long.parseLong(data);
    }

    @Override
    public byte getByte(String key) {
        String data = mDBDriver.getText(key);
        return Byte.parseByte(data);
    }

    @Override
    public char getChar(String key) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return ' ';
        return data.charAt(0);
    }

    @Override
    public short getShort(String key) {
        String data = mDBDriver.getText(key);
        return Short.parseShort(data);
    }

    @Override
    public byte[] getBlob(String key){
        byte[] data = mDBDriver.getBlob(key);
        return data;
    }

    @Override
    public String getString(String key) {
        return mDBDriver.getText(key);
    }

    @Override
    public IResult put(String key, Serializable value, long expireTime) {
        final byte[] res = Utils.serialize(value);
        if(isExists(key)){
            mDBDriver.update(key, Utils.createBlobParam(res, expireTime));
        }else{
            mDBDriver.insert(key, Utils.createBlobParam(res, expireTime));
        }

        return new SimpleResult(null);
    }

    @Override
    public IResult remove(String key) {
        mDBDriver.remove(key);
        IResult res = new SimpleResult(null);
        return res;
    }

    @Override
    public IResult get(String key) {
        final byte[] res = mDBDriver.getBlob(key);
        Serializable data = Utils.deSerialize(res);
        IResult result = new SimpleResult(data);
        return result;
    }

    private boolean isExists(String key){
        return mDBDriver.isExists(key);
    }
}
