package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicommons.people.contact.Contact;

import java.lang.ref.WeakReference;
import java.util.List;

public class AlUserSearchTask extends AsyncTask<Void, Void, List<Contact>> {

    private WeakReference<Context> context;
    private String searchString;
    private Exception exception;
    private UserService userService;
    private AlUserSearchHandler listener;

    public AlUserSearchTask(Context context, String searchString, AlUserSearchHandler listener) {
        this.context = new WeakReference<>(context);
        this.searchString = searchString;
        this.listener = listener;
        userService = UserService.getInstance(context);
    }

    @Override
    protected List<Contact> doInBackground(Void[] objects) {
        if (searchString == null) {
            exception = new ApplozicException("Empty search string");
            return null;
        }

        try {
            return userService.getUserListBySearch(searchString);
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Contact> contactList) {
        super.onPostExecute(contactList);

        if (listener != null) {
            if (contactList != null) {
                listener.onSuccess(contactList, context.get());
            } else {
                listener.onFailure(exception, context.get());
            }
        }
    }

    public interface AlUserSearchHandler {
        void onSuccess(List<Contact> contacts, Context context);

        void onFailure(Exception e, Context context);
    }
}
