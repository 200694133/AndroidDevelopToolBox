package com.hanyanan.tools.storage.Error;

/**
 * Created by hanyanan on 2014/7/28.
 */
public class TypeNotSupportError extends Error{
    public TypeNotSupportError() { super();}

    public TypeNotSupportError(String detailMessage) {super(detailMessage);}

    public TypeNotSupportError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TypeNotSupportError(Throwable throwable) { super(throwable); }
}
