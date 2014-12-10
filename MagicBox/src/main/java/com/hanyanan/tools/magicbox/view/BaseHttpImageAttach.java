package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.hanyanan.tools.magicbox.MagicApplication;
import com.hanyanan.tools.magicbox.MagicUtils;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.DefaultResponseDelivery;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.HttpRequest;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/12/10.
 */
public abstract class BaseHttpImageAttach<T> implements HttpImageAttach,Response.Listener<T>,
        View.OnAttachStateChangeListener {
    protected String mUrl;
    protected final ImageView mImageView;
    protected HttpRequest mImageRequest = null;
    protected boolean attach = false;

    public BaseHttpImageAttach(ImageView imageView){
        mImageView = imageView;
        imageView.addOnAttachStateChangeListener(this);
    }

    @Override
    public void setUrl(String url,HashMap<String,String> header) {
        update(url, header, Cache.Mode.Disable);
    }

    @Override
    public void refresh(String url,HashMap<String,String> header) {
        update(url, header, Cache.Mode.Refresh);
    }

    protected final void update(String url,HashMap<String,String> header, Cache.Mode mode){
        MagicUtils.checkThreadState();
        cancel();
        if(TextUtils.isEmpty(url)){
            mImageView.setImageResource(getDefaultResourceId());
            return ;
        }
        mUrl = url;
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return ;
        LruCache<String, Bitmap> cache = mApp.getBitmapLruCache();
        Bitmap bitmap = cache.get(mUrl);//TODO
        if(null != bitmap) {
            mImageView.setImageBitmap(bitmap);
            return ;
        }

        mImageRequest = generateHttpRequest(header, mode);
        loadImageIfNecessary();
    }

    protected abstract HttpRequest generateHttpRequest(HashMap<String,String> header, Cache.Mode mode);


    protected final void loadImageIfNecessary(){
        MagicUtils.checkThreadState();
        if(!attach || null==mUrl) return ;
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp || null==mImageRequest) return ;

        mApp.getRequestQueue().add(mImageRequest);
    }

    @Override
    public ImageView getAttachedView() {
        return mImageView;
    }

    @Override
    public int getMaxWidth() {
        return mImageView.getWidth();
    }

    @Override
    public int getMaxHeight() {
        return mImageView.getHeight();
    }

    @Override
    public int getDefaultResourceId() {
        return 0;
    }

    @Override
    public int getFaultResourceId() {
        return 0;
    }

    @Override
    public void cancel() {
        MagicUtils.checkThreadState();
        if (mImageRequest != null) {
            mImageRequest.cancel();
        }
        mImageRequest = null;
    }

    @Override
    public String generateKey() {
        return String.valueOf(Math.abs(mUrl.hashCode()));
    }

    @Override
    public void onResponse(Request request, T response) {
        if(request == mImageRequest){
            Bitmap bitmap = createBitmap(response);
            if(null != bitmap) mImageView.setImageBitmap(bitmap);
            else mImageView.setImageResource(getFaultResourceId());
            mImageRequest = null;
        }
    }

    protected abstract Bitmap createBitmap(T res);

    @Override
    public void onCanceledResponse(Request request) {
        if(request == mImageRequest){
            mImageView.setImageResource(getDefaultResourceId());
            mImageRequest = null;
        }
    }

    @Override
    public void onErrorResponse(Request request, XError error) {
        if(request == mImageRequest){
            mImageView.setImageResource(getFaultResourceId());
            mImageRequest = null;
        }
    }

    @Override
    public void onViewAttachedToWindow(View view) {
        attach  = true;
        loadImageIfNecessary();
    }

    @Override
    public void onViewDetachedFromWindow(View view) {
        attach  = false;
        cancel();
    }
}
