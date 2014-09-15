package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class XError extends Exception{
    private final Throwable mThrowable;
    public XError() {
        super();
        mThrowable = null;
    }

    public Throwable getThrowable(){
        return mThrowable;
    }

    public XError(java.lang.Throwable throwable) {
        super(throwable);
        mThrowable = throwable;
    }
}
