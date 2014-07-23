package com.hanyanan.tools.datapersistence.storage;

import android.widget.BaseAdapter;

import com.hanyanan.tools.datapersistence.IBaseDataWorkStation;
import com.hanyanan.tools.datapersistence.IResult;

/**
 * Created by hanyanan on 2014/7/23.
 */
class BaseDataRunnable<T> implements Runnable{
    IResult<T> result;
    private int mOperation;
    private IBaseDataWorkStation mBaseWorkStation;
    private String mKey;
    BaseDataRunnable(int operation, IBaseDataWorkStation workStation, String key){
        mOperation = operation;
        mBaseWorkStation = workStation;
        mKey = key;
    }

    @Override
    public void run() {
        if(T.class.asSubclass()  )
    }
}
