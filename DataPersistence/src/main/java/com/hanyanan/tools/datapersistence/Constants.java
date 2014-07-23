package com.hanyanan.tools.datapersistence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hanyanan on 2014/7/23.
 */
public class Constants {
    static final int PUT = 0x01;
    static final int GET = 0x02;
    static final int REMOVE = 0x03;

    public final static ExecutorService service = Executors.newSingleThreadExecutor();



}
