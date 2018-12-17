package io.github.martin1248.gtdlight;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    // DB Version history
    // DATABASE_VERSION = 1; -> ALL_COLUMNS = {NOTE_ID, NOTE_TEXT, NOTE_CREATED};
    // DATABASE_VERSION = 2; -> ALL_COLUMNS = {NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_STATE, NOTE_PROJECT, NOTE_DUEDATE};
    // DATABASE_VERSION = 3; -> ALL_COLUMNS = {NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_STATE, NOTE_CONTEXT, NOTE_PROJECT, NOTE_DUEDATE};
    private static final int DATABASE_VERSION = 3;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_STATE = "noteState";
    public static final String NOTE_CONTEXT = "noteContext";
    public static final String NOTE_PROJECT = "noteProject";
    public static final String NOTE_DUEDATE = "noteDueDate";

    public  static final String[] ALL_COLUMNS = {NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_STATE, NOTE_CONTEXT, NOTE_PROJECT, NOTE_DUEDATE};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    NOTE_STATE + " TEXT, " +
                    NOTE_CONTEXT + " TEXT, " +
                    NOTE_PROJECT + " TEXT, " +
                    NOTE_DUEDATE + " TEXT" +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //region SQLiteOpenHelper
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        // Note: Check database file creation by using android device monitor:
        //          /Library/Android/sdk/tools/lib/monitor-x86_64/monitor.app
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }
    //endregion
}
