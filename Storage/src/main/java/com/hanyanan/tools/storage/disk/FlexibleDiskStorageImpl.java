package com.hanyanan.tools.storage.disk;

import android.text.TextUtils;
import android.util.Log;

import com.hanyanan.tools.storage.Error.BusyInUsingError;
import com.hanyanan.tools.storage.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hanyanan on 2014/7/14.
 */
class FlexibleDiskStorageImpl implements IStreamStorage {
    private enum Action{
        DIRTY_ACTION("DIRTY"),
        CLEAN_ACTION("CLEAN"),
        REMOVE_ACTION("REMOVE"),
        READ_ACTION("READ");
        private String mAction;
        private Action(String action){
            mAction = action;
        }
        private String getAction(){
            return mAction;
        }
        public String toString(){
            return mAction;
        }
    }
    protected static final String JOURNAL_FILE = "journal";
    protected static final String JOURNAL_FILE_TEMP = "journal.tmp";
    protected static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    protected static final String MAGIC = "libcore.io.BasicDiskStorage";
    protected static final String VERSION_1 = "1";
    protected static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");
    protected final File directory;
    protected final File journalFile;
    protected final File journalFileTmp;
    protected final File journalFileBackup;
    protected final int appVersion;
    protected Writer journalWriter;
    protected final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    protected int redundantOpCount;
    protected long mCurrentSize = 0;

    public long getCurrentSize(){
        return mCurrentSize;
    }

    @Override
    public File getRootFile() {
        return directory;
    }

    @Override
    public boolean contain(String key) {
        synchronized (this){
            return lruEntries.containsKey(key);
        }
    }

    protected void onEntryChanged(String key, long oldLength, long currLength){
        mCurrentSize -= oldLength;
        mCurrentSize += currLength;
        //TODO
    }
    protected void onEntryRemoved(String key, long length){
        mCurrentSize -= length;
        //TODO
    }
    protected void onEntryAdded(String key, long length){
        mCurrentSize += length;
        //TODO
    }
    protected void onEntryClear(){
        mCurrentSize = 0;
        //TODO
    }
    protected FlexibleDiskStorageImpl(File directory, int appVersion) {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(directory, JOURNAL_FILE_BACKUP);
    }

    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param directory a writable directory
     * @throws java.io.IOException if reading or writing the cache directory fails
     */
    static FlexibleDiskStorageImpl open(File directory, int appVersion)
            throws IOException {
        // If a bkp file exists, use it instead.
        File backupFile = new File(directory, JOURNAL_FILE_BACKUP);
        if (backupFile.exists()) {
            File journalFile = new File(directory, JOURNAL_FILE);
            // If journal file also exists just delete backup file.
            if (journalFile.exists()) {
                backupFile.delete();
            } else {
                renameTo(backupFile, journalFile, false);
            }
        }

        // Prefer to pick up where we left off.
        FlexibleDiskStorageImpl cache = new FlexibleDiskStorageImpl(directory, appVersion);
        if (cache.journalFile.exists()) {
            try {
                cache.readJournal();
                cache.processJournal();
                cache.journalWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(cache.journalFile, true),
                                Utils.ASCII_CHARSET));
                return cache;
            } catch (IOException journalIsCorrupt) {
                System.out.println("DiskLruCache " + directory + " is corrupt: "
                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // Create a new empty cache.
        directory.mkdirs();
        cache = new FlexibleDiskStorageImpl(directory, appVersion);
        cache.rebuildJournal();
        return cache;
    }
    protected void readJournal() throws IOException {
        StrictLineReader reader = new StrictLineReader(new FileInputStream(journalFile), Utils.ASCII_CHARSET);
        try {
            String magic = reader.readLine();
            String version = reader.readLine();
            String appVersionString = reader.readLine();
            String blank = reader.readLine();
            if (!MAGIC.equals(magic)
                    || !VERSION_1.equals(version)
                    || !Integer.toString(appVersion).equals(appVersionString)
                    || !"".equals(blank)) {
                throw new IOException("unexpected journal header: ["
                        + magic + ", " + version + ", " + blank + "]");
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
    protected void readJournalLine(String line) throws Exception {
        JournalLine journalLine = new JournalLine(line);
        if (journalLine == null || !journalLine.isIllegal()) {
            return ;
        }
        String key = journalLine.getKey();
        if(journalLine.getAction() == Action.REMOVE_ACTION){
            lruEntries.remove(key);
            return ;
        }
        Entry entry = lruEntries.get(key);
        if (entry == null) {
            entry = new Entry(key);
            lruEntries.put(key, entry);
        }
        if(journalLine.getAction() == Action.CLEAN_ACTION){
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths(journalLine.mSize);
        }else if(journalLine.getAction() == Action.DIRTY_ACTION){
            entry.currentEditor = new Editor(entry);
        }else if(journalLine.getAction() == Action.READ_ACTION){
            //do nothing
        }
    }
    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    protected void processJournal() throws IOException {
        deleteIfExists(journalFileTmp);
        for (Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext(); ) {
            Entry entry = i.next();
            if (entry.currentEditor == null) {
                onEntryAdded(entry.key, entry.getLength());
            } else {
                entry.currentEditor = null;
                deleteIfExists(entry.getCleanFile());
                deleteIfExists(entry.getDirtyFile());
                i.remove();//delete from list
            }
        }
    }

    protected void rebuildJournal() throws IOException {
        synchronized (this) {
            if (journalWriter != null) {
                journalWriter.close();
            }
            Writer writer = new BufferedWriter(new FileWriter(journalFileTmp));
            writer.write(MAGIC);
            writer.write("\n");
            writer.write(VERSION_1);
            writer.write("\n");
            writer.write(Integer.toString(appVersion));
            writer.write("\n");
            writer.write("\n");
            for (Entry entry : lruEntries.values()) {
                if (entry.currentEditor != null) {
                    writer.write(parseJournal(Action.DIRTY_ACTION, entry));
                } else {
                    writer.write(parseJournal(Action.CLEAN_ACTION, entry));
                }
            }
            writer.close();

            journalFileTmp.renameTo(journalFile);
            journalWriter = new BufferedWriter(new FileWriter(journalFile, true));
        }
    }

    private void writeToJournal(String line)throws IOException{
        synchronized (this) {
            if (null != journalWriter) {
                journalWriter.write(line+"\n");
                journalWriter.flush();
            }
        }
        rebuildJournalIfNecessary();
    }

    private void rebuildJournalIfNecessary() throws IOException {
        final int redundantOpCompactThreshold = 2000;
        synchronized (this){
            if(redundantOpCount >= redundantOpCompactThreshold
                    && redundantOpCount >= lruEntries.size()){
                rebuildJournal();
            }
        }
    }
    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("delete failed");
        }
    }
    private static String parseJournal(Action action,Entry entry) {
        String res = action.getAction() + " " + entry.key;
        if (action == Action.CLEAN_ACTION) {
            res = res + " " + entry.expireTime + " " + entry.length;
        }
        return res + " " + System.currentTimeMillis();
    }
    protected static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
        if (deleteDestination) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException();
        }
    }
    private void checkNotClosed() {
        synchronized (this) {
            if (journalWriter == null) {
                throw new IllegalStateException("cache is closed");
            }
        }
    }
    private void validateKey(String key) {
        synchronized (this) {
            Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,64}: \"" + key + "\"");
            }
        }
    }
    /**
     * Returns a snapshot of the entry named {@code key}, or null if it doesn't exist is not
     * currently readable. If a value is returned, it is moved to the head of the LRU queue.
     */
    public Snapshot get(String key) throws IOException {
        synchronized (this) {
            checkNotClosed();
            validateKey(key);
            Entry entry = lruEntries.get(key);
            if (entry == null || !entry.readable) {
                return null;
            }

            redundantOpCount++;
//            journalWriter.append(Action.READ_ACTION.getAction() + ' ' + key + '\n');
            writeToJournal(parseJournal(Action.READ_ACTION, entry));
            return entry.newSnapshot();
        }
    }
    /**
     * Returns an editor for the entry named {@code key}, or null if another
     * edit is in progress.
     */
//    public Editor edit(String key) throws IOException,BusyInUsingError{
//        synchronized (this) {
//            checkNotClosed();
//            validateKey(key);
//            Entry entry = lruEntries.get(key);
//            if (entry == null) {
//                entry = new Entry(key);
//                lruEntries.put(key, entry);
//            } else if (entry.currentEditor != null) {
//                throw new BusyInUsingError("Another edit is in progress, please try later!");
//            }
//
//            Editor editor = new Editor(entry);
//            entry.currentEditor = editor;
//
//            // Flush the journal before creating files to prevent file leaks.
////            journalWriter.write(Action.DIRTY_ACTION.getAction() + ' ' + key + '\n');
////            journalWriter.flush();
//            writeToJournal(parseJournal(Action.DIRTY_ACTION, entry));
//            return editor;
//        }
//    }

    public Editor edit(String key) throws IOException,BusyInUsingError{
        synchronized (this) {
            checkNotClosed();
            validateKey(key);
            Entry entry = lruEntries.get(key);
            if (entry == null) {
                entry = new Entry(key);
                Editor editor = new Editor(entry);
                entry.currentEditor = editor;
                writeToJournal(parseJournal(Action.DIRTY_ACTION, entry));
                return editor;
//                lruEntries.put(key, entry);
            }else{
                if (entry.currentEditor != null) {
                    throw new BusyInUsingError("Another edit is in progress, please try later!");
                }
                Editor editor = new Editor(entry);
                entry.currentEditor = editor;
                writeToJournal(parseJournal(Action.DIRTY_ACTION, entry));
                return editor;
            }
        }
    }

    private boolean completeEdit(Editor editor, boolean success) throws IOException {
        synchronized (this) {
            Entry entry = editor.entry;
            if (entry.currentEditor != editor) {
                throw new IllegalStateException();
            }
            // If this edit is creating the entry for the first time, every index must have a value.
            if (success && !entry.readable) {
                if (!editor.isWritten) {
                    editor.abort();
                    throw new IllegalStateException("Newly created entry didn't create value. ");
                }
                if (!entry.getDirtyFile().exists()) {
                    editor.abort();
                    return false;
                }
            }

            redundantOpCount++;
            entry.currentEditor = null;
            File dirty = entry.getDirtyFile();
            if (success) {
                if (dirty.exists()) {
                    File clean = entry.getCleanFile();
                    dirty.renameTo(clean);
                    long oldLength = entry.getLength();
                    long newLength = clean.length();
                    entry.length = newLength;
//                size = size - oldLength + newLength;
                    entry.readable = true;
                    entry.setClean();//dispose all snapshots
                    if(lruEntries.containsKey(entry.getKey())) {
                        onEntryChanged(entry.key, oldLength, newLength);
                    }else{
                        onEntryAdded(entry.key, newLength);
                    }
                    lruEntries.put(entry.key,entry);
                }
            } else {
                deleteIfExists(dirty);//abort
            }


            if (entry.readable | success) {
                entry.readable = true;
//                journalWriter.write(Action.CLEAN_ACTION + " " + entry.key + entry.getLength() + "\n");
                writeToJournal(parseJournal(Action.CLEAN_ACTION, entry));
                return true;
            } else {
                lruEntries.remove(entry.key);
//                journalWriter.write(Action.REMOVE_ACTION.getAction() + ' ' + entry.key + '\n');
                writeToJournal(parseJournal(Action.REMOVE_ACTION, entry));
                onEntryRemoved(entry.key, entry.getLength());
                return false;
            }
//            journalWriter.flush();
        }
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete
     * all files in the cache directory including files that weren't created by
     * the cache.
     */
    public void delete() throws IOException {
        synchronized (this) {
            close();
            Utils.deleteContents(directory);
        }
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     * @return true if an entry was removed.
     */
    public boolean remove(String key) throws IOException {
        Log.d("FixSizeDiskStorage","try to remove "+key);
        synchronized (this) {
            checkNotClosed();
            validateKey(key);
            Entry entry = lruEntries.get(key);
            if (entry == null) {
                return false;
            }
            if(entry.currentEditor != null){
                entry.currentEditor.abort();
            }
            entry.currentEditor=null;

            File file = entry.getCleanFile();
            if (file.exists() && !file.delete()) {
                throw new IOException("failed to delete " + file);
            }
//            size -= entry.lengths[i];
//            entry.lengths[i] = 0;

            redundantOpCount++;
//            journalWriter.append(Action.REMOVE_ACTION.getAction() + ' ' + key + '\n');
            writeToJournal(parseJournal(Action.REMOVE_ACTION, entry));
            lruEntries.remove(key);

//        if (journalRebuildRequired()) {
//            executorService.submit(cleanupCallable);
//        }
            onEntryRemoved(key, entry.getLength());
            return true;
        }
    }

    /** Closes this cache. Stored values will remain on the filesystem. */
    public void close() throws IOException {
        synchronized (this) {
            if (journalWriter == null) {
                return; // Already closed.
            }
            for (Entry entry : new ArrayList<Entry>(lruEntries.values())) {
                if (entry.currentEditor != null) {
                    entry.currentEditor.abort();
                }
            }
            journalWriter.close();
            journalWriter = null;
        }
    }
    /** Returns true if this cache has been closed. */
    public boolean isClosed() {
        return journalWriter == null;
    }

    /** A snapshot of the values for an entry. */
    public class SnapshotImpl implements Snapshot {
        private final Entry entry;
        private final long lengths;
        private InputStream inputStream;
        private SnapshotImpl(Entry entry, long lengths) {
            this.entry = entry;
            this.lengths = lengths;
        }
        public InputStream getInputStream(){
            synchronized (this) {
                if (null != inputStream) return inputStream;
                try {
                    inputStream = new FileInputStream(entry.getCleanFile());
                    return inputStream;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        /** Returns the byte length of the value. */
        public long getLength() {
            return lengths;
        }

        public void close() {
            synchronized (this) {
                if (null != inputStream) {
                    Utils.closeQuietly(inputStream);
                }
                inputStream = null;
            }
        }
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
            String []strings  = line.split(" ");
            if(strings.length < 3){
                return;
            }
            String action = strings[0];
            mKey = strings[1];
            try {
                if(action.equalsIgnoreCase(Action.REMOVE_ACTION.getAction())){
                    mAction = Action.REMOVE_ACTION;
                    mActionTime = Long.parseLong(strings[2]);
                } else if(action.equalsIgnoreCase(Action.CLEAN_ACTION.getAction())){
                    mAction = Action.CLEAN_ACTION;
                    mExpireTime = Long.parseLong(strings[2]);
                    mSize = Long.parseLong(strings[3]);
                    mActionTime = Long.parseLong(strings[4]);
                } else if(action.equalsIgnoreCase(Action.REMOVE_ACTION.getAction())){
                    mAction = Action.REMOVE_ACTION;
                    mActionTime = Long.parseLong(strings[2]);
                } else if(action.equalsIgnoreCase(Action.DIRTY_ACTION.getAction())){
                    mAction = Action.DIRTY_ACTION;
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

    public final class Editor implements IStreamStorage.Editor {
        private final Entry entry;
        private boolean isWritten = false;
        private boolean hasErrors = false;
        private boolean committed = false;
        private Editor(Entry entry) {
            this.entry = entry;
        }
        public boolean commit() throws IOException {
            boolean success = false;
            if (hasErrors) {
                success = completeEdit(this, false);
                remove(entry.key); // The previous entry is stale.
            } else {
                success = completeEdit(this, true);
            }
            committed = true;
            return success;
        }
        public void abort() throws IOException {
            completeEdit(this, false);
        }

        public void setExpireTime(long expireTime){
            entry.expireTime = expireTime;
        }
        public OutputStream newOutputStream(){
            synchronized (FlexibleDiskStorageImpl.this){
                if (entry.currentEditor != this) {
                    throw new IllegalStateException();
                }
                if (!entry.readable) {
                    isWritten = true;//called when create the entry
                }
                File dirtyFile = entry.getDirtyFile();
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(dirtyFile);
                } catch (Exception e) {
                    // Attempt to recreate the cache directory.
                    directory.mkdirs();
                    try {
                        outputStream = new FileOutputStream(dirtyFile);
                    } catch (Exception e2) {
                        // We are unable to recover. Silently eat the writes.
                        return IStreamStorage.NULL_OUTPUT_STREAM;
                    }
                }
                return new IStreamStorage.FaultHidingOutputStream(outputStream,this);
            }
        }

        @Override
        public InputStream newInputStream() {
            synchronized (FlexibleDiskStorageImpl.this){
                if (entry.currentEditor != this) {
                    throw new IllegalStateException();
                }
                if (!entry.readable || this.isWritten) {
                    return null;
                }
                try {
                    return new FileInputStream(entry.getCleanFile());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        public void close(){

        }

        @Override
        public void setHasError(boolean error) {
            hasErrors = error;
        }
    }
    protected final class Entry {
        private final String key;
        /** Lengths of this entry's files. */
        private long length;
        /** Time of expired. */
        private long expireTime = Long.MAX_VALUE;
        /** True if this entry has ever been published. */
        private boolean readable;
        /** The ongoing edit or null if this entry is not being edited. */
        private Editor currentEditor;

        private final List<Snapshot> currSnapshots = new ArrayList<Snapshot>();

        private Snapshot newSnapshot(){
            Snapshot s = new SnapshotImpl(this,length){
                public void close() {
                    super.close();
                    currSnapshots.remove(this);
                }
            };
            currSnapshots.add(s);
            return s;
        }
        String getKey(){
            return key;
        }
        private void setClean(){
            for(Snapshot ss : currSnapshots){
                ss.close();
            }
            currSnapshots.clear();
        }
        private Entry(String key) {
            this.key = key;
        }

        public long getLength()  {
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
}
