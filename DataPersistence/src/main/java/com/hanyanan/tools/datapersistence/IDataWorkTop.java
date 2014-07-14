package com.hanyanan.tools.datapersistence;

import com.hanyanan.tools.datapersistence.IDataPersistence.Type;
/**
 * Created by hanyanan on 2014/7/14.
 * From this work top, you could do anything on data.
 */
public interface IDataWorkTop<T> {
    public interface IDataReadAsyncListener<T>{
        public void onReadCompleted(String key,Type type, IDataResult<T> result);
    }
    public interface IDataWriteAsyncListener<T>{
        public void onWriteCompleted(String key,Type type, IDataResult<T> result);
    }
    public interface IDataTypeChangedListener{
        public void onDataTypeChanged(Type prevType, Type newType);
    }
    public interface IDataRemovedListener<T>{
        public void onRemoveCompleted(String key,Type type, IDataResult<T> result);
    }
    public void changeDataType(Type type);
    public void changeDataTypeAsync(Type type, IDataTypeChangedListener listener);
    public T read(String key);
    public void readAsync(String key, IDataReadAsyncListener<T> listener);
    public T write(String key, T data);
    public void writeAsync(String key, T data, IDataWriteAsyncListener<T> listener);
    public T remove(String key);
    public void clearAsync(String key, IDataRemovedListener<T> listener);
}
