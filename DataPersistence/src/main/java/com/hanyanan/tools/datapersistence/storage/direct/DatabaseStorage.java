package com.hanyanan.tools.datapersistence.storage.direct;

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
    public IResult<String> put(String key, String value, long expireTime){
        String errInfo = putString(key, value, expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<String>(null);
        }
        return new SimpleResult<String>(null, new DataError(errInfo));
    }

    private String putString(String key, String value, long expireTime){
        long l ;
        if(isExists(key)){
            l = mDBDriver.update(key, Utils.createBaseParam(value, expireTime));
        }else{
            l = mDBDriver.insert(key, Utils.createBaseParam(value, expireTime));
        }
        if(l<=0){
            return "Operation error.";
        }
        return null;
    }
    @Override
    public IResult<Integer> put(String key, int value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Integer>(null);
        }
        return new SimpleResult<Integer>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Float> put(String key, float value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Float>(null);
        }
        return new SimpleResult<Float>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Double> put(String key, double value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Double>(null);
        }
        return new SimpleResult<Double>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Byte> put(String key, byte value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Byte>(null);
        }
        return new SimpleResult<Byte>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Long> put(String key, long value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Long>(null);
        }
        return new SimpleResult<Long>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Short> put(String key, short value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Short>(null);
        }
        return new SimpleResult<Short>(null, new DataError(errInfo));
    }

    @Override
    public IResult<Character> put(String key, char value, long expireTime) {
        String errInfo = putString(key, String.valueOf(value), expireTime);
        if(TextUtils.isEmpty(errInfo)){
            return new SimpleResult<Character>(null);
        }
        return new SimpleResult<Character>(null, new DataError(errInfo));
    }

    @Override
    public IResult<byte[]> put(String key, final byte[] value, long expireTime){
        long i ;
        if(isExists(key)){
            i = mDBDriver.update(key, Utils.createBlobParam(value, expireTime));
        }else{
            i = mDBDriver.insert(key, Utils.createBlobParam(value, expireTime));
        }
        if(i<=0){
            return new SimpleResult<byte[]>(null, new DataError("Operation error."));
        }
        return new SimpleResult<byte[]>(null);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Integer.parseInt(data);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Float.parseFloat(data);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Double.parseDouble(data);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Long.parseLong(data);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Byte.parseByte(data);
    }

    @Override
    public char getChar(String key, char defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return data.charAt(0);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        String data = mDBDriver.getText(key);
        if(TextUtils.isEmpty(data)) return defaultValue;
        return Short.parseShort(data);
    }

    @Override
    public byte[] getBlob(String key){
        return mDBDriver.getBlob(key);
    }

    @Override
    public String getString(String key) {
        return mDBDriver.getText(key);
    }

    @Override
    public IResult<Serializable> put(String key, Serializable value, long expireTime) {
        final byte[] res = Utils.serialize(value);
        if(isExists(key)){
            mDBDriver.update(key, Utils.createBlobParam(res, expireTime));
        }else{
            mDBDriver.insert(key, Utils.createBlobParam(res, expireTime));
        }

        return new SimpleResult<Serializable>(null);
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
