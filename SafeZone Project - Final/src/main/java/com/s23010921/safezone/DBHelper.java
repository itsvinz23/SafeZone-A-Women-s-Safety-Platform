package com.s23010921.safezone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "safeZone.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "contacts";
    private static final String COL_ID = "id";
    private static final String COL_FNAME = "fname";
    private static final String COL_LNAME = "lname";
    private static final String COL_NUMBER = "number";
    private static final String COL_PRIORITY = "priority";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FNAME + " TEXT, " +
                COL_LNAME + " TEXT, " +
                COL_NUMBER + " TEXT, " +
                COL_PRIORITY + " TEXT);";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertContact(String fname, String lname, String number, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FNAME, fname);
        values.put(COL_LNAME, lname);
        values.put(COL_NUMBER, number);
        values.put(COL_PRIORITY, priority);
        return db.insert(TABLE_NAME, null, values) != -1;
    }

    public List<ContactModel> getAllContacts() {
        List<ContactModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String fname = cursor.getString(cursor.getColumnIndexOrThrow(COL_FNAME));
                String lname = cursor.getString(cursor.getColumnIndexOrThrow(COL_LNAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(COL_NUMBER));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORITY));
                list.add(new ContactModel(id, fname, lname, number, priority));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateContact(int id, String fname, String lname, String number, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FNAME, fname);
        values.put(COL_LNAME, lname);
        values.put(COL_NUMBER, number);
        values.put(COL_PRIORITY, priority);
        return db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Delete by phone number
    public boolean deleteContact(String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_NUMBER + "=?", new String[]{number}) > 0;
    }


    public boolean deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean isPriorityTaken(String priority, int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
                COL_PRIORITY + "=? AND " + COL_ID + "!=?", new String[]{priority, String.valueOf(excludeId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isPriorityTaken(String priority) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_PRIORITY + "=?", new String[]{priority});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}