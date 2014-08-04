package com.hanyanan.tools.storage.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.hanyanan.tools.storage.Entry;
import com.hanyanan.tools.storage.Error.TypeNotSupportError;
import com.hanyanan.tools.storage.Utils;
import java.io.Serializable;


/**
 * Created by hanyanan on 2014/7/21.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DB_NAME = "private_data_persistence.db";
    private static final int VERSION = 1;
    private static final String TABLE = "data_table";
    public static interface BaseColumns{
        public static final String KEY = "key";
        public static final String CONTENT_BLOB = "content_blob";
        public static final String EXPIRE_TIME = "expire_time";
        public static final String TIME_STAMP = "time_stamp";
    };
    private SQLiteStatement mInsertTextStatement = null;
    private SQLiteStatement mInsertBlobStatement = null;
    private SQLiteStatement mDeleteStatement = null;
    private SQLiteStatement mUpdateTextStatement = null;
    private SQLiteStatement mUpdateBlobStatement = null;
    public DatabaseHelper(Context context) {
        super(context,context.getDatabasePath(DB_NAME).getAbsolutePath(), null,
                VERSION, mDatabaseErrorHandler);
    }
    private static DatabaseErrorHandler mDatabaseErrorHandler = new DatabaseErrorHandler(){
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {

        }
    };
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("create table ").append(TABLE)
                .append(" ( ")
                .append(BaseColumns.KEY).append(" TEXT PRIMARY KEY,")
                .append(BaseColumns.CONTENT_BLOB).append(" BLOB, ")
                .append(BaseColumns.EXPIRE_TIME).append(" LONG NOT NULL, ")
                .append(BaseColumns.TIME_STAMP).append(" LONG NOT NULL")
                .append(" )");
        String sCmd = cmd.toString();
        Log.d(TAG, "execute command " + sCmd);
        sqLiteDatabase.execSQL(sCmd);
    }

    public long insert(Entry content)throws TypeNotSupportError{
        if(null == mInsertTextStatement){
            mInsertTextStatement = getWritableDatabase().compileStatement(
                    "INSERT INTO  " + TABLE + "("
                            + BaseColumns.KEY + ","
                            + BaseColumns.CONTENT_BLOB + ","
                            + BaseColumns.EXPIRE_TIME + ","
                            + BaseColumns.TIME_STAMP
                            + ") VALUES (?,?,?,?)");

        }
        mInsertTextStatement.bindString(1, content.getKey());
        mInsertTextStatement.bindBlob(2, Utils.serialize(content.mData));
        mInsertTextStatement.bindLong(3, content.mExpireTime);
        mInsertTextStatement.bindLong(4, content.mTimeStamp);
        return mInsertTextStatement.executeInsert();
    }

    public Entry get(String primaryKey, String secondaryKey,Class<? extends Serializable> clazz)throws ClassNotFoundException{
        String key = Utils.generatorKey(primaryKey,secondaryKey);
        String sql = "select *  from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return null;
        if(cursor.moveToNext()){
            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            long ts = cursor.getLong(cursor.getColumnIndex(BaseColumns.TIME_STAMP));
            String k = cursor.getString(cursor.getColumnIndex(BaseColumns.KEY));
            byte data[] = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
            Entry entry = new Entry();
            entry.setKey(k);
            entry.mExpireTime = et;
            entry.mTimeStamp = ts;
            Serializable obj = Utils.deSerialize(data);
            entry.mData = clazz.cast(obj);
        }
        return null;
    }

    public Entry getByteArray(String primaryKey, String secondaryKey)throws ClassNotFoundException{
        String key = Utils.generatorKey(primaryKey,secondaryKey);
        String sql = "select *  from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return null;
        if(cursor.moveToNext()){
            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            long ts = cursor.getLong(cursor.getColumnIndex(BaseColumns.TIME_STAMP));
            String k = cursor.getString(cursor.getColumnIndex(BaseColumns.KEY));
            byte data[] = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
            Entry entry = new Entry();
            entry.setKey(k);
            entry.mExpireTime = et;
            entry.mTimeStamp = ts;
            entry.mData = data;
        }
        return null;
    }
//
//
//
//
//
//
//
//
//    public String getText(final String key){
//
//
//        if(cursor.moveToNext()){
//            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
//            byte data[] = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
//            et = Security.decode(et);
//            data = Security.decode(data);
//            if(null == data || !Filter.isIllegal(new QueryResultEntry(key, et, data))) {
//                return null;
//            }
//            return new String(data);
//        }
//        return null;
//    }
//
//    public byte[] getBlob(final String key){
//        String sql = "select "+ BaseColumns.CONTENT_BLOB+","+ BaseColumns.EXPIRE_TIME+
//                " from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
//        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
//        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return null;
//        if(cursor.moveToNext()){
//            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
//            byte[] data = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
//            et = Security.decode(et);
//            data = Security.decode(data);
//            if(Filter.isIllegal(new QueryResultEntry(key,et,data))) {
//                return data;
//            }
//        }
//        return null;
//    }

    public long remove(final String key){
        if(null == mDeleteStatement){
            mDeleteStatement = getWritableDatabase().compileStatement(
                    "DELETE FROM" + TABLE + " WHERE" + BaseColumns.KEY + " = ?");
        }
        mDeleteStatement.bindString(1, key);
        return mDeleteStatement.executeUpdateDelete();
    }

    public long update(Entry entry) throws TypeNotSupportError {
        if (mUpdateTextStatement == null) {
            mUpdateTextStatement = getWritableDatabase().compileStatement(
                    "UPDATE " + TABLE + " SET "
                            + BaseColumns.CONTENT_BLOB + " = ? , "+ BaseColumns.EXPIRE_TIME
                            +" = ? WHERE "
                            + BaseColumns.KEY + " = ?");
        }

        mUpdateTextStatement.bindBlob(1, Utils.serialize(entry.mData));
        mUpdateTextStatement.bindLong(2, entry.mExpireTime);
        mUpdateTextStatement.bindString(3, entry.getKey());
        return mUpdateTextStatement.executeUpdateDelete();
    }

    public boolean isExists(Entry entry){
        String sql = "select "+ BaseColumns.EXPIRE_TIME+
                " from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{entry.getKey()});
        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return false;
        if(cursor.moveToNext()){
            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            if(et > System.currentTimeMillis()) return false;
            return true;
        }
        return false;
    }

    public void close(){
        if(null != mInsertTextStatement){
            mInsertTextStatement.close();
            mInsertTextStatement = null;
        }
        if(null != mInsertBlobStatement){
            mInsertBlobStatement.close();
            mInsertBlobStatement = null;
        }
        if(null != mDeleteStatement){
            mDeleteStatement.close();
            mDeleteStatement = null;
        }
        if(null != mUpdateTextStatement){
            mUpdateTextStatement.close();
            mUpdateTextStatement = null;
        }
        if(null != mUpdateBlobStatement){
            mUpdateBlobStatement.close();
            mUpdateBlobStatement = null;
        }
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //TODO
    }
}
