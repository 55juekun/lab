package com.downloadvideo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by 55珏坤 on 2018/5/23.
 */

public class FileService {
    private DBOpenHelper dbOpenHelper;
    public  FileService(Context context){
        dbOpenHelper=new DBOpenHelper(context);
    }
    public Map<Integer,Integer> getData(String path){
        SQLiteDatabase db=dbOpenHelper.getReadableDatabase();
        Cursor cursor=db.rawQuery("select threadid, downlength from filedownload where downpath=?",
                new String[]{path});
        Map<Integer,Integer> data=new HashMap<>();
        cursor.moveToFirst();
        try{
            data.put(cursor.getInt(cursor.getColumnIndexOrThrow("threadid")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("downlength")));
        }catch (Exception ignored){
        }
        while (cursor.moveToNext()){
            data.put(cursor.getInt(cursor.getColumnIndexOrThrow("threadid")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("downlength")));
        }
        cursor.close();
        db.close();
        return data;
    }
    public void save(String path,Map<Integer,Integer>map){
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                db.execSQL("insert into filedownload(downpath, threadid, downlength) values(?,?,?)",
                        new Object[]{path, entry.getKey(), entry.getValue()});
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }
    public void update(String path,int threadid,int pos){
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        //更新特定下载路径下特定线程已下载的文件长度
        db.execSQL("update filedownload set downlength=? where downpath=? and threadid=?",
                new Object[]{pos, path, threadid});
        db.close();
    }
    public void delete(String path)
    {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.execSQL("delete from filedownload where downpath=?", new Object[]{path});
        db.close();
    }
}
