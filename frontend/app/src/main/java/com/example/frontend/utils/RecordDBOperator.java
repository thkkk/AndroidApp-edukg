package com.example.frontend.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.frontend.GlobalConst;

import java.util.ArrayList;
import java.util.List;

public class RecordDBOperator {
    private SQLiteOpenHelper helper;

    private Context context;

    public RecordDBOperator(Context context){
        this.context = context;
    }

    /**
     * Please use: void addRecord(RecordToStore rec)
     * @param rec
     */
    public void insert(RecordToStore rec){
        helper = new MySqliteHelper(context, GlobalConst.DBForEntity, null, 1);
        Log.i("MYSQLITEHELPER","before get db");
        SQLiteDatabase db = helper.getWritableDatabase();
        Log.i("MYSQLITEHELPER","after get db");
        db.execSQL("insert into t_record(type, uri, content) values(?,?,?)" , new Object[]{rec.type,rec.uri,rec.content});
        db.close();
    }

    public List<RecordToStore> getAllRecords(){
        List<RecordToStore> list = new ArrayList<RecordToStore>();
        helper = new MySqliteHelper(context,GlobalConst.DBForEntity, null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();

        // TODO
        Cursor cursor = db.rawQuery("select type,uri,content from t_record", null);

        if(cursor == null){
            return null;
        }

        while(cursor.moveToNext()){
            RecordToStore stu = new RecordToStore(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            Log.i("MYSQLITEHELPER",stu.toString());
            list.add(stu);
        }
        return list;
    }

    /**
     * 不能加重复uri的实体
     * @param rec
     */
    public void addRecord(RecordToStore rec){
        helper = new MySqliteHelper(context,GlobalConst.DBForEntity, null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", rec.type);
        values.put("uri", rec.uri);
        values.put("content", rec.content);

        if(!exist(rec.uri))
            db.insert("t_record", null, values);
    }

    public void deleteByUri(String uri){
        helper = new MySqliteHelper(context,GlobalConst.DBForEntity, null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("t_record", "uri=?", new String[]{uri+""});
    }

    public RecordToStore getRecordByUri(String uri){
        RecordToStore stu = null;
        helper = new MySqliteHelper(context,GlobalConst.DBForEntity, null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("t_record", new String[]{"type","uri","content"}, "uri=?", new String[]{uri+""}, null, null, null);
        if(cursor == null){
            return null;
        }
        if(cursor.moveToFirst()){
            stu = new RecordToStore(cursor.getString(0),cursor.getString(1),cursor.getString(2));
        }
        return stu;
    }

    public String getContentByUri(String uri) {
        return getRecordByUri(uri).content;
    }

    /**
     * 是否存在uri对应的实体
     * @param uri
     * @return
     */
    public boolean exist(String uri) {
        return getRecordByUri(uri) != null;
    }

    public void updateContentByUri(String uri, String newContent){
        helper = new MySqliteHelper(context,GlobalConst.DBForEntity, null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", newContent);
        db.update("t_record", values, "uri=?", new String[]{uri+""});
    }
}
