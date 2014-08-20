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
     * @see #getInputStream(String).
     * @param key key
     * @return file of key, null means that missing the key
     */
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
     * But remember close stream, or it useless.
     * @param key
     * @return output stream of current key, null means that failed.
     */
    OutputStream getOutputStream(String key);
    /**
     * Save the current input stream to storage. But notice that it's unsafe, it storage content
     * without encoding.
     * @param key key
     * @param inputStream the input stream need read and write to storage.
     * @return true means success, or other wise means failed.
     * @throws IOException
     */
    boolean save(String key, InputStream inputStream) throws IOException;

    /**
     * Saves image bitmap in disk cache.
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
     * Save Object to storage.
     * @param key key
     * @param serializable content to storage
     * @return <b>true</b> - if bitmap was saved successfully;
     *          <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    <T extends Serializable> boolean saveObject(String key, T serializable) throws IOException;

    <T extends Serializable> T getObject(String key, T clazz);
    /**
     * Removes file associated with incoming Key
     * @param key key to remove
     * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
     * incoming URI or image file can't be deleted.
     */
    boolean remove(String key);
    /** Closes disk storage, releases resources. */
    void close();

    /** Clears disk Storage. */
    void clear() throws IOException;
}
