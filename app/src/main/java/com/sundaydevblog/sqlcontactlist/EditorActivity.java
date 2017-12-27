package com.sundaydevblog.sqlcontactlist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Allows user to create a new contacts or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURRENT_LOADER_ID = 0;
    private static int RESULT_LOAD_IMAGE = 1;
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
    @BindView(R.id.image_contact)
    CircleImageView imageContact;

    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private Uri currentContactUri;
    private Bitmap bitmap;

    private boolean contactHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view. Uses to display an discard changes alert dialog.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View view, MotionEvent motionEvent) {
            contactHasChanged = true;
            return false;
        }
    };

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        // Get intent to figure out if we're creating a new contact or editing the existing one.
        Intent intent = getIntent();
        currentContactUri = intent.getData();

        if (currentContactUri == null) {
            setTitle(R.string.editor_title_add_contact);
            buttonAddImage.setImageResource(R.drawable.ic_button_add_photo);

            // Load a placeholder contact picture.
            Glide.with(getBaseContext())
                    .load(R.drawable.ic_contact)
                    .into(imageContact);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();

        } else {
            setTitle(R.string.editor_title_edit_contact);
            getLoaderManager().initLoader(CURRENT_LOADER_ID, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        inputFirstName.setOnTouchListener(touchListener);
        inputLastName.setOnTouchListener(touchListener);
        inputAddress.setOnTouchListener(touchListener);
        inputPhone.setOnTouchListener(touchListener);
        buttonAddImage.setOnTouchListener(touchListener);
    }

    // Opens gallery (photo) app to browse images located on phone memory.
    @OnClick(R.id.button_add_image)
    public void onViewClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    // Gets the picture selected by user in the gallery and load it into the app.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();

                Glide.with(getBaseContext())
                        .load(selectedImageUri)
                        .into(imageContact);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options item to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond on Save contact
            case R.id.action_save:
                saveContact();
                return true;
            // Respond on Delete contact
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond on back home arrows
            case android.R.id.home:
                // If the contact hasn't changed, then continue and back to CatalogActivity.
                if (!contactHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise display an alert dialog.
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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.editor_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the contact.
                deleteContact();
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

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new contact then hide the "Delete" menu item.
        if (currentContactUri == null) {
            MenuItem itemDelete = menu.findItem(R.id.action_delete);
            itemDelete.setVisible(false);
        }

        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
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

    /**
     * Perform the deletion of the contact in the database.
     */
    private void deleteContact() {
        // Only perform the delete if this is an existing contact.
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

    /**
     * Convert bitmap image to byte array.
     */
    public byte[] getBitmapAsByteArray(Bitmap bitmap) {
        bitmap = scaleBitmapAndKeepRation(bitmap, 600, 600);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Scale a Bitmap to a runtime dependant width and height, where the aspect ratio is
     * maintained and the Bitmap fills the entire width and centers the image vertically.
     */
    public Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp, int reqHeightInPixels, int reqWidthInPixels) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, targetBmp.getWidth(), targetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), m, true);
    }

    /**
     * Get user input from editor and save new contact into database.
     */
    private void saveContact() {
        firstName = inputFirstName.getText().toString().trim();
        lastName = inputLastName.getText().toString().trim();
        address = inputAddress.getText().toString().trim();
        phone = inputPhone.getText().toString().trim();

        // Check if picture exist, if not then save data in column as null.
        byte[] data = null;
        if (imageContact.getDrawable() != null && bitmap != null) {
            // Otherwise convert bitmap to byte array.
            data = getBitmapAsByteArray(bitmap);
        }

        // Validate user input.
        checkInputs();
        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
                && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(phone)) {

            // Create a ContentValues object from the user input values.
            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME, firstName + " " + lastName);
            values.put(ContactEntry.COLUMN_ADDRESS, address);
            values.put(ContactEntry.COLUMN_PHONE, phone);
            if (bitmap != null) {
                values.put(ContactEntry.COLUMN_PICTURE, data);
            }

            if (currentContactUri == null) {
                // Insert a new contact into the provider, returning the content URI for the new contact
                Uri newUri = getContentResolver().insert(ContactEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    Toast.makeText(this, R.string.editor_insert_contact_error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.editor_insert_contact_successful, Toast.LENGTH_SHORT).show();
                }

                // Otherwise update existing contact.
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

    /**
     * Checks if inputs fields are not empty if they are then displaying error message.
     */
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

    /**
     * Checks if inputs have changed and if they are then hiding error message.
     */
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
        // Check if URI is empty.
        if (currentContactUri == null) {
            return null;
        }

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME,
                ContactEntry.COLUMN_ADDRESS,
                ContactEntry.COLUMN_PHONE,
                ContactEntry.COLUMN_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, currentContactUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            // Split contact full name into first and last name separate.
            String[] fullName = cursor
                    .getString(cursor.getColumnIndex(ContactEntry.COLUMN_NAME))
                    .trim().split("\\s+");

            firstName = fullName[0];
            lastName = fullName[1];
            address = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_ADDRESS));
            phone = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_PHONE));
            byte[] picture = cursor.getBlob(cursor.getColumnIndex(ContactEntry.COLUMN_PICTURE));

            inputFirstName.setText(firstName);
            inputLastName.setText(lastName);
            inputAddress.setText(address);
            inputPhone.setText(phone);

            if (picture == null) {
                buttonAddImage.setImageResource(R.drawable.ic_button_add_photo);
            } else {
                buttonAddImage.setImageResource(R.drawable.ic_button_change_photo);
            }

            // Load picture from the database, if not exist (null) then load placeholder.
            Glide.with(getBaseContext())
                    .load(picture)
                    .asBitmap()
                    .placeholder(R.drawable.ic_contact)
                    .into(imageContact);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
