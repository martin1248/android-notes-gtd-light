package io.github.martin1248.gtdlight;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

public class EditorActivity extends AppCompatActivity implements
        View.OnClickListener{

    private String action;
    private EditText editorText;
    private Spinner editorState;
    private AutoCompleteTextView editorContext;
    private AutoCompleteTextView editorProject;
    private EditText editorDueDate;
    private String noteFilter;
    private String oldText;
    private String oldState;
    private String oldContext;
    private String oldProject;
    private String oldDueDate;

    Button btnDatePicker;
    private int mYear, mMonth, mDay;

    public static final String[] states = {"Inbox", "Next actions", "Calender", "Waiting for", "Some day/maybe", "Reference", "Trash", "Done"};
    private String[] contexts = {"Home", "Office", "Phone", "Computer", "Shopping", "Errands" , "Agendas",
            " Home", " Office", " Phone", " Computer", " Shopping", " Errands" , " Agendas"};
    private String[] projects = {".Project 1", ".Project 2", ".Project 3"};

    //region AppCompat-, Fragment- and Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editorText = findViewById(R.id.editText);
        editorState = findViewById(R.id.editState);
        editorContext = findViewById(R.id.editContext);
        editorProject = findViewById(R.id.editProject);
        editorDueDate = findViewById(R.id.editDueDate);

        // Set adapter for drop down like ui types
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, states);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editorState.setAdapter(adapter);

        ArrayAdapter<String> adapterContexts = new ArrayAdapter<String>(this,R.layout.spinner_item, contexts);
        editorContext.setThreshold(1);
        editorContext.setAdapter(adapterContexts);

        ArrayAdapter<String> adapterProjects = new ArrayAdapter<String>(this,R.layout.spinner_item, projects);
        editorProject.setThreshold(1);
        editorProject.setAdapter(adapterProjects);

        btnDatePicker=(Button)findViewById(R.id.selectDateButton);
        btnDatePicker.setOnClickListener(this);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        if(uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(R.string.new_note);

            // STATE
            editorState.setSelection(0);

            // TEXT
            editorText.requestFocus();
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null,null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            oldState = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_STATE));
            oldContext = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CONTEXT));
            oldProject = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_PROJECT));
            oldDueDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_DUEDATE));

            // STATE
            int position = Arrays.asList(states).indexOf(oldState);
            if (position >= 0) {
                editorState.setSelection(position);
            } else {
                editorState.setSelection(0);
            }

            // CONTEXT
            editorContext.setText(oldContext);

            // PROJECT
            editorProject.setText(oldProject);

            // DUE DATE
            editorDueDate.setText(oldDueDate);

            // TEXT
            editorText.setText(oldText);
            editorText.requestFocus();
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

    //region View.OnClickListener
    @Override
    public void onClick(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        editorDueDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
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
        String newContext = editorContext.getText().toString().trim();
        String newProject = editorProject.getText().toString().trim();
        String newDueDate = editorDueDate.getText().toString().trim();
        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText, newState, newProject, newDueDate, newContext);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {
                    deleteNote();
                } else if (noteIsUnchanged(newText, newState, newProject, newDueDate, newContext)){
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText, newState, newProject, newDueDate, newContext);
                }
        }
        finish();
    }

    private boolean noteIsUnchanged(String noteText, String noteState, String noteProject, String noteDueDate, String noteContext) {
        if (!(oldText == null ? noteText == null : oldText.equals(noteText))) {
            return false;
        }
        if (!(oldState == null ? noteState == null : oldState.equals(noteState))) {
            return false;
        }
        if (!(oldContext == null ? noteContext == null : oldContext.equals(noteContext))) {
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

    private void updateNote(String noteText, String noteState, String noteProject, String noteDueDate, String noteContext) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_STATE, noteState);
        values.put(DBOpenHelper.NOTE_CONTEXT, noteContext);
        values.put(DBOpenHelper.NOTE_PROJECT, noteProject);
        values.put(DBOpenHelper.NOTE_DUEDATE, noteDueDate);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this,getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteState, String noteProject, String noteDueDate, String noteContext) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_STATE, noteState);
        values.put(DBOpenHelper.NOTE_CONTEXT, noteContext);
        values.put(DBOpenHelper.NOTE_PROJECT, noteProject);
        values.put(DBOpenHelper.NOTE_DUEDATE, noteDueDate);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }
}
