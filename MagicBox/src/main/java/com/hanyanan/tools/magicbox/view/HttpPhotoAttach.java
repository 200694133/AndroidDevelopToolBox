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
import com.hanyanan.tools.magicbox.R;
import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.DefaultResponseDelivery;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/12/9.
 */
public class HttpPhotoAttach extends BaseHttpImageAttach<Bitmap> {
    public HttpPhotoAttach(ImageView imageView) {
        super(imageView);
    }

    @Override
    protected HttpRequest generateHttpRequest(HashMap<String, String> header, Cache.Mode mode) {
        ImageRequest imageRequest = new ImageRequest(MagicApplication.getInstance().getRequestQueue(),
                mUrl,new DefaultResponseDelivery(new Handler(Looper.getMainLooper())),this );
        imageRequest.setCacheMode(Cache.Mode.Refresh);
        imageRequest.setHttpCache(MagicApplication.getInstance().getHttpCache());
        imageRequest.setKey(generateKey());
        if(getMaxWidth() >0 && getMaxWidth() < 10000) imageRequest.setMaxWidth(getMaxWidth());
        if(getMaxHeight() >0 && getMaxHeight() < 10000) imageRequest.setMaxHeight(getMaxHeight());
        if(null != header) imageRequest.getRequestParam().setHeaderProperty(header);
        return imageRequest;
    }
    @Override
    public void setUrl(String url, HashMap<String, String> header) {
        update(url, header, Cache.Mode.SimpleMode);
    }

    @Override
    protected Bitmap createBitmap(Bitmap res) {
        return res;
    }
}
