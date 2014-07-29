package com.hanyanan.tools.xasynctask;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class XError extends Exception{
    public XError() {super();}

    public XError(java.lang.String detailMessage) { super(detailMessage); }

    public XError(java.lang.String detailMessage, java.lang.Throwable throwable) { super(detailMessage,throwable); }

    public XError(java.lang.Throwable throwable) {  super(throwable);  }
}
