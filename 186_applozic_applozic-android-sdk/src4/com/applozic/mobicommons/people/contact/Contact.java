package com.applozic.mobicommons.people.contact;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.ALContactProcessor;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author devashish
 */
public class Contact extends JsonMarker {

    public static final String R_DRAWABLE = "R.drawable";
    @Expose
    private String firstName = "";
    @Expose
    private String middleName = "";
    @Expose
    private String lastName = "";
    @Expose
    @SerializedName("emailIdList")
    private List<String> emailIds;
    @Expose
    @SerializedName("contactNumberList")
    private List<String> contactNumbers = new ArrayList<String>();
    private Map<String, String> phoneNumbers;
    private String contactNumber;
    private String formattedContactNumber;
    @Expose
    private long contactId;
    private String fullName;

    private String userId;

    @Expose
    private String imageURL;
    @Expose
    private String localImageUrl;
    @Expose
    private String emailId;
    private String applicationId;
    private boolean connected;
    private Long lastSeenAtTime;
    private boolean checked = false;
    private Integer unreadCount;
    private boolean blocked;
    private boolean blockedBy;
    private String status;
    private short contactType;
    private Short deviceContactType;
    private Short userTypeId;
    private Long deletedAtTime;
    private Long notificationAfterTime;
    private Long lastMessageAtTime;
    private Map<String, String> metadata;
    private Short roleType;
    private String phoneDisplayName;
    private boolean applozicType = true;

    public Contact() {

    }

    public Contact(long contactId) {
        this.contactId = contactId;
    }

    public Contact(String firstName, String lastName) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Contact(String fullName, List<String> emailIds, List<String> contactNumbers, long contactId) {
        this(contactId);
        processFullName(fullName);
        this.emailIds = emailIds;
        this.contactNumbers = contactNumbers;
    }

    public Contact(String userId) {
        this.userId = userId;
    }

    public Contact(Context context, String userId) {
        this.userId = userId;
        this.processContactNumbers(context);
    }

    public Short getDeviceContactType() {
        return deviceContactType;
    }

    public void setDeviceContactType(Short contactType) {
        if(contactType == null){
            return;
        }
        this.deviceContactType = contactType;
        setApplozicType(ContactType.APPLOZIC.getValue().equals(this.deviceContactType) || ContactType.DEVICE_AND_APPLOZIC.getValue().equals(this.deviceContactType));
    }

    public short getContactType() {
        return contactType;
    }

    public void setContactType(short contactType) {
        this.contactType = contactType;
    }

    public void processContactNumbers(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso().toUpperCase();
        if (TextUtils.isEmpty(formattedContactNumber)) {
            try {
                if (context.getApplicationContext() instanceof ALContactProcessor) {
                    setFormattedContactNumber(((ALContactProcessor) context.getApplicationContext()).processContact(getContactNumber(), countryCode));
                }
            }catch(ClassCastException e){
                e.printStackTrace();
            }
        }
    }

    public boolean isApplozicType() {
        return applozicType;
    }

    public void setApplozicType(boolean applozicType) {
        this.applozicType = applozicType;
    }


    public String getPhoneDisplayName() {
        return phoneDisplayName;
    }


    public void setPhoneDisplayName(String phoneDisplayName) {
        this.phoneDisplayName = phoneDisplayName;
    }

    public boolean isDeviceContact() {
        return (deviceContactType != null && ContactType.DEVICE.getValue().equals(deviceContactType));
    }

    public enum ContactType {
        APPLOZIC(Short.valueOf("0")), DEVICE(Short.valueOf("1")), DEVICE_AND_APPLOZIC(Short.valueOf("2"));

        private Short value;

        ContactType(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

  /*//Todo: Will be used for device contacts
    public void processContactNumbers(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso().toUpperCase();
        phoneNumbers = ContactUtils.getPhoneNumbers(context, getContactId());
        if (TextUtils.isEmpty(getFormattedContactNumber()) && !TextUtils.isEmpty(getContactNumber())) {
            setFormattedContactNumber(ContactNumberUtils.getPhoneNumber(getContactNumber(), countryCode));
        }

        if (!TextUtils.isEmpty(getContactNumber()) || phoneNumbers.isEmpty()) {
            return;
        }

        String mobileNumber = null;
        String mainNumber = null;
        for (String phoneNumber : phoneNumbers.keySet()) {
            setContactNumber(phoneNumber);
            //if (phoneNumbers.get(phoneNumber).equals(ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)) {
            if (phoneNumbers.get(phoneNumber).equals("Main")) {
                mainNumber = phoneNumber;
                break;
            }
            if (phoneNumbers.get(phoneNumber).equals("Mobile")) {
                //if (phoneNumbers.get(phoneNumber).equals(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)) {
                mobileNumber = phoneNumber;
            }
        }

        if (!TextUtils.isEmpty(mobileNumber)) {
            setContactNumber(mobileNumber);
        }

        if (!TextUtils.isEmpty(mainNumber)) {
            setContactNumber(mainNumber);
        }

        //Note: contact.getContactNumber() is not a formattedNumber with country code so it might not match with
        //phoneNumbers key
        if (phoneNumbers.get(getContactNumber()) == null) {
            for (String phoneNumber : phoneNumbers.keySet()) {
                if (PhoneNumberUtils.compare(getContactNumber(), phoneNumber)) {
                    setContactNumber(phoneNumber);
                    break;
                }
            }
        }

        setFormattedContactNumber(ContactNumberUtils.getPhoneNumber(getContactNumber(), countryCode));
    }*/

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getFormattedContactNumber() {
        return TextUtils.isEmpty(formattedContactNumber) ? getContactNumber() : formattedContactNumber;
    }

    public void setFormattedContactNumber(String formattedContactNumber) {
        this.formattedContactNumber = formattedContactNumber;
    }

    public List<String> getContactNumbers() {
        return contactNumbers;
    }

    public void setContactNumbers(List<String> contactNumbers) {
        this.contactNumbers = contactNumbers;
    }

    public List<String> getEmailIds() {
        return emailIds;
    }

    public void setEmailIds(List<String> emailIds) {
        this.emailIds = emailIds;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getDisplayName() {
        if (formattedContactNumber != null) {
            return TextUtils.isEmpty(phoneDisplayName) ? TextUtils.isEmpty(getFormattedContactNumber()) ? getContactIds() : getFormattedContactNumber() : phoneDisplayName;
        }
        return TextUtils.isEmpty(fullName) ? getContactIds() : fullName;
    }

    public String getFullName() {
        if (formattedContactNumber != null) {
            return TextUtils.isEmpty(phoneDisplayName) ? fullName : phoneDisplayName;
        }
        return fullName == null ? "" : fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Map<String, String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Map<String, String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public boolean hasMultiplePhoneNumbers() {
        return getPhoneNumbers() != null && !getPhoneNumbers().isEmpty() && getPhoneNumbers().size() > 1;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        checked = !checked;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDeletedAtTime() {
        return deletedAtTime == null ? 0 : deletedAtTime;
    }

    public void setDeletedAtTime(Long deletedAtTime) {
        this.deletedAtTime = deletedAtTime;
    }

    public boolean isDeleted() {
        return (deletedAtTime != null && deletedAtTime > 0);
    }

    public Long getNotificationAfterTime() {
        return notificationAfterTime;
    }

    public void setNotificationAfterTime(Long notificationAfterTime) {
        this.notificationAfterTime = notificationAfterTime;
    }

    public boolean isNotificationMuted() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return (getNotificationAfterTime() != null) && (getNotificationAfterTime() - date.getTime() > 0);

    }

    public void processFullName(String fullName) {
        this.fullName = fullName;
        if (fullName != null) {
            fullName = fullName.trim();
            String[] name = fullName.split(" ");
            firstName = name[0];
            if (firstName.length() <= 3 && name.length > 1) {
                firstName = name[1];
                lastName = name[name.length - 1];
                if (name.length > 2) {
                    middleName = fullName.substring(name[0].length() + firstName.length() + 1, fullName.length() - (lastName.length() + 1));
                }
            } else {
                if (name.length > 1) {
                    lastName = name[name.length - 1];
                    if (name.length > 2) {
                        middleName = fullName.substring(firstName.length() + 1, fullName.length() - (lastName.length() + 1));
                    }
                }
            }
        }
    }

    public Short getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactIds() {
        return TextUtils.isEmpty(getUserId()) ? getFormattedContactNumber() : getUserId();
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLocalImageUrl() {
        return localImageUrl;
    }

    public void setLocalImageUrl(String localImageUrl) {
        this.localImageUrl = localImageUrl;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public boolean isDrawableResources() {
        return (imageURL != null && imageURL.startsWith(R_DRAWABLE));
    }

    public String getrDrawableName() {
        return getImageURL() == null ? getImageURL() : getImageURL().substring(R_DRAWABLE.length() + 1);

    }

    public long getLastSeenAt() {
        return lastSeenAtTime == null ? 0 : lastSeenAtTime;
    }

    public void setLastSeenAt(Long lastSeenAt) {
        this.lastSeenAtTime = lastSeenAt;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(boolean blockedBy) {
        this.blockedBy = blockedBy;
    }

    public boolean isOnline() {
        return !isBlocked() && !isBlockedBy() && isConnected();
    }

    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    public Short getRoleType() {
        return roleType;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailIds=" + emailIds +
                ", contactNumbers=" + contactNumbers +
                ", phoneNumbers=" + phoneNumbers +
                ", contactNumber='" + contactNumber + '\'' +
                ", formattedContactNumber='" + formattedContactNumber + '\'' +
                ", contactId=" + contactId +
                ", fullName='" + fullName + '\'' +
                ", userId='" + userId + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", localImageUrl='" + localImageUrl + '\'' +
                ", emailId='" + emailId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", connected=" + connected +
                ", lastSeenAtTime=" + lastSeenAtTime +
                ", checked=" + checked +
                ", unreadCount=" + unreadCount +
                ", blocked=" + blocked +
                ", blockedBy=" + blockedBy +
                ", status='" + status + '\'' +
                ", contactType=" + contactType +
                ", deviceContactType=" + deviceContactType +
                ", userTypeId=" + userTypeId +
                ", deletedAtTime=" + deletedAtTime +
                ", notificationAfterTime=" + notificationAfterTime +
                ", lastMessageAtTime=" + lastMessageAtTime +
                ", metadata=" + metadata +
                ", roleType=" + roleType +
                ", phoneDisplayName='" + phoneDisplayName + '\'' +
                ", applozicType=" + applozicType +
                '}';
    }
}
