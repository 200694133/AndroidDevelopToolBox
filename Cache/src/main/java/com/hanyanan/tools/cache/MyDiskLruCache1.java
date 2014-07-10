package com.hanyanan.tools.cache;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by hanyanan on 2014/7/9.
 */
public class MyDiskLruCache1 {
    private static final String JOURNAL_FILE = "journal";
    private static final String JOURNAL_FILE_TMP = "journal.tmp";
    private static final String MAGIC = "libcore.io.DiskLruCache";
    private static final String VERSION_1 = "1";
    private static final String SPLIT = " ";

    private enum Action{
        PUT_ACTION("PUT"),
        REMOVE_ACTION("REMOVE"),
        READ_ACTION("READ");
        private String mAction;
        private Action(String action){
            mAction = action;
        }
        private String getAction(){
            return mAction;
        }
    }

    private enum STATUS{
        SYNCED("IN DISK OR FAILED REMOVE FROM APP"),
        NEED_SYNC("DIRTY"),
        SYNCING("WRIT TO DISK");
        private String mStatus;
        private STATUS(String action){
            mStatus = action;
        }
        private String getStatus(){
            return mStatus;
        }
    }

     private final File directory;
     private final File journalFile;
     private final File journalFileTmp;
     private final int appVersion;
     private final long maxSize;
     private final int valueCount;
     private long size = 0;
    private Writer journalWriter;
    private final transient LinkedHashMap<String, Entry> lruEntries  = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    private int redundantOpCount;
    private static final HandlerThread sHandlerThread = new android.os.HandlerThread("DiskLruCache");
    static {
        sHandlerThread.start();
    }
    private final ReentrantLock mLock = new ReentrantLock(false);
    private final Handler.Callback mWorkCallback = new Handler.Callback(){
        public boolean handleMessage(Message message) {
            if(null == message || message.obj == null) return false;
            Editor editor = (Editor)message.obj;
            doPutAction(editor);
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
        String key = journalLine.getKey();
        if(journalLine.getAction() == Action.REMOVE_ACTION){
            lruEntries.remove(key);
            return ;
        }
        if(journalLine.getAction() == Action.PUT_ACTION){
            lruEntries.put(key,new Entry(key, journalLine.mSize, journalLine.mActionTime));
            return ;
        }
        if(journalLine.getAction() == Action.READ_ACTION){
            //do nothing
            return ;
        }
    }

    private synchronized void writeJournal(String line){
        try {
            journalWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseJournal(Action action,Entry entry,long t) {
        String res = action.getAction() + " " + entry.mKey;
        if (action == Action.PUT_ACTION) {
            res = res + " " + entry.mCleanExpireTime + " " + entry.mCleanSize;
        }
        return res + " " + t;
    }

    private void doPutAction(Editor editor){
        long t = System.currentTimeMillis();
        Entry entry = editor.mEntry;
        mLock.lock();
        entry.mStatus = STATUS.SYNCING;
        mLock.unlock();
        File dirtyFile = new File(directory, entry.mKey);
        if(null == dirtyFile || !dirtyFile.exists()){
            lruEntries.remove(editor.getKey());
            System.out.println("Cannot find file "+editor.getKey());
            return;
        }
        dirtyFile.renameTo(new File(directory, entry.mKey+".tmp"));
        File cleanFile = new File(directory, entry.mKey);
        boolean ref = write(cleanFile, entry.mData);
        if(!ref){//failed
            lruEntries.remove(editor.getKey());
            writeJournal(parseJournal(Action.REMOVE_ACTION, entry,t));
        }else{
            writeJournal(parseJournal(Action.PUT_ACTION, entry,t));
        }
        try {
            if(!ref){
                deleteIfExists(cleanFile);
            }
            deleteIfExists(dirtyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLock.lock();
        entry.mStatus = STATUS.SYNCED;
        mLock.unlock();
    }

    private void doRemoveAction(Editor editor){
        long t = System.currentTimeMillis();
        Entry entry = editor.mEntry;
        mLock.lock();
        entry.mStatus = STATUS.SYNCING;
        mLock.unlock();
        lruEntries.remove(editor.getKey());
        try {
            deleteIfExists(new File(directory, entry.mKey));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeJournal(parseJournal(Action.REMOVE_ACTION, entry,t));
        mLock.lock();
        entry.mStatus = STATUS.SYNCED;
        mLock.unlock();
    }


    public byte[] get(String key) {
        long time = System.currentTimeMillis();
        mLock.lock();
        Entry entry = lruEntries.get(key);
        if(null == entry){
            mLock.unlock();
            return null;
        }
        if(entry.mStatus == STATUS.NEED_SYNC
                || entry.mStatus == STATUS.SYNCING) {
            mLock.unlock();
            return entry.mData;
        }
        byte[] data = read(entry.getFreshFile());
        mLock.unlock();
        writeJournal(parseJournal(Action.READ_ACTION, entry,time));
        return data;
    }

    public void put(String key, IDiskCacheable value, long exTime) {
        byte[] data  = value.toBytes();
        mLock.lock();
        Entry entry = lruEntries.get(key);
        if(null == entry){
            entry = new Entry(key, data.length, exTime);
        }
        entry.update(data.length, exTime, data);
        Editor editor = new Editor(entry);
        editor.mAction = Action.PUT_ACTION;
        mWorkHandler.removeMessages(key.hashCode());
        Message msg = Message.obtain(mWorkHandler, key.hashCode(),editor);
        mWorkHandler.sendMessage(msg);
        mLock.unlock();
    }

    public void remove(String key){
        mLock.lock();
        mWorkHandler.removeMessages(key.hashCode());
        Entry entry = lruEntries.get(key);
        if(null != entry){
            entry.remove();
            Editor editor = new Editor(entry);
            editor.mAction = Action.REMOVE_ACTION;
            mWorkHandler.removeMessages(key.hashCode());
            Message msg = Message.obtain(mWorkHandler, key.hashCode(),editor);
            mWorkHandler.sendMessage(msg);
        }
        mLock.unlock();
    }

    public void clear() {

    }

    /**
     * @param file
     * @param content
     * @return true success,
     *              false failed
     */
    private boolean write(File file, byte[] content){
        if(!file.exists()){
            System.out.println(file.getAbsolutePath() + " is not exists, create new one");
            try {
                boolean res = file.createNewFile();
                if(!res){
                    System.out.println("Create "+file.getAbsolutePath()+" failed!");
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            FileOutputStream fw = new FileOutputStream(file, false);
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private byte[] read(File file){
        if(!file.exists()){
            System.out.println(file.getAbsolutePath() + " is not exists, create new one");
            return null;
        }
        byte[] data = new byte[(int)file.length()];
        try {
            FileInputStream fw = new FileInputStream(file);
            fw.read(data,0,data.length);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
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
            mKey = strings[1];
            try {
                if(action.equalsIgnoreCase(Action.REMOVE_ACTION.getAction())){
                    mAction = Action.REMOVE_ACTION;
                    mActionTime = Long.parseLong(strings[2]);
                }else if(action.equalsIgnoreCase(Action.PUT_ACTION.getAction())){
                    mAction = Action.PUT_ACTION;
                    mExpireTime = Long.parseLong(strings[2]);
                    mSize = Long.parseLong(strings[3]);
                    mActionTime = Long.parseLong(strings[4]);
                }else if(action.equalsIgnoreCase(Action.REMOVE_ACTION.getAction())){
                    mAction = Action.REMOVE_ACTION;
                    mActionTime = Long.parseLong(strings[2]);
                }
                isIllegal = true;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public String getKey(){
            return mKey;
        }
        private Action getAction(){
            return mAction;
        }
        private boolean isIllegal(){
            return isIllegal;
        }
    }
    public final class Editor {
        private Entry mEntry;
        private Action mAction;
        private boolean abort = false;
        private boolean hasErrors = false;
        private Editor(Entry entry) {
            if(null == entry) throw new IllegalArgumentException("");
            mEntry = entry;
        }
        private String getKey(){
            return mEntry.mKey;
        }
        private File getFile() {
            return new File(getKey() + ".tmp");
        }
        public void put(long size, long exTime, byte[] data){
            mEntry.mData = data;
            mEntry.mDirtySize = size;
            mEntry.mDirtyExpireTime = exTime;
            mAction = Action.PUT_ACTION;
        }
        public void remove(){
            mAction = Action.REMOVE_ACTION;
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
        private String mKey;
        private long mDirtySize;
        private long mDirtyExpireTime;
        private byte[] mData;
        private STATUS mStatus = STATUS.SYNCED;
        private long mCleanSize;
        private long mCleanExpireTime;
        public File getFreshFile(){
            return new File(directory, mKey);
        }
        private Entry(String key, long size, long expireTime){
            mKey = key;
            mCleanSize = size;
            mCleanExpireTime = expireTime;
        }
        public void update(long size, long expireTime, byte[] data){
            mStatus = STATUS.NEED_SYNC;
            mDirtySize = mCleanSize;
            mDirtyExpireTime = mCleanExpireTime;
            mCleanSize = size;
            mData = data;
            mCleanExpireTime = expireTime;
        }

        public void remove(){
            mDirtySize = mCleanSize;
            mDirtyExpireTime = mCleanExpireTime;
            mCleanSize = 0;
            mCleanExpireTime = 0;
            mStatus = STATUS.NEED_SYNC;
        }
        public String toString(){
            if(mStatus == STATUS.SYNCED){
                return mKey+" "+System.currentTimeMillis()+" "+mCleanExpireTime+" "+mCleanSize;
            }else{
                return mKey+" "+System.currentTimeMillis()+" "+mDirtyExpireTime+" "+mDirtySize;
            }
        }
        public int hashCode(){
            return mKey.hashCode();
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
    }
}
