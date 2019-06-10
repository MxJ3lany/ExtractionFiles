import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;

import java.util.LinkedList;
import java.util.List;

import static com.applozic.mobicomkit.api.account.user.UserLoginTask.TaskListener;

/**
 * Created by applozic on 12/5/15.
 */
public class ApplozicBridge {

    /**
     * Starts the chat activity if user is not loggedIn then it will login to applozic server and launch the chat-list
     * else if user is loggedIn then directly opens chat-list
     *
     * @param context
     * @param user    :User object
     */

    private static void startChatActivity(Context context, User user) {
        if (!MobiComUserPreference.getInstance(context).isLoggedIn()) {

            TaskListener listener = new TaskListener() {

                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    String pushNotificationId = "";//Todo: get pushnotification id.
                    gcmRegister(context, pushNotificationId);
                    launchChat(context);
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                }
            };

            user = user != null ? user : getLoggedInUserInformation();

            new UserLoginTask(user, listener, context).execute((Void) null);

        } else {
            launchChat(context);
        }
    }

    /**
     * Method  to launch chat activity if user is already registered with applozic server
     *
     * @param context
     */

    public static void launchChat(Context context) {
        Intent intent = new Intent(context, ConversationActivity.class);
        context.startActivity(intent);
    }

    /**
     * Method to Registers the User and launch the chat list
     *
     * @param context
     * @param user    :user object
     */

    public static void registerUserAndLaunchChat(Context context, User user) {
        startChatActivity(context, user);
    }


    /**
     * Method to launch Individual chat pass userId and display name to whom u want to launch Individual Chat directly
     *
     * @param context
     * @param userId
     * @param displayName
     */

    public static void launchIndividualChat(Context context, String userId, String displayName) {
        Intent intent = new Intent(context, ConversationActivity.class);
        if (!TextUtils.isEmpty(userId)) {
            intent.putExtra(ConversationUIService.USER_ID, userId);
        }
        if (!TextUtils.isEmpty(displayName)) {
            intent.putExtra(ConversationUIService.DISPLAY_NAME, displayName);
        }
        context.startActivity(intent);
    }

    public static void gcmRegister(Context context, String pushnotificationId) {

        if (!MobiComUserPreference.getInstance(context).isRegistered()) {
            Log.i("ApplozicBridge", "user is not Registered");
            MobiComUserPreference pref = MobiComUserPreference.getInstance(context);

            if (!TextUtils.isEmpty(pushnotificationId)) {
                pref.setDeviceRegistrationId(pushnotificationId);
            }
            return;
        }

        PushNotificationTask pushNotificationTask = null;
        PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse) {
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
            }
        };

        pushNotificationTask = new PushNotificationTask(pushnotificationId, listener, context);
        pushNotificationTask.execute((Void) null);

    }

    /**
     * This method can be used to get app logged-in user's information.
     * if user information is stored in DB or preference, Code to get user's information should go here
     *
     * @return
     */

    public static User getLoggedInUserInformation() {
        User user = new User();
        user.setUserId("Applozic-test");//useIid of user
        user.setDisplayName("ApplozicChat");//displayName of user
        //user.setEmail(); optional
        return user;
    }

    /**
     * Method to get the  Email Id  from google primary account
     *
     * @param context
     * @return
     */

    public static String getUserEmailId(Context context) {
        String userEmailId = "";
        try {
            AccountManager manager = AccountManager.get(context);
            Account[] accounts = manager.getAccountsByType("com.google");
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain
                // account.type values.
                possibleEmails.add(account.name);
            }

            if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
                userEmailId = possibleEmails.get(0);
            }
        } catch (Exception e) {

        }
        return userEmailId;
    }

    /**
     * Method to get the User Name  from google primary account
     *
     * @param context
     * @return
     */

    public static String getUsername(Context context) {
        try {
            AccountManager manager = AccountManager.get(context);
            Account[] accounts = manager.getAccountsByType("com.google");
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                possibleEmails.add(account.name);
            }

            if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
                String email = possibleEmails.get(0);
                String[] parts = email.split("@");

                if (parts.length > 1)
                    return parts[0];
            }

        } catch (Exception e) {

        }
        return null;
    }

}
