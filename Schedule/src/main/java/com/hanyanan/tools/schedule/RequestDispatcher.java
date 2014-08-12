package com.hanyanan.tools.schedule;

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

    private final Cache mCache;

    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue Queue of incoming requests for triage
     * @param delivery Default delivery interface to use for posting responses
     */
    public RequestDispatcher(BlockingQueue<Request<?>> queue,Cache cache,
                             ResponseDelivery delivery) {
        mRequestQueue = queue;
        mCache = cache;
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
                CachePolicy cachePolicy = request.getCachePolicy();
                RequestExecutor re  = request.getRequestExecutor();
                ResponseDelivery rd = request.getResponseDelivery();
                if(null == rd) rd = this.mDefaultDelivery;
                //try get from cache
                if(cachePolicy!=null && !cachePolicy.skipCache() && cachePolicy.canReadFromCache()){
                    //TODO
                    request.addMarker("read-cache-complete");
                    return;
                }

                Response response = re.performRequest(request);
                request.addMarker("request-complete");
                // Post the response back.
                rd.postResponse(request, response);
                request.markDelivered();
                //check if need to insert to cache.
                if (null != cachePolicy && !cachePolicy.skipCache() && cachePolicy.shouldCache()) {
                    //mCache.put(cachePolicy.getCacheKey(), response.cacheEntry);
                    //TODO
                    request.addMarker("network-cache-written");
                }
            } catch (XError volleyError) {
//                parseAndDeliverNetworkError(request, volleyError);
                if(request.isCanceled()){
                    request.finish("discard-cancelled");
                }else{
                    if(request.attemptRetryOnError(volleyError)) {
                        mRequestQueue.add(request);
                    }else{
                        request.markDelivered();
                        ResponseDelivery rd = request.getResponseDelivery();
                        if(null == rd) rd = this.mDefaultDelivery;
                        rd.postError(request, volleyError);
                    }
                }
            } catch (Exception e) {
//                VolleyLog.e(e, "Unhandled exception %s", e.toString());
//                mDelivery.postError(request, new VolleyError(e));
                if(request.isCanceled()){
                    request.finish("discard-cancelled");
                }else{
                    request.markDelivered();
                    ResponseDelivery rd = request.getResponseDelivery();
                    if(null == rd) rd = this.mDefaultDelivery;
                    rd.postError(request, new XError(e));
                }
            }
        }
    }
}
