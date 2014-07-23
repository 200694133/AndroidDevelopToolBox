package com.hanyanan.tools.datapersistence.storage;

import android.text.TextUtils;

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
    public void put(String key, String value){
        if(isExists(key)){
            mDBDriver.update(key, value);
        }else{
            mDBDriver.insert(key, Utils.createBaseType(value));
        }
    }

    @Override
    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, float value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, byte value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, short value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, char value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void put(String key, final byte[] value){
            byte[] data = getBlob(key);
            if(isExists(key)){
                mDBDriver.update(key, value);
            }else{
                mDBDriver.insert(key, new BlobType() {
                    @Override
                    public byte[] getData() {
                        return value;
                    }
                    @Override
                    public long getExpireTime() {
                        return -1;
                    }
                });
            }
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
    public IResult put(String key, Serializable value) {
        final byte[] res = Utils.serialize(value);
        if(isExists(key)){
            mDBDriver.update(key, res);
        }else{
            mDBDriver.insert(key, new BlobType() {
                @Override
                public byte[] getData() {
                    return res;
                }
                @Override
                public long getExpireTime() {
                    return -1;
                }
            });
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
