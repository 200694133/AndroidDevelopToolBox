package com.hanyanan.tools.storage.db;

import com.hanyanan.tools.storage.Entry;
import com.hanyanan.tools.storage.Error.*;
import com.hanyanan.tools.storage.Error.Error;
import com.hanyanan.tools.storage.Operation;
import com.hanyanan.tools.storage.OperationTable;
import com.hanyanan.tools.storage.Result;

import java.io.Closeable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hanyanan on 2014/8/3.
 * Before put the new key-value pair, it must be encode for security.Then when loading data from
 * database, need to decode the raw data,or it's unrecognizable for up level.
 * Before get the key-value pair, it must filter the entry to get the illegal data.
 */
public final class DatabaseOperation implements Operation,Closeable {
    private final DatabaseHelper mDB;
    private final OperationTable mOperations;
    public DatabaseOperation(DatabaseHelper db,OperationTable operationSet){
        mDB = db;
        mOperations = operationSet;
    }

    public void close(){
        //TODO
    }

    public static long put(DatabaseHelper db, Entry entry){
        long l = 0;
        if(null != entry.mEncoder) {
            entry = entry.mEncoder.encode(entry);
        }
        try {
            if(db.isExists(entry)){
                l = db.update(entry);
            }else{
                l = db.insert(entry);
            }
            return l;
        } catch (TypeNotSupportError typeNotSupportError) {
            typeNotSupportError.printStackTrace();
        }
        return 0;
    }

    @Override
    public Result<Integer> put(String key, int value, long expireTime) {
        Entry<Integer> entry = new Entry<Integer>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Integer.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Integer>(new Error());
    }

    @Override
    public Result<Float> put(String key, float value, long expireTime) {
        Entry<Float> entry = new Entry<Float>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Float.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Float>(new Error());
    }

    @Override
    public Result<Double> put(String key, double value, long expireTime) {
        Entry<Double> entry = new Entry<Double>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Double.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Double>(new Error());
    }

    @Override
    public Result<Byte> put(String key, byte value, long expireTime) {
        Entry<Byte> entry = new Entry<Byte>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Byte.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Byte>(new Error());
    }

    @Override
    public Result<Long> put(String key, long value, long expireTime) {
        Entry<Long> entry = new Entry<Long>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Long.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Long>(new Error());
    }

    @Override
    public Result<Short> put(String key, short value, long expireTime) {
        Entry<Short> entry = new Entry<Short>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Short.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Short>(new Error());
    }

    @Override
    public Result<Character> put(String key, char value, long expireTime) {
        Entry<Character> entry = new Entry<Character>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = Character.valueOf(value);
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Character>(new Error());
    }

    @Override
    public Result<String> put(String key, String value, long expireTime) {
        Entry<String> entry = new Entry<String>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = value;
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<String>(new Error());
    }

    @Override
    public Result<byte[]> put(String key, byte[] value, long expireTime) {
        Entry<byte[]> entry = new Entry<byte[]>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = value;
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<byte[]>(new Error());
    }

    @Override
    public Result<Serializable> put(String key, Serializable value, long expireTime) {
        Entry<Serializable> entry = new Entry<Serializable>();
        entry.mPrimaryKey = mOperations.getTag();
        entry.mSecondaryKey = key;
        entry.mData = value;
        entry.mExpireTime = expireTime;
        entry.mTimeStamp = System.currentTimeMillis();
        if(put(mDB, entry) > 0){
            return Result.success(entry);
        }
        return new Result<Serializable>(new Error());
    }

    @Override
    public Result<?> put(HashMap<String, Object> pair, long expireTime) {
        mDB.getWritableDatabase().beginTransaction();
        Set<String> keys = pair.keySet();
        try {
            for(String key : keys){
                Object obj = pair.get(key);
                if(obj instanceof byte[]){
                    put(key,(byte[])obj, expireTime);
                }else if(obj instanceof Serializable){
                    put(key,(Serializable)obj, expireTime);
                }else{
                    return Result.failed(new Error(""+obj.getClass()+" is not supported"));
                }
            }
            mDB.getWritableDatabase().setTransactionSuccessful();
        } finally {
            mDB.getWritableDatabase().endTransaction();
        }
        return Result.success(null);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            Entry<Integer> entry = mDB.get(mOperations.getTag(),key,Integer.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
        return defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        try {
            Entry<Float> entry = mDB.get(mOperations.getTag(),key,Float.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        try {
            Entry<Double> entry = mDB.get(mOperations.getTag(),key,Double.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            Entry<Long> entry = mDB.get(mOperations.getTag(),key,Long.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        try {
            Entry<Byte> entry = mDB.get(mOperations.getTag(),key,Byte.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public char getChar(String key, char defaultValue) {
        try {
            Entry<Character> entry = mDB.get(mOperations.getTag(),key,Character.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public short getShort(String key, short defaultValue) {
        try {
            Entry<Short> entry = mDB.get(mOperations.getTag(),key,Short.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public byte[] getBlob(String key) {
        try {
            Entry<byte[]> entry = mDB.getByteArray(mOperations.getTag(),key);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getString(String key) {
        try {
            Entry<String> entry = mDB.get(mOperations.getTag(),key,String.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Serializable getSerializable(String key) {
        try {
            Entry<Serializable> entry = mDB.get(mOperations.getTag(),key,Serializable.class);
            if(null != mOperations.getFilter() && mOperations.getFilter().isValid(entry)){
                return entry.mData;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, Object> getAll(HashMap<String, Class> map) {
        Set<String> keys = map.keySet();
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        for(String key : keys){
            Class clazz = map.get(key);
            if(clazz.equals(byte[].class)) {
                byte[] res = getBlob(key);
                if(null != res) resMap.put(key, res);
            }else {
                Serializable s = getSerializable(key);
                if(s != null){
                    resMap.put(key,clazz.cast(s));
                }
            }
        }
        return resMap;
    }

    @Override
    public Result<?> remove(String key) {
        long l = mDB.remove(mOperations.getTag()+"_"+key);
        return null;
    }

    @Override
    public Result<?> remove(List<String> keys) {
        for(String key : keys){
            mDB.remove(key);
        }
        return Result.success(null);
    }
}
