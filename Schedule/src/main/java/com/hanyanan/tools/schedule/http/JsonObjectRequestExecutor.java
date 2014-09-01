package com.hanyanan.tools.schedule.http;

import android.text.TextUtils;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class JsonObjectRequestExecutor implements RequestExecutor<JSONObject, NetworkRequest> {
    private final HttpInterface mNetwork;
    public JsonObjectRequestExecutor(HttpInterface network){
        mNetwork = network;
    }
    @Override
    public Response<JSONObject> performRequest(NetworkRequest request) throws XError {
        HttpRequestParam param = request.getHttpRequestParam();
        param.setTransactionType(HttpRequestParam.TransactionType.JASON);

        try {
            NetworkResponse res = HttpUtils.doRequest(mNetwork,request);
            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            if(TextUtils.isEmpty(jsonString)){
                Response.success((JSONObject)null);
            }
            return Response.success(new JSONObject(jsonString));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error(new XError(e));
        }
    }
}
