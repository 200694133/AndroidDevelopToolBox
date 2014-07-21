package com.hanyanan.tools.datapersistence;


import com.hanyanan.tools.datapersistence.stream.StreamDataPersistence;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/18.
 */
public class ObjectStreamWorktop implements IObjectWorkTop{
    private static final int PUT = 0x01;
    private static final int REMOVE = 0x02;
    private static final int GET = 0x03;
    private static final String TAG = ObjectStreamWorktop.class.getSimpleName();
    private final StreamDataPersistence mStreamDataPersistence;
    private final static ExecutorService service = Executors.newSingleThreadExecutor();

    public ObjectStreamWorktop(File directory){
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
        StreamDataPersistence.Editor editor = null;
        SimpleDataResult result = null;
        try {
            editor = mStreamDataPersistence.edit(key);
            ObjectOutputStream os = new ObjectOutputStream(editor.newOutputStream());
            os.writeObject(value);
            os.close();
            result = new SimpleDataResult(null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void putAsync(String key, final Serializable content,IOnObjectResult listener) {
        Task task = new Task(PUT, key,content, listener);
        service.execute(task);
    }

    @Override
    public IDataResult remove(String key) {
        SimpleDataResult result = null;
        try {
            mStreamDataPersistence.remove(key);
            result = new SimpleDataResult(null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void removeAsync(String key, IOnObjectResult listener) {
        Task task = new Task(REMOVE, key, listener);
        service.execute(task);
    }

    @Override
    public IDataResult get(String key) {
        SimpleDataResult result = null;
        try {
            StreamDataPersistence.Snapshot shot = mStreamDataPersistence.get(key);
            ObjectInputStream is = new ObjectInputStream(shot.getInputStream());
            Object res = is.readObject();
            is.close();
            result = new SimpleDataResult(res);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SimpleDataResult(null, new DataError(DataError.FAILED, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = new SimpleDataResult(null, new DataError(DataError.FAILED, e.toString()));
        }
        return result;
    }

    @Override
    public void getAsync(String key, IOnObjectResult listener) {
        Task task = new Task(GET, key, listener);
        service.execute(task);
    }

    private class Task implements Runnable{
        private int mCommand;
        private String mKey;
        private Serializable mContent;
        private IOnObjectResult mListener;
        public Task(int command, String key, Serializable content, IOnObjectResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
            mContent = content;
        }
        public Task(int command, String key, IOnObjectResult listener){
            mCommand = command;
            mListener = listener;
            mKey = key;
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
                    IDataResult result = get(mKey);
                    if (null == mListener) return;
                    IOnObjectResult lis = (IOnObjectResult) mListener;
                    lis.onResult(mKey, result);
                    break;}
                case REMOVE: {
                    IDataResult result = remove(mKey);
                    if (null == mListener) return;
                    IOnObjectResult lis = (IOnObjectResult) mListener;
                    lis.onResult(mKey, result);
                    break;}
            }
        }
    }
}
