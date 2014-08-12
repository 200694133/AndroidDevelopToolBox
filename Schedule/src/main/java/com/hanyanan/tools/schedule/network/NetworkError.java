package com.hanyanan.tools.schedule.network;

import com.hanyanan.tools.schedule.XError;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class NetworkError extends XError{
    public final NetworkResponse networkResponse;

    public NetworkError() {
        networkResponse = null;
    }

    public NetworkError(NetworkResponse response) {
        networkResponse = response;
    }

    public NetworkError(String exceptionMessage) {
        super(exceptionMessage);
        networkResponse = null;
    }

    public NetworkError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public NetworkError(Throwable cause) {
        super(cause);
        networkResponse = null;
    }
}
