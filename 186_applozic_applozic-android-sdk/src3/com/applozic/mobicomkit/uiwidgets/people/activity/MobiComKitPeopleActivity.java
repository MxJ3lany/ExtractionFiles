package com.applozic.mobicomkit.uiwidgets.people.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.AlUserSearchTask;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.ContactsChangeObserver;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.people.channel.ChannelFragment;
import com.applozic.mobicomkit.uiwidgets.people.contact.AppContactFragment;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.OnContactsInteractionListener;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MobiComKitPeopleActivity extends AppCompatActivity implements OnContactsInteractionListener,
        SearchView.OnQueryTextListener, TabLayout.OnTabSelectedListener {

    public static final String SHARED_TEXT = "SHARED_TEXT";
    public static final String FORWARD_MESSAGE = "forwardMessage";
    public static final String USER_ID_ARRAY = "userIdArray";
    private static final String CONTACT_ID = "contactId";
    private static final String GROUP_ID = "groupId";
    private static final String GROUP_NAME = "groupName";
    private static final String USER_ID = "userId";
    public static boolean isSearching = false;
    protected SearchView searchView;
    protected String searchTerm;
    ViewPager viewPager;
    TabLayout tabLayout;
    ActionBar actionBar;
    String[] userIdArray;
    AppContactFragment appContactFragment;
    ChannelFragment channelFragment;
    ViewPagerAdapter adapter;
    AlCustomizationSettings alCustomizationSettings;
    Intent intentExtra;
    String action, type;
    OnContactsInteractionListener onContactsInteractionListener;
    private SearchListFragment searchListFragment;
    private boolean isSearchResultView = false;
    private ContactsChangeObserver observer;

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_child_activity, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MobiComUserPreference.getInstance(this).isLoggedIn()) {
            finish();
        }
        setContentView(R.layout.people_activity);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }

        onContactsInteractionListener = this;
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Set up the action bar.
        actionBar = getSupportActionBar();
        if (!TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimary()) && !TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimaryDark())) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(alCustomizationSettings.getThemeColorPrimary())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(alCustomizationSettings.getThemeColorPrimaryDark()));
            }
        }
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);


        intentExtra = getIntent();
        action = intentExtra.getAction();
        type = intentExtra.getType();

        if (getIntent().getExtras() != null) {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                actionBar.setTitle(getString(R.string.send_message_to));
            } else {
                actionBar.setTitle(getString(R.string.search_title));
                userIdArray = getIntent().getStringArrayExtra(USER_ID_ARRAY);
            }
        } else {
            actionBar.setTitle(getString(R.string.search_title));
        }
        appContactFragment = new AppContactFragment(userIdArray);
        appContactFragment.setAlCustomizationSettings(alCustomizationSettings);
        channelFragment = new ChannelFragment();
        setSearchListFragment(appContactFragment);
        if (alCustomizationSettings.isStartNewGroup() || ApplozicSetting.getInstance(this).isStartNewGroupButtonVisible()) {
            viewPager = (ViewPager) findViewById(R.id.viewPager);
            viewPager.setVisibility(View.VISIBLE);
            setupViewPager(viewPager);
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(this);
        } else {
            addFragment(this, appContactFragment, "AppContactFragment");
        }
      /*  mContactsListFragment = (AppContactFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);*/

        // This flag notes that the Activity is doing a search, and so the result will be
        // search results rather than all contacts. This prevents the Activity and Fragment
        // from trying to a search on search results.
        isSearchResultView = true;

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        // Set special title for search results

      /*  if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mContactsListFragment.onQueryTextChange(searchQuery);
        }*/
        if (Applozic.getInstance(this).isDeviceContactSync()) {
            observer = new ContactsChangeObserver(null, this);
            getApplicationContext().getContentResolver().registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI, true, observer);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        if (Utils.hasICS()) {
            searchItem.collapseActionView();
        }
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(true);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact has been selected.
     *
     * @param contactUri The contact Uri to the selected contact.
     */
    @Override
    public void onContactSelected(Uri contactUri) {
        Long contactId = ContactUtils.getContactId(getContentResolver(), contactUri);
        Map<String, String> phoneNumbers = ContactUtils.getPhoneNumbers(getApplicationContext(), contactId);

        if (phoneNumbers.isEmpty()) {
            Toast toast = Toast.makeText(this.getApplicationContext(), R.string.phone_number_not_present, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(CONTACT_ID, contactId);
        intent.setData(contactUri);
        finishActivity(intent);
    }

    public void startNewConversation(String contactNumber) {
        Intent intent = new Intent();
        intent.putExtra(USER_ID, contactNumber);
        finishActivity(intent);
    }


    @Override
    public void onGroupSelected(Channel channel) {
        Intent intent = null;
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (!ChannelService.getInstance(MobiComKitPeopleActivity.this).processIsUserPresentInChannel(channel.getKey())) {
                Toast.makeText(this, getString(R.string.unable_share_message), Toast.LENGTH_SHORT).show();
                return;
            }
            if ("text/plain".equals(type)) {
                intent = new Intent(this, ConversationActivity.class);
                intent.putExtra(GROUP_ID, channel.getKey());
                intent.putExtra(GROUP_NAME, channel.getName());
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, intentExtra.getStringExtra(Intent.EXTRA_TEXT));
                startActivity(intent);
                finish();
            } else if (type.startsWith("image/") || type.startsWith("audio/") || type.startsWith("video/")) {
                Uri fileUri = (Uri) intentExtra.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    long maxSize = alCustomizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
                    if (FileUtils.isMaxUploadSizeReached(this, fileUri, maxSize)) {
                        Toast.makeText(this, getString(R.string.info_attachment_max_allowed_file_size), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (FileUtils.isContentScheme(fileUri)) {
                        String mimeType = FileUtils.getMimeTypeByContentUriOrOther(this, fileUri);
                        if (TextUtils.isEmpty(mimeType)) {
                            this.finish();
                        } else {
                            new ShareAsyncTask(this, fileUri, null, channel, mimeType).execute();
                        }
                    } else {
                        Intent intentImage = new Intent(this, MobiComAttachmentSelectorActivity.class);
                        intentImage.putExtra(MobiComAttachmentSelectorActivity.GROUP_ID, channel.getKey());
                        intentImage.putExtra(MobiComAttachmentSelectorActivity.GROUP_NAME, channel.getName());
                        if (fileUri != null) {
                            intentImage.putExtra(MobiComAttachmentSelectorActivity.URI_LIST, fileUri);
                        }
                        startActivity(intentImage);
                    }

                }

            }
        } else {
            intent = new Intent();
            intent.putExtra(GROUP_ID, channel.getKey());
            intent.putExtra(GROUP_NAME, channel.getName());
            finishActivity(intent);
        }
    }

    @Override
    public void onCustomContactSelected(Contact contact) {
        Intent intent = null;
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (contact.isBlocked()) {
                Toast.makeText(this, getString(R.string.user_is_blocked), Toast.LENGTH_SHORT).show();
                return;
            }
            if ("text/plain".equals(type)) {
                intent = new Intent(this, ConversationActivity.class);
                intent.putExtra(USER_ID, contact.getUserId());
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, intentExtra.getStringExtra(Intent.EXTRA_TEXT));
                startActivity(intent);
                finish();
            } else if (type.startsWith("image/") || type.startsWith("audio/") || type.startsWith("video/")) {
                Uri fileUri = (Uri) intentExtra.getParcelableExtra(Intent.EXTRA_STREAM);
                long maxSize = alCustomizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
                if (FileUtils.isMaxUploadSizeReached(this, fileUri, maxSize)) {
                    Toast.makeText(this, getString(R.string.info_attachment_max_allowed_file_size), Toast.LENGTH_LONG).show();
                    return;
                }
                if (FileUtils.isContentScheme(fileUri)) {
                    String mimeType = FileUtils.getMimeTypeByContentUriOrOther(this, fileUri);
                    if (TextUtils.isEmpty(mimeType)) {
                        this.finish();
                    } else {
                        new ShareAsyncTask(this, fileUri, contact, null, mimeType).execute();
                    }

                } else {
                    Intent intentImage = new Intent(this, MobiComAttachmentSelectorActivity.class);
                    intentImage.putExtra(MobiComAttachmentSelectorActivity.USER_ID, contact.getUserId());
                    intentImage.putExtra(MobiComAttachmentSelectorActivity.DISPLAY_NAME, contact.getDisplayName());
                    if (fileUri != null) {
                        intentImage.putExtra(MobiComAttachmentSelectorActivity.URI_LIST, fileUri);
                    }
                    startActivity(intentImage);
                }

            }
        } else {

            if (ApplozicClient.getInstance(this).isStartGroupOfTwo()) {
                new ChannelCreateAsyncTask(MobiComUserPreference.getInstance(this).getParentGroupKey(), contact, MobiComKitPeopleActivity.this).execute((Void) null);
            } else {
                intent = new Intent();
                intent.putExtra(USER_ID, contact.getUserId());
                finishActivity(intent);
            }
        }
    }


    public void finishActivity(Intent intent) {
        String forwardMessage = getIntent().getStringExtra(FORWARD_MESSAGE);
        if (!TextUtils.isEmpty(forwardMessage)) {
            intent.putExtra(FORWARD_MESSAGE, forwardMessage);
        }

        String sharedText = getIntent().getStringExtra(SHARED_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            intent.putExtra(SHARED_TEXT, sharedText);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onSelectionCleared() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
            // For platforms earlier than Android 3.0, triggers the search activity
        } else if (i == R.id.menu_search) {// if (!Utils.hasHoneycomb()) {
            onSearchRequested();
            //}

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (alCustomizationSettings.isCreateAnyContact()) {
            this.searchTerm = query;
            startNewConversation(query);
            isSearching = false;
        }

        if (alCustomizationSettings.isContactSearchFromServer()) {
            processSearchCall(query);
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        this.searchTerm = query;
        if (getSearchListFragment() != null) {
            getSearchListFragment().onQueryTextChange(query);
            isSearching = true;

            if (query.isEmpty()) {
                isSearching = false;
            }
        }
        return true;
    }

    public void processSearchCall(String query) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.applozic_contacts_loading_info));
        dialog.show();

        new AlUserSearchTask(this, query, new AlUserSearchTask.AlUserSearchHandler() {
            @Override
            public void onSuccess(List<Contact> contacts, Context context) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (!contacts.isEmpty() && appContactFragment != null) {
                    appContactFragment.restartLoader();
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(context, R.string.applozic_server_error, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    public SearchListFragment getSearchListFragment() {
        return searchListFragment;
    }

    public void setSearchListFragment(SearchListFragment searchListFragment) {
        this.searchListFragment = searchListFragment;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(appContactFragment, getString(R.string.Contact));
        adapter.addFrag(channelFragment, getString(R.string.Group));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition(), true);
        switch (tab.getPosition()) {
            case 0:
                setSearchListFragment((AppContactFragment) adapter.getItem(0));
                if (getSearchListFragment() != null) {
                    getSearchListFragment().onQueryTextChange(null);
                }
                break;
            case 1:
                setSearchListFragment((ChannelFragment) adapter.getItem(1));
                if (getSearchListFragment() != null) {
                    getSearchListFragment().onQueryTextChange(null);
                }
                break;
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onContactsInteractionListener != null) {
            onContactsInteractionListener = null;
        }
        if (observer != null) {
            getApplicationContext().getContentResolver().unregisterContentObserver(observer);
        }
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> titleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

    }

    private class ShareAsyncTask extends AsyncTask<Void, Void, File> {

        WeakReference<Context> contextWeakReference;
        Uri uri;
        FileClientService fileClientService;
        Contact contact;
        Channel channel;
        String mimeType;

        public ShareAsyncTask(Context context, Uri uri, Contact contact, Channel channel, String mimType) {
            this.contextWeakReference = new WeakReference<Context>(context);
            this.uri = uri;
            this.contact = contact;
            this.channel = channel;
            this.mimeType = mimType;
            this.fileClientService = new FileClientService(context);
        }

        @Override
        protected File doInBackground(Void... voids) {

            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                if (context != null && !TextUtils.isEmpty(mimeType)) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String array[] = mimeType.split("/");
                    String fileFormat = null;
                    if (array.length > 1) {
                        fileFormat = array[1];
                    }

                    if (TextUtils.isEmpty(fileFormat)) {
                        return null;
                    }

                    String fileNameToWrite = timeStamp + "." + fileFormat;
                    File mediaFile = FileClientService.getFilePath(fileNameToWrite, context, mimeType);
                    fileClientService.writeFile(uri, mediaFile);
                    return mediaFile;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (contextWeakReference != null) {
                Context context = contextWeakReference.get();
                if (file != null && file.exists() && context != null) {
                    Uri fileUri = Uri.parse(file.getAbsolutePath());
                    Intent sendAttachmentIntent = new Intent(context, MobiComAttachmentSelectorActivity.class);
                    if (channel != null) {
                        sendAttachmentIntent.putExtra(MobiComAttachmentSelectorActivity.GROUP_ID, channel.getKey());
                        sendAttachmentIntent.putExtra(MobiComAttachmentSelectorActivity.GROUP_NAME, channel.getName());
                    } else if (contact != null) {
                        sendAttachmentIntent.putExtra(MobiComAttachmentSelectorActivity.USER_ID, contact.getUserId());
                        sendAttachmentIntent.putExtra(MobiComAttachmentSelectorActivity.DISPLAY_NAME, contact.getDisplayName());
                    }
                    if (fileUri != null) {
                        sendAttachmentIntent.putExtra(MobiComAttachmentSelectorActivity.URI_LIST, fileUri);
                    }
                    context.startActivity(sendAttachmentIntent);
                }
            }
        }
    }

    public class ChannelCreateAsyncTask extends AsyncTask<Void, Integer, Channel> {
        private ChannelService channelService;
        private ProgressDialog progressDialog;
        private Context context;
        Channel channel;
        String withUserId;
        AppContactService appContactService;
        String loggedInUserId;
        Contact withUserContact;
        Integer localParentGroupKey;

        public ChannelCreateAsyncTask(Integer parentGroupKey, Contact withUserContact, Context context) {
            this.context = context;
            this.channelService = ChannelService.getInstance(context);
            this.withUserContact = withUserContact;
            this.localParentGroupKey = parentGroupKey;
            this.appContactService = new AppContactService(context);
            this.loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.please_wait_creating_group_of_two), true);
        }

        @Override
        protected Channel doInBackground(Void... params) {

            if (localParentGroupKey != null && localParentGroupKey != 0 && withUserContact != null) {
                List<String> userIdList = new ArrayList<>();
                userIdList.add(withUserContact.getContactIds());
                int result = loggedInUserId.compareTo(withUserContact.getContactIds());
                StringBuffer stringBuffer = new StringBuffer();
                if (result == 0) {
                    stringBuffer.append(localParentGroupKey).append(":").append(loggedInUserId).append(":").append(withUserContact.getContactIds());
                } else if (result < 0) {
                    stringBuffer.append(localParentGroupKey).append(":").append(loggedInUserId).append(":").append(withUserContact.getContactIds());
                } else {
                    stringBuffer.append(localParentGroupKey).append(":").append(withUserContact.getContactIds()).append(":").append(loggedInUserId);
                }
                ChannelInfo channelInfo = new ChannelInfo(stringBuffer.toString(), userIdList);
                channelInfo.setClientGroupId(stringBuffer.toString());
                channelInfo.setType(Channel.GroupType.GROUPOFTWO.getValue());
                channelInfo.setParentKey(localParentGroupKey);
                channel = channelService.createGroupOfTwo(channelInfo);
            }
            return channel;
        }

        @Override
        protected void onPostExecute(Channel channel) {
            super.onPostExecute(channel);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (channel != null) {
                Intent intent = new Intent(context, ConversationActivity.class);
                intent.putExtra(ConversationUIService.GROUP_ID, channel.getKey());
                intent.putExtra(ConversationUIService.GROUP_NAME, channel.getName());
                startActivity(intent);
            }
        }

    }

}



