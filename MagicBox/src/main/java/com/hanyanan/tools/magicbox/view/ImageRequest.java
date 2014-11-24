package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import com.hanyanan.tools.magicbox.MagicApplication;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.storage.disk.DiskStorage;

/**
 * Created by hanyanan on 2014/8/13.
 */
class ImageRequest extends HttpRequest {
    private static final BitmapRequestExecutor S_BITMAP_REQUEST_EXECUTOR = new BitmapRequestExecutor();
    private int mMaxWidth, mMaxHeight;
    private String mKey;
    public void setMaxWidth(int width){
        mMaxWidth = width;
    }

    public void setMaxHeight(int height){
        mMaxHeight = height;
    }

    public int getMaxWidth(){
        return mMaxWidth;
    }

    public int getMaxHeight(){
        return mMaxHeight;
    }

    public ImageRequest(RequestQueue requestQueue,String url,ResponseDelivery delivery,
                        Response.Listener<Bitmap> listener) {
        super(requestQueue, S_BITMAP_REQUEST_EXECUTOR,parseHttpRequestParam(url));
        setListener(listener);
        setCacheMode(Cache.Mode.SimpleMode);
    }

    public ImageRequest setKey(String key){
        mKey = key;
        return this;
    }

    public String getKey(){
        if(null != mKey) return mKey;
        return super.getKey();
    }

    private static HttpRequestParam parseHttpRequestParam(String url){
        HttpRequestParam param = new HttpRequestParam(url);
        param.setRequestMethod(HttpRequestParam.Method.GET);
        param.setTransactionType(HttpRequestParam.TransactionType.STREAM);
        return param;
    }
}
