package com.hanyanan.tools.storage.Error;

/**
 * Created by hanyanan on 2014/7/28.
 */
public class Error extends Exception{
    public Error() { super();}

    public Error(java.lang.String detailMessage) {super(detailMessage);}

    public Error(java.lang.String detailMessage, java.lang.Throwable throwable) {
        super(detailMessage, throwable);
    }

    public Error(java.lang.Throwable throwable) { super(throwable); }
}
