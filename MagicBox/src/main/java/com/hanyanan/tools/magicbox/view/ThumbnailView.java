package com.hanyanan.tools.magicbox.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hanyanan.tools.magicbox.MagicUtils;
import com.hanyanan.tools.magicbox.R;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.HttpRequest;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/12/9.
 */
public class ThumbnailView extends ImageView implements Response.Listener<byte[]> {
    private String mUrl;
    private HttpRequest mHttpRequest = null;
    private boolean attach = false;

    public ThumbnailView(Context context) {
        super(context);
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void cancel(){
        if (mHttpRequest != null) {
            mHttpRequest.cancel();
        }
        mHttpRequest = null;
    }

    public void setUrl(String url) {
        MagicUtils.checkThreadState();
        cancel();

        if(TextUtils.isEmpty(url)){
            return ;
        }

        mUrl = url;


    }










    @Override
    public void onResponse(Request request, byte[] response) {

    }

    @Override
    public void onCanceledResponse(Request request) {

    }

    @Override
    public void onErrorResponse(Request request, XError error) {

    }
}
