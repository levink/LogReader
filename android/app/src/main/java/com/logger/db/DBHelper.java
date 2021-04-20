package com.logger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final String DB_NAME = "LogReaderDB";
    private static class Table {
        final static String FILES = "Files";
        final static String MASK = "Mask";
    }


    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1 );
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertMask(db,"*test?*");
        insertMask(db,"*test1*");
        insertMask(db,"*<d?v>*");
        insertMask(db,"*<div>*");
        insertMask(db,"*/n");
        insertMask(db,"*/r/n");
        insertMask(db,"*");
    }

    private void createTables(SQLiteDatabase db) {
        final String fileName = "data/db.sql";
        String[] cmdList = load(fileName).split(";");
        String emptyCmdReg = "^(\\s|\\n|\\r)*\\z";
        for (String cmd : cmdList) {
            if (!cmd.matches(emptyCmdReg))
                db.execSQL(cmd);
        }
    }

    private String load(String fileName) {
        String result = null;
        InputStream is;
        try {
            is = context.getAssets().open(fileName);
            result = IOUtils.toString(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Nothing to do here */
    }

    public ArrayList<String> getMasks() {
        ArrayList<String> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            final String sql = "select mask, [date] from Mask order by [date] desc";
            c = db.rawQuery(sql, null);

            if (c.moveToFirst()) {
                result.add(c.getString(0));
                c.moveToNext();
                while (!c.isAfterLast()) {
                    result.add(c.getString(0));
                    c.moveToNext();
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            db.close();
        }
        return result;
    }

    public void save(String url, String mask) {
        SQLiteDatabase db = getWritableDatabase();
        insertURL(db, url);
        insertMask(db, mask);
    }
    private void insertURL(SQLiteDatabase db, String url){
        ContentValues cv = new ContentValues();
        cv.put("url", url);
        cv.put("[date]", System.currentTimeMillis());
        db.insert(Table.FILES, null, cv);
    }
    private void insertMask(SQLiteDatabase db, String mask){
        ContentValues cv = new ContentValues();
        cv.put("mask", mask);
        cv.put("[date]", System.currentTimeMillis());
        db.insert(Table.MASK, null, cv);
    }

    public void clearMaskHistory(){
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete("Mask", null, null);
        } finally {
            db.close();
        }
    }
}
