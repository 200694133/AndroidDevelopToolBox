package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/9/15.
 */
public class RequestExStateListener {

    /** Callback interface for delivering status. */
    public interface StatusListener{
        /** called when status Changed. */
        public void onStatusChange(Request request,Request.Status status);
    }

    /** Callback interface for state changed listener. */
    public interface StateListener{
        public void onEnqueue(Request request);
        public void onReEnqueue(Request request);
        public void onRunning(Request request);
        public void onSuccess(Request request);
        public void onCanceled(Request request);
        public void onFailed(Request request, Throwable exception);
    }
}
