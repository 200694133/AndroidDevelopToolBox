package com.hanyanan.tools.storage.db;

import com.hanyanan.tools.storage.Error.TypeNotSupportError;
import com.hanyanan.tools.storage.Utils;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/8/5.
 */
class ByteHelper {
    private static final int CLASS_RAW = 1;
    private static final int CLASS_STRING = 2;
    private static final int CLASS_SER = 3;
    public static class ClassType{
        int value;
        public int getType(){
            return value;
        }
    }
    public static byte[] toByte(Object data, ClassType type) throws TypeNotSupportError {
        if(data instanceof String){
            type.value = CLASS_STRING;
            return ((String) data).getBytes();
        }

        if(data instanceof byte[]){
            type.value = CLASS_RAW;
            return (byte[])data;
        }
        type.value = CLASS_SER;
         return Utils.serialize(data);
    }

    public static Object toObject(byte[] data, int type) throws ClassNotFoundException {
        if(type == CLASS_STRING){
            return new String(data);
        }

        if(type == CLASS_RAW){
            return data;
        }
        return Utils.deSerialize(data);
    }
}
