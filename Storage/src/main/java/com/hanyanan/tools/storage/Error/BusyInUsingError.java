package com.hanyanan.tools.storage.Error;

/**
 * Created by hanyanan on 2014/7/28.
 */
public class BusyInUsingError extends Error{
    public BusyInUsingError() { super();}

    public BusyInUsingError(java.lang.String detailMessage) {super(detailMessage);}

    public BusyInUsingError(java.lang.String detailMessage, java.lang.Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BusyInUsingError(java.lang.Throwable throwable) { super(throwable); }
}
