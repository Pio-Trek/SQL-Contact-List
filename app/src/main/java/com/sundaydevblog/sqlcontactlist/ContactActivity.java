package com.sundaydevblog.sqlcontactlist;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.list_contacts)
    ListView listContacts;
    @BindView(R.id.empty_view_group)
    Group emptyViewGroup;
    private ContactCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);

        listContacts.setEmptyView(emptyViewGroup);

        cursorAdapter = new ContactCursorAdapter(this, null);
        listContacts.setAdapter(cursorAdapter);


        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ContactActivity.this, EditorActivity.class);

                Uri currentContactUri = ContentUris.withAppendedId(ContactEntry.CONTENT_URI, id);

                intent.setData(currentContactUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    @OnClick(R.id.fab)
    public void setFab(View view) {
        Intent intent = new Intent(ContactActivity.this, EditorActivity.class);
        startActivity(intent);

       /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();*/

/*        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME, "Test Name");
        values.put(ContactEntry.COLUMN_ADDRESS, "Some Address 123");
        values.put(ContactEntry.COLUMN_PHONE, "0123456789");

        Uri newUri = getContentResolver().insert(ContactEntry.CONTENT_URI, values);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                deleteAllContacts();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllContacts() {
        Snackbar.make(listContacts, "Delete all contacts?", Snackbar.LENGTH_LONG)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int rowsDeleted = getContentResolver().delete(ContactEntry.CONTENT_URI, null, null);
                        Toast.makeText(ContactActivity.this, "Rows Deleted: " + rowsDeleted,
                                Toast.LENGTH_SHORT).show();
                    }
                }).show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME,
                ContactEntry.COLUMN_ADDRESS,
                ContactEntry.COLUMN_PHONE};

        return new CursorLoader(this, ContactEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                cursorAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
