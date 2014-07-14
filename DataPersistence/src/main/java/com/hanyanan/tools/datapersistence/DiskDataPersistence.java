package com.hanyanan.tools.datapersistence;

import java.io.File;

/**
 * Created by hanyanan on 2014/7/14.
 */
public class DiskDataPersistence {
    private File directory;





    public final class Editor{
        private final Entry entry;
        private boolean isReadable = true;
        private boolean isWritten = false;
        private Editor(Entry entry) {
            this.entry = entry;
        }
        public void commit(){

        }
        public void abort(){

        }
        public void setOutputStream(SafeOutputStream outputStream, long expireTime,
                                    boolean autoClose){

        }
        public SafeOutputStream newOutputStream(){
            return null;
        }
        public SafeInputStream newInputStream(){
            return null;
        }
        public void close(){

        }
    }
    private final class Entry {
        private final String key;
        /** Lengths of this entry's files. */
        private long length;
        /** Time of expired. */
        private long expireTime;
        /** True if this entry has ever been published. */
        private boolean readable;

        /** The ongoing edit or null if this entry is not being edited. */
        private Editor currentEditor;

        private Entry(String key) {
            this.key = key;
        }

        public long getLengths()  {
            return length;
        }

        /** Set lengths using decimal numbers like "10123". */
        private void setLengths(long length)  {
            this.length = length;
        }

        public File getCleanFile() {
            return new File(directory, key);
        }

        public File getDirtyFile() {
            return new File(directory, key + ".tmp");
        }
    }

    public interface IDiskDataWorkTop{
        public void onBeforeDataWrite(String key);
        public void onDataWrite(String key);
        public void onAfterDataWrite(String key);
    }
}
