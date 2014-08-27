package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.apache.http.HttpConnection;

/**
 * Created by Administrator on 2014/8/27.
 */
public class HttpConnectionRequestExecutor implements RequestExecutor<byte[], NetworkRequest> {
    private HttpConnection mHttpConnection;
    public HttpConnectionRequestExecutor(){

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
