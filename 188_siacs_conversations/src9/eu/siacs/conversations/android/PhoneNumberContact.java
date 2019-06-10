package eu.siacs.conversations.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.utils.PhoneNumberUtilWrapper;
import io.michaelrocks.libphonenumber.android.NumberParseException;

public class PhoneNumberContact extends AbstractPhoneContact {

    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private PhoneNumberContact(Context context, Cursor cursor) throws IllegalArgumentException {
        super(cursor);
        try {
            this.phoneNumber = PhoneNumberUtilWrapper.normalize(context,cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        } catch (NumberParseException | NullPointerException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, PhoneNumberContact> load(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return Collections.emptyMap();
        }
        final String[] PROJECTION = new String[]{ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.PHOTO_URI,
                ContactsContract.Data.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        final Cursor cursor;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
        final HashMap<String, PhoneNumberContact> contacts = new HashMap<>();
        while (cursor != null && cursor.moveToNext()) {
            try {
                final PhoneNumberContact contact = new PhoneNumberContact(context, cursor);
                final PhoneNumberContact preexisting = contacts.get(contact.getPhoneNumber());
                if (preexisting == null || preexisting.rating() < contact.rating()) {
                    contacts.put(contact.getPhoneNumber(), contact);
                }
            } catch (IllegalArgumentException e) {
                Log.d(Config.LOGTAG, "unable to create phone contact");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return contacts;
    }

    public static PhoneNumberContact findByUri(Collection<PhoneNumberContact> haystack, Uri needle) {
        for(PhoneNumberContact contact : haystack) {
            if (needle.equals(contact.getLookupUri())) {
                return contact;
            }
        }
        return null;
    }
}
