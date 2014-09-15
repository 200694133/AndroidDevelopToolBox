package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/9/12.
 */
public class FaultError extends Exception {
    public final FaultError networkResponse;

    public FaultError() {
        networkResponse = null;
    }

    public FaultError(String exceptionMessage) {
        super(exceptionMessage);
        networkResponse = null;
    }

    public FaultError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public FaultError(Throwable cause) {
        super(cause);
        networkResponse = null;
    }
}
