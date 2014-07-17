package com.hanyanan.tools.datapersistence.stream;

import android.text.TextUtils;

import com.hanyanan.tools.datapersistence.BusyException;
import com.hanyanan.tools.datapersistence.Utils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
public class StreamDataPersistence {
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
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = "1";
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");
    private final File directory;
    private final File journalFile;
    private final File journalFileTmp;
    private final File journalFileBackup;
    private final int appVersion;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    private int redundantOpCount;

    protected void onEntryChanged(String key, long oldLength, long currLength){
        //TODO
    }
    protected void onEntryRemoved(String key, long length){
        //TODO
    }
    protected void onEntryAdded(String key, long length){
        //TODO
    }
    protected void onEntryClear(){
        //TODO
    }
    private StreamDataPersistence(File directory, int appVersion) {
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
    public static StreamDataPersistence open(File directory, int appVersion)
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
        StreamDataPersistence cache = new StreamDataPersistence(directory, appVersion);
        if (cache.journalFile.exists()) {
            try {
                cache.readJournal();
                cache.processJournal();
                cache.journalWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(cache.journalFile, true), Utils.ASCII_CHARSET));
                return cache;
            } catch (IOException journalIsCorrupt) {
                System.out.println("DiskLruCache " + directory + " is corrupt: "
                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // Create a new empty cache.
        directory.mkdirs();
        cache = new StreamDataPersistence(directory, appVersion);
        cache.rebuildJournal();
        return cache;
    }
    private void readJournal() throws IOException {
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
    private void processJournal() throws IOException {
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

    private void rebuildJournal() throws IOException {
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
                    writer.write(parseJournal(Action.DIRTY_ACTION, entry, System.currentTimeMillis()));
                } else {
                    writer.write(parseJournal(Action.CLEAN_ACTION, entry, System.currentTimeMillis()));
                }
            }

            writer.close();

            journalFileTmp.renameTo(journalFile);
            journalWriter = new BufferedWriter(new FileWriter(journalFile, true));
        }
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("delete failed");
        }
    }
    private static String parseJournal(Action action,Entry entry,long t) {
        String res = action.getAction() + " " + entry.key;
        if (action == Action.CLEAN_ACTION) {
            res = res + " " + entry.expireTime + " " + entry.length;
        }
        return res + " " + t;
    }
    private static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
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
            journalWriter.append(Action.READ_ACTION.getAction() + ' ' + key + '\n');
            if (journalRebuildRequired()) {
                //TODO executorService.submit(cleanupCallable);
            }

            return entry.newSnapshot();
        }
    }
    /**
     * Returns an editor for the entry named {@code key}, or null if another
     * edit is in progress.
     */
    private Editor edit(String key) throws IOException {
        synchronized (this) {
            checkNotClosed();
            validateKey(key);
            Entry entry = lruEntries.get(key);
            if (entry == null) {
                entry = new Entry(key);
                lruEntries.put(key, entry);
            } else if (entry.currentEditor != null) {
                throw new BusyException("Another edit is in progress, please try later!");
            }

            Editor editor = new Editor(entry);
            entry.currentEditor = editor;

            // Flush the journal before creating files to prevent file leaks.
            journalWriter.write(Action.DIRTY_ACTION.getAction() + ' ' + key + '\n');
            journalWriter.flush();
            return editor;
        }
    }

    private void completeEdit(Editor editor, boolean success) throws IOException {
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
                    return;
                }
            }

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
                    onEntryChanged(entry.key, oldLength, newLength);
                }
            } else {
                deleteIfExists(dirty);
            }

            redundantOpCount++;
            entry.currentEditor = null;
            if (entry.readable | success) {
                entry.readable = true;
                journalWriter.write(Action.CLEAN_ACTION + " " + entry.key + entry.getLength() + "\n");
            } else {
                lruEntries.remove(entry.key);
                journalWriter.write(Action.REMOVE_ACTION.getAction() + ' ' + entry.key + '\n');
                onEntryRemoved(entry.key, entry.getLength());
            }
            journalWriter.flush();

//        if (size > maxSize || journalRebuildRequired()) {
//            executorService.submit(cleanupCallable);
//        }
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal
     * and eliminate at least 2000 ops.
     */
    private boolean journalRebuildRequired() {
        synchronized (this) {
            final int redundantOpCompactThreshold = 2000;
            return redundantOpCount >= redundantOpCompactThreshold //
                    && redundantOpCount >= lruEntries.size();
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
        synchronized (this) {
            checkNotClosed();
            validateKey(key);
            Entry entry = lruEntries.get(key);
            if (entry == null || entry.currentEditor != null) {
                return false;
            }

            File file = entry.getCleanFile();
            if (file.exists() && !file.delete()) {
                throw new IOException("failed to delete " + file);
            }
//            size -= entry.lengths[i];
//            entry.lengths[i] = 0;

            redundantOpCount++;
            journalWriter.append(Action.REMOVE_ACTION.getAction() + ' ' + key + '\n');
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
    public class Snapshot implements Closeable {
        private final Entry entry;
        private final long lengths;
        private InputStream inputStream;
        private Snapshot(Entry entry, long lengths) {
            this.entry = entry;
            this.lengths = lengths;
        }
        public InputStream getInputStream(){
            synchronized (this) {
                if (null != inputStream) return inputStream;
                try {
                    inputStream = new SafeFileInputStream(entry.getCleanFile());
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

    public final class Editor{
        private final Entry entry;
        private boolean isWritten = false;
        private Editor(Entry entry) {
            this.entry = entry;
        }
        public void commit(){

        }
        public void abort(){

        }

        public SafeFileOutputStream newOutputStream(){
            synchronized (StreamDataPersistence.this){
                if (entry.currentEditor != this) {
                    throw new IllegalStateException();
                }
                if (!entry.readable) {
                    isWritten = true;
                }
                try {
                    File dirtyFile = entry.getDirtyFile();
                    SafeFileOutputStream outputStream = new SafeFileOutputStream(dirtyFile);
                    return outputStream;
                } catch (Exception e) {
                    // Attempt to recreate the cache directory.
                    directory.mkdirs();
                    try {
                        SafeFileOutputStream outputStream = new SafeFileOutputStream(entry.getDirtyFile());
                        return outputStream;
                    } catch (Exception e2) {
                        // We are unable to recover. Silently eat the writes.
                        return null;
                    }
                }
            }
        }
//        public SafeFileInputStream newInputStream(){
//            synchronized (StreamDataPersistence.this){
//                if (entry.currentEditor != this) {
//                    throw new IllegalStateException();
//                }
//                if (!entry.readable || this.isWritten) {
//                    return null;
//                }
//                try {
//                    return new SafeFileInputStream(entry.getCleanFile());
//                } catch (Exception e) {
//                    return null;
//                }
//            }
//        }
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

        private final List<Snapshot> currSnapshots = new ArrayList<Snapshot>();

        private Snapshot newSnapshot(){
            Snapshot s = new Snapshot(this,length){
                public void close() {
                    super.close();
                    currSnapshots.remove(this);
                }
            };
            currSnapshots.add(s);
            return s;
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
