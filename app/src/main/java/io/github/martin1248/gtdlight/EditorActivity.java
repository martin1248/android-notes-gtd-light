package io.github.martin1248.gtdlight;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editorText;
    private Spinner editorState;
    private AutoCompleteTextView editorProject;
    private EditText editorDueDate;
    private String noteFilter;
    private String oldText;
    private String oldState;
    private String oldProject;
    private String oldDueDate;

    private String[] states = {"Inbox", "Next actions", "Calender", "Some day/maybe", "Waiting for", "Reference", "Trash"};
    private String[] projects = {"Project 1", "Project 2", "Project 3"};

    //region AppCompat-, Fragment- and Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editorText = findViewById(R.id.editText);
        editorState = findViewById(R.id.editState);
        editorProject = findViewById(R.id.editProject);
        editorDueDate = findViewById(R.id.editDueDate);

        // Set adapter for drop down like ui types
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, states);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editorState.setAdapter(adapter);

        ArrayAdapter<String> adapterProjects = new ArrayAdapter<String>(this,R.layout.spinner_item, projects);
        editorProject.setThreshold(1);
        editorProject.setAdapter(adapterProjects);


        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        if(uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(R.string.new_note);

            // STATE
            editorState.setSelection(0);
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null,null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            oldState = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_STATE));
            oldProject = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_PROJECT));
            oldDueDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_DUEDATE));

            // TEXT
            editorText.setText(oldText);
            editorText.requestFocus();

            // STATE
            int position = Arrays.asList(states).indexOf(oldState);
            if (position >= 0) {
                editorState.setSelection(position);
            } else {
                editorState.setSelection(0);
            }

            // PROJECT
            editorProject.setText(oldProject);

            //DUE DATE
            editorDueDate.setText(oldDueDate);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
    //endregion

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();;
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = editorText.getText().toString().trim();
        String newState = editorState.getSelectedItem().toString();
        String newProject = editorProject.getText().toString().trim();
        String newDueDate = editorDueDate.getText().toString().trim();
        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText, newState, newProject, newDueDate);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {
                    deleteNote();
                } else if (noteIsUnchanged(newText, newState, newProject, newDueDate)){
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText, newState, newProject, newDueDate);
                }
        }
        finish();
    }

    private boolean noteIsUnchanged(String noteText, String noteState, String noteProject, String noteDueDate) {
        if (!(oldText == null ? noteText == null : oldText.equals(noteText))) {
            return false;
        }
        if (!(oldState == null ? noteState == null : oldState.equals(noteState))) {
            return false;
        }
        if (!(oldProject == null ? noteProject == null : oldProject.equals(noteProject))) {
            return false;
        }
        if (!(oldDueDate == null ? noteDueDate == null : oldDueDate.equals(noteDueDate))) {
            return false;
        }
        return true;
    }

    private void updateNote(String noteText, String noteState, String noteProject, String noteDueDate) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_STATE, noteState);
        values.put(DBOpenHelper.NOTE_PROJECT, noteProject);
        values.put(DBOpenHelper.NOTE_DUEDATE, noteDueDate);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this,getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteState, String noteProject, String noteDueDate) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_STATE, noteState);
        values.put(DBOpenHelper.NOTE_PROJECT, noteProject);
        values.put(DBOpenHelper.NOTE_DUEDATE, noteDueDate);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }
}
