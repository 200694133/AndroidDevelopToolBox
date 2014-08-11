package com.hanyanan.tools.data;

import com.hanyanan.tools.xasynctask.RequestQueue;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.XError;
import com.hanyanan.tools.xasynctask.network.BasicNetwork;
import com.hanyanan.tools.xasynctask.network.GsonObjectRequestExecutor;
import com.hanyanan.tools.xasynctask.network.HurlStack;
import com.hanyanan.tools.xasynctask.network.NetworkRequest;

/**
 * Created by hanyanan on 2014/8/11.
 */
public class JsonNode<T> extends Node<T> implements Response.Listener<T>, Response.ErrorListener{
    private final NetworkRequest<T> mNetworkRequest;
    GsonObjectRequestExecutor<T> mObjectExecutor = new GsonObjectRequestExecutor<T>(new BasicNetwork(new HurlStack()));
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
        //TODO
    }

    @Override
    public void onResponse(T response) {
        setNewData(response);
    }
}
