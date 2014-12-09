package com.hanyanan.tools.storage.disk;

import android.graphics.Bitmap;

import com.hanyanan.tools.storage.Error.BusyInUsingError;
import com.hanyanan.tools.storage.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Administrator on 2014/8/20.
 * This class is unlimited, which means there is no size limited and age limited.
 */
class BasicDiskStorage implements DiskStorage{
    protected final static int VERSION = 1;
    

    private final IStreamStorage mStreamStorage;
    BasicDiskStorage(IStreamStorage streamStorage){
        mStreamStorage = streamStorage;
    }

    BasicDiskStorage(File rootDirectory){
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
        return mStreamStorage.getRootFile();
    }

    public void setOverFlowRemoveListener(DiskStorage.OverFlowRemoveListener overFlowRemoveListener){
        mStreamStorage.setOverFlowRemoveListener(overFlowRemoveListener);
    }

    @Override
    public long getCurrentSize() {
        if(null == mStreamStorage) return -1;
        return mStreamStorage.getCurrentSize();
    }

    @Override
    public File get(String key) {
        throw new UnsupportedOperationException("Unsupported get operation.");
    }

    /**
     * User must close it.
     * @param key key
     * @return
     */
    @Override
    public InputStream getInputStream(String key) {
        try {
            IStreamStorage.Snapshot snapShot = mStreamStorage.get(key);
            if(null == snapShot) return null;
            return new InputStreamWrapper(snapShot, snapShot.getInputStream());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * User must close it.
     * @param key
     * @return
     */
    @Override
    public OutputStream getOutputStream(String key) {
        try {
            IStreamStorage.Editor editor = mStreamStorage.edit(key);
            if(null == editor) return null;
            OutputStream outputStream = editor.newOutputStream();
            if(null == outputStream){
                editor.abort();
                editor.close();
                return null;
            }
            return new OutputStreamWrapper(editor,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (BusyInUsingError busyInUsingError) {
            busyInUsingError.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean save(String key, InputStream inputStream) throws IOException {
        return save(key, inputStream, DEFAULT_COPIER, REST_STREAM_SIZE, VALID_FOREVER);
    }

    @Override
    public boolean save(String key, InputStream inputStream, Copier copier, long size, long expireTime) throws IOException {
        if(null == copier) throw new NullPointerException("");
        if(null == inputStream) return false;
        IStreamStorage.Editor editor = null;
        try {
            editor = mStreamStorage.edit(key);
        } catch (BusyInUsingError busyInUsingError) {
            busyInUsingError.printStackTrace();
            if(null != editor) editor.abort();
            return false;
        }
        if(null == editor) return false;
        OutputStream outputStream = editor.newOutputStream();
        if(null == outputStream){
            editor.abort();
            editor.close();
            return false;
        }
        copier.copy(inputStream, outputStream, size);
        outputStream.close();
        editor.setExpireTime(expireTime);
        editor.commit();
        return true;
    }

    @Override
    public boolean save(String key, InputStream inputStream, long expireTime) throws IOException {
        return save(key, inputStream, DEFAULT_COPIER, REST_STREAM_SIZE, expireTime);
    }

    @Override
    public boolean save(String key, InputStream inputStream, Copier copier,long expireTime) throws IOException {
        return save(key, inputStream, copier, REST_STREAM_SIZE, expireTime);
    }

    @Override
    public boolean save(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality) throws IOException {

        return save(key,bitmap,format,quality, Long.MAX_VALUE);
    }

    @Override
    public boolean save(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality, long expireTime) throws IOException {
        boolean savedSuccessfully = false;
        boolean commitSuccessfully = false;
        if(null == bitmap || format==null) return false;
        IStreamStorage.Editor editor = null;
        try {
            editor = mStreamStorage.edit(key);
        } catch (BusyInUsingError busyInUsingError) {
            busyInUsingError.printStackTrace();
            return false;
        }
        if(null == editor) return false;
        OutputStream outputStream = editor.newOutputStream();
        if(null == outputStream){
            editor.close();
            return false;
        }
        savedSuccessfully = bitmap.compress(format, quality, outputStream);
        outputStream.close();
        editor.setExpireTime(expireTime);
        commitSuccessfully = editor.commit();

        return savedSuccessfully&commitSuccessfully;
    }

    @Override
    public <T extends Serializable> boolean saveObject(String key, T serializable) throws IOException {
        return saveObject(key, serializable, VALID_FOREVER);
    }

    @Override
    public <T extends Serializable> boolean saveObject(String key, T serializable, long expireTime) throws IOException {
        IStreamStorage.Editor editor = null;
        try {
            editor = mStreamStorage.edit(key);
            if(null == editor) return false;
            OutputStream outputStream = editor.newOutputStream();
            if(null == outputStream){
                return false;
            }
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(serializable);
            out.close();
            outputStream.close();
            editor.setExpireTime(expireTime);
            return editor.commit();
        } catch (BusyInUsingError busyInUsingError) {
            busyInUsingError.printStackTrace();
            editor.abort();
            return false;
        }finally {
            if(null != editor) editor.close();
        }
    }

    @Override
    public <T extends Serializable> T getObject(String key, Class<T> clazz) throws IOException{
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(key);
            if(null == inputStream) return null;
            ObjectInputStream in = new ObjectInputStream(inputStream);

            T content = (T)in.readObject();

            in.close();
            inputStream.close();
            inputStream = null;
            return content;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(null != inputStream)  inputStream.close();
        }
    }

    @Override
    public boolean remove(String key) {
        try {
            return mStreamStorage.remove(key);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean contains(String key) {
        return mStreamStorage.contain(key);
    }

    @Override
    public void close() {
        try {
            mStreamStorage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() throws IOException {
        //TODO
    }
}
