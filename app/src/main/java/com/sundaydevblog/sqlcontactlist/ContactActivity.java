package com.sundaydevblog.sqlcontactlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;
import com.sundaydevblog.sqlcontactlist.data.DatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    ContactCursorAdapter cursorAdapter;
    @BindView(R.id.text_test)
    TextView textTest;


    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);


/*        cursorAdapter = new ContactCursorAdapter(this, null);
        listContacts.setAdapter(cursorAdapter);


        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Toast.makeText(getApplicationContext(), "AbAba", Toast.LENGTH_SHORT).show();

                Uri currentPetUri = ContentUris.withAppendedId(DatabaseContract.ContactEntry.CONTENT_URI, id);
            }
        });*/


    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDataBaseInfo();
    }

    private void displayDataBaseInfo() {

        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME,
                ContactEntry.COLUMN_ADDRESS,
                ContactEntry.COLUMN_PHONE};

        Cursor cursor = getContentResolver().query(
                ContactEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
