package io.github.martin1248.gtdlight;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import io.github.martin1248.gtdlight.model.GTDStates;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_PERMISSION_WRITE = 1001; // Any Value
    private static final int EDITOR_REQUEST_CODE = 1002;
    private boolean permissionGranted;

    private static final String noFilterKeyword = "No filter";
    private String[] currentContexts;
    private String[] currentProjects;
    private static final String allStatesKeyword = "All";
    private static final String[] statesExtended = {allStatesKeyword};
    private static final String[] statesAll = Stream.concat(Arrays.stream(GTDStates.gtdStates), Arrays.stream(statesExtended))
            .toArray(String[]::new); // Note: Requires Java 8
    private Spinner chooseState;
    private Spinner filterContext;
    private Spinner filterProjects;

    CursorAdapter cursorAdapter;

    //region AppCompat-, Fragment- and Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseState = findViewById(R.id.spinner_nav);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_nav, statesAll);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseState.setAdapter(adapter);
        chooseState.setSelection(0);
        chooseState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  restartLoader();
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {
                  // Nothing is done
              }
            });

        filterContext = findViewById(R.id.filterContext);
        filterProjects = findViewById(R.id.filterProject);
        reloadFilter();

        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        ListView list = findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        getSupportLoaderManager().initLoader(0,null,this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        */

        if(!permissionGranted) {
            checkPermissions();
            return;
        }
    }

    private void reloadFilter() {
        int selectedContext = filterContext.getSelectedItemPosition();
        int selectedProject = filterProjects.getSelectedItemPosition();

        String[] noFilterArray = {noFilterKeyword};
        String[] contexts = DBHelper.getContexts(getContentResolver());
        String[] projects = DBHelper.getProjects(getContentResolver());
        currentContexts = Stream.concat(Arrays.stream(noFilterArray), Arrays.stream(contexts))
                .toArray(String[]::new);
        currentProjects = Stream.concat(Arrays.stream(noFilterArray), Arrays.stream(projects))
                .toArray(String[]::new);

        ArrayAdapter<String> adapterFilterContext = new ArrayAdapter<String>(this,R.layout.spinner_item, currentContexts);
        adapterFilterContext.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterContext.setAdapter(adapterFilterContext);

        ArrayAdapter<String> adapterFilterProject = new ArrayAdapter<String>(this,R.layout.spinner_item, currentProjects);
        adapterFilterProject.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterProjects.setAdapter(adapterFilterProject);

        if(selectedContext >= 0) {
            filterContext.setSelection(selectedContext); // This is buggy in case of a new context
        }
        if (selectedProject >= 0) {
            filterProjects.setSelection(selectedProject);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    Toast.makeText(this, "External storage permission granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    //endregion

    private void insertNote(String newNote) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, newNote);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    private void insertSampleData() {
        insertNote("Simple note");
        insertNote("Multiline\nnote");
        insertNote("Very long note with a lot of text that exceeds screen size");

        restartLoader();
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(0,null,this);
        reloadFilter();
        Log.d("MainAc", " ### restart loader");
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Initiate request for permissions.
    private boolean checkPermissions() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
            return false;
        } else {
            return true;
        }
    }

    //region NavigationView
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

    //region LoaderManager.LoaderCallbacks
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String selection = "";
        String sortOrder = null;
        String selectedState = statesAll[chooseState.getSelectedItemPosition()];
        String selectedContext = currentContexts[filterContext.getSelectedItemPosition()];
        String selectedProject = currentProjects[filterProjects.getSelectedItemPosition()];

        ArrayList<String> whereMatches = new ArrayList<>();
        if (!selectedState.equals(allStatesKeyword)) {
            whereMatches.add(DBOpenHelper.NOTE_STATE + "='" + selectedState+ "'");
        }
        if (!selectedContext.equals(noFilterKeyword)) {
            whereMatches.add(DBOpenHelper.NOTE_CONTEXT + "='" + selectedContext+ "'");
        }
        if (!selectedProject.equals(noFilterKeyword)) {
            whereMatches.add(DBOpenHelper.NOTE_PROJECT + "='" + selectedProject+ "'");
        }

        boolean removeTrailingAnd = false;
        for (String whereMatch: whereMatches) {
            selection += whereMatch + " AND ";
            removeTrailingAnd = true;
        }
        if (removeTrailingAnd) {
            selection = selection.substring(0, selection.length() - 5);
        }
        Log.d("Main", "Selection is " + selection);

        if (selectedState.equals(GTDStates.stateCalender)) {
            sortOrder = DBOpenHelper.NOTE_DUEDATE + " ASC";
        }

        return new CursorLoader(this, NotesProvider.CONTENT_URI, null, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }
    //endregion
}
