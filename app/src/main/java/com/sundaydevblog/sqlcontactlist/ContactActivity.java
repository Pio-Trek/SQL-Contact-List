package com.sundaydevblog.sqlcontactlist;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
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

/**
 * Displays list of contacts that were entered and stored in the app.
 */
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

        // Set empty view on the ListView, when list has 0 items.
        listContacts.setEmptyView(emptyViewGroup);

        cursorAdapter = new ContactCursorAdapter(this, null);
        listContacts.setAdapter(cursorAdapter);

        // Setup contact list click listener
        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ContactActivity.this, EditorActivity.class);

                Uri currentContactUri = ContentUris.withAppendedId(ContactEntry.CONTENT_URI, id);

                intent.setData(currentContactUri);
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    // Setup FAB to open EditorActivity
    @OnClick(R.id.fab)
    public void setFab(View view) {
        Intent intent = new Intent(ContactActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    // Inflate the menu. This adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to a click on the "Delete All Contacts" menu option
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                showDeleteAllConfirmationDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int rowsDeleted = getContentResolver().delete(ContactEntry.CONTENT_URI, null, null);
                Toast.makeText(ContactActivity.this, "Contacts Deleted: " + rowsDeleted,
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the contact.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME,
                ContactEntry.COLUMN_ADDRESS,
                ContactEntry.COLUMN_PHONE,
                ContactEntry.COLUMN_PICTURE};

        // Sort contacts column by name ascending.
        String sortOrder = ContactEntry.COLUMN_NAME + " COLLATE NOCASE ASC";

        return new CursorLoader(this, ContactEntry.CONTENT_URI, projection, null, null, sortOrder);
    }

    // Update CursorAdapter with a new cursor
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
