package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.RawRequestExecutor;
import com.hanyanan.tools.schedule.http.cache.CacheableHttpConnectionImpl;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/12/10.
 */
public class HttpThumbnailAttach extends BaseHttpImageAttach<byte[]>  {

    public HttpThumbnailAttach(ImageView imageView) {
        super(imageView);
    }

    @Override
    protected HttpRequest generateHttpRequest(HashMap<String, String> header, Cache.Mode mode) {
        HttpRequest httpRequest = new HttpRequest(MagicApplication.getInstance().getRequestQueue(),
                new RawRequestExecutor(),new HttpRequestParam(mUrl), mode);
//        httpRequest.setHttpCache(MagicApplication.getInstance().getHttpCache());
        httpRequest.setKey(generateKey());
        httpRequest.setResponseDelivery(new DefaultResponseDelivery(new Handler(Looper.getMainLooper())));
        httpRequest.setListener(this);
        if(null != header) httpRequest.getRequestParam().setHeaderProperty(header);
        return httpRequest;
    }

    @Override
    public void setUrl(String url, HashMap<String, String> header) {
        update(url, header, Cache.Mode.Disable);
    }

    @Override
    protected Bitmap createBitmap(byte[] res) {
        if(null == res || res.length <= 0) return null;
        return BitmapFactory.decodeByteArray(res, 0, res.length);
    }
}
