package mega.privacy.android.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.Formatter;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import mega.privacy.android.app.AndroidLogger;
import mega.privacy.android.app.BaseActivity;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaAttributes;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.interfaces.AbortPendingTransferCallback;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatFullScreenImageViewer;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class Util {
	
	public static int ONTRANSFERUPDATE_REFRESH_MILLIS = 300;
	
	public static float dpWidthAbs = 360;
	public static float dpHeightAbs = 592;
	
	public static double percScreenLogin = 0.596283784; //The dimension of the grey zone (Login and Tour)
	public static double percScreenLoginReturning = 0.8;
	
	// Debug flag to enable logging and some other things
	public static boolean DEBUG = false;

	public static String mainDIR = "/MEGA";
	public static String offlineDIR = "MEGA/MEGA Offline";
	public static String downloadDIR ="MEGA/MEGA Downloads";
	public static String temporalPicDIR ="MEGA/MEGA AppTemp";
	public static String profilePicDIR ="MEGA/MEGA Profile Images";
	public static String logDIR = "MEGA/MEGA Logs";
	public static String advancesDevicesDIR = "MEGA/MEGA Temp";
	public static String chatTempDIR = "MEGA/MEGA Temp/Chat";
	public static String oldMKFile = "/MEGA/MEGAMasterKey.txt";
	public static String rKFile = "/MEGA/MEGARecoveryKey.txt";
	
	public static String base64EncodedPublicKey_1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0bZjbgdGRd6/hw5/J2FGTkdG";
	public static String base64EncodedPublicKey_2 = "tDTMdR78hXKmrxCyZUEvQlE/DJUR9a/2ZWOSOoaFfi9XTBSzxrJCIa+gjj5wkyIwIrzEi";
	public static String base64EncodedPublicKey_3 = "55k9FIh3vDXXTHJn4oM9JwFwbcZf1zmVLyes5ld7+G15SZ7QmCchqfY4N/a/qVcGFsfwqm";
	public static String base64EncodedPublicKey_4 = "RU3VzOUwAYHb4mV/frPctPIRlJbzwCXpe3/mrcsAP+k6ECcd19uIUCPibXhsTkNbAk8CRkZ";
	public static String base64EncodedPublicKey_5 = "KOy+czuZWfjWYx3Mp7srueyQ7xF6/as6FWrED0BlvmhJYj0yhTOTOopAXhGNEk7cUSFxqP2FKYX8e3pHm/uNZvKcSrLXbLUhQnULhn4WmKOQIDAQAB";
	
	/*public static String base64EncodedPublicKey_1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlxJdfjvhsCAK1Lu5n6WtQf";
	public static String base64EncodedPublicKey_2 = "MkjjOUCDDuM7zeiS3jsfCghG1bpwMmD4E8vQfPboyYtQBftdEG5GbWrqWJL+z6M/2SN";
	public static String base64EncodedPublicKey_3 = "+6pHqExFw8fjzP/4/CDzHLhmITKTOegm/6cfMUWcrghZuiHKfM6n4vmNYrHy4Bpx68RJW+J4B";
	public static String base64EncodedPublicKey_4 = "wL6PWE8ZGGeeJmU0eAJeRJMsNEwMrW2LATnIoJ4/qLYU4gKDINPMRaIE6/4pQnbd2NurWm8ZQT7XSMQZcisTqwRLS";
	public static String base64EncodedPublicKey_5 = "YgjYKCXtjloP8QnKu0IGOoo79Cfs3Z9eC3sQ1fcLQsMM2wExlbnYI2KPTs0EGCmcMXrrO5MimGjYeW8GQlrKsbiZ0UwIDAQAB";
	*/
	public static DatabaseHandler dbH;
	public static boolean fileLoggerSDK = false;
	public static boolean fileLoggerKarere = false;
	public static Context context;

	public static HashMap<String, String> countryCodeDisplay;
	
	/*
	 * Create progress dialog helper
	 */
	public static ProgressDialog createProgress(Context context, String message) {
		ProgressDialog progress = new ProgressDialog(context);
		progress.setMessage(message);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		return progress;
	}
	
	/*
	 * Create progress dialog with resId
	 */
	public static ProgressDialog createProgress(Context context, int stringResId) {
		return createProgress(context, context.getString(stringResId));
	}
	
	public static void showErrorAlertDialogFinish(MegaError error, Activity activity) {
		showErrorAlertDialog(error.getErrorString(), true, activity);
	}

	public static File createTemporalTextFile(String name, String data){

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR + "/";
		File tempDownDirectory = new File(path);
		if(!tempDownDirectory.exists()){
			tempDownDirectory.mkdirs();
		}

		String fileName = name+".txt";
		final File file = new File(path, fileName);

		try
		{
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(data);

			myOutWriter.close();

			fOut.flush();
			fOut.close();

			return file;
		}
		catch (IOException e)
		{
			log("File write failed: " + e.toString());
			return null;
		}
	}

	public static File createTemporalURLFile(String name, String data){

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR + "/";
		File tempDownDirectory = new File(path);
		if(!tempDownDirectory.exists()){
			tempDownDirectory.mkdirs();
		}

		String fileName = name+".url";
		final File file = new File(path, fileName);

		try
		{
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(data);

			myOutWriter.close();

			fOut.flush();
			fOut.close();

			return file;
		}
		catch (IOException e)
		{
			log("File write failed: " + e.toString());
			return null;
		}
	}

	public static void showErrorAlertDialogGroupCall(String message, final boolean finish, final Activity activity){
		if(activity == null){
			return;
		}

		try{
			AlertDialog.Builder dialogBuilder = getCustomAlertBuilder(activity, activity.getString(R.string.general_error_word), message, null);
			dialogBuilder.setPositiveButton(
					activity.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (finish) {
								if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
									activity.finishAndRemoveTask();
								}else{
									activity.finish();
								}
							}
						}
					});
			dialogBuilder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (finish) {
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							activity.finishAndRemoveTask();
						}else{
							activity.finish();
						}
					}
				}
			});


			AlertDialog dialog = dialogBuilder.create();
			dialog.show();
			brandAlertDialog(dialog);
		}catch(Exception ex){
			Util.showToast(activity, message);
		}
	}

	/*
	 * Build error dialog
	 * @param message Message to display
	 * @param finish Should activity finish after dialog dismis
	 * @param activity Source activity
	 */
	public static void showErrorAlertDialog(String message, final boolean finish, final Activity activity){
		if(activity == null){
			return;
		}
		
		try{ 
			AlertDialog.Builder dialogBuilder = getCustomAlertBuilder(activity, activity.getString(R.string.general_error_word), message, null);
			dialogBuilder.setPositiveButton(
				activity.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (finish) {
							activity.finish();
						}
					}
				});
			dialogBuilder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (finish) {
						activity.finish();
					}
				}
			});
		
		
			AlertDialog dialog = dialogBuilder.create();
			dialog.show(); 
			brandAlertDialog(dialog);
		}
		catch(Exception ex){
			Util.showToast(activity, message); 
		}
	}
	
	public static void showErrorAlertDialog(MegaError error, Activity activity) {
		showErrorAlertDialog(error.getErrorString(), false, activity);
	}
	
	public static void showErrorAlertDialog(int errorCode, Activity activity) {
		showErrorAlertDialog(MegaError.getErrorString(errorCode), false, activity);
	}

	public static int countMatches(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);

		int count = 0;
		int pos = 0;
		while (matcher.find(pos))
		{
			count++;
			pos = matcher.start() + 1;
		}

		return count;
	}
	
	public static boolean showMessageRandom(){
		Random r = new Random(System.currentTimeMillis());
		int randomInt = r.nextInt(100) + 1;
		
		if(randomInt<5){
			return true;
		}
		else{
			return false;
		}
//		return true;
	}
	
	public static int getFilesCount(File file) {
		File[] files = file.listFiles();
		int count = 0;
		for (File f : files)
			if (f.isDirectory())
				count += getFilesCount(f)+1;
			else
				count++;

		return count;
	}

    public static String toCDATA(String src) {
        if (src != null) {
            //solution from web client
            src = src.replaceAll("&","&amp;")
                    .replaceAll("\"","&quot;")
                    .replaceAll("'","&#39;")
                    .replaceAll("<","&lt;")
                    .replaceAll(">","&gt;");
            //another solution
//            src = src.replaceAll("]]>", "] ]>");
//            src = "<![CDATA[" + src + "]]>";
        }
        return src;
    }
	
	public static long getFreeExternalMemorySize() {
		log("getFreeExternalMemorySize");
        String secStore = System.getenv("SECONDARY_STORAGE");
        File path = new File(secStore);
        log("getFreeExternalMemorySize: "+path.getAbsolutePath());
        
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
	}
	
	public static String getExternalCardPath() {
		
        String secStore = System.getenv("SECONDARY_STORAGE");
        if (secStore == null){
        	return null;
        }
        else{
        	if (secStore.compareTo("") == 0){
        		return null;
        	}
	        log("getExternalCardPath secStore: "+secStore);
	        File path = new File(secStore);
	        log("getFreeSize: "+path.getUsableSpace());
	        if(path.getUsableSpace()>0)
	        {
	        	return path.getAbsolutePath();
	        }
        }

        return null;
	}

	public static String getNumberItemChildren(File file){
		File[] list = file.listFiles();
		int count = 0;
		if(list!=null){
			count =  list.length;
		}

		String numChilden = count + " " + context.getResources().getQuantityString(R.plurals.general_num_items, count);

		return numChilden;
	}
	
	public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
           case ExifInterface.ORIENTATION_ROTATE_90:
               matrix.setRotate(90);
               break;
           case ExifInterface.ORIENTATION_TRANSVERSE:
               matrix.setRotate(-90);
               matrix.postScale(-1, 1);
               break;
           case ExifInterface.ORIENTATION_ROTATE_270:
               matrix.setRotate(-90);
               break;
           default:
               return bitmap;
        }
	     
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null; 
            }
            return bmRotated;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    
	    int inSampleSize = 1;
	    
	    if (height > reqHeight || width > reqWidth) {

	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;

	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

	    return inSampleSize;
	}
	
	/*
	 * Build custom dialog
	 * @param activity Source activity
	 * @param title Dialog title
	 * @param message To display, could be null
	 * @param view Custom view to display in the dialog
	 */
	public static AlertDialog.Builder getCustomAlertBuilder(Activity activity, String title, String message, View view) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		ViewGroup customView = getCustomAlertView(activity, title, message);
		if (view != null) {
			customView.addView(view);
		}
		dialogBuilder.setView(customView);
		dialogBuilder.setInverseBackgroundForced(true);
		return dialogBuilder;
	}

	/*
	 * Create custom alert dialog view
	 */
	private static ViewGroup getCustomAlertView(Activity activity, String title, String message) {
		View customView = activity.getLayoutInflater().inflate(R.layout.alert_dialog, null);

		TextView titleView = (TextView)customView.findViewById(R.id.dialog_title);
		titleView.setText(title);

		TextView messageView = (TextView)customView.findViewById(R.id.message);
		if (message == null) {
			messageView.setVisibility(View.GONE);
		} else {
			messageView.setText(message);
		}
		return (ViewGroup)customView;
	}

	/*
	 * Show Toast message with resId
	 */
	public static void showToast(Context context, int resId) {
		try { Toast.makeText(context, resId, Toast.LENGTH_LONG).show(); } catch(Exception ex) {};
	}

	/*
	 * Show Toast message with String
	 */
	public static void showToast(Context context, String message) {
		try { Toast.makeText(context, message, Toast.LENGTH_LONG).show(); } catch(Exception ex) {};
	}
	
	public static float getScaleW(DisplayMetrics outMetrics, float density){
		
		float scale = 0;
		
		float dpWidth  = outMetrics.widthPixels / density;		
	    scale = dpWidth / dpWidthAbs;	    
		
	    return scale;
	}
	
	public static float getScaleH(DisplayMetrics outMetrics, float density){
		
		float scale = 0;
		
		float dpHeight  = outMetrics.heightPixels / density;		
	    scale = dpHeight / dpHeightAbs;	    
		
	    return scale;
	}
	
	public static int px2dp (float dp, DisplayMetrics outMetrics){
	
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, outMetrics));
	}
	
	/*
	 * AES encryption
	 */
	public static byte[] aes_encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}
	
	/*
	 * AES decryption
	 */
	public static byte[] aes_decrypt(byte[] raw, byte[] encrypted)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}
	
	/*
	 * Check is device on WiFi
	 */
	public static boolean isOnWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	static public boolean isOnline(Context context) {
	    if(context == null) return true;
		
		ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static String getSizeString(long size){
		String sizeString = "";
		DecimalFormat decf = new DecimalFormat("###.##");

		float KB = 1024;
		float MB = KB * 1024;
		float GB = MB * 1024;
		float TB = GB * 1024;
		
		if (size < KB){
			sizeString = size + " " + context.getString(R.string.label_file_size_byte);
		}
		else if (size < MB){
			sizeString = decf.format(size/KB) + " " + context.getString(R.string.label_file_size_kilo_byte);
		}
		else if (size < GB){
			sizeString = decf.format(size/MB) + " " + context.getString(R.string.label_file_size_mega_byte);
		}
		else if (size < TB){
			sizeString = decf.format(size/GB) + " " + context.getString(R.string.label_file_size_giga_byte);
		}
		else{
			sizeString = decf.format(size/TB) + " " + context.getString(R.string.label_file_size_tera_byte);
		}
		
		return sizeString;
	}
	
	private static long getDirSize(File dir) {

        long size = 0;
        if(dir==null){
        	return -1;
        }
        
        File[] files = dir.listFiles();

		if(files !=null){
			for (File file : files) {
				if (file.isFile()) {
					size += file.length();
				}
				else{
					size += getDirSize(file);
				}
			}
			return size;
		}
		log("Files is NULL");
        return size;
    }
	
    private static void cleanDir(File dir, long bytes) {

        long bytesDeleted = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            bytesDeleted += file.length();
            file.delete();

            if (bytesDeleted >= bytes) {
                break;
            }
        }
    }
    
    private static void cleanDir(File dir) {
        File[] files = dir.listFiles();

		if(files !=null){
			for (File file : files) {

				if (file.isFile()) {
					file.delete();
				}
				else{
					cleanDir(file);
					file.delete();
				}
			}
		}
		else{
			log("Files is NULL");
		}
    }
    
    public static String getCacheSize(Context context){
    	log("getCacheSize");
    	File cacheIntDir = context.getCacheDir();
    	File cacheExtDir = context.getExternalCacheDir();

		if(cacheIntDir!=null){
			log("Path to check internal: "+cacheIntDir.getAbsolutePath());
		}
    	long size = getDirSize(cacheIntDir)+getDirSize(cacheExtDir);    	
    	
    	String sizeCache = getSizeString(size);
    	return sizeCache; 
    }
	
    public static void clearCache(Context context){
    	log("clearCache");
    	File cacheIntDir = context.getCacheDir();
    	File cacheExtDir = context.getExternalCacheDir();

    	cleanDir(cacheIntDir);
    	cleanDir(cacheExtDir);    	
    }
    
    public static String getOfflineSize(Context context){
    	log("getOfflineSize");
    	File offline = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offlineDIR);
    	long size = 0;
    	if(offline.exists()){
    		size = getDirSize(offline);    	
        	
        	String sizeOffline = getSizeString(size);
        	return sizeOffline; 
    	}
    	else{
    		return getSizeString(0);
    	}        	
    }
	
    public static void clearOffline(Context context){
    	log("clearOffline");
    	File offline = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offlineDIR);
    	if(offline.exists()){
        	cleanDir(offline);
        	offline.delete();
    	}
    }
    
	public static String getDateString(long date){
		DateFormat datf = DateFormat.getDateTimeInstance();
		String dateString = "";
		
		dateString = datf.format(new Date(date*1000));
		
		return dateString;
	}

	public static void setContext(Context c){
		context = c;
	}

	public static void setDBH(DatabaseHandler d){
		dbH = d;
	}

	public static void setFileLoggerSDK(boolean fL){
		fileLoggerSDK = fL;
	}

	public static boolean getFileLoggerSDK(){
		return fileLoggerSDK;
	}

	public static void setFileLoggerKarere(boolean fL){
		fileLoggerKarere = fL;
	}

    public static boolean getFileLoggerKarere(){
        return fileLoggerKarere;
    }

	/*
	 * Global log handler
	 */
	public static void log(String origin, String message) {
		MegaApiAndroid.log(MegaApiAndroid.LOG_LEVEL_WARNING, "[clientApp] "+ origin + ": " + message, origin);

//		try {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String currentDateandTime = sdf.format(new Date());
//
//			message = "(" + currentDateandTime + ") - " + message;
//		}
//		catch (Exception e){}

//		File logFile=null;
//		if (DEBUG) {
//			MegaApiAndroid.log(MegaApiAndroid.LOG_LEVEL_INFO, message, origin);
//		}

//		if (fileLogger) {
//			//Send the log to a file
//
//			String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + logDIR + "/";
//			//			String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+logDIR+"/log.txt";
//			File dirFile = new File(dir);
//			if (!dirFile.exists()) {
//				dirFile.mkdirs();
//				logFile = new File(dirFile, "log.txt");
//				if (!logFile.exists()) {
//					try {
//						logFile.createNewFile();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			} else {
//				logFile = new File(dirFile, "log.txt");
//				if (!logFile.exists()) {
//					try {
//						logFile.createNewFile();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//
//			if (logFile != null && logFile.exists()) {
//				appendStringToFile(origin + ": " + message + "\n", logFile);
//			}
//		}
	}

	public static boolean appendStringToFile(final String appendContents, final File file) {
		boolean result = false;
		try {
			if (file != null && file.canWrite()) {
				file.createNewFile(); // ok if returns false, overwrite
				Writer out = new BufferedWriter(new FileWriter(file, true), 1024);
				out.write(appendContents);
				out.close();
				result = true;
			}
		} catch (IOException e) {
			//   Log.e(Constants.LOG_TAG, "Error appending string data to file " + e.getMessage(), e);
		}
		return result;
	}
	
	public static void brandAlertDialog(AlertDialog dialog) {
	    try {
	        Resources resources = dialog.getContext().getResources();

	        int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");

	        TextView alertTitle = (TextView) dialog.getWindow().getDecorView().findViewById(alertTitleId);
	        if (alertTitle != null){	        	
	        	alertTitle.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.mega)); // change title text color
	        }

	        int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
	        View titleDivider = dialog.getWindow().getDecorView().findViewById(titleDividerId);
	        if (titleDivider != null){
	        	titleDivider.setBackgroundColor(ContextCompat.getColor(dialog.getContext(), R.color.mega)); // change divider color
	        }
	    } catch (Exception ex) {
	    	Toast.makeText(dialog.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
	        ex.printStackTrace();
	    }
	}
	
	public static String getLocalFile(Context context, String fileName, long fileSize,
			String destDir)
	{
		Cursor cursor = null;
		try 
		{
			if(MimeTypeList.typeForName(fileName).isImage())
			{
				final String[] projection = { MediaStore.Images.Media.DATA };
				final String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ? AND " + MediaStore.Images.Media.SIZE + " = ?";
				final String[] selectionArgs = { fileName, String.valueOf(fileSize) };
				
		        cursor = context.getContentResolver().query(
		                        Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
		                        selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
			        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			        String path =  cursor.getString(dataColumn);
			        cursor.close();
			        cursor = null;
			        if(new File(path).exists()){
			        	return path;
			        }
				}
				if(cursor != null) cursor.close();
			
				cursor = context.getContentResolver().query(
	                    Images.Media.INTERNAL_CONTENT_URI, projection, selection,
	                    selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
			        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			        String path =  cursor.getString(dataColumn);
			        cursor.close();
			        cursor = null;
			        if(new File(path).exists()) return path;
				}
				if(cursor != null) cursor.close();
			}
			else if(MimeTypeList.typeForName(fileName).isVideoReproducible())
			{
				final String[] projection = { MediaStore.Video.Media.DATA };
				final String selection = MediaStore.Video.Media.DISPLAY_NAME + " = ? AND " + MediaStore.Video.Media.SIZE + " = ?";
				final String[] selectionArgs = { fileName, String.valueOf(fileSize) };
				
		        cursor = context.getContentResolver().query(
                        Video.Media.EXTERNAL_CONTENT_URI, projection, selection,
                        selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
			        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			        String path =  cursor.getString(dataColumn);
			        cursor.close();
			        cursor = null;
			        if(new File(path).exists()) return path;
				}
				if(cursor != null) cursor.close();
			
				cursor = context.getContentResolver().query(
		                Video.Media.INTERNAL_CONTENT_URI, projection, selection,
		                selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
			        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			        String path =  cursor.getString(dataColumn);
			        cursor.close();
			        cursor = null;
			        if(new File(path).exists()) return path;
				}
				if(cursor != null) cursor.close();
			}
			else if (MimeTypeList.typeForName(fileName).isAudio()) {
				final String[] projection = { MediaStore.Audio.Media.DATA };
				final String selection = MediaStore.Audio.Media.DISPLAY_NAME + " = ? AND " + MediaStore.Audio.Media.SIZE + " = ?";
				final String[] selectionArgs = { fileName, String.valueOf(fileSize) };

				cursor = context.getContentResolver().query(
						Video.Media.EXTERNAL_CONTENT_URI, projection, selection,
						selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
					int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
					String path =  cursor.getString(dataColumn);
					cursor.close();
					cursor = null;
					if(new File(path).exists()) return path;
				}
				if(cursor != null) cursor.close();

				cursor = context.getContentResolver().query(
						Video.Media.INTERNAL_CONTENT_URI, projection, selection,
						selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
					int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
					String path =  cursor.getString(dataColumn);
					cursor.close();
					cursor = null;
					if(new File(path).exists()) return path;
				}
				if(cursor != null) cursor.close();
			}
		} catch (Exception e) 
		{
			if(cursor != null) cursor.close();
		}
		
		//Not found, searching in the download folder
		if(destDir != null)
		{
			File file = new File(destDir, fileName);
			if(file.exists() && (file.length() == fileSize))
				return file.getAbsolutePath();
		}
		return null;
	}
	
	/*
	 * Check is file belongs to the app
	 */
	public static boolean isLocal(Context context, File file) {
		File tmp = null;
		if (context.getExternalCacheDir() != null){
			tmp = new File (context.getExternalCacheDir(), "tmp");
		}
		else{
			tmp = context.getDir("tmp", 0);
		}
			
		return file.getAbsolutePath().contains(tmp.getParent());
	}
	
	/*
	 * Check is file belongs to the app and temporary
	 */
	public static boolean isLocalTemp(Context context, File file) {
		return isLocal(context, file) && file.getAbsolutePath().endsWith(".tmp");
	}
	
	/*
	 * Get localized progress size
	 */
	public static String getProgressSize(Context context, long progress,
			long size) {
		return String.format("%s/%s",
				Formatter.formatFileSize(context, progress),
				Formatter.formatFileSize(context, size));
	}
	
	/*
	 * Set alpha transparency for view
	 */
	@SuppressLint("NewApi")
	public static void setViewAlpha(View view, float alpha) {
		if (Build.VERSION.SDK_INT >= 11) {
			view.setAlpha(alpha);
		} else {
			AlphaAnimation anim = new AlphaAnimation(alpha, alpha);
			anim.setDuration(0);
			anim.setFillAfter(true);
			view.startAnimation(anim);
		}
	}
	
	/*
	 * Make part of the string bold
	 */
	public static SpannableStringBuilder makeBold(String text, String boldText) {
		SpannableStringBuilder sb = new SpannableStringBuilder(text);
		StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
		sb.setSpan(bss, text.length() - boldText.length(), text.length(),
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		return sb;
	}
	
	/*
	 * Delete file if it belongs to the app
	 */
	public static void deleteIfLocal(Context context, File file) {
		if (isLocal(context, file) && file.exists()) {
			log("delete");
			file.delete();
		}
	}
	
	public static void copyFile(File source, File dest) throws IOException{
		log("copyFile");

		if (!source.getAbsolutePath().equals(dest.getAbsolutePath())){
			FileChannel inputChannel = null;
			FileChannel outputChannel = null;
			FileInputStream inputStream = new FileInputStream(source);
			FileOutputStream outputStream = new FileOutputStream(dest);
			inputChannel = inputStream.getChannel();
			outputChannel = outputStream.getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());    
			inputChannel.close();
			outputChannel.close();
			inputStream.close();
			outputStream.close();
		}
	}
	
	/*
	 * Start activity to open URL
	 */
	public static void openUrl(Context context, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(intent);
	}
	
	public static void deleteFolderAndSubfolders(Context context, File f) throws IOException {
		
		if (f != null){
			log("deleteFolderAndSubfolders: "+ f.getAbsolutePath());
			if (f.isDirectory()) {
				if (f.listFiles() != null){
					for (File c : f.listFiles()){
						deleteFolderAndSubfolders(context, c);
					}
				}
			}
			
			if (!f.delete()){
				throw new FileNotFoundException("Failed to delete file: " + f);
			}
			else{
				try {
					Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					File fileToDelete = new File(f.getAbsolutePath());
					Uri contentUri;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						contentUri = FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", fileToDelete);
					}
					else{
						contentUri = Uri.fromFile(fileToDelete);
					}
					mediaScanIntent.setData(contentUri);
					mediaScanIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					context.sendBroadcast(mediaScanIntent);
				}
				catch (Exception e){ log ("Exception while deleting media scanner file: " + e.getMessage()); }
		    
			}
		}
	}
	
	public static String getSpeedString (long speed){
		String speedString = "";
		double speedDouble = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		
		if (speed > 1024){
			if (speed > 1024*1024){
				if (speed > 1024*1024*1024){
					speedDouble = speed / (1024.0*1024.0*1024.0);
					speedString = df.format(speedDouble) + " GB/s";
				}
				else{
					speedDouble = speed / (1024.0*1024.0);
					speedString = df.format(speedDouble) + " MB/s";
				}
			}
			else{
				speedDouble = speed / 1024.0;
				speedString = df.format(speedDouble) + " KB/s";	
			}
		}
		else{
			speedDouble = speed;
			speedString = df.format(speedDouble) + " B/s";
		}
		
		return speedString;
	}
	
	public static String getPhotoSyncName (long timeStamp, String fileName){
		String photoSyncName = null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeStamp);
		
		String extension = "";
		String[] s = fileName.split("\\.");
		if (s != null){
			if (s.length > 0){
				extension = s[s.length-1];
			}
		}
				
		String year;
		String month;
		String day;
		String hour;
		String minute;
		String second;
		
		year = cal.get(Calendar.YEAR) + "";
		month = (cal.get(Calendar.MONTH)+1) + "";
		if ((cal.get(Calendar.MONTH) + 1) < 10){
			month = "0" + month;
		}
		
		day = cal.get(Calendar.DAY_OF_MONTH) + "";
		if (cal.get(Calendar.DAY_OF_MONTH) < 10){
			day = "0" + day;
		}
		
		hour = cal.get(Calendar.HOUR_OF_DAY) + "";
		if (cal.get(Calendar.HOUR_OF_DAY) < 10){
			hour = "0" + hour;
		}
		
		minute = cal.get(Calendar.MINUTE) + "";
		if (cal.get(Calendar.MINUTE) < 10){
			minute = "0" + minute;
		}
		
		second = cal.get(Calendar.SECOND) + "";
		if (cal.get(Calendar.SECOND) < 10){
			second = "0" + second;
		}

		photoSyncName = year + "-" + month + "-" + day + " " + hour + "." + minute + "." + second + "." + extension;
		
		return photoSyncName;
	}
	
	public static String getPhotoSyncNameWithIndex (long timeStamp, String fileName, int photoIndex){
		
		if (photoIndex == 0){
			return getPhotoSyncName(timeStamp, fileName);
		}
		
		String photoSyncName = null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeStamp);
		
		String extension = "";
		String[] s = fileName.split("\\.");
		if (s != null){
			if (s.length > 0){
				extension = s[s.length-1];
			}
		}
				
		String year;
		String month;
		String day;
		String hour;
		String minute;
		String second;
		
		year = cal.get(Calendar.YEAR) + "";
		month = (cal.get(Calendar.MONTH)+1) + "";
		if ((cal.get(Calendar.MONTH) + 1) < 10){
			month = "0" + month;
		}
		
		day = cal.get(Calendar.DAY_OF_MONTH) + "";
		if (cal.get(Calendar.DAY_OF_MONTH) < 10){
			day = "0" + day;
		}
		
		hour = cal.get(Calendar.HOUR_OF_DAY) + "";
		if (cal.get(Calendar.HOUR_OF_DAY) < 10){
			hour = "0" + hour;
		}
		
		minute = cal.get(Calendar.MINUTE) + "";
		if (cal.get(Calendar.MINUTE) < 10){
			minute = "0" + minute;
		}
		
		second = cal.get(Calendar.SECOND) + "";
		if (cal.get(Calendar.SECOND) < 10){
			second = "0" + second;
		}

		photoSyncName = year + "-" + month + "-" + day + " " + hour + "." + minute + "." + second + "_" + photoIndex + "." + extension;
		
		return photoSyncName;
	}
	
	public static int getNumberOfNodes (MegaNode parent, MegaApiAndroid megaApi){
		int numberOfNodes = 0;
		
		ArrayList<MegaNode> children = megaApi.getChildren(parent);
		for (int i=0; i<children.size(); i++){
			if (children.get(i).isFile()){
				numberOfNodes++;
			}
			else{
				numberOfNodes = numberOfNodes + getNumberOfNodes(children.get(i), megaApi);
			}
		}
		
		return numberOfNodes;
	}
	
	public static String getLocalIpAddress()
  {
		  try {
			  for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				  NetworkInterface intf = en.nextElement();
				  for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					  InetAddress inetAddress = enumIpAddr.nextElement();
					  if (!inetAddress.isLoopbackAddress()) {
						  return inetAddress.getHostAddress().toString();
					  }
				  }
			  }
		  } catch (Exception ex) {
			  log("Error IP Address: " + ex.toString());
		  }
		  return null;
   }
	
	@SuppressLint("InlinedApi") 
	public static boolean isCharging(Context context) {
		final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		if (Build.VERSION.SDK_INT < 17) {
			return status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB;
		} else {
			return status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB || status == BatteryManager.BATTERY_PLUGGED_WIRELESS;
		}
		
	}
	
	/** Returns the consumer friendly device name */
	public static String getDeviceName() {
	    final String manufacturer = Build.MANUFACTURER;
	    final String model = Build.MODEL;
	    if (model.startsWith(manufacturer)) {
	        return model;
	    }
	    if (manufacturer.equalsIgnoreCase("HTC")) {
	        // make sure "HTC" is fully capitalized.
	        return "HTC " + model;
	    }
	    return manufacturer + " " + model;
	}
	
	public static String getCountryCode(String countryString){
		String countryCode= "";
		
		countryCode = countryCodeDisplay.get(countryString);
		
		return countryCode;
	}
	
	public static ArrayList<String> getCountryList(Context context){
		ArrayList<String> countryCodes = new ArrayList<String>();
		
		countryCodes.add("US");
		countryCodes.add("GB");
		countryCodes.add("CA");
		countryCodes.add("AX");
		countryCodes.add("AF");
		countryCodes.add("AP");
		countryCodes.add("AL");
		countryCodes.add("DZ");
		countryCodes.add("AS");
		countryCodes.add("AD");
		countryCodes.add("AO");
		countryCodes.add("AI");
		countryCodes.add("AQ");
		countryCodes.add("AG");
		countryCodes.add("AR");
		countryCodes.add("AM");
		countryCodes.add("AW");
		countryCodes.add("AU");
		countryCodes.add("AT");
		countryCodes.add("AZ");
		countryCodes.add("BS");
		countryCodes.add("BH");
		countryCodes.add("BD");
		countryCodes.add("BB");
		countryCodes.add("BY");
		countryCodes.add("BE");
		countryCodes.add("BZ");
		countryCodes.add("BJ");
		countryCodes.add("BM");
		countryCodes.add("BT");
		countryCodes.add("BO");
		countryCodes.add("BA");
		countryCodes.add("BW");
		countryCodes.add("BV");
		countryCodes.add("BR");
		countryCodes.add("IO");
		countryCodes.add("BN");
		countryCodes.add("BG");
		countryCodes.add("BF");
		countryCodes.add("BI");
		countryCodes.add("KH");
		countryCodes.add("CM");
		countryCodes.add("CV");
		countryCodes.add("KY");
		countryCodes.add("CF");
		countryCodes.add("TD");
		countryCodes.add("CL");
		countryCodes.add("CN");
		countryCodes.add("CX");
		countryCodes.add("CC");
		countryCodes.add("CO");
		countryCodes.add("KM");
		countryCodes.add("CG");
		countryCodes.add("CD");
		countryCodes.add("CK");
		countryCodes.add("CR");
		countryCodes.add("CI");
		countryCodes.add("HR");
		countryCodes.add("CU");
		countryCodes.add("CY");
		countryCodes.add("CZ");
		countryCodes.add("DK");
		countryCodes.add("DJ");
		countryCodes.add("DM");
		countryCodes.add("DO");
		countryCodes.add("TL");
		countryCodes.add("EC");
		countryCodes.add("EG");
		countryCodes.add("SV");
		countryCodes.add("GQ");
		countryCodes.add("ER");
		countryCodes.add("EE");
		countryCodes.add("ET");
		countryCodes.add("FK");
		countryCodes.add("FO");
		countryCodes.add("FJ");
		countryCodes.add("FI");
		countryCodes.add("FR");
		countryCodes.add("GF");
		countryCodes.add("PF");
		countryCodes.add("TF");
		countryCodes.add("GA");
		countryCodes.add("GM");
		countryCodes.add("GE");
		countryCodes.add("DE");
		countryCodes.add("GH");
		countryCodes.add("GI");
		countryCodes.add("GR");
		countryCodes.add("GL");
		countryCodes.add("GD");
		countryCodes.add("GP");
		countryCodes.add("GU");
		countryCodes.add("GG");
		countryCodes.add("GT");
		countryCodes.add("GN");
		countryCodes.add("GW");
		countryCodes.add("GY");
		countryCodes.add("HT");
		countryCodes.add("HN");
		countryCodes.add("HK");
		countryCodes.add("HU");
		countryCodes.add("IS");
		countryCodes.add("IN");
		countryCodes.add("ID");
		countryCodes.add("IR");
		countryCodes.add("IQ");
		countryCodes.add("IE");
		countryCodes.add("IM");
		countryCodes.add("IL");
		countryCodes.add("IT");
		countryCodes.add("JM");
		countryCodes.add("JP");
		countryCodes.add("JE");
		countryCodes.add("JO");
		countryCodes.add("KZ");
		countryCodes.add("KE");
		countryCodes.add("KI");
		countryCodes.add("KW");
		countryCodes.add("KG");
		countryCodes.add("LA");
		countryCodes.add("LV");
		countryCodes.add("LB");
		countryCodes.add("LS");
		countryCodes.add("LR");
		countryCodes.add("LY");
		countryCodes.add("LI");
		countryCodes.add("LT");
		countryCodes.add("LU");
		countryCodes.add("MO");
		countryCodes.add("MK");
		countryCodes.add("MG");
		countryCodes.add("MW");
		countryCodes.add("MY");
		countryCodes.add("MV");
		countryCodes.add("ML");
		countryCodes.add("MT");
		countryCodes.add("MH");
		countryCodes.add("MQ");
		countryCodes.add("MR");
		countryCodes.add("MU");
		countryCodes.add("YT");
		countryCodes.add("MX");
		countryCodes.add("FM");
		countryCodes.add("MD");
		countryCodes.add("MC");
		countryCodes.add("MN");
		countryCodes.add("ME");
		countryCodes.add("MS");
		countryCodes.add("MA");
		countryCodes.add("MZ");
		countryCodes.add("MM");
		countryCodes.add("NA");
		countryCodes.add("NR");
		countryCodes.add("NP");
		countryCodes.add("NL");
		countryCodes.add("AN");
		countryCodes.add("NC");
		countryCodes.add("NZ");
		countryCodes.add("NI");
		countryCodes.add("NE");
		countryCodes.add("NG");
		countryCodes.add("NU");
		countryCodes.add("NF");
		countryCodes.add("KP");
		countryCodes.add("MP");
		countryCodes.add("NO");
		countryCodes.add("OM");
		countryCodes.add("PK");
		countryCodes.add("PW");
		countryCodes.add("PS");
		countryCodes.add("PA");
		countryCodes.add("PG");
		countryCodes.add("PY");
		countryCodes.add("PE");
		countryCodes.add("PH");
		countryCodes.add("PN");
		countryCodes.add("PL");
		countryCodes.add("PT");
		countryCodes.add("PR");
		countryCodes.add("QA");
		countryCodes.add("RE");
		countryCodes.add("RO");
		countryCodes.add("RU");
		countryCodes.add("RW");
		countryCodes.add("MF");
		countryCodes.add("KN");
		countryCodes.add("LC");
		countryCodes.add("VC");
		countryCodes.add("WS");
		countryCodes.add("SM");
		countryCodes.add("ST");
		countryCodes.add("SA");
		countryCodes.add("SN");
		countryCodes.add("RS");
		countryCodes.add("SC");
		countryCodes.add("SL");
		countryCodes.add("SG");
		countryCodes.add("SK");
		countryCodes.add("SI");
		countryCodes.add("SB");
		countryCodes.add("SO");
		countryCodes.add("ZA");
		countryCodes.add("GS");
		countryCodes.add("KR");
		countryCodes.add("SS");
		countryCodes.add("ES");
		countryCodes.add("LK");
		countryCodes.add("SH");
		countryCodes.add("PM");
		countryCodes.add("SD");
		countryCodes.add("SR");
		countryCodes.add("SJ");
		countryCodes.add("SZ");
		countryCodes.add("SE");
		countryCodes.add("CH");
		countryCodes.add("SY");
		countryCodes.add("TW");
		countryCodes.add("TJ");
		countryCodes.add("TZ");
		countryCodes.add("TH");
		countryCodes.add("TG");
		countryCodes.add("TK");
		countryCodes.add("TO");
		countryCodes.add("TT");
		countryCodes.add("TN");
		countryCodes.add("TR");
		countryCodes.add("TM");
		countryCodes.add("TC");
		countryCodes.add("TV");
		countryCodes.add("UG");
		countryCodes.add("UA");
		countryCodes.add("AE");
		countryCodes.add("UM");
		countryCodes.add("UY");
		countryCodes.add("UZ");
		countryCodes.add("VU");
		countryCodes.add("VA");
		countryCodes.add("VE");
		countryCodes.add("VN");
		countryCodes.add("VG");
		countryCodes.add("VI");
		countryCodes.add("WF");
		countryCodes.add("EH");
		countryCodes.add("YE");
		countryCodes.add("ZM");
		countryCodes.add("ZW");
		
		Locale currentLocale = Locale.getDefault();
//		Toast.makeText(context, currentLocale.getLanguage(), Toast.LENGTH_LONG).show();
		
		countryCodeDisplay = new HashMap<String, String>();
		
		ArrayList<String> countryList = new ArrayList<String>();
		for (int i=0;i<countryCodes.size();i++){
			Locale l = new Locale (currentLocale.getLanguage(), countryCodes.get(i));
			String country = l.getDisplayCountry();
			if (country.length() > 0 && !countryList.contains(country)){
				countryList.add(country);
				countryCodeDisplay.put(country, countryCodes.get(i));				
			}
		}
		
//		Toast.makeText(context, "CONTRYLIST: " + countryList.size() + "___ " + countryCodes.size(), Toast.LENGTH_LONG).show();
		Collections.sort(countryList, String.CASE_INSENSITIVE_ORDER);
		countryList.add(0, context.getString(R.string.country_cc));
		
		return countryList;
		
//		Locale[] locale = Locale.getAvailableLocales();
//		String country;
//		Toast.makeText(context, "LOCALEAAAAAA: " + locale.length, Toast.LENGTH_LONG).show();
//		for (Locale loc : locale){
//			country = loc.getCountry();
//			if (country.length() > 0 && !countryList.contains(country)){
//				countryList.add(country);
//			}
//		}
//		Toast.makeText(context, "CONTRYLIST: " + countryList.size(), Toast.LENGTH_LONG).show();
//				
//		Collections.sort(countryList, String.CASE_INSENSITIVE_ORDER);
//		countryList.add(0, context.getString(R.string.country_cc));
//		
//		return countryList;
		
	}
	
	public static ArrayList<String> getMonthListInt(Context context){
		ArrayList<String> monthList = new ArrayList<String>();
		
		monthList.add(context.getString(R.string.month_cc));
		
		monthList.add("01");
		monthList.add("02");
		monthList.add("03");
		monthList.add("04");
		monthList.add("05");
		monthList.add("06");
		monthList.add("07");
		monthList.add("08");
		monthList.add("09");
		monthList.add("10");
		monthList.add("11");
		monthList.add("12");
		
		return monthList;
	}
	
	public static ArrayList<String> getYearListInt(Context context){
		ArrayList<String> yearList = new ArrayList<String>();
		
		yearList.add(context.getString(R.string.year_cc));
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		
		for (int i=year;i<=(year+20);i++){
			yearList.add(i+"");
		}
		
		return yearList;
	}


	public static String getSubtitleDescription(ArrayList<MegaNode> nodes){
		int numFolders = 0;
		int numFiles = 0;

		for (int i=0;i<nodes.size();i++){
			MegaNode c = nodes.get(i);
			if (c.isFolder()){
				numFolders++;
			}
			else{
				numFiles++;
			}
		}

		String info = "";
		if (numFolders > 0){
			info = numFolders +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		else {
			if (numFiles == 0){
				info = context.getString(R.string.no_folders_shared);
			}
			else{
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}

		return info;
	}

	public static BitSet convertToBitSet(long value) {
	    BitSet bits = new BitSet();
	    int index = 0;
	    while (value != 0L) {
	      if (value % 2L != 0) {
	        bits.set(index);
	      }
	      ++index;
	      value = value >>> 1;
	    }
	    return bits;
	}
	
	public static boolean checkBitSet(BitSet paymentBitSet, int position){
		log("checkBitSet");
		if (paymentBitSet != null){
			if (paymentBitSet.get(position)){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public static long getLastPublicHandle(MegaAttributes attributes){
		long lastPublicHandle = -1;

		if (attributes != null){
			if (attributes.getLastPublicHandle() != null){
				try{
					long currentTime = System.currentTimeMillis()/1000;
					long lastPublicHandleTimeStamp = Long.parseLong(attributes.getLastPublicHandleTimeStamp());
					log("currentTime: " + currentTime + " _ " + lastPublicHandleTimeStamp);
					if ((currentTime - lastPublicHandleTimeStamp) < 86400){
						if (Long.parseLong(attributes.getLastPublicHandle()) != -1){
							lastPublicHandle = Long.parseLong(attributes.getLastPublicHandle());
						}
					}
				}
				catch (Exception e){
					lastPublicHandle = -1;
				}
			}
		}

		return lastPublicHandle;
	}
	
	public static boolean isPaymentMethod(BitSet paymentBitSet, int plan){
		
		boolean r = false;
		if (paymentBitSet != null){
			if (!Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD)){
				r = true;
			}
			if (!Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_GOOGLE_WALLET)){
				r = true;
			}
			if (!Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_FORTUMO)){
				if (plan == 4){
					r = true;
				}
			}
			if (!Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CENTILI)){
				if (plan == 4){
					r = true;
				}
			}
		}
		
		return r;
	}
	
	public static int scaleHeightPx(int px, DisplayMetrics metrics){
		int myHeightPx = metrics.heightPixels;
		
		return px*myHeightPx/548; //Based on Eduardo's measurements				
	}
	
	public static int scaleWidthPx(int px, DisplayMetrics metrics){
		int myWidthPx = metrics.widthPixels;
		
		return px*myWidthPx/360; //Based on Eduardo's measurements		
		
	}
	
	public static boolean isVideoFile(String path) {
		log("isVideoFile: "+path);
		try{
			String mimeType = URLConnection.guessContentTypeFromName(path);
		    return mimeType != null && mimeType.indexOf("video") == 0;
		}
		catch(Exception e){
			log("Exception: "+e.getMessage());
			return false;
		}	    
	}

	/*
	 * Validate email
	 */
	public static String getEmailError(String value, Context context) {
		log("getEmailError");
		if (value.length() == 0) {
			return context.getString(R.string.error_enter_email);
		}
		if (!Constants.EMAIL_ADDRESS.matcher(value).matches()) {
			return context.getString(R.string.error_invalid_email);
		}
		return null;
	}

	public static int getAvatarTextSize (float density){
		float textSize = 0.0f;

		if (density > 3.0){
			textSize = density * (DisplayMetrics.DENSITY_XXXHIGH / 72.0f);
		}
		else if (density > 2.0){
			textSize = density * (DisplayMetrics.DENSITY_XXHIGH / 72.0f);
		}
		else if (density > 1.5){
			textSize = density * (DisplayMetrics.DENSITY_XHIGH / 72.0f);
		}
		else if (density > 1.0){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_HIGH / 72.0f);
		}
		else if (density > 0.75){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_MEDIUM / 72.0f);
		}
		else{
			textSize = density * (72.0f / DisplayMetrics.DENSITY_LOW / 72.0f);
		}

		return (int)textSize;
	}

	public static void showAlert(Context context, String message, String title) {
		log("showAlert");
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if(title!=null){
			builder.setTitle(title);
		}
		builder.setMessage(message);
		builder.setPositiveButton("OK",null);
		builder.show();
	}

	public static long calculateTimestampMinDifference(String timeStamp) {
		log("calculateTimestampDifference");

		Long actualTimestamp = System.currentTimeMillis()/1000;

		Long oldTimestamp = Long.parseLong(timeStamp);

		Long difference = actualTimestamp - oldTimestamp;

		difference = difference/60;

		return difference;
	}

	public static int getVersion(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static boolean isFile (String path){
		if (path == null) {
			path = "";
		}
		String fixedName = path.trim().toLowerCase();
		String extension = null;
		int index = fixedName.lastIndexOf(".");
		if((index != -1) && ((index+1)<fixedName.length())) {
			extension = fixedName.substring(index + 1);
		}

		if(extension!=null){
			return true;
		}
		else{
			return false;
		}
	}

	public static long calculateTimestamp(String time)
	{
		log("calculateTimestamp: "+time);
		long unixtime;
		DateFormat dfm = new SimpleDateFormat("yyyyMMddHHmm");
		dfm.setTimeZone( TimeZone.getDefault());//Specify your timezone
		try
		{
			unixtime = dfm.parse(time).getTime();
			unixtime=unixtime/1000;
			return unixtime;
		}
		catch (ParseException e)
		{
			log("ParseException!!!");
		}
		return 0;
	}

	public static Calendar calculateDateFromTimestamp (long timestamp){
		log("calculateTimestamp: "+timestamp);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp*1000);
		log("calendar: "+cal.get(Calendar.YEAR)+ " "+cal.get(Calendar.MONTH));
		return cal;
	}

	public static boolean isChatEnabled (){
		log("isChatEnabled");
		if (dbH == null){
			dbH = DatabaseHandler.getDbHandler(context);
		}
		ChatSettings chatSettings = dbH.getChatSettings();
		boolean chatEnabled;

		if(chatSettings!=null){
			if(chatSettings.getEnabled()!=null){
				chatEnabled = Boolean.parseBoolean(chatSettings.getEnabled());
				log("A - chatEnabled: " + chatEnabled);
				return chatEnabled;
			}
			else{
				chatEnabled=true;
				log("B - chatEnabled: " + chatEnabled);
				return chatEnabled;
			}
		}
		else{
			chatEnabled=true;
			log("C - chatEnabled: " + chatEnabled);
			return chatEnabled;
		}
	}

	public static void resetAndroidLogger(){

		MegaApiAndroid.addLoggerObject(new AndroidLogger(AndroidLogger.LOG_FILE_NAME, Util.getFileLoggerSDK()));
		MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);

		boolean fileLogger = false;

		if (dbH == null){
			dbH = DatabaseHandler.getDbHandler(context);
		}

		if (dbH != null) {
			MegaAttributes attrs = dbH.getAttributes();
			if (attrs != null) {
				if (attrs.getFileLoggerSDK() != null) {
					try {
						fileLogger = Boolean.parseBoolean(attrs.getFileLoggerSDK());
					} catch (Exception e) {
						fileLogger = false;
					}
				} else {
					fileLogger = false;
				}
			} else {
				fileLogger = false;
			}
		}

		if (Util.DEBUG){
			MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
		}
		else {
			setFileLoggerSDK(fileLogger);
			if (fileLogger) {
				MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
			} else {
				MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_FATAL);
			}
		}
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap) {
		final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);

		final int color = Color.RED;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		bitmap.recycle();

		return output;
	}

	//restrict the scale factor to below 1.1 to allow user to have some level of freedom and also prevent ui issues
	public static void setAppFontSize(Activity activity) {
		float scale = activity.getResources().getConfiguration().fontScale;
		log("system font size scale is " + scale);

		float newScale;

		if (scale <= 1.1) {
			newScale = scale;
		} else {
			newScale = (float) 1.1;
		}

		log("new font size new scale is " + newScale);
		Configuration configuration = activity.getResources().getConfiguration();
		configuration.fontScale = newScale;

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		metrics.scaledDensity = configuration.fontScale * metrics.density;
		activity.getBaseContext().getResources().updateConfiguration(configuration, metrics);
	}
    
    //reduce font size for scale mode to prevent title and subtitle overlap
    public static SpannableString adjustForLargeFont(String original) {
        float scale = context.getResources().getConfiguration().fontScale;
        if(scale > 1){
            scale = (float)0.9;
        }
        SpannableString spannableString = new SpannableString(original);
        spannableString.setSpan(new RelativeSizeSpan(scale),0, original.length(),0);
        return spannableString;
    }

	public static Drawable mutateIcon (Context context, int idDrawable, int idColor) {

		Drawable icon = ContextCompat.getDrawable(context, idDrawable);
		icon = icon.mutate();
		icon.setColorFilter(ContextCompat.getColor(context, idColor), PorterDuff.Mode.MULTIPLY);

		return icon;
	}

	public static Drawable mutateIconSecondary(Context context, int idDrawable, int idColor) {
		Drawable icon = ContextCompat.getDrawable(context, idDrawable);
		icon = icon.mutate();
		icon.setColorFilter(ContextCompat.getColor(context, idColor), PorterDuff.Mode.SRC_ATOP);

		return icon;
	}

	//Notice user that any transfer prior to login will be destroyed
	public static void checkPendingTransfer(MegaApiAndroid megaApi, Context context, final AbortPendingTransferCallback callback){
		if(megaApi.getNumPendingDownloads() > 0 || megaApi.getNumPendingUploads() > 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);

			if(context instanceof ManagerActivityLollipop){
				log("Show dialog to cancel transfers before logging OUT");
				builder.setMessage(R.string.logout_warning_abort_transfers);
				builder.setPositiveButton(R.string.action_logout, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callback.onAbortConfirm();
					}
				});
			}
			else{
				log("Show dialog to cancel transfers before logging IN");
				builder.setMessage(R.string.login_warning_abort_transfers);
				builder.setPositiveButton(R.string.login_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callback.onAbortConfirm();
					}
				});
			}

			builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callback.onAbortCancel();
				}
			});
			builder.show();
		}else{
			callback.onAbortConfirm();
		}
	}

	public static void changeStatusBarColorActionMode (final Context context, final Window window, Handler handler, int option) {
		log("changeStatusBarColor");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			if (option ==  1) {
				window.setStatusBarColor(ContextCompat.getColor(context, R.color.accentColorDark));
			}
			else if (option == 2) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						window.setStatusBarColor(0);
					}
				}, 500);
			}
			else if (option == 3) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						window.setStatusBarColor(ContextCompat.getColor(context, R.color.status_bar_search));
					}
				}, 500);
			}
			else {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						window.setStatusBarColor(ContextCompat.getColor(context, R.color.dark_primary_color_secondary));
					}
				}, 500);
			}
		}
	}

	public static Bitmap createDefaultAvatar (String color, String firstLetter) {
		log("createDefaultAvatar color: '"+color+"' firstLetter: '"+firstLetter+"'");

		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint paintText = new Paint();
		Paint paintCircle = new Paint();

		paintText.setColor(Color.WHITE);
		paintText.setTextSize(150);
		paintText.setAntiAlias(true);
		paintText.setTextAlign(Paint.Align.CENTER);
		Typeface face = Typeface.SANS_SERIF;
		paintText.setTypeface(face);
		paintText.setAntiAlias(true);
		paintText.setSubpixelText(true);
		paintText.setStyle(Paint.Style.FILL);

		if(color!=null){
			log("The color to set the avatar is "+color);
			paintCircle.setColor(Color.parseColor(color));
			paintCircle.setAntiAlias(true);
		}
		else{
			log("Default color to the avatar");
			paintCircle.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
			paintCircle.setAntiAlias(true);
		}


		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius,paintCircle);

		log("Draw letter: "+firstLetter);
		Rect bounds = new Rect();

		paintText.getTextBounds(firstLetter,0,firstLetter.length(),bounds);
		int xPos = (c.getWidth()/2);
		int yPos = (int)((c.getHeight()/2)-((paintText.descent()+paintText.ascent()/2))+20);
		c.drawText(firstLetter.toUpperCase(Locale.getDefault()), xPos, yPos, paintText);

		return defaultAvatar;
	}

	public static String getDownloadLocation (Context context) {
        DatabaseHandler dbH = DatabaseHandler.getDbHandler(context);
        MegaPreferences prefs = dbH.getPreferences();

        if (prefs != null){
            log("prefs != null");
            if (prefs.getStorageAskAlways() != null){
                if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
                    log("askMe==false");
                    if (prefs.getStorageDownloadLocation() != null){
                        if (prefs.getStorageDownloadLocation().compareTo("") != 0){
                            return prefs.getStorageDownloadLocation();
                        }
                    }
                }
            }
        }
        return Util.downloadDIR;
    }

    public static boolean askMe (Context context) {
		DatabaseHandler dbH = DatabaseHandler.getDbHandler(context);
		MegaPreferences prefs = dbH.getPreferences();

		if (prefs != null){
			if (prefs.getStorageAskAlways() != null){
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							return false;
						}
					}
				}
			}
		}
		return true;
	}
    
    public static void showSnackBar(Context context,int snackbarType,String message,int idChat) {
        if (context instanceof ChatFullScreenImageViewer) {
            ((ChatFullScreenImageViewer)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof AudioVideoPlayerLollipop) {
            ((AudioVideoPlayerLollipop)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof PdfViewerActivityLollipop) {
            ((PdfViewerActivityLollipop)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof ChatActivityLollipop) {
            ((ChatActivityLollipop)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof NodeAttachmentHistoryActivity) {
            ((NodeAttachmentHistoryActivity)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof ManagerActivityLollipop) {
            ((ManagerActivityLollipop)context).showSnackbar(snackbarType,message,idChat);
        } else if (context instanceof BaseActivity) {
            View rootView = getRootViewFromContext(context);
            if (rootView == null) {
                log("unable to show snack bar, view does not exist");
            } else {
                ((BaseActivity)context).showSnackbar(snackbarType,rootView,message,idChat);
            }
        }
    }
    
    private static View getRootViewFromContext(Context context) {
        BaseActivity activity = (BaseActivity)context;
        View rootView = null;
        try {
            rootView = activity.findViewById(android.R.id.content);
            if (rootView == null) {
                rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            }
            if (rootView == null) {
                rootView = ((ViewGroup)((BaseActivity)context).findViewById(android.R.id.content)).getChildAt(0);//get first view
            }
        } catch (Exception e) {
            log("getRootViewFromContext " + e.getMessage());
        }
        return rootView;
    }

	public static void hideKeyboard(Activity activity, int flag){

		View v = activity.getCurrentFocus();
		if (v != null){
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), flag);
		}
	}

	public static void hideKeyboardView(Context context, View v, int flag){

		if (v != null){
			InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), flag);
		}
	}

	private static void log(String message) {
		log("Util", message);
	}
}
