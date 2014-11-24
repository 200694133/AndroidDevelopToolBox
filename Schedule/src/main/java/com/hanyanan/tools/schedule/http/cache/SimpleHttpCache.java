package com.hanyanan.tools.schedule.http.cache;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hanyanan.tools.storage.database.BasicDatabaseHelper;
import com.hanyanan.tools.storage.database.Entry;
import com.hanyanan.tools.storage.disk.DiskStorage;
import com.hanyanan.tools.storage.disk.LimitedSizeDiskStorage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by hanyanan on 2014/11/17.
 */
@Deprecated
public class SimpleHttpCache extends StrictHttpCache {
    public SimpleHttpCache(DiskStorage diskStorage, BasicDatabaseHelper db) {
        super(diskStorage, db);
    }

    public SimpleHttpCache(Context context, File file, int size){
        super(context, file, size);
    }

    public BasicHttpResponse get(String key) throws IOException {
        checkHttpCacheHeader(key);
        InputStream inputStream = mDiskStorage.getInputStream(key);
        if(null == inputStream) return null;
        if(null != mHttpCacheHeader){
            //read from cache, and return the result
            return parseHttpResponse(mHttpCacheHeader, mDiskStorage.getInputStream(key));
        }
        //设置为默认的
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        BasicHttpResponse response = new BasicHttpResponse(protocolVersion, 200, "success");
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(inputStream);
        entity.setContentLength(inputStream.available());
        entity.setContentEncoding("utf-8");
        entity.setContentType("*/*");
        response.setEntity(entity);

        return response;
    }

    public BasicHttpResponse put(String key, BasicHttpResponse httpResponse) throws IOException {
        checkHttpCacheHeader(key);
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        HttpCacheHeader httpCacheHeader = parseCacheHeader(httpResponse);

        if(null != httpCacheHeader){//support cache, should store on data base
            mHttpCacheHeader = httpCacheHeader;
            //update head info
            Entry entry = new Entry();
            entry.key = key;
            entry.expire = httpCacheHeader.ttl;
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            entry.data = gson.toJson(httpCacheHeader);
            mBasicDatabaseHelper.put(entry);
        }else{
            //cannot cache on disk,so do not storage the head info, just store the content.
        }



        if(statusCode == HttpStatus.SC_NOT_MODIFIED){//just update head, and red from disk cache
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) {
                //TODO，可以设为默认的流
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            BasicHttpEntity res = new BasicHttpEntity();
            res.setContent(inputStream);
            res.setContentLength(entity.getContentLength());
            res.setContentEncoding(entity.getContentEncoding());
            res.setContentType(entity.getContentType());
            httpResponse.setEntity(res);
            entity.consumeContent();
            return httpResponse;
        }

        if(statusCode>=200 && statusCode<300) {//cache content to disk cache
            //save to disk cache
            mDiskStorage.save(key, httpResponse.getEntity().getContent(), httpCacheHeader.ttl + 30 * 1000 * 60 * 60L);
            InputStream inputStream = mDiskStorage.getInputStream(key);
            if(null == inputStream) {
                //TODO，可以设为默认的流
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            BasicHttpEntity res = new BasicHttpEntity();
            res.setContent(inputStream);
            res.setContentLength(entity.getContentLength());
            res.setContentEncoding(entity.getContentEncoding());
            res.setContentType(entity.getContentType());
            httpResponse.setEntity(res);
            entity.consumeContent();
            return httpResponse;
        }
        return httpResponse;
    }
}
