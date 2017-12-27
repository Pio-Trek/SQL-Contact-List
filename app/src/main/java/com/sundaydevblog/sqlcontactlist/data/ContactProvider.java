package com.sundaydevblog.sqlcontactlist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;


public class ContactProvider extends ContentProvider {

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     */
    static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * URI matcher code for the content URI for the contacts table
     */
    private static final int TABLE_CODE = 100;

    /**
     * URI matcher code for the content URI for a single contact in the contacts table
     */
    private static final int CONTACT_CODE = 101;

    // Static initializer. This is run the first time anything is called from this class.
    static {

        /* The calls to addURI() go here, for all of the content URI patterns that the provider
        should recognize. All paths added to the UriMatcher have a corresponding code to return
        when a match is found.
        */
        uriMatcher.addURI(DatabaseContract.PROVIDER_NAME, DatabaseContract.PATH_CONTACTS, TABLE_CODE);
        uriMatcher.addURI(DatabaseContract.PROVIDER_NAME, DatabaseContract.PATH_CONTACTS + "/#", CONTACT_CODE);
    }

    /**
     * Database helper object
     */
    private DatabaseHelper dbHelper;

    /**
     * Initialize the provider and the database helper object with a getContext() parameter.
     */
    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return false;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection,
     * selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        switch (uriMatcher.match(uri)) {
            // SELECT * FROM 'table_name'
            case TABLE_CODE:
                cursor = database.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            // SELECT 'projection' {id, name, etc} 'FROM table_name'
            // WHERE 'selection' {id} = ? 'selectionArgs' {2, 5, etc}
            // URI: content://com.sundaydevblog.sqlcontactlist/contacts/2
            case CONTACT_CODE:
                selection = ContactEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TABLE_CODE:
                return ContactEntry.CONTENT_LIST_TYPE;
            case CONTACT_CODE:
                return ContactEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + uriMatcher.match(uri));
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String name = values.getAsString(ContactEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Contact requires a name");
        }

        String address = values.getAsString(ContactEntry.COLUMN_ADDRESS);
        if (address == null) {
            throw new IllegalArgumentException("Contact requires valid address");
        }

        String phone = values.getAsString(ContactEntry.COLUMN_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Contact requires valid phone number");
        }

        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new contact with the given values
        long id = database.insert(ContactEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Toast.makeText(getContext(), "Failed to insert row for " + uri, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Notify all listeners that the data has changed for the contact content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Delete the data at the given selection (whole table or single row).
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case TABLE_CODE:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_CODE:
                // Delete a single row given by the ID in the URI
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String
            selection, @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case TABLE_CODE:
                return updateContact(uri, contentValues, selection, selectionArgs);
            case CONTACT_CODE:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateContact(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateContact(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Get writable database to update the data
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ContactEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
