package com.sundaydevblog.sqlcontactlist;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactCursorAdapter extends CursorAdapter {

    @BindView(R.id.image_contact)
    CircleImageView imageContact;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_phone)
    TextView textPhone;
    @BindView(R.id.text_address)
    TextView textAddress;

    private Unbinder unbinder;

    /**
     * Construct a new {@link ContactCursorAdapter}
     *
     * @param context The context.
     * @param c       The cursor from which to get the data.
     */
    public ContactCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * The newView method is used to inflate a new view and return it,
     * you don't bind any data to the view at this point.
     *
     * @param context app context.
     * @param cursor  The cursor from which to get the data.
     * @param parent  The parent to which the new view is attached to.
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_contact_item, parent, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    /**
     * The bindView method is used to bind all data to a given view
     * such as setting the text on a TextView.
     *
     * @param view    Existing view, returned earlier by newView() method.
     * @param context app context.
     * @param cursor  The cursor from which to get the data.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
/*        TextView textName = view.findViewById(R.id.text_name);
        TextView textAddress = view.findViewById(R.id.text_address);
        TextView textPhone = view.findViewById(R.id.text_phone);*/

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_NAME));
        String address = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_ADDRESS));
        String phone = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_PHONE));
        //byte[] picture = cursor.getBlob(cursor.getColumnIndex(ContactEntry.COLUMN_PICTURE));

        // Update Views with the attributes for the current contact object
        textName.setText(name);
        textAddress.setText(address);
        textPhone.setText(phone);
        //TODO update picture {imageContact}

    }
}
