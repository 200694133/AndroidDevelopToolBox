package com.hanyanan.tools.storage.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.hanyanan.tools.storage.Error.TypeNotSupportError;
import com.hanyanan.tools.storage.Utils;

import java.io.Serializable;


/**
 * Created by hanyanan on 2014/7/21.
 * 基础的数据存储，所有的数据存储成String类型，对于基本的数据类型，可以使用这个，某些简单的对象，
 * 可以使用gson转化成String，然后在转换成对象。
 */
public final class BasicDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BasicDatabaseHelper";
    private static final String DB_NAME = "private_data_persistence.db";
    private static final int VERSION = 1;
    private static final String TABLE = "basic_data_table";
    public static interface BaseColumns{
        public static final String KEY = "key";
        public static final String EXPIRE_TIME = "expire_time";
        public static final String DATA = "data";
    };
    private SQLiteStatement mInsertTextStatement = null;
    private SQLiteStatement mDeleteStatement = null;
    private SQLiteStatement mUpdateTextStatement = null;
    private SQLiteStatement mPutTextStatement = null;

    public BasicDatabaseHelper(Context context) {
        super(context,context.getDatabasePath(DB_NAME).getAbsolutePath(), null,
                VERSION, mDatabaseErrorHandler);
    }

    public BasicDatabaseHelper(Context context, String path) {
        super(context,path, null, VERSION, mDatabaseErrorHandler);
    }

    private static DatabaseErrorHandler mDatabaseErrorHandler = new DatabaseErrorHandler(){
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {
            //TODO
        }
    };
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("create table ").append(TABLE)
                .append(" ( ")
                .append(BaseColumns.KEY).append(" TEXT PRIMARY KEY,")
                .append(BaseColumns.EXPIRE_TIME).append(" LONG NOT NULL, ")
                .append(BaseColumns.DATA).append(" TEXT ")
                .append(" )");
        String sCmd = cmd.toString();
        Log.d(TAG, "execute command " + sCmd);
        sqLiteDatabase.execSQL(sCmd);
    }

    public long put(Entry content){
        if(mPutTextStatement == null){
            mPutTextStatement = getWritableDatabase().compileStatement(
                    "INSERT OR REPLACE INTO " + TABLE + "( "
                            + BaseColumns.KEY + ","
                            + BaseColumns.EXPIRE_TIME + ","
                            + BaseColumns.DATA
                            + ") VALUES (?,?,?)");
        }
        mPutTextStatement.bindString(1, content.getKey());
        mPutTextStatement.bindLong(2, content.getExpire());
        mPutTextStatement.bindString(3, content.getData());
        return mPutTextStatement.executeInsert();
    }

    public long insert(Entry content)throws TypeNotSupportError{
        if(null == mInsertTextStatement){
            mInsertTextStatement = getWritableDatabase().compileStatement(
                    "INSERT INTO  " + TABLE + "("
                            + BaseColumns.KEY + ","
                            + BaseColumns.EXPIRE_TIME + ","
                            + BaseColumns.DATA + ","
                            + ") VALUES (?,?,?)");

        }
        mInsertTextStatement.bindString(1, content.getKey());
        mInsertTextStatement.bindLong(2, content.getExpire());
        mInsertTextStatement.bindString(3, content.getData());
        return mInsertTextStatement.executeInsert();
    }

    public Entry get(String key){
        String sql = "select "+BaseColumns.EXPIRE_TIME+" , "+BaseColumns.DATA+"  from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
        if(cursor == null || cursor.isClosed() ||cursor.getCount()<=0) return null;
        if(cursor.moveToFirst()){
            long expire = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            String data = cursor.getString(cursor.getColumnIndex(BaseColumns.DATA));
            Entry entry = new Entry();
            entry.data = data;
            entry.expire = expire;
            entry.key = key;
            return entry;
        }
        return null;
    }

//    public Entry getByteArray(String primaryKey, String secondaryKey)throws ClassNotFoundException{
//        String key = Utils.generatorKey(primaryKey,secondaryKey);
//        String sql = "select *  from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
//        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
//        if(cursor == null || cursor.isClosed() ||cursor.getCount()<=0) return null;
//        if(cursor.moveToFirst()){
//            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
//            long ts = cursor.getLong(cursor.getColumnIndex(BaseColumns.TIME_STAMP));
//            String k = cursor.getString(cursor.getColumnIndex(BaseColumns.KEY));
//            byte data[] = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
//            Entry entry = new Entry();
//            entry.setKey(k);
//            entry.mExpireTime = et;
//            entry.mTimeStamp = ts;
//            entry.mData = data;
//            return entry;
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

    public long update(Entry entry)  {
        if (mUpdateTextStatement == null) {
            mUpdateTextStatement = getWritableDatabase().compileStatement(
                    "UPDATE " + TABLE + " SET "
                            + BaseColumns.DATA + " = ? , "+ BaseColumns.EXPIRE_TIME
                            +" = ?  WHERE " + BaseColumns.KEY + " = ?");
        }
        mUpdateTextStatement.bindString(1, entry.getKey());
        mUpdateTextStatement.bindLong(2, entry.getExpire());
        mUpdateTextStatement.bindString(3, entry.getKey());

        return mUpdateTextStatement.executeUpdateDelete();
    }

    public boolean isExists(Entry entry){
        String sql = "select "+ BaseColumns.EXPIRE_TIME+
                " from " + TABLE + " where "+ BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{entry.getKey()});
        if(cursor == null || cursor.isClosed()) return false;
        if(cursor.getCount() > 0){
            return true;
        }
        return false;
    }

    public void close(){
        if(null != mInsertTextStatement){
            mInsertTextStatement.close();
            mInsertTextStatement = null;
        }
        if(null != mDeleteStatement){
            mDeleteStatement.close();
            mDeleteStatement = null;
        }
        if(null != mUpdateTextStatement){
            mUpdateTextStatement.close();
            mUpdateTextStatement = null;
        }
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //TODO
    }
}
