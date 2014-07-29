package com.hanyanan.tools.storage.disk;


import com.hanyanan.tools.datapersistence.DataError;
import com.hanyanan.tools.datapersistence.IAsyncObjectWorkStation;
import com.hanyanan.tools.datapersistence.IAsyncResult;
import com.hanyanan.tools.datapersistence.IObjectWorkStation;
import com.hanyanan.tools.datapersistence.IResult;
import com.hanyanan.tools.datapersistence.SimpleResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/18.
 */
public class ObjectStreamStorage implements IAsyncObjectWorkStation, IObjectWorkStation {

    private static final String TAG = ObjectStreamStorage.class.getSimpleName();
    private final FlexibleDiskStorage mFlexibleDiskStorage;
    private final static ExecutorService service = Executors.newSingleThreadExecutor();

    ObjectStreamStorage(FlexibleDiskStorage driver){
        mFlexibleDiskStorage = driver;
    }

    @Override
    public IResult put(final String key, final Serializable value, final long expireTime) {
        //TODO expireTime
        FlexibleDiskStorage.Editor editor = null;
        SimpleResult result = null;
        try {
            editor = mFlexibleDiskStorage.edit(key);
            ObjectOutputStream os = new ObjectOutputStream(editor.newOutputStream());
            os.writeObject(value);
            os.close();
            result = new SimpleResult(null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public IResult remove(String key) {
        SimpleResult result = null;
        try {
            mFlexibleDiskStorage.remove(key);
            result = new SimpleResult(null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void putAsync(String key, Serializable value, long expireTime, IAsyncResult listener) {
        Task task = new Task(PUT, key,value, listener);
        service.execute(task);
    }

    @Override
    public void removeAsync(String key, IAsyncResult listener) {
        Task task = new Task(REMOVE, key, listener);
        service.execute(task);
    }

    @Override
    public IResult get(String key) {
        SimpleResult result = null;
        try {
            FlexibleDiskStorage.Snapshot shot = mFlexibleDiskStorage.get(key);
            ObjectInputStream is = new ObjectInputStream(shot.getInputStream());
            Object res = is.readObject();
            is.close();
            result = new SimpleResult(res);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleResult(null, new DataError(DataError.FAILED, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = new SimpleResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void getAsync(String key, IAsyncResult listener) {
        Task task = new Task(GET, key, listener);
        service.execute(task);
    }

    private class Task implements Runnable{
        private int mCommand;
        private String mKey;
        private Serializable mContent;
        private IAsyncResult mListener;
        public Task(int command, String key, Serializable content, IAsyncResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
            mContent = content;
        }
        public Task(int command, String key, IAsyncResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
        }
        @Override
        public void run() {
            switch (mCommand){
                case PUT: {
                    IResult result = put(mKey, mContent, -1);
                    if (null == mListener) return;
                    IAsyncResult lis = mListener;
                    lis.onResult(mKey, result);
                    break;}
                case GET:{
                    IResult result = get(mKey);
                    if (null == mListener) return;
                    IAsyncResult lis = mListener;
                    lis.onResult(mKey, result);
                    break;}
                case REMOVE: {
                    IResult result = remove(mKey);
                    if (null == mListener) return;
                    IAsyncResult lis = mListener;
                    lis.onResult(mKey, result);
                    break;}
            }
        }
    }
}
