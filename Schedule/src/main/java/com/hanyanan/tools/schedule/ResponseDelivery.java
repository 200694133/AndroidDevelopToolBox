package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/7/29.
 */
public interface ResponseDelivery {
    /**
     * Parses a runnable from the request or cache and delivers it.
     */
    public void postRunnable(Request<?> request, Runnable response);
    /**
     * Parses a response from the network or cache and delivers it.
     */
    public void postResponse(Request<?> request, Response<?> response);

    /**
     * Parses a response from the network or cache and delivers it. The provided
     * Runnable will be executed after delivery.
     */
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    /**
     * Posts an error for the given request.
     */
    public void postError(Request<?> request, XError error);

    /**
     * Post an canceled state to given request.
     */
    public void postCanceled(Request request);
}