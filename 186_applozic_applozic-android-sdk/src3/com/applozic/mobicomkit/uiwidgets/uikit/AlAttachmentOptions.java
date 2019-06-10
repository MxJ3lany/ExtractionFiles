package com.applozic.mobicomkit.uiwidgets.uikit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.contact.ContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobicomLocationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.AudioMessageFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicommons.commons.core.utils.LocationInfo;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ashish on 15/05/18.
 */

public class AlAttachmentOptions {

    public static final String GOOGLE_API_KEY_META_DATA = "com.google.android.geo.API_KEY";
    private static final String API_KYE_STRING = "YOUR_GEO_API_KEY";
    public static final int LOCATION_SERVICE_ENABLE = 1001;
    public static final int REQUEST_CODE_CONTACT_GROUP_SELECTION = 1011;
    public static final int RESULT_OK = -1;
    public static final int REQUEST_CODE_SEND_LOCATION = 10;
    public static final int REQUEST_CODE_TAKE_PHOTO = 11;
    public static final int REQUEST_CODE_ATTACH_PHOTO = 12;
    public static final int REQUEST_MULTI_ATTCAHMENT = 16;
    public static final int REQUEST_CODE_ATTACHE_AUDIO = 13;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY = 14;
    public static final int REQUEST_CODE_CONTACT_SHARE = 15;
    public static final String MULTISELECT_SELECTED_FILES = "multiselect.selectedFiles";
    public static final String MULTISELECT_MESSAGE = "multiselect.message";
    public static final String URI_LIST = "URI_LIST";
    private static Bundle bundle;

    public static void processCameraAction(final Activity activity, LinearLayout layout) {
        if (PermissionsUtils.isCameraPermissionGranted(activity)) {
            captureImage(activity);
        } else {
            if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(activity)) {
                new ApplozicPermissions(activity).requestCameraPermission();
            } else {
                captureImage(activity);
            }
        }
    }

    public static void processLocationAction(final Activity activity, LinearLayout layout) {
        String geoApiKey = Utils.getMetaDataValue(activity.getApplicationContext(), GOOGLE_API_KEY_META_DATA);

        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfPermissionForLocation(activity)) {
            new ApplozicPermissions(activity, layout).requestLocationPermissions();
        } else {
            if (!TextUtils.isEmpty(geoApiKey) && !API_KYE_STRING.equals(geoApiKey)) {
                Intent toMapActivity = new Intent(activity, MobicomLocationActivity.class);
                activity.startActivityForResult(toMapActivity, REQUEST_CODE_SEND_LOCATION);
            } else {
                if (!((LocationManager) activity.getSystemService(Context.LOCATION_SERVICE))
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.location_services_disabled_title)
                            .setMessage(R.string.location_services_disabled_message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.location_service_settings, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    activity.startActivityForResult(intent, LOCATION_SERVICE_ENABLE);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(activity, R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
    }

    public static void processVideoAction(Activity activity, LinearLayout layout) {
        try {
            if (PermissionsUtils.isCameraPermissionGranted(activity)) {
                captureVideo(activity);
            } else {
                if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(activity)) {
                    new ApplozicPermissions(activity).requestCameraPermission();
                } else {
                    captureVideo(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processAudioAction(AppCompatActivity activity, LinearLayout layout) {
        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfPermissionForAudioRecording(activity)) {
            new ApplozicPermissions(activity, layout).requestAudio();
        } else if (PermissionsUtils.isAudioRecordingPermissionGranted(activity)) {
            FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
            DialogFragment fragment = AudioMessageFragment.newInstance();
            FragmentTransaction fragmentTransaction = supportFragmentManager
                    .beginTransaction().add(fragment, "AudioMessageFragment");

            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            //Permissions not granted error
        }
    }

    public static void processContactAction(Activity activity, LinearLayout layout) {
        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForContactPermission(activity)) {
            new ApplozicPermissions(activity).requestContactPermission();
        } else {
            Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            activity.startActivityForResult(contactIntent, REQUEST_CODE_CONTACT_SHARE);
        }
    }

    public static void processFileAction(Activity activity, LinearLayout layout) {
        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForStoragePermission(activity)) {
            new ApplozicPermissions(activity).requestStoragePermissions();
        } else {
            Intent intentPick = new Intent(activity, MobiComAttachmentSelectorActivity.class);
            activity.startActivityForResult(intentPick, REQUEST_MULTI_ATTCAHMENT);
        }
    }

    public static void captureVideo(Activity activity) {

        try {
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "VID_" + timeStamp + "_" + ".mp4";

            File mediaFile = FileClientService.getFilePath(imageFileName, activity.getApplicationContext(), "video/mp4");

            Uri videoFileUri = FileProvider.getUriForFile(activity, Utils.getMetaDataValue(activity, MobiComKitConstants.PACKAGE_NAME) + ".provider", mediaFile);

            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(activity.getContentResolver(), "a Video", videoFileUri);

                videoIntent.setClipData(clip);
                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList =
                        activity.getPackageManager()
                                .queryIntentActivities(videoIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    activity.grantUriPermission(packageName, videoFileUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.grantUriPermission(packageName, videoFileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                }
            }

            if (videoIntent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
                if (mediaFile != null) {
                    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    if (bundle != null) {
                        bundle = null;
                    }
                    bundle = new Bundle();
                    bundle.putParcelable("videoUri", videoFileUri);
                    bundle.putString("videoPath", mediaFile.getAbsolutePath());
                    activity.startActivityForResult(videoIntent, REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void captureImage(Activity activity) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";

            File mediaFile = FileClientService.getFilePath(imageFileName, activity.getApplicationContext(), "image/jpeg");

            Uri capturedImageUri = FileProvider.getUriForFile(activity, Utils.getMetaDataValue(activity, MobiComKitConstants.PACKAGE_NAME) + ".provider", mediaFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(activity.getContentResolver(), "a Photo", capturedImageUri);

                cameraIntent.setClipData(clip);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList = activity.getPackageManager()
                        .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    activity.grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }


            if (cameraIntent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
                if (mediaFile != null) {
                    if (bundle != null) {
                        bundle = null;
                    }
                    bundle = new Bundle();
                    bundle.putParcelable("imageUri", capturedImageUri);
                    bundle.putString("imagePath", mediaFile.getAbsolutePath());
                    activity.startActivityForResult(cameraIntent, MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleAttachmentOptionsResult(int requestCode, int resultCode, Intent intent, Activity activity, String userId, Integer groupId) {
        try {
            MessageBuilder messageBuilder = new MessageBuilder(activity);

            if (groupId == null && userId != null) {
                messageBuilder.setTo(userId);
            } else if (userId == null && groupId != null) {
                messageBuilder.setGroupId(groupId);
            }

            if ((requestCode == REQUEST_CODE_ATTACH_PHOTO ||
                    requestCode == REQUEST_CODE_TAKE_PHOTO)
                    && resultCode == Activity.RESULT_OK) {
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                if (selectedFileUri == null) {
                    selectedFileUri = bundle.getParcelable("imageUri");
                    String selectedFilePath = bundle.getString("imagePath");
                    if (selectedFilePath != null) {
                        messageBuilder.setFilePath(bundle.getString("imagePath")).setContentType(Message.ContentType.ATTACHMENT.getValue()).send();
                        bundle = null;
                    }
                }

                String absoluteFilePath = getFilePath(selectedFileUri, activity);

                MediaScannerConnection.scanFile(activity,
                        new String[]{absoluteFilePath}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
            }

            if (requestCode == REQUEST_CODE_CONTACT_GROUP_SELECTION && resultCode == Activity.RESULT_OK) {
                //checkForStartNewConversation(intent);
            }

            if (requestCode == REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY && resultCode == Activity.RESULT_OK) {

                String selectedFilePath = bundle.getString("videoPath");

                if (selectedFilePath != null) {
                    messageBuilder.setFilePath(bundle.getString("videoPath")).setContentType(Message.ContentType.VIDEO_MSG.getValue()).send();
                    bundle = null;
                }
            }

            if (requestCode == REQUEST_CODE_CONTACT_SHARE && resultCode == Activity.RESULT_OK) {

                try {
                    File vCradFile = new ContactService(activity).vCard(intent.getData());

                    if (vCradFile != null) {
                        messageBuilder.setFilePath(vCradFile.getAbsolutePath()).setContentType(Message.ContentType.CONTACT_MSG.getValue()).send();
                    }

                } catch (Exception e) {
                    Toast.makeText(activity, activity.getString(R.string.applozic_failed_to_load_contact), Toast.LENGTH_SHORT).show();
                    Log.e("Exception::", "Exception", e);
                }
            }
            if (requestCode == REQUEST_MULTI_ATTCAHMENT && resultCode == Activity.RESULT_OK) {

                ArrayList<Uri> attachmentList = intent.getParcelableArrayListExtra(MULTISELECT_SELECTED_FILES);
                String messageText = intent.getStringExtra(MULTISELECT_MESSAGE);

                //TODO: check performance, we might need to put in each posting in separate thread.

                for (Uri info : attachmentList) {
                    messageBuilder.setFilePath(info.toString()).setMessage(messageText).setContentType(Message.ContentType.ATTACHMENT.getValue()).send();
                }

            }

            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_SEND_LOCATION && resultCode == Activity.RESULT_OK) {
                Double latitude = intent.getDoubleExtra("latitude", 0);
                Double longitude = intent.getDoubleExtra("longitude", 0);
                //TODO: put your location(lat/lon ) in constructor.
                LocationInfo info = new LocationInfo(latitude, longitude);
                String locationInfo = GsonUtils.getJsonFromObject(info, LocationInfo.class);
                messageBuilder.setMessage(locationInfo).setContentType(Message.ContentType.LOCATION.getValue()).send();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, LinearLayout snackbarLayout, Activity activity) {
        if (requestCode == PermissionsUtils.REQUEST_STORAGE) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(snackbarLayout, R.string.storage_permission_granted);
                //put a check for if is file action
                processFileAction(activity, snackbarLayout);
            } else {
                showSnackBar(snackbarLayout, R.string.storage_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_LOCATION) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(snackbarLayout, R.string.location_permission_granted);
                processLocationAction(activity, snackbarLayout);
            } else {
                showSnackBar(snackbarLayout, R.string.location_permission_not_granted);
            }

        } else if (requestCode == PermissionsUtils.REQUEST_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.phone_state_permission_granted);
            } else {
                showSnackBar(snackbarLayout, R.string.phone_state_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CALL_PHONE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.phone_call_permission_granted);
                //processCall(contact, currentConversationId);
            } else {
                showSnackBar(snackbarLayout, R.string.phone_call_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_AUDIO_RECORD) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.record_audio_permission_granted);
                if (activity instanceof AppCompatActivity) {
                    processAudioAction((AppCompatActivity) activity, snackbarLayout);
                }
            } else {
                showSnackBar(snackbarLayout, R.string.record_audio_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.phone_camera_permission_granted);
               /* if (isTakePhoto) {
                    processCameraAction(activity, snackbarLayout);
                } else {
                    processVideoAction(activity, snackbarLayout);
                }*/
            } else {
                showSnackBar(snackbarLayout, R.string.phone_camera_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CONTACT) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.contact_permission_granted);
                processContactAction(activity, snackbarLayout);
            } else {
                showSnackBar(snackbarLayout, R.string.contact_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA_FOR_PROFILE_PHOTO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(snackbarLayout, R.string.phone_camera_permission_granted);
                /*if (profilefragment != null) {
                    profilefragment.processPhotoOption();
                }*/
            } else {
                showSnackBar(snackbarLayout, R.string.phone_camera_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_STORAGE_FOR_PROFILE_PHOTO) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(snackbarLayout, R.string.storage_permission_granted);
                /*if (profilefragment != null) {
                    profilefragment.processPhotoOption();
                }*/
            } else {
                showSnackBar(snackbarLayout, R.string.storage_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA_AUDIO) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(snackbarLayout, R.string.phone_camera_and_audio_permission_granted);
            } else {
                showSnackBar(snackbarLayout, R.string.audio_or_camera_permission_not_granted);
            }
        }
    }

    public static boolean isApplozicPermissionCode(int requestCode) {
        return requestCode >= 0 && requestCode <= 9;
    }

    public static void showSnackBar(LinearLayout layout, int resId) {
        if (layout != null) {
            Snackbar.make(layout, resId, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static String getFilePath(Uri uri, Context context) {
        if (uri == null) {
            Toast.makeText(context, R.string.file_not_selected, Toast.LENGTH_LONG).show();
            return null;
        }
        //handleSendAndRecordButtonView(true);
        //errorEditTextView.setVisibility(View.GONE);
        File file = FileUtils.getFile(context, uri);
        long fileSize = file.length() / 1024;
        long maxFileSize = 25 * 1024 * 1024;
        if (fileSize > maxFileSize) {
            Toast.makeText(context, R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
            return null;
        }

        return Uri.parse(file.getAbsolutePath()).toString();
    }
}
