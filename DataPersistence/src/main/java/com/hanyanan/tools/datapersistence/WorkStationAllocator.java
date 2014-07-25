package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/25.
 */
public class WorkStationAllocator {
    private static WorkStationAllocator sInstance = null;
    public static synchronized WorkStationAllocator getInstance(){
        if(null == sInstance){
            sInstance = new WorkStationAllocator();
        }
        return sInstance;
    }
    private WorkStationAllocator(){
        //TODO
    }



    public static synchronized void dispose(){
        //TODO
    }
}
