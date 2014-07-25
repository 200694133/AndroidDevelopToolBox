package com.hanyanan.tools.datapersistence;

/**
 * Created by hanyanan on 2014/7/25.
 */
public interface IBaseDataEditorWorkStation {

    public interface Editor {
        public Editor put(String key, int value, long expireTime);
        public Editor put(String key, float value, long expireTime);
        public Editor put(String key, double value, long expireTime);
        public Editor put(String key, byte value, long expireTime);
        public Editor put(String key, long value, long expireTime);
        public Editor put(String key, short value, long expireTime);
        public Editor put(String key, char value, long expireTime);
        public Editor put(String key, boolean value, long expireTime);
        public Editor put(String key, String value, long expireTime);
        public Editor put(String key, byte[] value, long expireTime);
        public Editor remove(String key);
        public Editor clear(String key);
        public boolean commit();
    }

    public int getInt(String key, int defaultValue);
    public float getFloat(String key, float defaultValue);
    public double getDouble(String key, double defaultValue);
    public long getLong(String key, long defaultValue);
    public byte getByte(String key, byte defaultValue);
    public char getChar(String key, char defaultValue);
    public short getShort(String key, short defaultValue);
    public byte[] getBlob(String key);
    public String getString(String key);

    public IResult remove(String key);
}
