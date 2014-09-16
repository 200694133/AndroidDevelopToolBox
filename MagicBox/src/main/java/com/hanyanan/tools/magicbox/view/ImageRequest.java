package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import com.hanyanan.tools.magicbox.MagicApplication;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.NetworkRequest;
import com.hanyanan.tools.storage.disk.DiskStorage;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/13.
 */
class ImageRequest extends NetworkRequest {
    private static final BitmapRequestExecutor S_BITMAP_REQUEST_EXECUTOR = new BitmapRequestExecutor();
    private int mMaxWidth, mMaxHeight;

    public void setMaxWidth(int width){
        mMaxWidth = width;
    }

    public void setMaxHeight(int height){
        mMaxHeight = height;
    }

    public DiskStorage getFixSizeDiskStorage(){
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return null;
        return mApp.getFixSizeDiskStorage();
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
    }

    private static HttpRequestParam parseHttpRequestParam(String url){
        HttpRequestParam param = new HttpRequestParam(url);
        param.setRequestMethod(HttpRequestParam.Method.GET);
        param.setTransactionType(HttpRequestParam.TransactionType.STREAM);
        return param;
    }
}
