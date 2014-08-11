package com.hanyanan.tools.data;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2014/8/11.
 */
public class DataChangeHandler<T>{
    private final Handler mHandler;
    public DataChangeHandler(Handler handler){
        mHandler = handler;
    }
    private final List<Node.OnDataChangedListener<T>> nOnDataChangedListeners = new ArrayList<Node.OnDataChangedListener<T>>();
    public void registerDataChangeListener(Node.OnDataChangedListener<T> lis){
        synchronized (this){
            nOnDataChangedListeners.remove(lis);
        }
    }
    public void unregisterDataChangeListener(Node.OnDataChangedListener<T> lis){
        synchronized (this){
            if(!nOnDataChangedListeners.contains(lis)){
                nOnDataChangedListeners.add(lis);
            }
        }
    }

    public void notifyDataChange(T newData){
        List<Node.OnDataChangedListener<T>> all = new ArrayList<Node.OnDataChangedListener<T>>();
        synchronized (this) {
            all.addAll(nOnDataChangedListeners);
        }
        for(Node.OnDataChangedListener<T> lis : all){
            lis.onDataChanged(newData);
        }
    }

}
