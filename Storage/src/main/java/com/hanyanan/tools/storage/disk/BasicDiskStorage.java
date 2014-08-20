package com.hanyanan.tools.storage.disk;

import android.graphics.Bitmap;

import com.hanyanan.tools.storage.IStreamStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Administrator on 2014/8/20.
 * This class is unlimited, which means there is no size limited and age limited.
 */
public class BasicDiskStorage implements DiskStorage{
    protected final static int VERSION = 1;
    protected final static int M = 1024 * 1024;

    private final IStreamStorage mStreamStorage;
    public BasicDiskStorage(IStreamStorage streamStorage){
        mStreamStorage = streamStorage;
    }

    public BasicDiskStorage(File rootDirectory){
        if(null == rootDirectory)
            throw new NullPointerException("Input root directory for BasicDiskStorage is empty.");
        if(!rootDirectory.isDirectory())
            throw new IllegalArgumentException("BasicStorage need directory!");
        IStreamStorage streamStorage = null;
        try {
            streamStorage = FlexibleDiskStorageImpl.open(rootDirectory, VERSION);
        } catch (IOException e) {
            streamStorage = null;
        }finally {
            mStreamStorage = streamStorage;
        }
    }



    @Override
    public File getDirectory() {
        if(null == mStreamStorage) return null;
        return null;
    }

    @Override
    public long getCurrentSize() {
        if(null == mStreamStorage) return -1;
        return mStreamStorage.getCurrentSize();
    }

    @Override
    public File get(String key) {
        return null;
    }

    @Override
    public InputStream getInputStream(String key) {
        return null;
    }

    @Override
    public OutputStream getOutputStream(String key) {
        return null;
    }

    @Override
    public boolean save(String key, InputStream inputStream) throws IOException {
        return false;
    }

    @Override
    public boolean save(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality) throws IOException {
        return false;
    }

    @Override
    public <T extends Serializable> boolean saveObject(String key, T serializable) throws IOException {
        return false;
    }

    @Override
    public <T extends Serializable> T getObject(String key, T clazz) {
        return null;
    }

    @Override
    public boolean remove(String key) {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void clear() throws IOException {

    }
}
