package com.hanyanan.tools.datapersistence.storage;

import com.hanyanan.tools.datapersistence.Constants;
import com.hanyanan.tools.datapersistence.IAsyncBaseDataWorkStation;
import com.hanyanan.tools.datapersistence.IAsyncObjectWorkStation;
import com.hanyanan.tools.datapersistence.IResult;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/23.
 */
public class ExDatabaseStorage extends DatabaseStorage implements IAsyncBaseDataWorkStation,
        IAsyncObjectWorkStation{
    private static final String TAG = ExDatabaseStorage.class.getSimpleName();
    public final static ExecutorService service = Constants.service;
    ExDatabaseStorage(DatabaseStorageDriver db) {
        super(db);
    }

    @Override
    public void putAsync(String key, int value, IResult<Integer> result) {

    }

    @Override
    public void putAsync(String key, float value, IResult<Float> result) {

    }

    @Override
    public void putAsync(String key, double value, IResult<Double> result) {

    }

    @Override
    public void putAsync(String key, byte value, IResult<Byte> result) {

    }

    @Override
    public void putAsync(String key, long value, IResult<Long> result) {

    }

    @Override
    public void putAsync(String key, short value, IResult<Short> result) {

    }

    @Override
    public void putAsync(String key, char value, IResult<Character> result) {

    }

    @Override
    public void putAsync(String key, String value, IResult<String> result) {

    }

    @Override
    public void putAsync(String key, byte[] value, IResult<byte[]> result) {

    }

    @Override
    public void getInt(String key, IResult<Integer> result) {

    }

    @Override
    public void getFloat(String key, IResult<Float> result) {

    }

    @Override
    public void getDouble(String key, IResult<Double> result) {

    }

    @Override
    public void getLong(String key, IResult<Long> result) {

    }

    @Override
    public void getByte(String key, IResult<Byte> result) {

    }

    @Override
    public void getChar(String key, IResult<Character> result) {

    }

    @Override
    public void getShort(String key, IResult<Short> result) {

    }

    @Override
    public void getString(String key, IResult<String> result) {

    }

    @Override
    public void getBlob(String key, IResult<byte[]> result) {

    }

    @Override
    public void remove(String key, IResult result) {

    }

    @Override
    public void putAsync(String key, Serializable value, IOnObjectResult listener) {

    }

    @Override
    public void removeAsync(String key, IOnObjectResult listener) {

    }

    @Override
    public void getAsync(String key, IOnObjectResult listener) {

    }
}
