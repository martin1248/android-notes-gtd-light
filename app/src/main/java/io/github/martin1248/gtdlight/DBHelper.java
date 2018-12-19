package io.github.martin1248.gtdlight;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import java.util.Set;
import java.util.TreeSet;

public class DBHelper {
    public static String[] getContexts(ContentResolver contentResolver) {
        return getAllValuesFromTableColumn(contentResolver, DBOpenHelper.NOTE_CONTEXT);
    }

    public static String[] getProjects(ContentResolver contentResolver) {
        return getAllValuesFromTableColumn(contentResolver, DBOpenHelper.NOTE_PROJECT);
    }

    private static String[] getAllValuesFromTableColumn(ContentResolver contentResolver, String tableColumn) {
        Set<String> values = new TreeSet<>();
        Cursor mCursor = contentResolver.query(NotesProvider.CONTENT_URI, null, null,null, null);
        int indexContext = mCursor.getColumnIndex(tableColumn);
        if (mCursor == null) {
            Log.e("DBHelper", "Failed to query notes");
        } else if (mCursor.getCount() < 1) {
            Log.d("DBHelper", "No notes");
        } else {
            while (mCursor.moveToNext()) {
                String value = mCursor.getString(indexContext);
                if(value != null && !value.equals("")) {
                    values.add(" " + value); // TRICK: With this line by pressing space in TextView all elements are shown ;-) (SPACE is removed by trim method)
                }
            }
        }
        return values.toArray(new String[]{});
    }
}
