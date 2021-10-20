package com.example.frontend.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySqliteHelper extends SQLiteOpenHelper {

    public static final String TAG = "MYSQLITEHELPER";

    public static final String CREATE_RECORD = "create table t_record (" +
            "type varchar(200), uri varchar(200), content varchar(30000))";  //TODO

    public MySqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG,"open db");
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG,"create db");
        Log.i(TAG,"before excSql");
        db.execSQL(CREATE_RECORD);
        Log.i(TAG,"after excSql");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
