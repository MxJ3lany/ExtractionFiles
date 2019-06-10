package mega.privacy.android.app;

import mega.privacy.android.app.utils.Util;

public class MegaPreferences{
	
	String firstTime = "";
	String camSyncWifi = "";
	String camSyncCharging = "";
	String camSyncEnabled = "";
	String camSyncHandle = "";
	String camSyncLocalPath = "";
	String camSyncFileUpload = "";
	String camSyncTimeStamp = "";
	String pinLockEnabled = "";
	String pinLockCode = "";
	String storageAskAlways = "";
	String storageDownloadLocation = "";
	String lastFolderUpload = "";
	String lastFolderCloud = "";
	String secondaryMediaFolderEnabled = "";
	String localPathSecondaryFolder = "";
	String megaHandleSecondaryFolder = "";
	String secSyncTimeStamp = "";
	String keepFileNames = "";
	String storageAdvancedDevices = "";
	String preferredViewList = "";
	String preferredViewListCameraUploads = "";
	String uriExternalSDCard = "";
	String cameraFolderExternalSDCard = "";	
	String pinLockType = "";
	String preferredSortCloud = "";
	String preferredSortContacts = "";
	String preferredSortOthers = "";
	String firstTimeChat = "";
	String smallGridCamera = "";
	String isAutoPlayEnabled = "";
		
	public final static int ONLY_PHOTOS = 1001;
	public final static int ONLY_VIDEOS = 1002;
	public final static int PHOTOS_AND_VIDEOS = 1003;
	
	MegaPreferences(String firstTime, String camSyncWifi, String camSyncEnabled, String camSyncHandle, String camSyncLocalPath, String camSyncFileUpload, String camSyncTimeStamp, String pinLockEnabled, String pinLockCode, String storageAskAlways, 
			String storageDownloadLocation, String camSyncCharging, String lastFolderUpload, String lastFolderCloud, String secondaryMediaFolderEnabled, String localPathSecondaryFolder, String megaHandleSecondaryFolder, String secSyncTimeStamp, 
			String keepFileNames, String storageAdvancedDevices, String preferredViewList, String preferredViewListCameraUploads, String uriExternalSDCard, String cameraFolderExternalSDCard, String pinLockType, String preferredSortCloud, String preferredSortContacts,
			String preferredSortOthers, String firstTimeChat, String smallGridCamera, String isAutoPlayEnabled){
		this.firstTime = firstTime;
		this.camSyncWifi = camSyncWifi;
		this.camSyncEnabled = camSyncEnabled;
		this.camSyncHandle = camSyncHandle;
		this.camSyncLocalPath = camSyncLocalPath;
		this.camSyncFileUpload = camSyncFileUpload;
		this.camSyncTimeStamp = camSyncTimeStamp;
		this.pinLockEnabled = pinLockEnabled;
		this.pinLockCode = pinLockCode;
		this.storageAskAlways = storageAskAlways;
		this.storageDownloadLocation = storageDownloadLocation;
		this.camSyncCharging = camSyncCharging;
		this.lastFolderUpload = lastFolderUpload;
		this.lastFolderCloud = lastFolderCloud;
		this.secondaryMediaFolderEnabled = secondaryMediaFolderEnabled;
		this.localPathSecondaryFolder = localPathSecondaryFolder;
		this.megaHandleSecondaryFolder = megaHandleSecondaryFolder;
		this.secSyncTimeStamp = secSyncTimeStamp;
		this.keepFileNames = keepFileNames;
		this.storageAdvancedDevices = storageAdvancedDevices;
		this.preferredViewList = preferredViewList;
		this.preferredViewListCameraUploads = preferredViewListCameraUploads;
		this.uriExternalSDCard = uriExternalSDCard;
		this.cameraFolderExternalSDCard = cameraFolderExternalSDCard;
		this.pinLockType = pinLockType;
		this.preferredSortCloud = preferredSortCloud;
		this.preferredSortContacts = preferredSortContacts;
		this.preferredSortOthers = preferredSortOthers;
		this.firstTimeChat = firstTimeChat;
		this.smallGridCamera = smallGridCamera;
		this.isAutoPlayEnabled = isAutoPlayEnabled;
	}

	public String getFirstTime (){
		return firstTime;
	}
	
	public void setFirstTime(String firstTime){
		this.firstTime = firstTime;
	}
	
	public String getCamSyncEnabled(){
		return camSyncEnabled;
	}
	
	public void setCamSyncEnabled(String camSyncEnabled){
		this.camSyncEnabled = camSyncEnabled;
	}
	
	public String getCamSyncHandle(){
		return camSyncHandle;
	}
	
	public void setCamSyncHandle(String camSyncHandle){
		this.camSyncHandle = camSyncHandle;
	}
	
	public String getCamSyncLocalPath(){
		return camSyncLocalPath;
	}
	
	public void setCamSyncLocalPath(String camSyncLocalPath){
		this.camSyncLocalPath = camSyncLocalPath;
	}
	
	public String getCamSyncWifi (){
		return camSyncWifi;
	}
	
	public void setCamSyncWifi(String camSyncWifi){
		this.camSyncWifi = camSyncWifi;
	}
	
	public String getCamSyncCharging (){
		return camSyncCharging;
	}
	
	public void setCamSyncCharging(String camSyncCharging){
		this.camSyncCharging = camSyncCharging;
	}
	
	public String getCamSyncFileUpload(){
		return camSyncFileUpload;
	}
	
	public void setCamSyncFileUpload(String camSyncFileUpload){
		this.camSyncFileUpload = camSyncFileUpload;
	}
	
	public String getCamSyncTimeStamp(){
		return camSyncTimeStamp;
	}
	
	public void setCamSyncTimeStamp(String camSyncTimeStamp){
		this.camSyncTimeStamp = camSyncTimeStamp;
	}
	
	public String getPinLockEnabled(){
		return pinLockEnabled;
	}
	
	public void setPinLockEnabled(String pinLockEnabled){
		this.pinLockEnabled = pinLockEnabled;
	}
	
	public String getPinLockCode(){
		return pinLockCode;
	}
	
	public void setPinLockCode(String pinLockCode){
		this.pinLockCode = pinLockCode;
	}
	
	public String getStorageAskAlways(){
		return storageAskAlways;
	}
	
	public void setStorageAskAlways(String storageAskAlways){
		this.storageAskAlways = storageAskAlways;
	}
	
	public String getStorageDownloadLocation(){
		return storageDownloadLocation;
	}
	
	public void setStorageDownloadLocation(String storageDownloadLocation){
		this.storageDownloadLocation = storageDownloadLocation;
	}

	public String getLastFolderUpload() {
		if(lastFolderUpload == null || lastFolderUpload.length() == 0)
			return null;
		return lastFolderUpload;
	}

	public void setLastFolderUpload(String lastFolderUpload) {
		this.lastFolderUpload = lastFolderUpload;
	}

	public String getLastFolderCloud() {
		if(lastFolderCloud == null || lastFolderCloud.length() == 0)
			return null;
		
		return lastFolderCloud;
	}

	public void setLastFolderCloud(String lastFolderCloud) {
		this.lastFolderCloud = lastFolderCloud;
	}

	public String getSecondaryMediaFolderEnabled() {
		return secondaryMediaFolderEnabled;
	}

	public void setSecondaryMediaFolderEnabled(String secondaryMediaFolderEnabled) {
		this.secondaryMediaFolderEnabled = secondaryMediaFolderEnabled;
	}

	public String getLocalPathSecondaryFolder() {
		return localPathSecondaryFolder;
	}

	public void setLocalPathSecondaryFolder(String localPathSecondaryFolder) {
		this.localPathSecondaryFolder = localPathSecondaryFolder;
	}

	public String getMegaHandleSecondaryFolder() {
		log("getMegaHandleSecondaryFolder "+megaHandleSecondaryFolder);
		return megaHandleSecondaryFolder;
	}

	public void setMegaHandleSecondaryFolder(String megaHandleSecondaryFolder) {
		this.megaHandleSecondaryFolder = megaHandleSecondaryFolder;
	}
	private static void log(String log) {
		Util.log("Preferences", log);
	}

	public String getSecSyncTimeStamp() {
		return secSyncTimeStamp;
	}

	public void setSecSyncTimeStamp(String secSyncTimeStamp) {
		this.secSyncTimeStamp = secSyncTimeStamp;
	}

	public String getKeepFileNames() {
		return keepFileNames;
	}

	public void setKeepFileNames(String keepFileNames) {
		this.keepFileNames = keepFileNames;
	}

	public String getStorageAdvancedDevices() {
		return storageAdvancedDevices;
	}

	public void setStorageAdvancedDevices(String storageAdvancedDevices) {
		this.storageAdvancedDevices = storageAdvancedDevices;
	}

	public String getPreferredViewList() {
		return preferredViewList;
	}

	public void setPreferredViewList(String preferredViewList) {
		this.preferredViewList = preferredViewList;
	}

	public String getPreferredViewListCameraUploads() {
		return preferredViewListCameraUploads;
	}

	public void setPreferredViewListCameraUploads(
			String preferredViewListCameraUploads) {
		this.preferredViewListCameraUploads = preferredViewListCameraUploads;
	}
	
	public String getUriExternalSDCard(){
		return this.uriExternalSDCard;
	}
	
	public void setUriExternalSDCard(String uriExternalSDCard){
		this.uriExternalSDCard = uriExternalSDCard;
	}
	
	public String getCameraFolderExternalSDCard(){
		return this.cameraFolderExternalSDCard;
	}
	
	public void setCameraFolderExternalSDCard(String cameraFolderExternalSDCard){
		this.cameraFolderExternalSDCard = cameraFolderExternalSDCard;
	}

	public String getPinLockType() {
		return pinLockType;
	}

	public void setPinLockType(String pinLockType) {
		this.pinLockType = pinLockType;
	}


	public String getPreferredSortCloud() {
		return preferredSortCloud;
	}

	public void setPreferredSortCloud(String preferredSortCloud) {
		this.preferredSortCloud = preferredSortCloud;
	}

	public String getPreferredSortContacts() {
		return preferredSortContacts;
	}

	public void setPreferredSortContacts(String preferredSortContacts) {
		this.preferredSortContacts = preferredSortContacts;
	}

	public String getPreferredSortOthers() {
		return preferredSortOthers;
	}

	public void setPreferredSortOthers(String preferredSortOthers) {
		this.preferredSortOthers = preferredSortOthers;
	}

	public String getFirstTimeChat (){
		return firstTimeChat;
	}

	public void setFirstTimeChat(String firstTimeChat){
		this.firstTimeChat = firstTimeChat;
	}

	public String getSmallGridCamera() {
		return smallGridCamera;
	}

	public void setSmallGridCamera(String smallGridCamera) {
		this.smallGridCamera = smallGridCamera;
	}
    
    public boolean isAutoPlayEnabled(){
        return Boolean.parseBoolean(isAutoPlayEnabled);
    }
}

