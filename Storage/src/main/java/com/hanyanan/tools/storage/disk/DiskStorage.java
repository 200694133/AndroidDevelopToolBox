package com.hanyanan.tools.storage.disk;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by hanyanan on 2014/8/20.
 * This interface define the major function for the disk storage, it provide the major operation
 * for user to implement it.
 */
public interface DiskStorage {
    public static final long VALID_FOREVER = -1;
    public static final int REST_STREAM_SIZE = Integer.MAX_VALUE;
    public static final Copier DEFAULT_COPIER = new DefaultCopier();

    /**
     * return root directory fot this storage module.
     * @return root directory
     */
    File getDirectory();

    /**
     * return current size has cost.May be it's different from size which get from Operation System.
     * @return current file size under root directory.
     */
    long getCurrentSize();

    /**
     * return clean file of key. In fact it's not recommend use this method to get the expected file.
     * In fact this function is not recommend, it's unsafe, please use getInputStream instead.
     * @see #getInputStream(String).
     * @param key key
     * @return file of key, null means that missing the key
     */
    @Deprecated
    File get(String key);

    /**
     * Get the input stream of key, it's recommend to read data from storage. Notices that it need
     * to close the stream.
     * @param key key
     * @return  a input stream of current key, null means that it's not exits..
     */
    InputStream getInputStream(String key);

    /**
     * Provide a safe and strong way(but not easy) to write data to storage, user can encoding data,
     * But remember close stream, or it useless. Notice that user cannot set the expire time for current
     * entry, invoke save* get a better experience.
     * @param key
     * @return output stream of current key, null means that failed.
     */
    OutputStream getOutputStream(String key);
    /**
     * Save the current input stream to storage. But notice that it's unsafe, it storage content
     * without encoding.In this method, it's no limited expire time.It's illegal forever until it
     * has been delete in some reasons, such as disk full or user delete..
     * @param key key
     * @param inputStream the input stream need read and write to storage.
     * @return true means success, or other wise means failed.
     * @throws IOException
     */
    boolean save(String key, InputStream inputStream) throws IOException;

    /**
     * Save the current input stream to storage. But notice that it's unsafe, it storage content
     * without encoding. It's valid before expire time. It must be provide a interface to do the
     * hard work.
     * @param key key
     * @param inputStream the input stream need read and write to storage.
     * @param expireTime expire time for current entry.
     * @param copier the copier to copy data from input stream to output stream..
     * @return true means success, or other wise means failed.
     * @throws IOException
     */
    boolean save(String key, InputStream inputStream, Copier copier, long size, long expireTime) throws IOException;

    /**
     * Save the current input stream to storage. But notice that it's unsafe, it storage content
     * without encoding. It's valid before expire time.
     * @param key key
     * @param inputStream the input stream need read and write to storage.
     * @param expireTime expire time for current entry.
     * @return true means success, or other wise means failed.
     * @throws IOException
     */
    boolean save(String key, InputStream inputStream, long expireTime) throws IOException;

    /**
     * Saves image bitmap in disk cache.Do set expire time.
     * @param key key to store
     * @param bitmap bitmap to store
     * @param format bitmap storage format, support jpeg and png ...
     * @param quality quality to compress, 1-100, 100 is best.
     * @return <b>true</b> - if bitmap was saved successfully;
     *          <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    boolean save(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality) throws IOException;

    /**
     * Saves image bitmap in disk cache.Set expire time.
     * @param key key to store
     * @param bitmap bitmap to store
     * @param format bitmap storage format, support jpeg and png ...
     * @param quality quality to compress, 1-100, 100 is best.
     * @param expireTime expire time for current data.
     * @return <b>true</b> - if bitmap was saved successfully;
     *          <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    boolean save(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality, long expireTime) throws IOException;
    /**
     * Save Object to storage. It's valid forever.
     * @param key key
     * @param serializable content to storage
     * @return <b>true</b> - if bitmap was saved successfully;
     *          <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    <T extends Serializable> boolean saveObject(String key, T serializable) throws IOException;
    /**
     * Save Object to storage. It's valid till times up.
     * @param key key
     * @param serializable content to storage
     * @param expireTime expire time for current data.
     * @return <b>true</b> - if bitmap was saved successfully;
     *          <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    <T extends Serializable> boolean saveObject(String key, T serializable, long expireTime) throws IOException;


    <T extends Serializable> T getObject(String key, Class<T> clazz)throws IOException;
    /**
     * Removes file associated with incoming Key
     * @param key key to remove
     * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
     * incoming URI or image file can't be deleted.
     */
    boolean remove(String key);

    /**
     * Check if current storage contain wanted value/
     * @param key the key to search
     * @return true means that contain the wanted, others wise it's not.
     */
    boolean contains(String key);

    /** Closes disk storage, releases all resources. */
    void close();

    /** Clears disk Storage. */
    void clear() throws IOException;

    /**
     * Copy content from input stream to output stream.
     * The main propose for this interface is to encrypt the content or decrypt from disk .
     */
    public static interface Copier{
        public void copy(InputStream inputStream, OutputStream outputStream, long length)throws IOException;
    }
}
