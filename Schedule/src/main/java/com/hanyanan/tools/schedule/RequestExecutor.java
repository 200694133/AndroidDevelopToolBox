package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/7/29.
 * This class is a executor for request, it perform request and return the result.
 */
public interface RequestExecutor<T, R extends Request> {

    /**
     * Perform current request and return the result.It's a execution unit.
      * @param request
     * @return response with current
     * @throws XError runtime exception
     */
    public Response<T> performRequest(R request) throws XError;
}
