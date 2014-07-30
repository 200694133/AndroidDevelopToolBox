package com.hanyanan.tools.xasynctask;

import android.os.*;
import android.os.Process;

import java.util.concurrent.BlockingQueue;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class RequestDispatcher extends Thread{
    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mRequestQueue;

    /** For posting responses and errors. */
    private final ResponseDelivery mDefaultDelivery;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue Queue of incoming requests for triage
     * @param delivery Default delivery interface to use for posting responses
     */
    public RequestDispatcher(BlockingQueue<Request<?>> queue,
                             ResponseDelivery delivery) {
        mRequestQueue = queue;
        mDefaultDelivery = delivery;
    }
    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }


    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request<?> request;
        while (true) {
            try {
                // Take a request from the queue.
                request = mRequestQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
                request.addMarker("request-queue-take");

                // If the request was cancelled already, do not perform the
                // network request.
                if (request.isCanceled()) {
                    request.finish("network-discard-cancelled");
                    continue;
                }
//                addTrafficStatsTag(request);

                // Perform the network request.
//                NetworkResponse networkResponse = mNetwork.performRequest(request);
//                request.addMarker("network-http-complete");

                RequestExecutor re  = request.getRequestExecutor();
                ResponseDelivery rd = request.getResponseDelivery();
                Response response = re.performRequest(request);
                request.addMarker("request-complete");
                // Post the response back.
                request.markDelivered();
//                mDelivery.postResponse(request, response);
                rd.postResponse(request, response);
            } catch (XError volleyError) {
//                parseAndDeliverNetworkError(request, volleyError);
                if(request.isCanceled()){
                    request.finish("discard-cancelled");
                }else{
                    if(request.attemptRetryOnError(volleyError)) {
                        mRequestQueue.add(request);
                    }else{
                        request.markDelivered();
                        request.getResponseDelivery().postError(request, volleyError);
                    }
                }
            } catch (Exception e) {
//                VolleyLog.e(e, "Unhandled exception %s", e.toString());
//                mDelivery.postError(request, new VolleyError(e));
                if(request.isCanceled()){
                    request.finish("discard-cancelled");
                }else{
                    request.markDelivered();
                    request.getResponseDelivery().postError(request, new XError(e));
                }
            }
        }
    }
}
