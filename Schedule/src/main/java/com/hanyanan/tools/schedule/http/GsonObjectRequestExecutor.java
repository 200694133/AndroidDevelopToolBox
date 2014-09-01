package com.hanyanan.tools.schedule.http;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class GsonObjectRequestExecutor<T> implements RequestExecutor<T, NetworkRequest> {
    private final HttpInterface mNetwork;
    public GsonObjectRequestExecutor(HttpInterface network){
        mNetwork = network;
    }
    @Override
    public Response<T> performRequest(NetworkRequest request) throws XError {
        HttpRequestParam param = request.getHttpRequestParam();
        param.setTransactionType(HttpRequestParam.TransactionType.JASON);

        try {
            NetworkResponse res = HttpUtils.doRequest(mNetwork,request);
            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            if(TextUtils.isEmpty(jsonString)){
                Response.success((T)null);
            }
            Gson gson = new Gson();
            T t = (T)gson.fromJson(jsonString, new TypeToken<T>() {}.getType());
            return Response.success(t);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error(new XError(e));
        }
    }
}
