package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.apache.http.HttpConnection;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Created by Administrator on 2014/8/27.
 */
public class HttpConnectionRequestExecutor implements RequestExecutor<byte[], NetworkRequest> {
    private HttpConnection mHttpConnection;
    public HttpConnectionRequestExecutor(){

    }

    private static void parseHttpRequestHeader(HttpURLConnection connection, HttpRequestParam param) throws ProtocolException {
        if(null == connection || null==param) throw new NullPointerException("Input error.");
        int method = param.getMethod();
        connection.setUseCaches(param.useCache());
        connection.setRequestProperty("Content-type", param.getContentType());

        switch (method){
            case HttpRequestParam.Method.GET:
                connection.setDoInput(true);
                break;
            case HttpRequestParam.Method.POST:
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                break;
            case HttpRequestParam.Method.PUT:

                break;
            default:
                //TODO
        }
    }
    private static void performGetRequest(HttpURLConnection connection, HttpRequestParam param) throws ProtocolException {
        if(null == connection || null==param) throw new NullPointerException("Input error.");
        connection.setUseCaches(param.useCache());
        connection.setRequestMethod("GET");
        connection.setDoInput(false);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-type", param.getContentType());
        connection.setRequestProperty("Connection", "close");
        connection.setInstanceFollowRedirects(true);
    }

    private static InputStream performGetRequest(HttpURLConnection connection, HttpRequestParam param) throws ProtocolException {

    }
    private void init(){
//        try {
//            //Create connection
//            url = new URL(targetURL);
//            connection = (HttpURLConnection)url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type",
//                    "application/x-www-form-urlencoded");
//
//            connection.setRequestProperty("Content-Length", "" +
//                    Integer.toString(urlParameters.getBytes().length));
//            connection.setRequestProperty("Content-Language", "en-US");
//
//            connection.setUseCaches (false);
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//
//            //Send request
//            DataOutputStream wr = new DataOutputStream (
//                    connection.getOutputStream ());
//            wr.writeBytes (urlParameters);
//            wr.flush ();
//            wr.close ();
//
//            //Get Response
//            InputStream is = connection.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            String line;
//            StringBuffer response = new StringBuffer();
//            while((line = rd.readLine()) != null) {
//                response.append(line);
//                response.append('\r');
//            }
//            rd.close();
//            return response.toString();
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            return null;
//
//        } finally {
//
//            if(connection != null) {
//                connection.disconnect();
//            }
//        }
    }
    @Override
    public Response<byte[]> performRequest(NetworkRequest request) throws XError {
        return null;
    }
}
