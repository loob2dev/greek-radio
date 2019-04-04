package com.Greek.Radios.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.Greek.Radios.models.Radio;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_your_radio_radio";
    private static final String TABLE_NAME = "tbl_radio_favorite";
    private static final String KEY_ID = "id";
    private static final String KEY_RADIOID = "radio_id";
    private static final String KEY_RADIO_NAME = "radio_name";
    private static final String KEY_RADIO_CATEGORY_NAME = "category_name";
    private static final String KEY_RADIO_IMAGE = "radio_image";
    private static final String KEY_RADIO_URL = "radio_url";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_RADIOID + " TEXT,"
                + KEY_RADIO_NAME + " TEXT,"
                + KEY_RADIO_CATEGORY_NAME + " TEXT,"
                + KEY_RADIO_IMAGE + " TEXT,"
                + KEY_RADIO_URL + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    //Adding Record in Database

    public void AddtoFavorite(Radio pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RADIOID, pj.getRadio_id());
        values.put(KEY_RADIO_NAME, pj.getRadio_name());
        values.put(KEY_RADIO_CATEGORY_NAME, pj.getCategory_name());
        values.put(KEY_RADIO_IMAGE, pj.getRadio_image());
        values.put(KEY_RADIO_URL, pj.getRadio_url());
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Getting All Data
    public List<Radio> getAllData() {
        List<Radio> dataList = new ArrayList<Radio>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Radio contact = new Radio();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setRadio_id(cursor.getString(1));
                contact.setRadio_name(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setRadio_image(cursor.getString(4));
                contact.setRadio_url(cursor.getString(5));
                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        db.close();
        return dataList;
    }

    //getting single row

    public List<Radio> getFavRow(String id) {
        List<Radio> dataList = new ArrayList<Radio>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE radio_id=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Radio contact = new Radio();

                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setRadio_id(cursor.getString(1));
                contact.setRadio_name(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setRadio_image(cursor.getString(4));
                contact.setRadio_url(cursor.getString(5));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }
        // return contact list
        db.close();
        return dataList;
    }

    //for remove favorite

    public void RemoveFav(Radio contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_RADIOID + " = ?",
                new String[]{String.valueOf(contact.getRadio_id())});
        db.close();
    }

    public enum DatabaseManager {
        INSTANCE;
        private SQLiteDatabase db;
        private boolean isDbClosed = true;
        DatabaseHandler dbHelper;

        public void init(Context context) {
            dbHelper = new DatabaseHandler(context);
            if (isDbClosed) {
                isDbClosed = false;
                this.db = dbHelper.getWritableDatabase();
            }

        }

        public boolean isDatabaseClosed() {
            return isDbClosed;
        }

        public void closeDatabase() {
            if (!isDbClosed && db != null) {
                isDbClosed = true;
                db.close();
                dbHelper.close();
            }
        }
    }
}
