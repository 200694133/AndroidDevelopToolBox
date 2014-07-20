package com.hanyanan.tools.datapersistence;

import android.os.Environment;

import com.hanyanan.tools.datapersistence.stream.StreamDataPersistence;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanyanan on 2014/7/18.
 */
public class ObjectStreamDataPersistence implements IObjectWorkTop{
    private static final int PUT = 0x01;
    private static final int REMOVE = 0x02;
    private static final int GET = 0x03;
    private static final String TAG = ObjectStreamDataPersistence.class.getSimpleName();
    private final StreamDataPersistence mStreamDataPersistence;
    private final static ExecutorService service = Executors.newSingleThreadExecutor();

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
    public IDataResult put(final String key, final Serializable value) {
        final Type type = value.getClass().getGenericSuperclass();
        value.getClass().cast(new Object());
        StreamDataPersistence.Editor editor = null;
        SimpleDataResult<type> result = null;
        try {
            StreamDataPersistence.Snapshot shot = mStreamDataPersistence.get(key);
            ObjectInputStream is = new ObjectInputStream(shot.getInputStream());
            Type res = ()is.readObject();
            is.close();
            editor = mStreamDataPersistence.edit(key);
            ObjectOutputStream os = new ObjectOutputStream(editor.newOutputStream());
            os.writeObject(value);
            os.close();
            result = new SimpleDataResult<Type>(res);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void putAsync(String key, final Serializable content,IOnObjectResult listener) {
        Task task = new Task(PUT, key,content, listener);
        service.execute(task);
    }

    @Override
    public IDataResult remove(String key, Class clazz) {
        final Type type = clazz.getGenericSuperclass();
        SimpleDataResult<Type> result = null;
        try {
            StreamDataPersistence.Snapshot shot = mStreamDataPersistence.get(key);
            ObjectInputStream is = new ObjectInputStream(shot.getInputStream());
            Type res = (Type)is.readObject();
            is.close();
            mStreamDataPersistence.remove(key);
            result = new SimpleDataResult<Type>(res);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void removeAsync(String key, Class clazz, IOnObjectResult listener) {
        Task task = new Task(REMOVE, key, clazz, listener);
        service.execute(task);
    }

    @Override
    public IDataResult get(String key, Class clazz) {
        final Type type = clazz.getGenericSuperclass();
        SimpleDataResult<Type> result = null;
        try {
            StreamDataPersistence.Snapshot shot = mStreamDataPersistence.get(key);
            ObjectInputStream is = new ObjectInputStream(shot.getInputStream());
            Type res = (Type)is.readObject();
            is.close();
            result = new SimpleDataResult<Type>(res);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = new SimpleDataResult<Type>(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void getAsync(String key, Class clazz, IOnObjectResult listener) {
        Task task = new Task(GET, key, clazz, listener);
        service.execute(task);
    }

    private class Task implements Runnable{
        private int mCommand;
        private String mKey;
        private Serializable mContent;
        private IOnObjectResult mListener;
        private Class mClazz;
        public Task(int command, String key, Serializable content, IOnObjectResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
            mContent = content;
        }
        public Task(int command, String key, Class clazz, IOnObjectResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
            mClazz = clazz;
        }
        @Override
        public void run() {
            switch (mCommand){
                case PUT: {
                    IDataResult result = put(mKey, mContent);
                    if (null == mListener) return;
                    IOnObjectResult lis = (IOnObjectResult) mListener;
                    lis.onResult(mKey, result);
                    break;}
                case GET:{
                    IDataResult result = get(mKey, mClazz);
                    if (null == mListener) return;
                    IOnObjectResult lis = (IOnObjectResult) mListener;
                    lis.onResult(mKey, result);
                    break;}
                case REMOVE: {
                    IDataResult result = remove(mKey, mClazz);
                    if (null == mListener) return;
                    IOnObjectResult lis = (IOnObjectResult) mListener;
                    lis.onResult(mKey, result);
                    break;}
            }
        }
    }
}
