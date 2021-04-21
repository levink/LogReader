package com.logger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

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

        insertURL(db, "https://snowrider.pro:1306/test_20mb.txt");
        insertURL(db, "https://snowrider.pro:1306/Hello.html");
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

    public ArrayList<String> getUrlHistory() {
        String sql =
                "select url " +
                "from " + Table.FILES + " " +
                "order by [date] desc";
        ArrayList<String> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, null)) {
            if (cursor.moveToFirst()) {
                do {
                    String item = cursor.getString(0);
                    result.add(item);
                } while (cursor.moveToNext());
            }
        }
        return result;
    }

    public ArrayList<String> getMaskHistory() {
        String sql =
                "select mask " +
                "from " + Table.MASK + " " +
                "order by [date] desc";
        ArrayList<String> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, null)) {
            if (cursor.moveToFirst()) {
                do {
                    String item = cursor.getString(0);
                    result.add(item);
                } while (cursor.moveToNext());
            }
        }
        return result;
    }

    public void saveMask(String mask) {
        if (mask != null) {
            SQLiteDatabase db = getWritableDatabase();
            insertMask(db, mask);
        }
    }
    public void saveUrl(String url) {
        if (url != null) {
            SQLiteDatabase db = getWritableDatabase();
            insertURL(db, url);
        }
    }
    private void insertURL(SQLiteDatabase db, String url) {
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
