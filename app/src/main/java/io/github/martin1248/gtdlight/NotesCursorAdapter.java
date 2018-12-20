package io.github.martin1248.gtdlight;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class NotesCursorAdapter extends CursorAdapter{

    public NotesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    //region CursorAdapter
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.note_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String noteText = cursor.getString(
                cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT)
        );

        int pos = noteText.indexOf(10); // ASCII for \n
        if (pos != -1) {
            noteText = noteText.substring(0, pos) + "...";
        }

        TextView tv = view.findViewById(R.id.tvNote);
        tv.setText(noteText);

        // sub text
        String subtext = "";
        String noteContext = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CONTEXT));
        if (noteContext != null && !noteContext.equals("")) {
            subtext += noteContext;
        }
        String noteProject = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_PROJECT));
        if (noteProject != null && !noteProject.equals("")) {
            subtext += noteProject;
        }
        String noteDueDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_DUEDATE));
        if (noteDueDate != null && !noteDueDate.equals("")) {
            subtext += noteDueDate;
        }
        TextView tvSub = view.findViewById(R.id.tvNoteSub);
        tvSub.setText(subtext);
    }
    //endregion
}
