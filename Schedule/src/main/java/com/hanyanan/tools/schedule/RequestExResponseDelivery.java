package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/9/15.
 */
public interface RequestExResponseDelivery {
    /** Delivery status change for a request. */
    public void postStatusChanged(Request request,Request.Status status);

    /** Request enqueue and delivers it. */
    public void postRequestEnqueue(Request request);

    /** Request re-enqueue and delivers it. */
    public void postRequestReEnqueue(Request request);

    public void postRequestRunning(Request request);

    public void postRequestSuccess(Request request);

    public void postRequestCanceled(Request request);

    public void postRequestFailed(Request request,Throwable exception);
}
