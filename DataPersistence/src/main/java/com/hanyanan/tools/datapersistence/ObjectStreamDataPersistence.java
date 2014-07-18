package com.hanyanan.tools.datapersistence;

import android.os.Environment;

import com.hanyanan.tools.datapersistence.stream.StreamDataPersistence;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/18.
 */
public class ObjectStreamDataPersistence implements IObjectWorkTop{
    private static final String TAG = ObjectStreamDataPersistence.class.getSimpleName();
    private final StreamDataPersistence mStreamDataPersistence;

    public ObjectStreamDataPersistence(File directory){
        StreamDataPersistence mStreamDataPersistence1;
        try {
            mStreamDataPersistence1 = StreamDataPersistence.open(directory, 1);
        } catch (IOException e) {
            e.printStackTrace();
            mStreamDataPersistence1 = null;
        }
        mStreamDataPersistence = mStreamDataPersistence1;
    }











    @Override
    public IDataResult put(String key, Serializable value) {
        return null;
    }

    @Override
    public void putAsync(String key, IObjectPutListener listener) {

    }

    @Override
    public IDataResult remove(String key) {
        return null;
    }

    @Override
    public void removeAsync(String key, IObjectRemoveListener listener) {

    }

    @Override
    public IDataResult get(String key) {
        return null;
    }

    @Override
    public void getAsync(String key, IObjectGetListener listener) {

    }
}
