package com.hanyanan.tools.magicbox.data;

import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.BasicNetwork;
import com.hanyanan.tools.schedule.http.GsonObjectRequestExecutor;
import com.hanyanan.tools.schedule.http.HttpConnectionExecutor;
import com.hanyanan.tools.schedule.http.NetworkRequest;

/**
 * Created by hanyanan on 2014/8/11.
 */
public class JsonNode<T> extends Node<T> implements Response.Listener<T>, Response.ErrorListener{
    private final NetworkRequest<T> mNetworkRequest;
    GsonObjectRequestExecutor<T> mObjectExecutor = new GsonObjectRequestExecutor<T>(new BasicNetwork(new HttpConnectionExecutor()));
    private final RequestQueue mRequestQueue;
    public JsonNode(RequestQueue requestQueue,String url,Object tag, boolean isPositive) {
        super(tag, isPositive);
        mRequestQueue = requestQueue;
        mNetworkRequest = new NetworkRequest<T>(url,null,mObjectExecutor,null,this);
        mNetworkRequest.setListener(this);
    }
    @Override
    public void fetchData() {
        mNetworkRequest.reset();
        mRequestQueue.add(mNetworkRequest);
    }
    @Override
    public void onErrorResponse(XError error) {
        setError(error.toString());
    }

    @Override
    public void onResponse(T response) {
        setNewData(response);
    }
}
