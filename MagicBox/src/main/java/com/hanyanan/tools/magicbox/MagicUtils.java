package com.hanyanan.tools.magicbox;

import android.os.Looper;

/**
 * Created by hanyanan on 2014/9/2.
 */
public class MagicUtils {

    public static void checkThreadState(){
        if(Looper.myLooper() != Looper.getMainLooper()){
            throw new IllegalThreadStateException("Called not in main thread.");
        }
    }
}
