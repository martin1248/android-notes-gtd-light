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
    private Context context;

    public NotesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
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

        ImageButton checkBtn = (ImageButton)view.findViewById(R.id.btnCheck);
        ImageButton deleteBtn = (ImageButton)view.findViewById(R.id.btnDelete);

        checkBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                Log.d("MainActivity", " ### checkBtn");
                notifyDataSetChanged();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                Log.d("MainActivity", " ### deleteBtn");
                notifyDataSetChanged();
            }
        });
    }
    //endregion
}
