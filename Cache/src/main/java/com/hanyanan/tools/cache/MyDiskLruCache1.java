package com.hanyanan.tools.cache;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2014/7/9.
 */
public class MyDiskLruCache1 implements ICache {
    private static final String JOURNAL_FILE = "journal";
    private static final String JOURNAL_FILE_TMP = "journal.tmp";
    private static final String MAGIC = "libcore.io.DiskLruCache";
    private static final String VERSION_1 = "1";
    private static final String SPLIT = " ";
    private enum Action{
        PUT_ACTION("PUT"),
        REMOVE_ACTION("REMOVE"),
        COMPLETE_ACTION("COMPLETE"),
        ABORT_ACTION("ABORT");
        private String mAction;
        private Action(String action){
            mAction = action;
        }
        private String getAction(){
            return mAction;
        }
    }

    private enum Position{
        DISK("DISK"),
        BLOCK_QUEUE("BlockQueue");
        private String mAction;
        private Position(String action){
            mAction = action;
        }
        private String getAction(){
            return mAction;
        }
    }

     /*
     * This cache uses a journal file named "journal". A typical journal file
     * looks like this:
     *     libcore.io.DiskLruCache
     *     1
     *     100
     *     2
     *
     *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
     *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
     *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
     *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
     *     DIRTY 1ab96a171faeeee38496d8b330771a7a
     *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
     *     READ 335c4c6028171cfddfbaae1a9c313c52
     *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
     *
     * The first five lines of the journal form its header. They are the
     * constant string "libcore.io.DiskLruCache", the disk cache's version,
     * the application's version, the value count, and a blank line.
     *
     * Each of the subsequent lines in the file is a record of the state of a
     * cache entry. Each line contains space-separated values: a state, a key,
     * and optional state-specific values.
     *   o DIRTY lines track that an entry is actively being created or updated.
     *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
     *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
     *     temporary files may need to be deleted.
     *   o CLEAN lines track a cache entry that has been successfully published
     *     and may be read. A publish line is followed by the lengths of each of
     *     its values.
     *   o READ lines track accesses for LRU.
     *   o REMOVE lines track entries that have been deleted.
     *
     * The journal file is appended to as cache operations occur. The journal may
     * occasionally be compacted by dropping redundant lines. A temporary file named
     * "journal.tmp" will be used during compaction; that file should be deleted if
     * it exists when the cache is opened.
     */
     private final File directory;
     private final File journalFile;
     private final File journalFileTmp;
     private final int appVersion;
     private final long maxSize;
     private final int valueCount;
     private long size = 0;
    private Writer journalWriter;
    private ICacheTypeListener mCacheListener = null;
    private final transient LinkedHashMap<String, Entry> lruEntries  = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    private int redundantOpCount;
    private static final HandlerThread sHandlerThread = new android.os.HandlerThread("DiskLruCache");
    static {
        sHandlerThread.start();
    }
    private final Handler.Callback mWorkCallback = new Handler.Callback(){
        public boolean handleMessage(Message message) {
            if(null == message || message.obj == null) return false;
            Editor editor = (Editor)message.obj;
            processEntry(editor);
            return true;
        }
    };
    private transient final Handler mWorkHandler = new Handler(sHandlerThread.getLooper(), mWorkCallback);
    private MyDiskLruCache1(File directory, int appVersion, int valueCount, long maxSize) {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TMP);
        this.valueCount = valueCount;
        this.maxSize = maxSize;
    }

    private void readJournal() throws IOException {
        StrictLineReader reader = new StrictLineReader(new FileInputStream(journalFile), Utils.ASCII_CHARSET);
        try {
            String magic = reader.readLine();
            String version = reader.readLine();
            String appVersionString = reader.readLine();
            String valueCountString = reader.readLine();
            String blank = reader.readLine();
            if (!MAGIC.equals(magic)
                    || !VERSION_1.equals(version)
                    || !Integer.toString(appVersion).equals(appVersionString)
                    || !Integer.toString(valueCount).equals(valueCountString)
                    || !"".equals(blank)) {
                throw new IOException("unexpected journal header: ["
                        + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
            }
            int lineCount = 0;
            while (true) {
                try {
                    readJournalLine(reader.readLine());
                    lineCount++;
                } catch (Exception endOfJournal) {
                    break;
                }
            }
            redundantOpCount = lineCount - lruEntries.size();
        } finally {
            Utils.closeQuietly(reader);
        }
    }
    private void readJournalLine(String line) throws Exception {
        JournalLine journalLine = new JournalLine(line);
        if (journalLine == null || !journalLine.isIllegal()) {
            return ;
        }
        if(journalLine.getAction() == Action.REMOVE_ACTION){
            lruEntries.remove(journalLine.getKey());
            return ;
        }
        if(journalLine.getAction() == Action.DIRTY_ACTION){
            lruEntries.remove(journalLine.getKey());
            return ;
        }
        if (secondSpace != -1 && firstSpace == CLEAN.length() && line.startsWith(CLEAN)) {
            String[] parts = line.substring(secondSpace + 1).split(" ");
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths(parts);
        } else if (secondSpace == -1 && firstSpace == DIRTY.length() && line.startsWith(DIRTY)) {
            entry.currentEditor = new Editor(entry);
        } else if (secondSpace == -1 && firstSpace == READ.length() && line.startsWith(READ)) {
            // this work was already done by calling lruEntries.get()
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    private synchronized void recordJournal(String line){
        try {
            journalWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void processEntry(Editor editor){
        Entry newEntry = editor.mNewEntry;
        switch(newEntry.mAction){
            case PUT_ACTION:
                recordJournal(entry.toString());
                break;
            case ABORT_ACTION:

                break;
            case REMOVE_ACTION:

                break;
            case COMPLETE_ACTION:

                break;
        }
    }
    private synchronized void submitEdit(final Editor editor, final boolean abort) throws IOException {
        if(abort){
                Runnable mAbortRunnable = new Runnable(){
                    public void run(){

                    }
                };
            mExecutorService.submit(mAbortRunnable);
        }else{
            Runnable mWriteRunnable = new Runnable(){
                public void run(){

                }
            };
            mExecutorService.submit(mWriteRunnable);
        }

        // if this edit is creating the entry for the first time, every index must have a value
        if (success && !entry.readable) {
            for (int i = 0; i < valueCount; i++) {
                if (!editor.written[i]) {
                    editor.abort();
                    throw new IllegalStateException("Newly created entry didn't create value for index " + i);
                }
                if (!entry.getDirtyFile(i).exists()) {
                    editor.abort();
                    System.out.println("DiskLruCache: Newly created entry doesn't have file for index " + i);
                    return;
                }
            }
        }
        for (int i = 0; i < valueCount; i++) {
            File dirty = entry.getDirtyFile(i);
            if (success) {
                if (dirty.exists()) {
                    File clean = entry.getCleanFile(i);
                    dirty.renameTo(clean);
                    long oldLength = entry.lengths[i];
                    long newLength = clean.length();
                    entry.lengths[i] = newLength;
                    size = size - oldLength + newLength;
                }
            } else {
                deleteIfExists(dirty);
            }
        }
        redundantOpCount++;
        entry.currentEditor = null;
        if (entry.readable | success) {
            entry.readable = true;
            journalWriter.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n');
            if (success) {
                entry.sequenceNumber = nextSequenceNumber++;
            }
        } else {
            lruEntries.remove(entry.key);
            journalWriter.write(REMOVE + ' ' + entry.key + '\n');
        }
        if (size > maxSize || journalRebuildRequired()) {
            executorService.submit(cleanupCallable);
        }
    }

    private void trimToSize(){
        //TODO
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("delete failed");
        }
    }

    public void close(){
        mWorkHandler.removeCallbacksAndMessages(null);
    }




    @Override
    public CacheType getType() {
        return CacheType.DISK;
    }

    @Override
    public ICacheable get(String key) {
        return null;
    }

    @Override
    public ICacheable put(String key, ICacheable value) {
        return null;
    }

    @Override
    public ICacheable pull(String key) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void reSize(long newMaxSize) {

    }

    @Override
    public void addCacheTypeListener(ICacheTypeListener listener) {

    }

    private static class JournalLine{
        private boolean isIllegal = false;
        Action mAction;
        String mKey;
        long mActionTime;
        long mExpireTime;
        long mSize;

        private JournalLine(String line){
            if(TextUtils.isEmpty(line)) {
                throw new IllegalArgumentException("parse line cannot empty.");
            }
            line = line.trim();
            if(TextUtils.isEmpty(line)) {
                throw new IllegalArgumentException("parse line cannot empty.");
            }
            String []strings  = line.split(SPLIT);
            if(strings.length < 3){
                return;
            }
            String action = strings[0];
            if(action.equalsIgnoreCase(Action.REMOVE_ACTION.getAction())){
                mAction = Action.REMOVE_ACTION;
            }else if(action.equalsIgnoreCase(Action.PUT_ACTION.getAction())){
                mAction = Action.PUT_ACTION;
            }else if(action.equalsIgnoreCase(Action.COMPLETE_ACTION.getAction())){
                mAction = Action.COMPLETE_ACTION;
            }else if(action.equalsIgnoreCase(Action.ABORT_ACTION.getAction())){
                mAction = Action.ABORT_ACTION;
            }
            mKey = strings[1];
            try {
                mActionTime = Long.parseLong(strings[2]);
                mExpireTime = Long.parseLong(strings[3]);
                mSize = Long.parseLong(strings[4]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public String getKey(){
            return mKey;
        }
        public String toString(){
            return null;
        }
        private Action getAction(){
            return mAction;
        }
        private boolean isIllegal(){
            return isIllegal;
        }
    }
    public final class Editor {
        private Entry mCurrEntry;
        private Entry mNewEntry;
        private boolean abort = false;
        private boolean hasErrors = false;
        private Editor(Entry entry) {
            if(null == entry) throw new IllegalArgumentException("");
            mCurrEntry = entry;
        }

        public void setNewValue(long size, long exTime, byte[] data){
            mNewEntry = new Entry(Action.PUT_ACTION, mCurrEntry.mKey, size, exTime);
            mNewEntry.mData = data;
        }
        /**
         * Commits this edit so it is visible to readers.  This releases the
         * edit lock so another edit may be started on the same key.
         */
        public void commit() throws IOException {
            if(abort){
                return ;
            }
            if (hasErrors) {
                completeEdit(this, false);
                remove(entry.key); // the previous entry is stale
            } else {
                completeEdit(this, true);
            }
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         */
        public void abort() throws IOException {
            abort = true;
        }



        private class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }
            @Override public void write(int oneByte) {
                try {
                    out.write(oneByte);
                } catch (IOException e) {
                    hasErrors = true;
                }
            }
            @Override public void write(byte[] buffer, int offset, int length) {
                try {
                    out.write(buffer, offset, length);
                } catch (IOException e) {
                    hasErrors = true;
                }
            }
            @Override public void close() {
                try {
                    out.close();
                } catch (IOException e) {
                    hasErrors = true;
                }
            }
            @Override public void flush() {
                try {
                    out.flush();
                } catch (IOException e) {
                    hasErrors = true;
                }
            }
        }
    }
    private final class Entry{
        Action mAction;
        String mKey;
        long mSize;
        long mExpireTime;
        byte[] mData = null;
        private Entry(Action action, String key, long size, long expireTime){
            mAction = action;
            mKey = key;
            mSize = size;
            mExpireTime = expireTime;
        }
        public String toString(){
            return mAction.getAction()+" "+mKey+" "+System.currentTimeMillis()+" "+mExpireTime+" "+mSize;
        }
        public int hashCode(){
            return mKey.hashCode();
        }
        public byte[] getData(){
            return mData;
        }
        @Override
        public boolean equals(Object obj){
            if(obj == this) return true;
            if(Entry.class.isInstance(obj)){
                Entry e = (Entry)obj;
                return e.mKey.equals(e.mKey);
            }
            return false;
        }
        public long getSize(){
            return mSize;
        }
        public File getFile() {
            return new File(directory, mKey);
        }
    }
}
