package com.sundaydevblog.sqlcontactlist;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactList extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    //@BindView(R.id.image_contact)
    //CircleImageView imageContact;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_phone)
    TextView textPhone;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    ContactCursorAdapter cursorAdapter;
    @BindView(R.id.list_contacts)
    ListView listContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        cursorAdapter = new ContactCursorAdapter(this, null);
        listContacts.setAdapter(cursorAdapter);

        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Toast.makeText(getApplicationContext(), "AbAba", Toast.LENGTH_SHORT).show();

                Uri currentPetUri = ContentUris.withAppendedId(DatabaseContract.ContactEntry.CONTENT_URI, id);
            }
        });



    }


    @OnClick(R.id.fab)
    public void setFab(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
