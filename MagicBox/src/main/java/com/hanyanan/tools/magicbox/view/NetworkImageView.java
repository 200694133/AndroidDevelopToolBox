package com.hanyanan.tools.magicbox.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.hanyanan.tools.magicbox.MagicApplication;
import com.hanyanan.tools.magicbox.MagicUtils;
import com.hanyanan.tools.magicbox.R;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.DefaultResponseDelivery;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.*;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/12.
 */
public class NetworkImageView extends ImageView implements Response.Listener<Bitmap>{
//    public static int sDefaultImageId = R.drawable.ic_launcher;
//    public static int sDefaultFaultId = R.drawable.ic_launcher;
//    private int mDefaultImageId = sDefaultImageId;
//    private int mDefaultFaultId = sDefaultFaultId;
    private String mUrl;
    private ImageRequest mImageRequest = null;
    private boolean attach = false;
    public NetworkImageView(Context context) {
        super(context);
    }
    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
//    public void setDefaultImageId(int id){
//        MagicUtils.checkThreadState();
//        mDefaultImageId = id;
//    }
    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
//    public void setDefaultFaultId(int id){
//        MagicUtils.checkThreadState();
//        mDefaultFaultId = id;
//    }
    public void cancelUrl(){
        if (mImageRequest != null) {
            mImageRequest.cancel();
        }
        mImageRequest = null;
    }
    public void setUrl(String url){
        setUrl(url,null,null);
    }
    public void setUrl(String url, boolean refresh){
        setUrl(url,null,null, refresh?Cache.Mode.Refresh:Cache.Mode.SimpleMode);
    }
    public void setUrl(String url,HashMap<String,String> property,String key){
        setUrl(url,property,key,Cache.Mode.SimpleMode);
    }

    public void setUrl(String url,HashMap<String,String> property,String key, Cache.Mode mode){
        MagicUtils.checkThreadState();

//        if(mUrl == url) return ;
//        if(!TextUtils.isEmpty(mUrl) && mUrl.equals(url)) return ;

        if (mImageRequest != null) {
            mImageRequest.cancel();
        }
        mImageRequest = null;
//        setImageBitmap(null);
        mUrl = url;
        if(TextUtils.isEmpty(url)){
            this.setImageResource(R.drawable.ic_launcher);
            return ;
        }

        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return ;
        LruCache<String, Bitmap> cache = mApp.getBitmapLruCache();
        Bitmap bitmap = cache.get(mUrl);
        if(null != bitmap) {
            this.setImageBitmap(bitmap);
            return ;
        }

        mImageRequest = new ImageRequest(MagicApplication.getInstance().getRequestQueue(),
                mUrl,new DefaultResponseDelivery(new Handler(Looper.getMainLooper())),this );
        mImageRequest.setCacheMode(mode);
        mImageRequest.setHttpCache(MagicApplication.getInstance().getHttpCache());
        mImageRequest.setKey(key);
        if(null != property) mImageRequest.getRequestParam().setHeaderProperty(property);
        loadImageIfNecessary();
    }


    private void loadImageIfNecessary(){
        if(!attach || null==mUrl) return ;
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return ;
        if(null != mImageRequest){
            if(null != mMaxWidth)mImageRequest.setMaxWidth(mMaxWidth);
            if(null != mMaxHeight) mImageRequest.setMaxHeight(mMaxHeight);
//            mImageRequest.setMaxWidth(getMaxWidth());
//            mImageRequest.setMaxHeight(getMaxHeight());
            mApp.getRequestQueue().add(mImageRequest);
        }
    }

    private Integer mMaxWidth = null;
    private Integer mMaxHeight = null;
    public void setMaxWidth(int width){
        mMaxWidth = width;
        super.setMaxWidth(width);
    }

    public void setMaxHeight(int height){
        mMaxHeight = height;
        super.setMaxHeight(height);
    }


//    void loadImageIfNecessary(HashMap<String,String> property){
//        if(!attach || null==mUrl) return ;
//        MagicApplication mApp = MagicApplication.getInstance();
//        if(null == mApp) return ;
//        LruCache<String, Bitmap> cache = mApp.getBitmapLruCache();
//        Bitmap bitmap = cache.get(mUrl);
//        if(null != bitmap) {
//            this.setImageBitmap(bitmap);
//        }
//        mImageRequest = new ImageRequest(MagicApplication.getInstance().getRequestQueue(),
//                mUrl,new DefaultResponseDelivery(new Handler(Looper.getMainLooper())),this );
//        if(null != property) mImageRequest.getRequestParam().setHeaderProperty(property);
//        mImageRequest.setMaxWidth(getMaxWidth());
//        mImageRequest.setMaxHeight(getMaxHeight());
//        mApp.getRequestQueue().add(mImageRequest);
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        attach  = true;
        loadImageIfNecessary();
    }
    @Override
    protected void onDetachedFromWindow() {
        attach  = false;
        if (mImageRequest != null) {
            mImageRequest.cancel();
            setImageBitmap(null);
        }
        mImageRequest = null;
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public void onErrorResponse(Request request, XError error) {
//        this.setImageResource(mDefaultFaultId);
        Log.d("ddd", "error "+error.toString());
    }

    @Override
    public void onResponse(Request request, Bitmap response) {
        if(null == mImageRequest) return ;
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return ;
        LruCache<String, Bitmap> cache = mApp.getBitmapLruCache();
        if (null != response && null != cache) {
            cache.put(mUrl, response);
        }
        this.setImageBitmap(response);
    }

    @Override
    public void onCanceledResponse(Request request) {
        //TODO
        Log.d("ddd", "onCanceledResponse");
    }
}
