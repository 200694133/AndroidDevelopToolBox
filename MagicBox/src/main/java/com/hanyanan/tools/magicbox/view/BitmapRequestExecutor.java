package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.hanyanan.tools.schedule.FaultError;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.HttpConnectionImpl;
import com.hanyanan.tools.schedule.http.HttpInterface;
import com.hanyanan.tools.schedule.http.HttpRequest;
import com.hanyanan.tools.schedule.http.NetworkError;
import com.hanyanan.tools.schedule.http.cache.CacheableHttpConnectionImpl;
import com.hanyanan.tools.schedule.http.cache.HttpCache;
import com.hanyanan.tools.storage.disk.DiskStorage;

import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/8/13.
 */
public class BitmapRequestExecutor implements RequestExecutor<Bitmap,ImageRequest> {
    private static final String TAG = "BitmapExecutor";
    private static Response downLoad(HttpInterface httpExecutor,DiskStorage fixSizeDiskStorage,HttpRequest request) throws NetworkError {
        try {
            BasicHttpResponse basicHttpResponse = httpExecutor.performDownLoadRequest(request);
            if(null == basicHttpResponse || basicHttpResponse.getEntity() ==null) return Response.success(request.getKey());
            InputStream inputStream = basicHttpResponse.getEntity().getContent();
            if(null == inputStream)return Response.success(request.getKey());
            Log.d(TAG, "performRequest InputStream " + inputStream);
            fixSizeDiskStorage.save(request.getKey(), inputStream);
            inputStream.close();
            basicHttpResponse.getEntity().consumeContent();
            Log.d(TAG, "download "+request.getKey()+"    "+request.getUrl()+"  success");
            return Response.success(request.getKey());
        } catch (IOException e) {
            Log.d(TAG, "download "+request.getKey()+"    "+request.getUrl()+"  failed "+e.toString());
            e.printStackTrace();
            throw new NetworkError(e);
        }
    }

    private static final class Size{
        int mWidth, mHeight;
        public Size(int w,int h){
            mWidth = w;
            mHeight = h;
        }
    }
    private Size calculateDesiredSize(int maxWidth, int maxHeight, int actualWidth, int actualHeight){
        if(maxWidth == 0 && maxHeight == 0) return new Size(actualWidth, actualHeight);
        if(maxWidth == 0){
            return new Size(actualWidth,maxHeight<=actualHeight?maxHeight:actualHeight);
        }
        if(maxHeight == 0){
            return new Size(maxWidth>=actualWidth?actualWidth:maxWidth, actualHeight);
        }
        double wr = (double)maxWidth/actualWidth;
        double hr = (double)maxHeight/actualHeight;
        double r = Math.min(wr,hr);
        return new Size((int)(r*actualWidth),(int)(r*actualHeight));
    }

    @Deprecated
    private Bitmap parseBitmap(DiskStorage cache, String key, int maxWidth, int maxHeight) throws IOException {
        InputStream inputStream = cache.getInputStream(key);
        if(null == inputStream) return null;
        Bitmap.Config config = Bitmap.Config.RGB_565;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = config;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,decodeOptions);
            inputStream.close();
            return bitmap;
        }
        inputStream = cache.getInputStream(key);
        if(null == inputStream) return null;
        Bitmap bitmap = null;
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, decodeOptions);
        inputStream.close();
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        Size size = calculateDesiredSize(maxWidth, maxHeight, actualWidth, actualHeight);
        int desiredWidth = size.mWidth;
        int desiredHeight = size.mHeight;
        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        inputStream = cache.getInputStream(key);
        if(null == inputStream) return null;
        Bitmap tempBitmap =
                BitmapFactory.decodeStream(inputStream,null,decodeOptions);
        inputStream.close();
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                tempBitmap.getHeight() > desiredHeight)) {
            bitmap = Bitmap.createScaledBitmap(tempBitmap,
                    desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
        } else {
            bitmap = tempBitmap;
        }
        return bitmap;
    }


    private Bitmap parseBitmap(HttpCache cache, HttpRequest httpRequest, int maxWidth, int maxHeight) throws IOException {
        String key = httpRequest.getKey();
        InputStream inputStream = cache.getContentInputStream(httpRequest);
        if(null == inputStream) return null;
        Bitmap.Config config = Bitmap.Config.RGB_565;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = config;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,decodeOptions);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            inputStream.close();
            return bitmap;
        }
        inputStream = cache.getContentInputStream(httpRequest);
        if(null == inputStream) return null;
        Bitmap bitmap = null;
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, decodeOptions);
        inputStream.close();
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        Size size = calculateDesiredSize(maxWidth, maxHeight, actualWidth, actualHeight);
        int desiredWidth = size.mWidth;
        int desiredHeight = size.mHeight;
        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        inputStream = cache.getContentInputStream(httpRequest);
        if(null == inputStream) return null;
        Bitmap tempBitmap =
                BitmapFactory.decodeStream(inputStream,null,decodeOptions);
        inputStream.close();
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                tempBitmap.getHeight() > desiredHeight)) {
            bitmap = Bitmap.createScaledBitmap(tempBitmap,
                    desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
        } else {
            bitmap = tempBitmap;
        }
        return bitmap;
    }

    @Override
    public Response<Bitmap> performRequest(ImageRequest request) throws XError {
        HttpCache cache = request.getHttpCache();
        Bitmap bitmap = null;

        try {
            CacheableHttpConnectionImpl httpConnection = new CacheableHttpConnectionImpl();
            BasicHttpResponse basicHttpResponse = httpConnection.performDownLoadRequest(request);
            if(null != basicHttpResponse && basicHttpResponse.getEntity()!=null && basicHttpResponse.getEntity().getContent()!=null){
                basicHttpResponse.getEntity().consumeContent();
            }
            bitmap = parseBitmap(cache, request,request.getMaxWidth(),request.getMaxHeight());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if(null != bitmap) return Response.success(bitmap);
        return Response.error(new XError(new FaultError("Decode failed")));
    }
    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }
}
