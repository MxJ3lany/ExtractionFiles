package mega.privacy.android.app.lollipop.megachat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.ExtendedViewPager;
import mega.privacy.android.app.components.TouchImageView;
import mega.privacy.android.app.components.dragger.DraggableView;
import mega.privacy.android.app.components.dragger.ExitViewAnimator;
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop;
import mega.privacy.android.app.lollipop.LoginActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaChatFullScreenImageAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUserAlert;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;

public class ChatFullScreenImageViewer extends PinActivityLollipop implements OnPageChangeListener, MegaRequestListenerInterface, MegaGlobalListenerInterface, DraggableView.DraggableListener {

	boolean fromChatSavedInstance = false;
	int[] screenPosition;
	int mLeftDelta;
	int mTopDelta;
	float mWidthScale;
	float mHeightScale;
	public DraggableView draggableView;
	public static int screenHeight;
	int screenWidth;
	RelativeLayout relativeImageViewerLayout;
	ImageView ivShadow;
	private Handler handler;

	private DisplayMetrics outMetrics;

	private boolean aBshown = true;

	ProgressDialog statusDialog;
	private android.support.v7.app.AlertDialog downloadConfirmationDialog;

	float scaleText;
	AppBarLayout appBarLayout;
	Toolbar tB;
	ActionBar aB;

	private MenuItem downloadIcon;
	private MenuItem importIcon;
	private MenuItem saveForOfflineIcon;
	private MenuItem removeIcon;

	private MegaChatFullScreenImageAdapter adapterMega;
	private int positionG;
	private ArrayList<Long> imageHandles;
	private RelativeLayout fragmentContainer;
	private TextView fileNameTextView;
	private RelativeLayout bottomLayout;
	private ExtendedViewPager viewPager;

	static ChatFullScreenImageViewer fullScreenImageViewer;
    private MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;

    private ArrayList<String> paths;
	MegaNode nodeToImport;

	long [] messageIds;
	long chatId = -1;

	ArrayList<MegaChatMessage> messages;

	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;

	ArrayList<Long> handleListM = new ArrayList<Long>();

	boolean isDeleteDialogShow = false;

	ChatController chatC;

	@Override
	public void onDestroy(){
		if(megaApi != null)
		{
			megaApi.removeRequestListener(this);
		}

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenu");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_chat_full_screen_image_viewer, menu);

		downloadIcon = menu.findItem(R.id.chat_full_image_viewer_download);
		importIcon = menu.findItem(R.id.chat_full_image_viewer_import);
		saveForOfflineIcon = menu.findItem(R.id.chat_full_image_viewer_save_for_offline);
        saveForOfflineIcon.setIcon(Util.mutateIconSecondary(this, R.drawable.ic_b_save_offline, R.color.white));
		removeIcon = menu.findItem(R.id.chat_full_image_viewer_remove);

//		Drawable drawable = importIcon.getIcon();
//		if (drawable != null) {
//			// If we don't mutate the drawable, then all drawable's with this id will have a color
//			// filter applied to it.
//			drawable.mutate();
//			drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//			drawable.setAlpha(255);
//		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		log("onPrepareOptionsMenu");

		MegaNode node = null;

		if(!messages.isEmpty()){
			if(messages.get(positionG).getMegaNodeList()!=null && messages.get(positionG).getMegaNodeList().size()>0){
				node = messages.get(positionG).getMegaNodeList().get(0);
			}
		}

        if(megaApi==null || !Util.isOnline(this)){
            downloadIcon.setVisible(false);
            importIcon.setVisible(false);
            saveForOfflineIcon.setVisible(false);

            if(MegaApiJava.userHandleToBase64(messages.get(positionG).getUserHandle()).equals(megaChatApi.getMyUserHandle()) && messages.get(positionG).isDeletable()) {
                removeIcon.setVisible(true);
            }
            else{
                removeIcon.setVisible(false);
            }
        }
        else if (node != null){
            downloadIcon.setVisible(true);
            if (chatC.isInAnonymousMode()) {
                importIcon.setVisible(false);
                saveForOfflineIcon.setVisible(false);
            }
            else {
                importIcon.setVisible(true);
                saveForOfflineIcon.setVisible(true);
            }

            if (messages.get(positionG).getUserHandle()==megaChatApi.getMyUserHandle() && messages.get(positionG).isDeletable()) {
                removeIcon.setVisible(true);
            }
            else {
                removeIcon.setVisible(false);
            }
        }
        else {
            downloadIcon.setVisible(false);
            importIcon.setVisible(false);
            saveForOfflineIcon.setVisible(false);
            removeIcon.setVisible(false);
        }

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		log("onRequestPermissionsResult");
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode){
			case Constants.REQUEST_WRITE_STORAGE:{
				boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
				if (hasStoragePermission) {
					MegaNode node = messages.get(positionG).getMegaNodeList().get(0);
					chatC.prepareForChatDownload(node);
				}
				break;
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected");
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home: {
				onBackPressed();
				break;
			}
			case R.id.chat_full_image_viewer_download: {
				log("download option");
				MegaNode node = chatC.authorizeNodeIfPreview(messages.get(positionG).getMegaNodeList().get(0), megaChatApi.getChatRoom(chatId));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
					if (!hasStoragePermission) {
						ActivityCompat.requestPermissions(this,
								new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
						handleListM.add(node.getHandle());
						break;
					}
				}
				chatC.prepareForChatDownload(node);

				break;
			}

			case R.id.chat_full_image_viewer_import: {
				log("import option");
				MegaNode node = chatC.authorizeNodeIfPreview(messages.get(positionG).getMegaNodeList().get(0), megaChatApi.getChatRoom(chatId));
				importNode(node);
				break;
			}
			case R.id.chat_full_image_viewer_save_for_offline: {
				log("save for offline option");
//				showSnackbar("Coming soon...");
				if (messages.get(positionG) != null){
					chatC.saveForOffline(messages.get(positionG).getMegaNodeList(), megaChatApi.getChatRoom(chatId));
				}
				break;
			}
			case R.id.chat_full_image_viewer_remove: {
				log("remove option");
				MegaChatMessage msg = messages.get(positionG);
				showConfirmationDeleteNode(chatId, msg);
				break;
			}

		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		super.onCreate(savedInstanceState);

		handler = new Handler();
		fullScreenImageViewer = this;

		chatC = new ChatController(this);

		Display display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);
		screenHeight = outMetrics.heightPixels;
		screenWidth = outMetrics.widthPixels;
		float density  = getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);
		if (scaleH < scaleW){
			scaleText = scaleH;
		}
		else{
			scaleText = scaleW;
		}
		if (savedInstanceState != null){
			isDeleteDialogShow = savedInstanceState.getBoolean("isDeleteDialogShow", false);
		}
		else {
			isDeleteDialogShow = false;
		}

		dbH = DatabaseHandler.getDbHandler(this);

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    }

		MegaApplication app = (MegaApplication)getApplication();

		if(Util.isOnline(this)){
			megaApi = app.getMegaApi();

			if((megaApi==null||megaApi.getRootNode()==null) && !chatC.isInAnonymousMode()){
				log("Refresh session - sdk");
				Intent intent = new Intent(this, LoginActivityLollipop.class);
				intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return;
			}
		}

		if(Util.isChatEnabled()){
			if (megaChatApi == null){
				megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
			}

			if(megaChatApi==null||megaChatApi.getInitState()== MegaChatApi.INIT_ERROR){
				log("Refresh session - karere");
				Intent intent = new Intent(this, LoginActivityLollipop.class);
				intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return;
			}
		}

		if(megaApi!=null){
			megaApi.addGlobalListener(this);
		}

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

		setContentView(R.layout.activity_chat_full_screen_image_viewer);

		draggableView.setViewAnimator(new ExitViewAnimator());

		relativeImageViewerLayout = (RelativeLayout) findViewById(R.id.full_image_viewer_layout);
		fragmentContainer = (RelativeLayout) findViewById(R.id.chat_full_image_viewer_parent_layout);
		appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
		viewPager = (ExtendedViewPager) findViewById(R.id.image_viewer_pager);
		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			// optional
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

			// optional
			@Override
			public void onPageSelected(int position) {
				log("onPageSelected");
				supportInvalidateOptionsMenu();
			}

			// optional
			@Override
			public void onPageScrollStateChanged(int state) { }
		});

		viewPager.setPageMargin(40);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
		}

		tB = (Toolbar) findViewById(R.id.call_toolbar);
		if (tB == null) {
			log("Tb is Null");
			return;
		}

		tB.setVisibility(View.VISIBLE);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
		log("aB.setHomeAsUpIndicator_1");
		aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
		aB.setHomeButtonEnabled(true);
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setTitle(" ");

		Intent intent = getIntent();
		positionG = intent.getIntExtra("position", 0);

		messageIds = intent.getLongArrayExtra("messageIds");
		chatId = intent.getLongExtra("chatId", -1);

		messages = new ArrayList<MegaChatMessage>();

		imageHandles = new ArrayList<Long>();
		paths = new ArrayList<String>();

		if(messageIds==null){
			return;
		}

		for(int j=0; j<messageIds.length; j++){
			MegaChatMessage message = megaChatApi.getMessage(chatId, messageIds[j]);
			if(message!=null){
				MegaNodeList list = message.getMegaNodeList();
				if(list.size()==1){
					MegaNode node = list.get(0);
					if(MimeTypeList.typeForName(node.getName()).isImage()){
						messages.add(message);
					}
				}
				else{
					log("Messages with more than one attachment - do not supported");
				}
			}
			else{
				log("ERROR - the message is NULL");
			}
		}

		if(messages.size() == 0)
		{
			finish();
			return;
		}

		int imageNumber = 0;
		for (int i=0;i<messages.size();i++){
			MegaNode n = messages.get(i).getMegaNodeList().get(0);
			if (MimeTypeList.typeForName(n.getName()).isImage()){
				imageHandles.add(n.getHandle());
				if (i == positionG){
					positionG = imageNumber;
				}
				imageNumber++;
			}
		}

		if(positionG >= imageHandles.size())
		{
			positionG = 0;
		}

		adapterMega = new MegaChatFullScreenImageAdapter(this, fullScreenImageViewer,messages, megaApi);

		viewPager.setAdapter(adapterMega);

		viewPager.setCurrentItem(positionG);

		viewPager.setOnPageChangeListener(this);

		bottomLayout = (RelativeLayout) findViewById(R.id.chat_image_viewer_layout_bottom);
		fileNameTextView = (TextView) findViewById(R.id.chat_full_image_viewer_file_name);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
		}
		else{
			fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
		}
		fileNameTextView.setText(messages.get(positionG).getMegaNodeList().get(0).getName());

		if (isDeleteDialogShow && chatId != -1 && messages.get(positionG) != null) {
			showConfirmationDeleteNode(chatId, messages.get(positionG));
		}

		if (savedInstanceState == null && adapterMega!= null){
			ViewTreeObserver observer = viewPager.getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {

					viewPager.getViewTreeObserver().removeOnPreDrawListener(this);
					int[] location = new int[2];
					viewPager.getLocationOnScreen(location);

					if (screenPosition != null){
						mLeftDelta = screenPosition[0] - (screenPosition[2]/2) - location[0];
						mTopDelta = screenPosition[1] - (screenPosition[3]/2) - location[1];

						mWidthScale = (float) screenPosition[2] / viewPager.getWidth();
						mHeightScale = (float) screenPosition[3] / viewPager.getHeight();
					}
					else {
						mLeftDelta = (screenWidth/2) - location[0];
						mTopDelta = (screenHeight/2) - location[1];

						mWidthScale = (float) (screenWidth/4) / viewPager.getWidth();
						mHeightScale = (float) (screenHeight/4) / viewPager.getHeight();
					}

					runEnterAnimation();

					return true;
				}
			});
		}
		else {
			fromChatSavedInstance = true;
		}
	}

	public void runEnterAnimation() {
		log("runEnterAnimation");
		final long duration = 600;
		if (aB != null && aB.isShowing()) {
			if(tB != null) {
				tB.animate().translationY(-220).setDuration(0)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								aB.hide();
							}
						}).start();
				bottomLayout.animate().translationY(220).setDuration(0).start();
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				aB.hide();
			}
		}

		fragmentContainer.setBackgroundColor(TRANSPARENT);
		relativeImageViewerLayout.setBackgroundColor(TRANSPARENT);
		appBarLayout.setBackgroundColor(TRANSPARENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			fragmentContainer.setElevation(0);
			relativeImageViewerLayout.setElevation(0);
			appBarLayout.setElevation(0);

		}

		viewPager.setPivotX(0);
		viewPager.setPivotY(0);
		viewPager.setScaleX(mWidthScale);
		viewPager.setScaleY(mHeightScale);
		viewPager.setTranslationX(mLeftDelta);
		viewPager.setTranslationY(mTopDelta);

		ivShadow.setAlpha(0);

		viewPager.animate().setDuration(duration).scaleX(1).scaleY(1).translationX(0).translationY(0).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
			@Override
			public void run() {
				showActionBar();
				fragmentContainer.setBackgroundColor(BLACK);
				relativeImageViewerLayout.setBackgroundColor(BLACK);
				appBarLayout.setBackgroundColor(BLACK);
			}
		});

		ivShadow.animate().setDuration(duration).alpha(1);
	}

	@Override
	public void onPageSelected(int position) {
		return;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		return;
	}

	@Override
	public void onPageScrollStateChanged(int state) {

		if (state == ViewPager.SCROLL_STATE_IDLE){
			if (viewPager.getCurrentItem() != positionG){
				int oldPosition = positionG;
				int newPosition = viewPager.getCurrentItem();
				positionG = newPosition;

				try{
					TouchImageView tIV = (TouchImageView) adapterMega.getVisibleImage(oldPosition);
					if (tIV != null){
						tIV.setZoom(1);
					}
				}catch(Exception e){}
				fileNameTextView.setText(messages.get(positionG).getMegaNodeList().get(0).getName());
			}
		}
	}

	public void askSizeConfirmationBeforeChatDownload(String parentPath, ArrayList<MegaNode> nodeList, long size){
		log("askSizeConfirmationBeforeChatDownload");

		final String parentPathC = parentPath;
		final ArrayList<MegaNode> nodeListC = nodeList;
		final long sizeC = size;

		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
		LinearLayout confirmationLayout = new LinearLayout(this);
		confirmationLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

		final CheckBox dontShowAgain =new CheckBox(this);
		dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
		dontShowAgain.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

		confirmationLayout.addView(dontShowAgain, params);

		builder.setView(confirmationLayout);

		builder.setMessage(getString(R.string.alert_larger_file, Util.getSizeString(sizeC)));
		builder.setPositiveButton(getString(R.string.general_save_to_device),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(dontShowAgain.isChecked()){
							dbH.setAttrAskSizeDownload("false");
						}
						chatC.download(parentPathC, nodeListC);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if(dontShowAgain.isChecked()){
					dbH.setAttrAskSizeDownload("false");
				}
			}
		});

		downloadConfirmationDialog = builder.create();
		downloadConfirmationDialog.show();
	}
	

	public void importNode(MegaNode node){
		log("importNodesFromAndroidMessages");

		nodeToImport = node;
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_IMPORT_FOLDER);
		startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_IMPORT_FOLDER);
	}

	@Override
	public void onSaveInstanceState (Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if (getIntent() != null) {
			getIntent().putExtra("position", positionG);
		}
		savedInstanceState.putBoolean("aBshown", adapterMega.isaBshown());
		savedInstanceState.putBoolean("overflowVisible", adapterMega.isMenuVisible());
		savedInstanceState.putBoolean("isDeleteDialogShow", isDeleteDialogShow);
	}
	
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);

		aBshown = savedInstanceState.getBoolean("aBshown");
		adapterMega.setaBshown(aBshown);
	}

	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getRequestString());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {

		log("onRequestFinish: "+e.getErrorCode());
		if(request.getType() == MegaRequest.TYPE_COPY){
			if (e.getErrorCode() != MegaError.API_OK) {

				log("e.getErrorCode() != MegaError.API_OK");

				if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
					log("OVERQUOTA ERROR: "+e.getErrorCode());
					Intent intent = new Intent(this, ManagerActivityLollipop.class);
					intent.setAction(Constants.ACTION_OVERQUOTA_STORAGE);
					startActivity(intent);
					finish();
				}
				else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
					log("OVERQUOTA ERROR: "+e.getErrorCode());
					Intent intent = new Intent(this, ManagerActivityLollipop.class);
					intent.setAction(Constants.ACTION_PRE_OVERQUOTA_STORAGE);
					startActivity(intent);
					finish();
				}
				else if(e.getErrorCode()==MegaError.API_ENOENT){
					showSnackbar(Constants.SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, 1, 1));
				}
				else
				{
					showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error));
				}

			}else{
				showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_message));
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getRequestString());	
	}
	
	public static void log(String message) {
		Util.log("ChatFullScreenImageViewer", message);
	}

	public void showConfirmationDeleteNode(final long chatId, final MegaChatMessage message){
		log("showConfirmationDeleteNode");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						chatC.deleteMessage(message, chatId);
						isDeleteDialogShow = false;
						finish();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						isDeleteDialogShow = false;
						break;
				}
			}
		};

		android.support.v7.app.AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
		}
		else{
			builder = new android.support.v7.app.AlertDialog.Builder(this);
		}

		builder.setMessage(R.string.confirmation_delete_one_attachment);

		builder.setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

		isDeleteDialogShow = true;

		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				isDeleteDialogShow = false;
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (intent == null) {
			return;
		}
		if (requestCode == Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
			log("local folder selected");
			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES);
			if (hashes != null) {
				ArrayList<MegaNode> megaNodes = new ArrayList<>();
				for (int i=0; i<hashes.length; i++) {
					MegaNode node = megaApi.getNodeByHandle(hashes[i]);
					if (node != null) {
						megaNodes.add(node);
					}
					else {
						log("Node NULL, not added");
					}
				}
				if (megaNodes.size() > 0) {
					chatC.checkSizeBeforeDownload(parentPath, megaNodes);
				}
			}

//			String url = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_URL);
//			long size = intent.getLongExtra(FileStorageActivityLollipop.EXTRA_SIZE, 0);
//			log("URL: " + url + "___SIZE: " + size);
//			downloadTo (parentPath, url, size, hashes);
		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_IMPORT_FOLDER && resultCode == RESULT_OK) {
			log("onActivityResult REQUEST_CODE_SELECT_IMPORT_FOLDER OK");

			if(!Util.isOnline(this)||megaApi==null) {
				try{
					statusDialog.dismiss();
				} catch(Exception ex) {};

				showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem));
				return;
			}

			final long toHandle = intent.getLongExtra("IMPORT_TO", 0);

			MegaNode target = null;
			target = megaApi.getNodeByHandle(toHandle);
			if(target == null){
				target = megaApi.getRootNode();
			}
			log("TARGET: " + target.getName() + "and handle: " + target.getHandle());
			if (nodeToImport != null) {
				log("DOCUMENT: " + nodeToImport.getName() + "_" + nodeToImport.getHandle());
				if (target != null) {
					megaApi.copyNode(nodeToImport, target, this);
				} else {
					log("TARGET: null");
					showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error));
				}
			}
			else{
				log("DOCUMENT: null");
				showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error));
			}

		}
	}
	

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {}
	
	public void downloadTo(String parentPath, String url, long size, long [] hashes){

		if(!Util.isOnline(this)||megaApi==null) {
			try{
				statusDialog.dismiss();
			} catch(Exception ex) {};

			showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem));
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}
		}
		
		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(parentPath);
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}
		
		
		if (hashes == null){
			if(url != null) {
				if(availableFreeSpace < size) {
					showSnackbar(Constants.NOT_SPACE_SNACKBAR_TYPE, null);
					return;
				}
				
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_URL, url);
				service.putExtra(DownloadService.EXTRA_SIZE, size);
				service.putExtra(DownloadService.EXTRA_PATH, parentPath);
				service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
				startService(service);
			}
		}
		else{
			if(hashes.length == 1){
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					log("ISFILE");
					String localPath = Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
					if(localPath != null){	
						try { 
							Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName())); 
						}
						catch(Exception e) {}

						try {

							Intent viewIntent = new Intent(Intent.ACTION_VIEW);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								viewIntent.setDataAndType(FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							} else {
								viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							}
							viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							if (MegaApiUtils.isIntentAvailable(this, viewIntent))
								startActivity(viewIntent);
							else {
								Intent intentShare = new Intent(Intent.ACTION_SEND);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
									intentShare.setDataAndType(FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								} else {
									intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								}
								intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								if (MegaApiUtils.isIntentAvailable(this, intentShare))
									startActivity(intentShare);
								String message = getString(R.string.general_already_downloaded) + ": " + localPath;
								showSnackbar(Constants.SNACKBAR_TYPE, message);
							}
						}
						catch (Exception e){
							String message = getString(R.string.general_already_downloaded) + ": " + localPath;
							showSnackbar(Constants.SNACKBAR_TYPE, message);
						}
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
//						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						
						if(availableFreeSpace < document.getSize()){
							showSnackbar(Constants.NOT_SPACE_SNACKBAR_TYPE, null);
							continue;
						}
						
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
						startService(service);
					}
				}
				else if(url != null) {
					if(availableFreeSpace < size) {
						showSnackbar(Constants.NOT_SPACE_SNACKBAR_TYPE, null);
						continue;
					}
					
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
					startService(service);
				}
				else {
					log("node not found");
				}
			}
		}
	}

	public void showSnackbar(int type, String s){
		showSnackbar(type, fragmentContainer, s);
	}
    
    public void showSnackbar(int type, String s, int chatId){
        showSnackbar(type, fragmentContainer, s, chatId);
    }

	public void touchImage() {
		log("touchImage");
		if(aB.isShowing()){
			hideActionBar();
		}else{
			showActionBar();
		}
	}

	protected void hideActionBar(){
		if (aB != null && aB.isShowing()) {
			if(tB != null) {
				tB.animate().translationY(-220).setDuration(400L)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								aB.hide();
							}
						}).start();
				bottomLayout.animate().translationY(220).setDuration(400L).start();
			} else {
				aB.hide();
			}
		}
	}
	protected void showActionBar(){
		if (aB != null && !aB.isShowing()) {
			aB.show();
			if(tB != null) {
				tB.animate().translationY(0).setDuration(400L).start();
				bottomLayout.animate().translationY(0).setDuration(400L).start();
			}

		}
	}
	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {}

	@Override
	public void onUserAlertsUpdate(MegaApiJava api, ArrayList<MegaUserAlert> userAlerts) {
		log("onUserAlertsUpdate");
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodeList) {}

	@Override
	public void onReloadNeeded(MegaApiJava api) {}

	@Override
	public void onAccountUpdate(MegaApiJava api) {}

	@Override
	public void onEvent(MegaApiJava api, MegaEvent event) {

	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> requests) {}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(getContainer());
		View view = LayoutInflater.from(this).inflate(layoutResID, null);
		draggableView.addView(view);
	}

	private View getContainer() {
		RelativeLayout container = new RelativeLayout(this);
		draggableView = new DraggableView(this);
		if (getIntent() != null) {
			screenPosition = getIntent().getIntArrayExtra("screenPosition");
			draggableView.setScreenPosition(screenPosition);
		}
		draggableView.setDraggableListener(this);
		ivShadow = new ImageView(this);
		ivShadow.setBackgroundColor(ContextCompat.getColor(this, R.color.black_p50));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		container.addView(ivShadow, params);
		container.addView(draggableView);
		return container;
	}

	@Override
	public void onViewPositionChanged(float fractionScreen) {

	}

	@Override
	public void onDragActivated(boolean activated) {
		log("onDragActivated");

		if (activated) {
//			updateCurrentImage();
			if (aB != null && aB.isShowing()) {
				if(tB != null) {
					tB.animate().translationY(-220).setDuration(0)
							.withEndAction(new Runnable() {
								@Override
								public void run() {
									aB.hide();
								}
							}).start();
					bottomLayout.animate().translationY(220).setDuration(0).start();
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				} else {
					aB.hide();
				}
			}
			fragmentContainer.setBackgroundColor(TRANSPARENT);
			relativeImageViewerLayout.setBackgroundColor(TRANSPARENT);
			appBarLayout.setBackgroundColor(TRANSPARENT);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				fragmentContainer.setElevation(0);
				relativeImageViewerLayout.setElevation(0);
				appBarLayout.setElevation(0);

			}
			if (fromChatSavedInstance) {
				draggableView.setCurrentView(null);
			}
			else {
				draggableView.setCurrentView(adapterMega.getVisibleImage(positionG));
			}
		}
		else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
//					showActionBar();
					fragmentContainer.setBackgroundColor(BLACK);
					relativeImageViewerLayout.setBackgroundColor(BLACK);
					appBarLayout.setBackgroundColor(BLACK);
				}
			}, 300);
		}
	}
}
