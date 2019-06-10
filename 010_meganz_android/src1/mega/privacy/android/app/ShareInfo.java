package mega.privacy.android.app;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.utils.Util;


/*
 * Helper class to process shared files from other activities
 */
public class ShareInfo {

	public String title = null;
	private long lastModified;
	public InputStream inputStream = null;
	public long size = -1;
	private File file = null;
	public boolean isContact = false;
	public Uri contactUri = null;
	
	/*
	 * Get ShareInfo from File
	 */
	public static ShareInfo infoFromFile(File file) {
		ShareInfo info = new ShareInfo();
		info.file = file;
		info.size = file.length();
		info.title = file.getName();
		try {
			info.inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		return info;
	}
	
	public String getFileAbsolutePath() {
		return file.getAbsolutePath();
	}
	
	public String getTitle() {
		return title;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
	    return lastModified;
    }
	
	/*
	 * Process incoming Intent and get list of ShareInfo objects
	 */
	public static List<ShareInfo> processIntent(Intent intent, Context context) {
		log(intent.getAction() + " of action");
		
		if (intent.getAction() == null || intent.getAction().equals(FileExplorerActivityLollipop.ACTION_PROCESSED)||intent.getAction().equals(FileExplorerActivityLollipop.ACTION_PROCESSED)) {
			return null;
		}
		if (context == null) {
			return null;
		}
		// Process multiple items
		if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
			log("multiple!");
			return processIntentMultiple(intent, context);
		}
		ShareInfo shareInfo = new ShareInfo();

		Bundle extras = intent.getExtras();
		// File data in EXTRA_STREAM
		if (extras != null && extras.containsKey(Intent.EXTRA_STREAM)) {
			log("extras is not null");
			Object streamObject = extras.get(Intent.EXTRA_STREAM);
			if (streamObject instanceof Uri) {
				log("instance of URI");
				log(streamObject.toString());
				shareInfo.processUri((Uri) streamObject, context);
			} else if (streamObject == null) {
				log("stream object is null!");
				return null;
			} else {
				log("unhandled type " + streamObject.getClass().getName());
				for (String key : extras.keySet()) {
					log("Key " + key);
				}
				return processIntentMultiple(intent, context);
			}
		}
		else if (intent.getClipData() != null) {
			if(Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
				log("Multiple ACTION_GET_CONTENT");
				return processGetContentMultiple(intent, context);
			}
		}
		// Get File info from Data URI
		else {
			Uri dataUri = intent.getData();
			if (dataUri == null) {
				log("data uri is null");
//
//				if(Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
//					log("Multiple ACTION_GET_CONTENT");
//					return processGetContentMultiple(intent, context);
//				}
				return null;
			}
			shareInfo.processUri(dataUri, context);
		}
		if (shareInfo.file == null) {
			log("share info file is null");
			return null;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {	
			intent.setAction(FileExplorerActivityLollipop.ACTION_PROCESSED);
		}
		else{
			intent.setAction(FileExplorerActivityLollipop.ACTION_PROCESSED);
		}
		
		ArrayList<ShareInfo> result = new ArrayList<ShareInfo>();
		result.add(shareInfo);
		return result;
	}
	
	/*
	 * Process Multiple files from GET_CONTENT Intent
	 */
	@SuppressLint("NewApi")
	public static List<ShareInfo> processGetContentMultiple(Intent intent,Context context) {
		log("processIntentMultiple");
		ArrayList<ShareInfo> result = new ArrayList<ShareInfo>();
		ClipData cD = intent.getClipData();
		if(cD!=null&&cD.getItemCount()!=0){
		
            for(int i = 0; i < cD.getItemCount(); i++){
            	ClipData.Item item = cD.getItemAt(i);
            	Uri uri = item.getUri();
            	log("ClipData uri: "+uri);
            	if (uri == null)
    				continue;
    			log("----: "+uri.toString());
    			ShareInfo info = new ShareInfo();
    			info.processUri(uri, context);
    			if (info.file == null) {
    				continue;
    			}
    			result.add(info);
            }
		}
		else{
			log("ClipData NUll or size=0");
			return null;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {	
			intent.setAction(FileExplorerActivityLollipop.ACTION_PROCESSED);
		}
		else{
			intent.setAction(FileExplorerActivityLollipop.ACTION_PROCESSED);
		}
		
		return result;
	}
	
	
	/*
	 * Process Multiple files
	 */
	public static List<ShareInfo> processIntentMultiple(Intent intent,Context context) {
		log("processIntentMultiple");
		ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		
		ArrayList<Uri> imageUri = intent.getParcelableArrayListExtra(Intent.EXTRA_ALLOW_MULTIPLE);

		if (imageUris == null || imageUris.size() == 0) {
			log("imageUris == null || imageUris.size() == 0");
			return null;
		}
		ArrayList<ShareInfo> result = new ArrayList<ShareInfo>();
		for (Uri uri : imageUris) {
			if (uri == null) {
				log("continue --> uri null");
				continue;
			}
			log("----: "+uri.toString());
			ShareInfo info = new ShareInfo();
			info.processUri(uri, context);
			if (info.file == null) {
				log("continue -->info.file null");
				continue;
			}
			result.add(info);
		}

		intent.setAction(FileExplorerActivityLollipop.ACTION_PROCESSED);

		return result;
	}

	/*
	 * Get info from Uri
	 */
	private void processUri(Uri uri, Context context) {
		log("processUri: "+uri);
		// getting input stream
		inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch (Exception e) {
			log("inputStream EXCEPTION!");
			log(""+e);
			String path = uri.getPath();
			log("processUri-path en la exception: "+path);
		}

		String scheme = uri.getScheme();
		if(scheme != null)
		{
			if (scheme.equals("content")) {
				log("processUri go to scheme content");
				processContent(uri, context);
			} else if (scheme.equals("file")) {
				log("processUri go to file content");
				processFile(uri, context);
			}
		}
		else{
			log("scheme NULL");
		}

		if (inputStream != null) {
			log("processUri inputStream != null");

			file = null;
			String path = uri.getPath();
			log("processUri-path: "+path);
			try{
				file = new File(path);
			}
			catch(Exception e){
				log("error when creating File!");
//					log(e.getMessage());
			}

			if((file != null) && file.exists() && file.canRead())
			{
				size = file.length();
				log("The file is accesible!");
				return;
			}

			file = null;
			path = getRealPathFromURI(context, uri);
			if(path!=null){
				log("RealPath: "+path);
				try
				{
					file = new File(path);
				}
				catch(Exception e){
					log("EXCEPTION: No real path from URI");
				}
			}
			else{
				log("Real path is NULL");
			}

			if((file != null) && file.exists() && file.canRead())
			{
				size = file.length();
				log("Return here");
				return;
			}

			if (context.getExternalCacheDir() != null){
				if (title != null){
					if (title.contains("../") || title.contains(("..%2F"))){
						log("External path traversal: " + title);
						return;
					}
					log("External No path traversal: " + title);
					if (context instanceof PdfViewerActivityLollipop){
						log("context of PdfViewerActivityLollipop");
						if (!title.endsWith(".pdf")){
							title += ".pdf";
						}
					}
					file = new File(context.getExternalCacheDir(), title);
				}
				else{
					return;
				}
			}
			else{
				if (title != null){
					if (title.contains("../") || title.contains(("..%2F"))){
						log("Internal path traversal: " + title);
						return;
					}
					log("Internal No path traversal: " + title);
					if (context instanceof PdfViewerActivityLollipop){
						log("context of PdfViewerActivityLollipop");
						if (!title.endsWith(".pdf")){
							title += ".pdf";
						}
					}
					file = new File(context.getCacheDir(), title);
				}
				else{
					return;
				}
			}
			log("Start copy to: "+file.getAbsolutePath());

			try {
				OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					stream.write(buffer, 0, len);
				}
				if (stream != null) {
					stream.close();
				}

				inputStream = new FileInputStream(file);
				size = file.length();
				log("File size: "+size);
			}
			catch (IOException e) {
				log("Catch IO exception");
				inputStream = null;
				if (file != null) {
					file.delete();
				}
			}
		}
		else{
			log("inputStream is NULL");
			String path = uri.getPath();
			log("PATH: " + path);
			if (path != null){
				String [] s = path.split("file://");
				if (s.length > 1){
					String p = s[1];
					String [] s1 = p.split("/ORIGINAL");
					if (s1.length > 1){
						path = s1[0];
//						path.replaceAll("%20", " ");
						try {
							path = URLDecoder.decode(path, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							path.replaceAll("%20", " ");
						}
					}
				}
			}
			log("REAL PATH: " + path);

			file = null;
			try{
				file = new File(path);
			}
			catch(Exception e){
				log("error when creating File!");
//					log(e.getMessage());
			}
			if((file != null) && file.exists() && file.canRead()) {
				size = file.length();
				log("The file is accesible!");
				return;
			}
			else{
				log("The file is not accesible!");
				isContact = true;
				contactUri = uri;
			}
		}
		log("END processUri");
	}
	
	/*
	 * Get info from content provider
	 */
	private void processContent(Uri uri, Context context) {
		log("processContent: "+uri);
		ContentProviderClient client = null;

		client = context.getContentResolver().acquireContentProviderClient(uri);
		Cursor cursor = null;
		try {
			cursor = client.query(uri, null, null, null, null);
		} catch (RemoteException e1) {
			log("cursor EXCEPTION!!!");
		}
		if(cursor!=null){
			if(cursor.getCount()==0){
				log("RETURN - Cursor get count is 0");
				return;
			}
		}
		else{
			log("RETURN - Cursor is NULL");
			return;
		}
		cursor.moveToFirst();
		int displayIndex = cursor.getColumnIndex("_display_name");
		if(displayIndex != -1)
			title = cursor.getString(displayIndex);
		int sizeIndex = cursor.getColumnIndex("_size");
		if (sizeIndex != -1) {
			String sizeString = cursor.getString(sizeIndex);
			if(sizeString!=null){
				long size = Long.valueOf(sizeString);
				if (size > 0) {
					log("Size: "+size);
					this.size = size;
				}
			}
		}
		int lastModifiedIndex = cursor.getColumnIndex("last_modified");
		if(lastModifiedIndex != -1) {
		    this.lastModified = cursor.getLong(lastModifiedIndex);
        }

		if (size == -1 || inputStream == null) {
			log("Keep going");
			int dataIndex = cursor.getColumnIndex("_data");
			if (dataIndex != -1) {
				String data = cursor.getString(dataIndex);
				if (data == null){
					log("RETURN - data is NULL");
					return;
				}
				File dataFile = new File(data);
				if (dataFile.exists() && dataFile.canRead()) {
					if (size == -1) {
						long size = dataFile.length();
						if (size > 0) {
							log("Size is: "+size);
							this.size = size;
						}
					}
					else{
						log("Not valid size");
					}

					if (inputStream == null) {
						try {
							inputStream = new FileInputStream(dataFile);
						} catch (FileNotFoundException e) {
							log("Exception FileNotFoundException");
						}

					}
					else{
						log("inputStream is NULL");
					}
				}
			}
		}
		else{
			log("Nothing done!");
		}
	
		client.release();
		log("---- END process content----");
	}
	
	/*
	 * Process Uri as File path
	 */
	private void processFile(Uri uri, Context context) {
		log("processing file");
		File file = null;
		try {
			file = new File(new URI(uri.toString()));
		} catch (URISyntaxException e1) {
			file = new File(uri.toString().replace("file:///", "/"));
		}
		if (!file.exists() || !file.canRead()) {
			log("cantread :( " + file.exists() + " " + file.canRead() + " "
					+ uri.toString());
			return;
		}
		if (file.isDirectory()) {
			log("is folder");
			return;
		}
		log("continue processing..");
		size = file.length();
		title = file.getName();
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
		}
		log(title + " " + size);
	}
	
	private String getRealPathFromURI(Context context, Uri contentURI) {
		if(contentURI == null) return null;
	    String path = null;
	    Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(contentURI, null, null, null, null);
		    if(cursor == null) return null;
		    if(cursor.getCount() == 0)
		    {
		    	cursor.close();
		    	return null;
		    }
		    
		    cursor.moveToFirst();
		    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
		    if(idx == -1) 
		    {
		    	cursor.close();
		    	return null;
		    }
		    
		    try { path = cursor.getString(idx); } 
		    catch(Exception ex)
		    {
		    	cursor.close();
		    	return null;
		    }
	    }
	    catch(Exception e)
	    {
	    	if(cursor != null)
	    		cursor.close();
	    	return null;
	    }
		
    	if(cursor != null)
    		cursor.close();
	    return path;
	}
	
	private static void log(String log) {
		Util.log("ShareInfo", log);
	}
	
}
