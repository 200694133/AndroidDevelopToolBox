package com.hanyanan.tools.schedule.http;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class GsonObjectRequestExecutor<T> implements RequestExecutor<T, HttpRequest> {
    private final HttpInterface mNetwork;
    private final Class<T> clazz;
    public GsonObjectRequestExecutor(HttpInterface network, Class<T> clazz){
        this.clazz = clazz;
        mNetwork = network;
    }
    @Override
    public Response<T> performRequest(HttpRequest request) throws XError {
        HttpRequestParam param = request.getRequestParam();
        param.setTransactionType(HttpRequestParam.TransactionType.JASON);

        try {
            NetworkResponse res = HttpUtils.doRequest(mNetwork,request);
            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            if(TextUtils.isEmpty(jsonString)){
                Response.success((T)null);
            }
            Gson gson = new Gson();
            T t = (T)gson.fromJson(jsonString, clazz);
            return Response.success(t);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error(new XError(e));
        }
    }
}
