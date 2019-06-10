package mega.privacy.android.app.lollipop;

import android.content.Context;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.Product;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.app.utils.billing.Purchase;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaPricing;

public class MyAccountInfo {

    int usedPerc = -1;
    long usedStorage = -1;
    int accountType = -1;
    MegaAccountDetails accountInfo = null;
    BitSet paymentBitSet = null;
    long numberOfSubscriptions = -1;
    int subscriptionStatus = -1;
    long subscriptionRenewTime = -1;
    long proExpirationTime = -1;
    String usedFormatted = "";
    String totalFormatted = "";
    String formattedUsedCloud = "";
    String formattedUsedInbox = "";
    String formattedUsedIncoming = "";
    String formattedUsedRubbish = "";
    String formattedAvailableSpace = "";
    String usedTransferFormatted = "";
    String totalTransferFormatted = "";
    int levelInventory = -1;
    int levelAccountDetails = -1;

    boolean inventoryFinished = false;
    boolean accountDetailsFinished = false;
    boolean getPaymentMethodsBoolean = false;

    MegaApiAndroid megaApi;

    String firstNameText = "";
    String lastNameText = "";
    String firstLetter;
    String fullName = "";

    String lastSessionFormattedDate;
    long createSessionTimeStamp = -1;

    DatabaseHandler dbH;
    Context context;

    public ArrayList<Product> productAccounts;

    Purchase proLiteMonthly = null;
    Purchase proLiteYearly = null;
    Purchase proIMonthly = null;
    Purchase proIYearly = null;
    Purchase proIIMonthly = null;
    Purchase proIIYearly = null;
    Purchase proIIIMonthly = null;
    Purchase proIIIYearly = null;

    MegaPricing pricing;

    int numVersions = -1;
    long previousVersionsSize = -1;

    public final int hasStorageDetails = 0x01;
    public final int hasTransferDetails = 0x02;
    public final int hasProDetails = 0x04;
    public final int hasSessionsDetails = 0x020;

    public MyAccountInfo(Context context){
        log("MyAccountInfo created");

        this.context = context;

        if (megaApi == null){
            megaApi = ((MegaApplication) context).getMegaApi();
        }

        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(context);
        }
    }

    public void setAccountDetails(int numDetails){
        log("setAccountDetails");

        if(accountInfo==null){
            log("Error because account info is NUll in setAccountDetails");
            return;
        }

        boolean storage = (numDetails & hasStorageDetails) != 0;
        boolean transfer = (numDetails & hasTransferDetails) != 0;
        boolean pro = (numDetails & hasProDetails) != 0;

        if (storage) {
            long totalStorage = accountInfo.getStorageMax();
            long usedCloudDrive = -1;
            long usedInbox = -1;
            long usedRubbish = -1;
            long usedIncoming = 0;

            //Check size of the different nodes
            if(megaApi.getRootNode()!=null){
                usedCloudDrive = accountInfo.getStorageUsed(megaApi.getRootNode().getHandle());
                formattedUsedCloud = Util.getSizeString(usedCloudDrive);
            }

            if(megaApi.getInboxNode()!=null){
                usedInbox = accountInfo.getStorageUsed(megaApi.getInboxNode().getHandle());
                if(usedInbox<1){
                    formattedUsedInbox = "";
                }
                else {
                    formattedUsedInbox = Util.getSizeString(usedInbox);
                }
            }

            if(megaApi.getRubbishNode()!=null){
                usedRubbish = accountInfo.getStorageUsed(megaApi.getRubbishNode().getHandle());
                formattedUsedRubbish = Util.getSizeString(usedRubbish);
            }

            ArrayList<MegaNode> nodes=megaApi.getInShares();
            if(nodes!=null){
                for(int i=0;i<nodes.size();i++){
                    MegaNode nodeIn = nodes.get(i);
                    usedIncoming = usedIncoming + accountInfo.getStorageUsed(nodeIn.getHandle());
                }
            }

            formattedUsedIncoming = Util.getSizeString(usedIncoming);

            totalFormatted = Util.getSizeString(totalStorage);

            usedStorage = accountInfo.getStorageUsed();
            usedFormatted=Util.getSizeString(usedStorage);

            usedPerc = 0;
            if (totalStorage != 0){
                usedPerc = (int)((100 * usedStorage) / totalStorage);
            }

            long availableSpace = totalStorage - usedStorage;
            if (availableSpace < 0) {
                formattedAvailableSpace = Util.getSizeString(0);
            }
            else{
                formattedAvailableSpace = Util.getSizeString(availableSpace);
            }
        }

        if (transfer) {
            totalTransferFormatted = Util.getSizeString(accountInfo.getTransferMax());
            usedTransferFormatted = Util.getSizeString(accountInfo.getTransferOwnUsed());
        }

        if (pro) {
            accountType = accountInfo.getProLevel();
            subscriptionStatus = accountInfo.getSubscriptionStatus();
            subscriptionRenewTime = accountInfo.getSubscriptionRenewTime();
            proExpirationTime = accountInfo.getProExpiration();

            switch (accountType){
                case 0:{
                    levelAccountDetails = -1;
                    break;
                }
                case 1:{
                    levelAccountDetails = 1;
                    break;
                }
                case 2:{
                    levelAccountDetails = 2;
                    break;
                }
                case 3:{
                    levelAccountDetails = 3;
                    break;
                }
                case 4:{
                    levelAccountDetails = 0;
                    break;
                }
            }
        }

        accountDetailsFinished = true;

        log("LEVELACCOUNTDETAILS: " + levelAccountDetails + "; LEVELINVENTORY: " + levelInventory + "; INVENTORYFINISHED: " + inventoryFinished);
    }

    public MegaAccountDetails getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(MegaAccountDetails accountInfo) {

        this.accountInfo = accountInfo;
        log("***Renews ts: "+accountInfo.getSubscriptionRenewTime());
        log("***Renews on: "+Util.getDateString(accountInfo.getSubscriptionRenewTime()));
        log("***Expires ts:"+accountInfo.getProExpiration());
        log("***Expires on: "+Util.getDateString(accountInfo.getProExpiration()));
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public long getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public void setNumberOfSubscriptions(long numberOfSubscriptions) {
        this.numberOfSubscriptions = numberOfSubscriptions;
    }

    public int getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(int subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public long getSubscriptionRenewTime() {
        return subscriptionRenewTime;
    }

    public void setSubscriptionRenewTime(long subscriptionRenewTime) {
        this.subscriptionRenewTime = subscriptionRenewTime;
    }

    public long getProExpirationTime() {
        return proExpirationTime;
    }

    public void setProExpirationTime(long proExpirationTime) {
        this.proExpirationTime = proExpirationTime;
    }

    public BitSet getPaymentBitSet() {
        return paymentBitSet;
    }

    public void setPaymentBitSet(BitSet paymentBitSet) {
        this.paymentBitSet = paymentBitSet;
    }

    public int getUsedPerc() {
        return usedPerc;
    }

    public void setUsedPerc(int usedPerc) {
        this.usedPerc = usedPerc;
    }

    public String getTotalFormatted() {
        return totalFormatted;
    }

    public void setTotalFormatted(String totalFormatted) {
        this.totalFormatted = totalFormatted;
    }

    public String getUsedFormatted() {
        return usedFormatted;
    }

    public void setUsedFormatted(String usedFormatted) {
        this.usedFormatted = usedFormatted;
    }

    public int getLevelInventory() {
        return levelInventory;
    }

    public void setLevelInventory(int levelInventory) {
        this.levelInventory = levelInventory;
    }

    public int getLevelAccountDetails() {
        return levelAccountDetails;
    }

    public void setLevelAccountDetails(int levelAccountDetails) {
        this.levelAccountDetails = levelAccountDetails;
    }


    public boolean isAccountDetailsFinished() {
        return accountDetailsFinished;
    }

    public void setAccountDetailsFinished(boolean accountDetailsFinished) {
        this.accountDetailsFinished = accountDetailsFinished;
    }

    public boolean isInventoryFinished() {
        return inventoryFinished;
    }

    public void setInventoryFinished(boolean inventoryFinished) {
        this.inventoryFinished = inventoryFinished;
    }

    public String getLastSessionFormattedDate() {
        return lastSessionFormattedDate;
    }

    public void setLastSessionFormattedDate(String lastSessionFormattedDate) {
        log("setLastSessionFormattedDate: "+lastSessionFormattedDate);
        this.lastSessionFormattedDate = lastSessionFormattedDate;
    }

    public static void log(String message) {
        Util.log("MyAccountInfo", message);
    }

    public void setFullName(){
        log("setFullName");
        if (firstNameText.trim().length() <= 0){
            fullName = lastNameText;
        }
        else{
            fullName = firstNameText + " " + lastNameText;
        }

        if (fullName.trim().length() <= 0){
            log("Put email as fullname");
            String email = megaApi.getMyUser().getEmail();
            String[] splitEmail = email.split("[@._]");
            fullName = splitEmail[0];
        }

        if (fullName.trim().length() <= 0){
            fullName = context.getString(R.string.name_text)+" "+context.getString(R.string.lastname_text);
            log("Full name set by default: "+fullName);
        }

        firstLetter = fullName.charAt(0) + "";
        firstLetter = firstLetter.toUpperCase(Locale.getDefault());
    }

    public void setProductAccounts(MegaPricing p){
        log("setProductAccounts");

        if(productAccounts==null){
            productAccounts = new ArrayList<Product>();
        }
        else{
            productAccounts.clear();
        }

        for (int i = 0; i < p.getNumProducts(); i++) {
            log("p[" + i + "] = " + p.getHandle(i) + "__" + p.getAmount(i) + "___" + p.getGBStorage(i) + "___" + p.getMonths(i) + "___" + p.getProLevel(i) + "___" + p.getGBTransfer(i));

            Product account = new Product(p.getHandle(i), p.getProLevel(i), p.getMonths(i), p.getGBStorage(i), p.getAmount(i), p.getGBTransfer(i));

            productAccounts.add(account);
        }
    }

    public ArrayList<Product> getProductAccounts() {
        return productAccounts;
    }

    public void setProductAccounts(ArrayList<Product> productAccounts) {
        this.productAccounts = productAccounts;
    }

    public String getFirstNameText() {
        return firstNameText;
    }

    public void setFirstNameText(String firstNameText) {
        this.firstNameText = firstNameText;
        setFullName();
    }

    public String getLastNameText() {
        return lastNameText;
    }

    public void setLastNameText(String lastNameText) {
        this.lastNameText = lastNameText;
        setFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public String getFormattedUsedCloud() {
        return formattedUsedCloud;
    }

    public void setFormattedUsedCloud(String formattedUsedCloud) {
        this.formattedUsedCloud = formattedUsedCloud;
    }

    public String getFormattedUsedInbox() {
        return formattedUsedInbox;
    }

    public void setFormattedUsedInbox(String formattedUsedInbox) {
        this.formattedUsedInbox = formattedUsedInbox;
    }

    public String getFormattedUsedIncoming() {
        return formattedUsedIncoming;
    }

    public void setFormattedUsedIncoming(String formattedUsedIncoming) {
        this.formattedUsedIncoming = formattedUsedIncoming;
    }

    public String getFormattedUsedRubbish() {
        return formattedUsedRubbish;
    }

    public void setFormattedUsedRubbish(String formattedUsedRubbish) {
        this.formattedUsedRubbish = formattedUsedRubbish;
    }

    public String getFormattedAvailableSpace() {
        return formattedAvailableSpace;
    }

    public String getFormattedPreviousVersionsSize() {
        return Util.getSizeString(previousVersionsSize);
    }

    public void setFormattedAvailableSpace(String formattedAvailableSpace) {
        this.formattedAvailableSpace = formattedAvailableSpace;
    }

    public String getTotalTansferFormatted() {
        return totalTransferFormatted;
    }

    public void setTotalTransferFormatted(String totalTransferFormatted) {
        this.totalTransferFormatted = totalTransferFormatted;
    }

    public String getUsedTransferFormatted() {
        return usedTransferFormatted;
    }

    public void setUsedTransferFormatted(String usedTransferFormatted) {
        this.usedTransferFormatted = usedTransferFormatted;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public void setProLiteMonthly(Purchase proLiteMonthly) {
        this.proLiteMonthly = proLiteMonthly;
    }

    public void setProLiteYearly(Purchase proLiteYearly) {
        this.proLiteYearly = proLiteYearly;
    }

    public void setProIMonthly(Purchase proIMonthly) {
        this.proIMonthly = proIMonthly;
    }

    public void setProIYearly(Purchase proIYearly) {
        this.proIYearly = proIYearly;
    }

    public void setProIIMonthly(Purchase proIIMonthly) {
        this.proIIMonthly = proIIMonthly;
    }

    public void setProIIYearly(Purchase proIIYearly) {
        this.proIIYearly = proIIYearly;
    }

    public void setProIIIMonthly(Purchase proIIIMonthly) {
        this.proIIIMonthly = proIIIMonthly;
    }

    public void setProIIIYearly(Purchase proIIIYearly) {
        this.proIIIYearly = proIIIYearly;
    }

    public Purchase getProLiteMonthly() {
        return proLiteMonthly;
    }

    public Purchase getProLiteYearly() {
        return proLiteYearly;
    }

    public Purchase getProIMonthly() {
        return proIMonthly;
    }

    public Purchase getProIYearly() {
        return proIYearly;
    }

    public Purchase getProIIMonthly() {
        return proIIMonthly;
    }

    public Purchase getProIIYearly() {
        return proIIYearly;
    }

    public Purchase getProIIIMonthly() {
        return proIIIMonthly;
    }

    public Purchase getProIIIYearly() {
        return proIIIYearly;
    }

    public boolean isGetPaymentMethodsBoolean() {
        return getPaymentMethodsBoolean;
    }

    public void setGetPaymentMethodsBoolean(boolean getPaymentMethodsBoolean) {
        this.getPaymentMethodsBoolean = getPaymentMethodsBoolean;
    }

    public long getCreateSessionTimeStamp() {
        return createSessionTimeStamp;
    }

    public void setCreateSessionTimeStamp(long createSessionTimeStamp) {
        log("setCreateSessionTimeStamp: " + createSessionTimeStamp);
        this.createSessionTimeStamp = createSessionTimeStamp;
    }

    public int getNumVersions() {
        return numVersions;
    }

    public void setNumVersions(int numVersions) {
        this.numVersions = numVersions;
    }

    public long getPreviousVersionsSize() {
        return previousVersionsSize;
    }

    public void setPreviousVersionsSize(long previousVersionsSize) {
        this.previousVersionsSize = previousVersionsSize;
    }

    public MegaPricing getPricing() {
        return pricing;
    }

    public void setPricing(MegaPricing pricing) {
        this.pricing = pricing;
    }

    public long getUsedStorage() {
        return usedStorage;
    }

}
