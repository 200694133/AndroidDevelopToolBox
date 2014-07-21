package com.hanyanan.tools.datapersistence.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by hanyanan on 2014/7/21.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static final String DB_NAME = "private_data_persistence.db";
    private static final int VERSION = 1;
    private static final String TABLE = "data_table";
    public static interface BaseColumns{
        public static final String KEY = "key";
        public static final String CONTENT_TEXT = "content_text";
        public static final String CONTENT_BLOB = "content_blob";
        public static final String EXPIRE_TIME = "expire_time";
        public static final String TIME_STAMP = "time_stamp";
    };
    private SQLiteStatement mInsertTextStatement = null;
    private SQLiteStatement mInsertBlobStatement = null;
    private SQLiteStatement mDeleteStatement = null;
    private SQLiteStatement mUpdateTextStatement = null;
    private SQLiteStatement mUpdateBlobStatement = null;
    public DBHelper(Context context) {
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
                .append(BaseColumns.CONTENT_TEXT).append(" TEXT, ")
                .append(BaseColumns.CONTENT_BLOB).append(" BLOB, ")
                .append(BaseColumns.EXPIRE_TIME).append(" LONG NOT NULL, ")
                .append(BaseColumns.TIME_STAMP).append(" LONG NOT NULL")
                .append(" )");
        String sCmd = cmd.toString();
        Log.d(TAG, "execute command " + sCmd);
        sqLiteDatabase.execSQL(sCmd);
    }

    public long insert(final String key, BaseType content){
        if(null == mInsertTextStatement){
            mInsertTextStatement = getWritableDatabase().compileStatement(
                    "INSERT INTO  " + TABLE + "("
                            + BaseColumns.KEY + ","
                            + BaseColumns.CONTENT_TEXT + ","
                            + BaseColumns.EXPIRE_TIME + ","
                            + BaseColumns.TIME_STAMP
                            + ") VALUES (?,?,?,?)");

        }
        mInsertTextStatement.bindString(1, key);
        mInsertTextStatement.bindString(2, content.getContent());
        mInsertTextStatement.bindLong(3, content.getExpireTime());
        mInsertTextStatement.bindLong(4, System.currentTimeMillis());
        return mInsertTextStatement.executeInsert();
    }

    public long insert(final String key, BlobType blob){
        if(null == mInsertBlobStatement){
            mInsertBlobStatement = getWritableDatabase().compileStatement(
                    "INSERT INTO  " + TABLE + "("
                            + BaseColumns.KEY + ","
                            + BaseColumns.CONTENT_BLOB + ","
                            + BaseColumns.EXPIRE_TIME + ","
                            + BaseColumns.TIME_STAMP
                            + ") VALUES (?,?,?,?)");

        }
        mInsertBlobStatement.bindString(1, key);
        mInsertBlobStatement.bindBlob(2, blob.getData());
        mInsertBlobStatement.bindLong(3, blob.getExpireTime());
        mInsertBlobStatement.bindLong(4, System.currentTimeMillis());
        return mInsertBlobStatement.executeInsert();
    }

    public String getText(final String key){
        String sql = "select "+BaseColumns.CONTENT_TEXT+","+BaseColumns.EXPIRE_TIME+
                " from " + TABLE + " where "+BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return null;
        if(cursor.moveToNext()){
            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            if(et > System.currentTimeMillis()) return null;
            String data = cursor.getString(cursor.getColumnIndex(BaseColumns.CONTENT_TEXT));
            return data;
        }
        return null;
    }

    public byte[] getBlob(final String key){
        String sql = "select "+BaseColumns.CONTENT_BLOB+","+BaseColumns.EXPIRE_TIME+
                " from " + TABLE + " where "+BaseColumns.KEY + " = ? ";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{key});
        if(cursor == null || cursor.isClosed() || !cursor.moveToFirst()) return null;
        if(cursor.moveToNext()){
            long et = cursor.getLong(cursor.getColumnIndex(BaseColumns.EXPIRE_TIME));
            if(et > System.currentTimeMillis()) return null;
            byte[] data = cursor.getBlob(cursor.getColumnIndex(BaseColumns.CONTENT_BLOB));
            return data;
        }
        return null;
    }

    public long remove(final String key){
        if(null == mDeleteStatement){
            mDeleteStatement = getWritableDatabase().compileStatement(
                    "DELETE FROM" + TABLE + " WHERE" + BaseColumns.KEY + " = ?");
        }
        mDeleteStatement.bindString(1, key);
        return mDeleteStatement.executeUpdateDelete();
    }

    public long update(String key, String data){
        if (mUpdateStatement == null) {
            mUpdateStatement = getWritableDatabase().compileStatement(
                    "UPDATE " + TABLE_NAME + " SET "
                            + FIELD_END_TIME + " = ? WHERE "
                            + FIELD_ID + " = ?");
        }
        mUpdateStatement.bindLong(1, endTime);
        mUpdateStatement.bindLong(2, id);
        return mUpdateStatement.executeUpdateDelete();
    }

    public void close(){
        super.close();
        //TODO
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //TODO
    }
}
