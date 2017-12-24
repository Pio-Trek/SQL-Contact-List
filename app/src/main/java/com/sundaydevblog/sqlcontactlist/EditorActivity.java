package com.sundaydevblog.sqlcontactlist;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURRENT_LOADER_ID = 0;

    @BindView(R.id.input_first_name)
    EditText inputFirstName;
    @BindView(R.id.input_last_name)
    EditText inputLastName;
    @BindView(R.id.input_address)
    EditText inputAddress;
    @BindView(R.id.input_phone)
    EditText inputPhone;
    @BindView(R.id.button_add_image)
    ImageButton buttonAddImage;

    private String firstName;
    private String lastName;
    private String address;
    private String phone;

    private Uri currentContactUri;

    private boolean contactHasChanged = false;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            contactHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        currentContactUri = intent.getData();

        if (currentContactUri == null) {
            setTitle(R.string.editor_title_add_contact);
        } else {
            setTitle(R.string.editor_title_edit_contact);
            getLoaderManager().initLoader(CURRENT_LOADER_ID, null, this);
        }

        inputFirstName.setOnTouchListener(touchListener);
        inputLastName.setOnTouchListener(touchListener);
        inputAddress.setOnTouchListener(touchListener);
        inputPhone.setOnTouchListener(touchListener);
    }

    @OnClick(R.id.button_add_image)
    public void onViewClicked() {
        Toast.makeText(this, "Add Photo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveContact();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!contactHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, close the current activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.editor_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteContact();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentContactUri == null) {
            MenuItem itemDelete = menu.findItem(R.id.action_delete);
            itemDelete.setVisible(false);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!contactHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void deleteContact() {
        if (currentContactUri != null) {
            int rowsDeleted = getContentResolver().delete(currentContactUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_contact_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_contact_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void saveContact() {
        firstName = inputFirstName.getText().toString().trim();
        lastName = inputLastName.getText().toString().trim();
        address = inputAddress.getText().toString().trim();
        phone = inputPhone.getText().toString().trim();

        checkInputs();

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
                && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(phone)) {

            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME, firstName + " " + lastName);
            values.put(ContactEntry.COLUMN_ADDRESS, address);
            values.put(ContactEntry.COLUMN_PHONE, phone);

            if (currentContactUri == null) {

                Uri newUri = getContentResolver().insert(ContactEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Toast.makeText(this, R.string.editor_insert_contact_error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.editor_insert_contact_successful, Toast.LENGTH_SHORT).show();
                }

            } else {
                int rowsUpdated = getContentResolver().update(currentContactUri, values, null, null);

                if (rowsUpdated == 0) {
                    Toast.makeText(this, R.string.editor_update_contact_error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.editor_update_contact_successful, Toast.LENGTH_SHORT).show();
                }
            }

            finish();
        }
    }

    private void checkInputs() {
        if (TextUtils.isEmpty(firstName)) {
            inputFirstName.setError("First name is required!");
        }
        if (TextUtils.isEmpty(lastName)) {
            inputLastName.setError("Last name is required!");
        }
        if (TextUtils.isEmpty(address)) {
            inputAddress.setError("Address is required!");
        }
        if (TextUtils.isEmpty(phone)) {
            inputPhone.setError("Phone number is required!");
        }
    }

    @OnTextChanged(R.id.input_first_name)
    public void checkInputFirstName() {
        if (!TextUtils.isEmpty(inputFirstName.getText().toString().trim())) {
            inputFirstName.setError(null);
        }
    }

    @OnTextChanged(R.id.input_last_name)
    public void checkInputLastName() {
        if (!TextUtils.isEmpty(inputLastName.getText().toString().trim())) {
            inputLastName.setError(null);
        }
    }

    @OnTextChanged(R.id.input_address)
    public void checkInputAddress() {
        if (!TextUtils.isEmpty(inputAddress.getText().toString().trim())) {
            inputAddress.setError(null);
        }
    }

    @OnTextChanged(R.id.input_phone)
    public void checkInputPhone() {
        if (!TextUtils.isEmpty(inputPhone.getText().toString().trim())) {
            inputPhone.setError(null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (currentContactUri == null) {
            return null;
        }

        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME,
                ContactEntry.COLUMN_ADDRESS,
                ContactEntry.COLUMN_PHONE};

        return new CursorLoader(this, currentContactUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            String[] fullName = cursor
                    .getString(cursor.getColumnIndex(ContactEntry.COLUMN_NAME))
                    .trim().split("\\s+");

            firstName = fullName[0];
            lastName = fullName[1];
            address = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_ADDRESS));
            phone = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_PHONE));

            inputFirstName.setText(firstName);
            inputLastName.setText(lastName);
            inputAddress.setText(address);
            inputPhone.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
