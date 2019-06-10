package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeThumbnail;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.CustomizedGridLayoutManager;
import mega.privacy.android.app.components.NewGridRecyclerView;
import mega.privacy.android.app.components.NewHeaderItemDecoration;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.ZipBrowserActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaNodeAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaOfflineLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaNode;

public class OfflineFragmentLollipop extends Fragment{

	public static ImageView imageDrag;
	public static final String REFRESH_OFFLINE_FILE_LIST = "refresh_offline_file_list";
	
	MegaPreferences prefs;
	
	Context context;
	ActionBar aB;
	RecyclerView recyclerView;
	LinearLayoutManager mLayoutManager;
	CustomizedGridLayoutManager gridLayoutManager;

	Stack<Integer> lastPositionStack;
	public NewHeaderItemDecoration headerItemDecoration;
	ImageView emptyImageView;
	LinearLayout emptyTextView;
	TextView emptyTextViewFirst;

	MegaOfflineLollipopAdapter adapter;
	OfflineFragmentLollipop offlineFragment = this;
	DatabaseHandler dbH = null;
	ArrayList<MegaOffline> mOffList= null;
	String pathNavigation = null;
	TextView contentText;
//	boolean isList = true;
	int orderGetChildren;
	public static String DB_FILE = "0";
	public static String DB_FOLDER = "1";
	MegaApiAndroid megaApi;
	RelativeLayout contentTextLayout;
	
	float density;
	DisplayMetrics outMetrics;
	Display display;

	private ActionMode actionMode;
	
	private int placeholderCount;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}
	};

	@Override
	public void onResume(){
		super.onResume();
		IntentFilter filter = new IntentFilter(REFRESH_OFFLINE_FILE_LIST);
		LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
	}

	@Override
	public void onPause(){
		super.onPause();
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
	}

	public void activateActionMode(){
		log("activateActionMode");
		if (!adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
		}
	}

	public void updateScrollPosition(int position) {
		log("updateScrollPosition");
		if (adapter != null) {
			if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST && mLayoutManager != null) {
				mLayoutManager.scrollToPosition(position);
			}
			else if (gridLayoutManager != null) {
				gridLayoutManager.scrollToPosition(position);
			}
		}
	}
	
	private int getAdapterType() {
		return ((ManagerActivityLollipop)context).isList ? MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST : MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID;
	}
    
    public void addSectionTitle(List<MegaOffline> nodes) {
	    if(adapter != null) {
	        adapter.setRecylerView(recyclerView);
        }
        Map<Integer, String> sections = new HashMap<>();
        int folderCount = 0;
        int fileCount = 0;
        for (MegaOffline node : nodes) {
            if (node == null) {
                continue;
            }
            if (node.isFolder()) {
                folderCount++;
            } else {
                fileCount++;
            }
        }

        if (getAdapterType() == MegaNodeAdapter.ITEM_VIEW_TYPE_GRID) {
            int spanCount = 2;
            if (recyclerView instanceof NewGridRecyclerView) {
                spanCount = ((NewGridRecyclerView)recyclerView).getSpanCount();
            }
            if(folderCount > 0) {
                for (int i = 0;i < spanCount;i++) {
                    sections.put(i, getString(R.string.general_folders));
                }
            }
            
            if(fileCount > 0 ) {
                placeholderCount =  (folderCount % spanCount) == 0 ? 0 : spanCount - (folderCount % spanCount);
                if (placeholderCount == 0) {
                    for (int i = 0;i < spanCount;i++) {
                        sections.put(folderCount + i, getString(R.string.general_files));
                    }
                } else {
                    for (int i = 0;i < spanCount;i++) {
                        sections.put(folderCount + placeholderCount + i, getString(R.string.general_files));
                    }
                }
            }
        } else {
            placeholderCount = 0;
            sections.put(0, getString(R.string.general_folders));
            sections.put(folderCount, getString(R.string.general_files));
        }
		if (headerItemDecoration == null) {
			headerItemDecoration = new NewHeaderItemDecoration(context);
			recyclerView.addItemDecoration(headerItemDecoration);
		}
		headerItemDecoration.setType(getAdapterType());
		headerItemDecoration.setKeys(sections);
    }

	public ImageView getImageDrag(int position) {
		log("getImageDrag");
		if (adapter != null) {
			if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST && mLayoutManager != null) {
				View v = mLayoutManager.findViewByPosition(position);
				if (v != null) {
					return (ImageView) v.findViewById(R.id.offline_list_thumbnail);
				}
			}
			else if (gridLayoutManager != null){
				View v = gridLayoutManager.findViewByPosition(position);
                MegaOffline offline = adapter.getItemOff(position);
                if (v != null && offline != null) {
                    if (MimeTypeThumbnail.typeForName(offline.getName()).isImage()){
                        return v.findViewById(R.id.file_grid_thumbnail);
                    } else {
                        //videos don't have thumnail, only have icon.here should use the ImageView of icon.
                        return v.findViewById(R.id.file_grid_icon_for_file);
                    }
                }
			}
		}

		return null;
	}

	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			log("ActionBarCallBack::onActionItemClicked");
			List<MegaOffline> documents = adapter.getSelectedOfflineNodes();
			
			switch(item.getItemId()){
				case R.id.cab_menu_download:{
					
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);	
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.prepareForDownload(handleList, false);
					break;
				}
				case R.id.cab_menu_rename:{

					if (documents.size()==1){
						String path = documents.get(0).getPath() + documents.get(0).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							break;
						}
						((ManagerActivityLollipop) context).showRenameDialog(n, n.getName());
					}
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_share_link:{

					if (documents.size()==1){
						String path = documents.get(0).getPath() + documents.get(0).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							break;
						}
						NodeController nC = new NodeController(context);
						nC.exportLink(n);
					}

					break;
				}
				case R.id.cab_menu_share:{
					//Check that all the selected options are folders
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.selectContactToShareFolders(handleList);
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_move:{					
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);			
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}
					NodeController nC = new NodeController(context);
					nC.chooseLocationToMoveNodes(handleList);
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_copy:{
					ArrayList<Long> handleList = new ArrayList<Long>();					
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToCopyNodes(handleList);
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_delete:{
					((ManagerActivityLollipop) context).showConfirmationRemoveSomeFromOffline(documents);
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_select_all:{
					selectAll();
					break;
				}
				case R.id.cab_menu_unselect_all:{
					hideMultipleSelect();
					break;
				}				
			}
			return false;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			log("ActionBarCallBack::onCreateActionMode");
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.offline_browser_action, menu);
			((ManagerActivityLollipop) context).showHideBottomNavigationView(true);
			((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ACCENT);
			checkScroll();
			return true;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			log("ActionBarCallBack::onDestroyActionMode");
			hideMultipleSelect();
			adapter.setMultipleSelect(false);
			((ManagerActivityLollipop) context).showHideBottomNavigationView(false);
			((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ZERO_DELAY);
			checkScroll();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			log("ActionBarCallBack::onPrepareActionMode");
//			ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white)
			List<MegaOffline> selected = adapter.getSelectedOfflineNodes();
			
			if (Util.isOnline(context)){
				if (selected.size() != 0) {
					menu.findItem(R.id.cab_menu_download).setVisible(false);
					menu.findItem(R.id.cab_menu_share).setVisible(false);

					if(selected.size() == adapter.getItemCountWithoutRK()){
						menu.findItem(R.id.cab_menu_select_all).setVisible(false);
						menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);
					}else{
						menu.findItem(R.id.cab_menu_select_all).setVisible(true);
						menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);	
					}

				}else{

					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					menu.findItem(R.id.cab_menu_download).setVisible(false);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
				}
				menu.findItem(R.id.cab_menu_share_link).setVisible(false);
				menu.findItem(R.id.cab_menu_copy).setVisible(false);
				menu.findItem(R.id.cab_menu_move).setVisible(false);				
				menu.findItem(R.id.cab_menu_delete).setVisible(true);
				menu.findItem(R.id.cab_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				menu.findItem(R.id.cab_menu_rename).setVisible(false);
			}
			else{
				if (selected.size() != 0) {
					menu.findItem(R.id.cab_menu_delete).setVisible(true);
					menu.findItem(R.id.cab_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

					if(selected.size()==adapter.getItemCountWithoutRK()){
						menu.findItem(R.id.cab_menu_select_all).setVisible(false);
						menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);			
					}else{
						menu.findItem(R.id.cab_menu_select_all).setVisible(true);
						menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);	
					}	
				}else{
					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
				}

				menu.findItem(R.id.cab_menu_download).setVisible(false);			
				menu.findItem(R.id.cab_menu_copy).setVisible(false);
				menu.findItem(R.id.cab_menu_move).setVisible(false);
				menu.findItem(R.id.cab_menu_share_link).setVisible(false);
				menu.findItem(R.id.cab_menu_rename).setVisible(false);
			}
			return false;
		}
		
	}

	public void selectAll(){
		log("selectAll");
		if (adapter != null){
			if(adapter.isMultipleSelect()){
				adapter.selectAll();
			}
			else{
				adapter.setMultipleSelect(true);
				adapter.selectAll();
				
				actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
			}
			
			updateActionModeTitle();
		}
	}
	
	public boolean showSelectMenuItem(){
		log("showSelectMenuItem");
		if (adapter != null){
			return adapter.isMultipleSelect();
		}
		
		return false;
	}
		
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		log("onCreate");
		
		if (Util.isOnline(context)){
			if (megaApi == null){
				megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
			}
		}
		else{
			megaApi=null;
		}			
		
//		dbH = new DatabaseHandler(context);
		lastPositionStack = new Stack<>();

		dbH = DatabaseHandler.getDbHandler(context);
		
		mOffList = new ArrayList<MegaOffline>();
	}

	public void checkScroll () {
		if (recyclerView != null ) {
			if (recyclerView.canScrollVertically(-1) || (adapter != null && adapter.isMultipleSelect())) {
				((ManagerActivityLollipop) context).changeActionBarElevation(true);
			}
			else {
				((ManagerActivityLollipop) context).changeActionBarElevation(false);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		if (aB == null){
			aB = ((AppCompatActivity)context).getSupportActionBar();
		}

		if (context instanceof ManagerActivityLollipop){

			String pathNavigationOffline = ((ManagerActivityLollipop)context).getPathNavigationOffline();
			if(pathNavigationOffline!=null){
				pathNavigation = pathNavigationOffline;
			}

			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
//		    isList = ((ManagerActivityLollipop)context).isList();
			orderGetChildren = ((ManagerActivityLollipop)context).getOrderOthers();
		}

		display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;

		//Check pathNAvigation
		if (((ManagerActivityLollipop)context).isList){
			log("onCreateList");
			View v = inflater.inflate(R.layout.fragment_offlinelist, container, false);
			recyclerView = (RecyclerView) v.findViewById(R.id.offline_view_browser);
            recyclerView.removeItemDecoration(headerItemDecoration);
            headerItemDecoration = null;
//			recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
			mLayoutManager = new LinearLayoutManager(context);
			recyclerView.setLayoutManager(mLayoutManager);
			//Add bottom padding for recyclerView like in other fragments.
			recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
			recyclerView.setClipToPadding(false);
			recyclerView.setItemAnimator(new DefaultItemAnimator());
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					checkScroll();
				}
			});

			emptyImageView = (ImageView) v.findViewById(R.id.offline_empty_image);
			emptyTextView = (LinearLayout) v.findViewById(R.id.offline_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.offline_empty_text_first);
			contentTextLayout = (RelativeLayout) v.findViewById(R.id.offline_content_text_layout);
			contentText = (TextView) v.findViewById(R.id.offline_content_text);			

			findNodes();
			addSectionTitle(mOffList);
			if (adapter == null){
				adapter = new MegaOfflineLollipopAdapter(this, context, mOffList, recyclerView, emptyImageView, emptyTextView, aB, MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
				adapter.setPositionClicked(-1);
				adapter.setMultipleSelect(false);
				recyclerView.setAdapter(adapter);

				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.ic_empty_offline);
					}
					String textToShow = getString(R.string.context_empty_offline);
					try {
						textToShow = textToShow.replace("[A]","<font color=\'#000000\'>");
						textToShow = textToShow.replace("[/A]","</font>");
						textToShow = textToShow.replace("[B]","<font color=\'#7a7a7a\'>");
						textToShow = textToShow.replace("[/B]","</font>");
					} catch (Exception e) {
						e.printStackTrace();
					}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextViewFirst.setText(result);
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
//					contentText.setText(getInfoFolder(mOffList));
				}
			}
			else{
			    adapter.setRecylerView(recyclerView);
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			}

			return v;
		}
		else{
			log("onCreateGRID");
			View v = inflater.inflate(R.layout.fragment_offlinegrid, container, false);
			
			recyclerView = (NewGridRecyclerView) v.findViewById(R.id.offline_view_browser_grid);
			recyclerView.removeItemDecoration(headerItemDecoration);
			headerItemDecoration = null;
			recyclerView.setHasFixedSize(true);
			gridLayoutManager = (CustomizedGridLayoutManager) recyclerView.getLayoutManager();

			recyclerView.setItemAnimator(new DefaultItemAnimator());
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					checkScroll();
				}
			});
			
			emptyImageView = (ImageView) v.findViewById(R.id.offline_empty_image_grid);
			emptyTextView = (LinearLayout) v.findViewById(R.id.offline_empty_text_grid);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.offline_empty_text_grid_first);
			contentTextLayout = (RelativeLayout) v.findViewById(R.id.offline_content_grid_text_layout);

			contentText = (TextView) v.findViewById(R.id.offline_content_text_grid);			

			findNodes();
			addSectionTitle(mOffList);
			if (adapter == null){
				adapter = new MegaOfflineLollipopAdapter(this, context, mOffList, recyclerView, emptyImageView, emptyTextView, aB, MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
				adapter.setPositionClicked(-1);
				adapter.setMultipleSelect(false);
				recyclerView.setAdapter(adapter);

				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.ic_empty_offline);
					}
					String textToShow = getString(R.string.context_empty_offline);
					try{
						textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
						textToShow = textToShow.replace("[/A]", "</font>");
						textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
						textToShow = textToShow.replace("[/B]", "</font>");
					}
					catch (Exception e){}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextViewFirst.setText(result);
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
//					contentText.setText(getInfoFolder(mOffList));
				}
			}
			else{
                adapter.setRecylerView(recyclerView);
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			}

			return v;
		}		
	}

	public void findNodes(){
		log("findNodes");

		if((getActivity() == null) || (!isAdded())){
			log("Fragment NOT Attached!");
			return;
		}

		mOffList=dbH.findByPath(pathNavigation);

		log("Number of elements: "+mOffList.size());

		for(int i=0; i<mOffList.size();i++){

			MegaOffline checkOffline = mOffList.get(i);
			File offlineDirectory = null;
			if(checkOffline.getOrigin()==MegaOffline.INCOMING){

				log("isIncomingOffline");

				if (Environment.getExternalStorageDirectory() != null){
					offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" +checkOffline.getHandleIncoming() + "/" + checkOffline.getPath()+checkOffline.getName());
					log("offlineDirectory: "+offlineDirectory);
				}
				else{
					offlineDirectory = context.getFilesDir();
				}
			}
			else if(checkOffline.getOrigin()==MegaOffline.INBOX){

				if (Environment.getExternalStorageDirectory() != null){
					offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/" + checkOffline.getPath()+checkOffline.getName());
					log("offlineDirectory: "+offlineDirectory);
				}
				else{
					offlineDirectory = context.getFilesDir();
				}
			}
			else{
				log("FROM other origin");

				if (Environment.getExternalStorageDirectory() != null){
					offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + checkOffline.getPath()+checkOffline.getName());
				}
				else{
					offlineDirectory = context.getFilesDir();
				}

				if (!offlineDirectory.exists()){
					log("Not exists for not incoming offline");

					if (Environment.getExternalStorageDirectory() != null){
						offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in" + checkOffline.getPath()+checkOffline.getName());
					}
					else{
						offlineDirectory = context.getFilesDir();
					}
				}
			}

			if(offlineDirectory!=null){
				if (!offlineDirectory.exists()){
					log("Path to remove B: "+(mOffList.get(i).getPath()+mOffList.get(i).getName()));
					//dbH.removeById(mOffList.get(i).getId());
					mOffList.remove(i);
					i--;
				}
			}
			addSectionTitle(mOffList);
		}

		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}

		if(adapter!=null){
			adapter.setNodes(mOffList);

			adapter.setPositionClicked(-1);
			adapter.setMultipleSelect(false);
			
			recyclerView.setAdapter(adapter);

			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				contentTextLayout.setVisibility(View.GONE);
				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.ic_empty_offline);
				}

				String textToShow = getString(R.string.context_empty_offline);
				try{
					textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
					textToShow = textToShow.replace("[/A]", "</font>");
					textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
					textToShow = textToShow.replace("[/B]", "</font>");
				}
				catch (Exception e){}
				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}
				emptyTextViewFirst.setText(result);
			}
			else{
				recyclerView.setVisibility(View.VISIBLE);
				contentTextLayout.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
//				contentText.setText(getInfoFolder(mOffList));
			}
		}
	}

	public void sortByNameDescending(){
		log("sortByNameDescending");
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();
		
		//Remove MK before sorting
		if(mOffList.size()>0){
			MegaOffline lastItem = mOffList.get(mOffList.size()-1);
			if(lastItem.getHandle().equals("0")){
				mOffList.remove(mOffList.size()-1);
			}
		}		
		else{
			return;
		}
		
		for(int k = 0; k < mOffList.size() ; k++) {
			MegaOffline node = mOffList.get(k);
			if(node.getType().equals("1")){
				foldersOrder.add(node.getName());
			}
			else{
				filesOrder.add(node.getName());
			}
		}		
	
		Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.reverse(foldersOrder);
		Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.reverse(filesOrder);

		for(int k = 0; k < foldersOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = foldersOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));
				}				
			}
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = filesOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));					
				}				
			}
		}
		
		mOffList.clear();
		mOffList.addAll(tempOffline);
		if (adapter!= null) {
			adapter.setNodes(mOffList);
		}
//		contentText.setText(getInfoFolder(mOffList));
	}
    
    
    public void sortByNameAscending() {
		log("sortByNameAscending");
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();
		
		//Remove MK before sorting
		if(mOffList.size()>0){
			MegaOffline lastItem = mOffList.get(mOffList.size()-1);
			if(lastItem.getHandle().equals("0")){
				mOffList.remove(mOffList.size()-1);
			}
		}		
		else{
			return;
		}
				
		for(int k = 0; k < mOffList.size() ; k++) {
			MegaOffline node = mOffList.get(k);
			if(node == null) {
			    continue;
            }
			if(node.getType().equals("1")){
				foldersOrder.add(node.getName());
			}
			else{
				filesOrder.add(node.getName());
			}
		}		
	
		Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);

		for(int k = 0; k < foldersOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = foldersOrder.get(k);
                MegaOffline offline = mOffList.get(j);
                if(offline == null) {
                    continue;
                }
                String nameOffline = offline.getName();
				if(name.equals(nameOffline)){
					tempOffline.add(offline);
				}				
			}			
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = filesOrder.get(k);
                MegaOffline offline = mOffList.get(j);
                if(offline == null) {
                    continue;
                }
                String nameOffline = offline.getName();
				if(name.equals(nameOffline)){
					tempOffline.add(offline);
				}				
			}
			
		}
		
		mOffList.clear();
		mOffList.addAll(tempOffline);
		if (adapter!= null) {
			adapter.setNodes(mOffList);
		}
//		contentText.setText(getInfoFolder(mOffList));
	}
	
//	public void updateView (){
//		log("updateView");
//		mOffList=dbH.findByPath(pathNavigation);
//		
//		for(int i=0; i<mOffList.size();i++){
//			
//			File offlineDirectory = null;
//			if (Environment.getExternalStorageDirectory() != null){
//				offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + mOffList.get(i).getPath()+mOffList.get(i).getName());
//			}
//			else{
//				offlineDirectory = context.getFilesDir();
//			}	
//			
//			if (!offlineDirectory.exists()){
//				dbH.removeById(mOffList.get(i).getId());
//				mOffList.remove(i);
//				
//			}			
//		}
//		this.setNodes(mOffList);
//	}
//	

	public boolean isFolder(String path){
		log("isFolder");

		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return false;
		}
		
		if(n.isFile()){
			return false;
		}
		else{
			return true;
		}		
	}
	
	private String getInfoFolder(ArrayList<MegaOffline> mOffInfo) {
		log("getInfoFolder");
//		log("primer elemento: "+mOffInfo.get(0).getName());
		
		String info = "";
		int numFolders=0;
		int numFiles=0;
		
		String pathI=null;
		
		if(mOffInfo.size()>0){
			if(mOffInfo.get(0).getOrigin()==MegaOffline.INCOMING){
				pathI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + mOffInfo.get(0).getHandleIncoming() + "/";
			}
			else if(mOffInfo.get(0).getOrigin()==MegaOffline.INBOX){
				pathI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/";
			}
			else{
				pathI= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
			}	
			
			for(int i=0; i<mOffInfo.size();i++){
				MegaOffline mOff = (MegaOffline) mOffInfo.get(i);
				String path = pathI + mOff.getPath() + mOff.getName();			

				File destination = new File(path);
				if (destination.exists()){
					if(destination.isFile()){
						numFiles++;					
					}
					else{
						numFolders++;					
					}
				}
				else{
					log("File do not exist");
				}		
			}
		}
		
		//Check if the file MarterKey is exported
		String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
		log("Export in: "+path);
		File file= new File(path);
		if(file.exists()){
			numFiles++;
		}
		
		if (numFolders > 0) {
			info = numFolders
					+ " "
					+ context.getResources().getQuantityString(
							R.plurals.general_num_folders, numFolders);
			if (numFiles > 0) {
				info = info
						+ ", "
						+ numFiles
						+ " "
						+ context.getResources().getQuantityString(
								R.plurals.general_num_files, numFiles);
			}
		} else {
			info = numFiles
					+ " "
					+ context.getResources().getQuantityString(
							R.plurals.general_num_files, numFiles);
		}

		log(info);
		return info;
	}

	@Override
    public void onAttach(Activity activity) {
		log("onAttach");
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
        if(mOffList != null && mOffList.size() != 0) {
			addSectionTitle(mOffList);
		}
    }

    public void itemClick(int position, int[] screenPosition, ImageView imageView) {
		log("itemClick");
		//Otherwise out of bounds exception happens.
		if(position >= adapter.folderCount && getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID && placeholderCount != 0) {
			position -= placeholderCount;
		}
		if (adapter.isMultipleSelect()){
			log("multiselect");
			MegaOffline item = mOffList.get(position);
			if(!(item.getHandle().equals("0"))){
				adapter.toggleSelection(position);
				List<MegaOffline> selectedNodes = adapter.getSelectedOfflineNodes();
				if (selectedNodes.size() > 0){
					updateActionModeTitle();

				}
			}
		}
		else{
			MegaOffline currentNode = mOffList.get(position);
			File currentFile=null;
			
			if(currentNode.getHandle().equals("0")){
				log("click on Master Key");
				String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
				openFile(new File(path));
//				viewIntent.setDataAndType(Uri.fromFile(new File(path)), MimeTypeList.typeForName("MEGAMasterKey.txt").getType());
//				((ManagerActivityLollipop)context).clickOnMasterKeyFile();
				return;
			}
						
			if(currentNode.getOrigin()==MegaOffline.INCOMING){
				String handleString = currentNode.getHandleIncoming();
				currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + handleString + "/"+currentNode.getPath() + "/" + currentNode.getName());
			}
			else if(currentNode.getOrigin()==MegaOffline.INBOX){
				String handleString = currentNode.getHandleIncoming();
				currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/"+currentNode.getPath() + "/" + currentNode.getName());
			}
			else{
				currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + currentNode.getPath() + "/" + currentNode.getName());
			}
			
			if(currentFile.exists() && currentFile.isDirectory()){

				int lastFirstVisiblePosition = 0;
				if(((ManagerActivityLollipop)context).isList){
					lastFirstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
				}
				else{
					lastFirstVisiblePosition = ((NewGridRecyclerView) recyclerView).findFirstCompletelyVisibleItemPosition();
					if(lastFirstVisiblePosition==-1){
						log("Completely -1 then find just visible position");
						lastFirstVisiblePosition = ((NewGridRecyclerView) recyclerView).findFirstVisibleItemPosition();
					}
				}

				log("Push to stack "+lastFirstVisiblePosition+" position");
				lastPositionStack.push(lastFirstVisiblePosition);

				pathNavigation= currentNode.getPath()+ currentNode.getName()+"/";
				
				if (context instanceof ManagerActivityLollipop){
					((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
					((ManagerActivityLollipop)context).setPathNavigationOffline(pathNavigation);
				}
				((ManagerActivityLollipop)context).setToolbarTitle();

				mOffList=dbH.findByPath(currentNode.getPath()+currentNode.getName()+"/");
				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);						
				}
				else{
					File offlineDirectory = null;
					String path;

					if(currentNode.getOrigin()==MegaOffline.INCOMING){
						path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming();
					}
					else if(currentNode.getOrigin()==MegaOffline.INBOX){
						path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in";
					}
					else{							
						path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
						log("Path NOT INCOMING: "+path);							
					}						
											
					for(int i=0; i<mOffList.size();i++){
						
						if (Environment.getExternalStorageDirectory() != null){								
							log("mOffList path: "+path+mOffList.get(i).getPath());
							offlineDirectory = new File(path + mOffList.get(i).getPath()+mOffList.get(i).getName());
						}
						else{
							offlineDirectory = context.getFilesDir();
						}	

						if (!offlineDirectory.exists()){
							//Updating the DB because the file does not exist	
							log("Path to remove C: "+(path + mOffList.get(i).getName()));
							dbH.removeById(mOffList.get(i).getId());
							mOffList.remove(i);
							i--;
						}			
					}
				}
//				contentText.setText(getInfoFolder(mOffList));
				adapter.setNodes(mOffList);
				
				if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
					sortByNameDescending();
				}
				else{
					sortByNameAscending();
				}
				
				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.ic_empty_offline);
					}
					String textToShow = getString(R.string.context_empty_offline);
					try{
						textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
						textToShow = textToShow.replace("[/A]", "</font>");
						textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
						textToShow = textToShow.replace("[/B]", "</font>");
					}
					catch (Exception e){}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextViewFirst.setText(result);
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
				}
				
				adapter.setPositionClicked(-1);
				recyclerView.scrollToPosition(0);
				notifyDataSetChanged();
			}
			else{
				if(currentFile.exists() && currentFile.isFile()){			
					
					//Open it!
					if(MimeTypeList.typeForName(currentFile.getName()).isZip()){
						log("MimeTypeList ZIP");
						Intent intentZip = new Intent();
						intentZip.setClass(context, ZipBrowserActivityLollipop.class);
						intentZip.setAction(ZipBrowserActivityLollipop.ACTION_OPEN_ZIP_FILE);
						intentZip.putExtra(ZipBrowserActivityLollipop.EXTRA_ZIP_FILE_TO_OPEN, pathNavigation);
						intentZip.putExtra(ZipBrowserActivityLollipop.EXTRA_PATH_ZIP, currentFile.getAbsolutePath());
						context.startActivity(intentZip);
					}
					else if (MimeTypeList.typeForName(currentFile.getName()).isImage()){
						Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
                        intent.putExtra("placeholder", placeholderCount);
						intent.putExtra("position", position);
						intent.putExtra("adapterType", Constants.OFFLINE_ADAPTER);
						intent.putExtra("parentNodeHandle", -1L);
						intent.putExtra("offlinePathDirectory", currentFile.getParent());
						intent.putExtra("pathNavigation", pathNavigation);
						intent.putExtra("orderGetChildren", orderGetChildren);
						intent.putExtra("screenPosition", screenPosition);

						startActivity(intent);
						((ManagerActivityLollipop) context).overridePendingTransition(0,0);
						imageDrag = imageView;
					}
					else if (MimeTypeList.typeForName(currentFile.getName()).isVideoReproducible() || MimeTypeList.typeForName(currentFile.getName()).isAudio()) {
						log("Video file");

						Intent mediaIntent;
						boolean internalIntent;
						boolean opusFile = false;
						if (MimeTypeList.typeForName(currentFile.getName()).isVideoNotSupported() || MimeTypeList.typeForName(currentFile.getName()).isAudioNotSupported()) {
							mediaIntent = new Intent(Intent.ACTION_VIEW);
							internalIntent = false;
							String[] s = currentFile.getName().split("\\.");
							if (s != null && s.length > 1 && s[s.length-1].equals("opus")) {
								opusFile = true;
							}
						}
						else {
							internalIntent = true;
							mediaIntent = new Intent(context, AudioVideoPlayerLollipop.class);
						}

						mediaIntent.putExtra("HANDLE", Long.parseLong(currentNode.getHandle()));
						mediaIntent.putExtra("FILENAME", currentNode.getName());
						mediaIntent.putExtra("path", currentFile.getAbsolutePath());
						mediaIntent.putExtra("adapterType", Constants.OFFLINE_ADAPTER);
                        mediaIntent.putExtra("placeholder", placeholderCount);
						mediaIntent.putExtra("position", position);
						mediaIntent.putExtra("parentNodeHandle", -1L);
						mediaIntent.putExtra("offlinePathDirectory", currentFile.getParent());
						mediaIntent.putExtra("pathNavigation", pathNavigation);
						mediaIntent.putExtra("orderGetChildren", orderGetChildren);
						mediaIntent.putExtra("screenPosition", screenPosition);
						mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							mediaIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
						}
						else{
							mediaIntent.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
						}
						if (opusFile){
							mediaIntent.setDataAndType(mediaIntent.getData(), "audio/*");
						}
						if (internalIntent){
							startActivity(mediaIntent);
						}
						else {
							if (MegaApiUtils.isIntentAvailable(context, mediaIntent)){
								startActivity(mediaIntent);
							}
							else {
								((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.intent_not_available), -1);

								Intent intentShare = new Intent(Intent.ACTION_SEND);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
									intentShare.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
								}
								else {
									intentShare.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
								}
								intentShare.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								if (MegaApiUtils.isIntentAvailable(context, intentShare)) {
									log("call to startActivity(intentShare)");
									context.startActivity(intentShare);
								}
							}
						}
						((ManagerActivityLollipop) context).overridePendingTransition(0,0);
						imageDrag = imageView;
					}
					else if (MimeTypeList.typeForName(currentFile.getName()).isPdf()){
						log("Pdf file");

						//String localPath = Util.getLocalFile(context, currentFile.getName(), currentFile.get, currentFile.getParent());

						Intent pdfIntent = new Intent(context, PdfViewerActivityLollipop.class);

						pdfIntent.putExtra("inside", true);
						pdfIntent.putExtra("HANDLE", Long.parseLong(currentNode.getHandle()));
						pdfIntent.putExtra("adapterType", Constants.OFFLINE_ADAPTER);
						pdfIntent.putExtra("path", currentFile.getAbsolutePath());
						pdfIntent.putExtra("pathNavigation", pathNavigation);
						pdfIntent.putExtra("screenPosition", screenPosition);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							pdfIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
						}
						else{
							pdfIntent.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
						}
						pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						context.startActivity(pdfIntent);
						((ManagerActivityLollipop) context).overridePendingTransition(0,0);
						imageDrag = imageView;
					}
					else if (MimeTypeList.typeForName(currentFile.getName()).isURL()) {
						log("Is URL file");
						InputStream instream = null;
						try {
							// open the file for reading
							instream = new FileInputStream(currentFile.getAbsolutePath());

							// if file the available for reading
							if (instream != null) {
								// prepare the file for reading
								InputStreamReader inputreader = new InputStreamReader(instream);
								BufferedReader buffreader = new BufferedReader(inputreader);

								String line1 = buffreader.readLine();
								if (line1 != null) {
									String line2 = buffreader.readLine();

									String url = line2.replace("URL=", "");

									log("Is URL - launch browser intent");
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse(url));
									startActivity(i);
								} else {
									log("Not expected format: Exception on processing url file");
									openFile(currentFile);
								}
							}
						} catch (Exception ex) {

							openFile(currentFile);

						} finally {
							// close the file.
							try {
								instream.close();
							} catch (IOException e) {
								log("EXCEPTION closing InputStream");
							}
						}
					}
					else{
						openFile(currentFile);
					}
				}
			}
		}
    }
    
    public void openFile (File currentFile){
		log("openFile");
    	Intent viewIntent = new Intent(Intent.ACTION_VIEW);

    	String type = "";
		if (MimeTypeList.typeForName(currentFile.getName()).isURL()){
			type = "text/plain";
		}
		else{
			type = MimeTypeList.typeForName(currentFile.getName()).getType();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			viewIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", currentFile), type);
		}
		else{
			viewIntent.setDataAndType(Uri.fromFile(currentFile), type);
		}
		viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if (MegaApiUtils.isIntentAvailable(context, viewIntent)){
			context.startActivity(viewIntent);
		}
		else{
			Intent intentShare = new Intent(Intent.ACTION_SEND);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				intentShare.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
			}
			else{
				intentShare.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
			}
			intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			if (MegaApiUtils.isIntentAvailable(context, intentShare)){
				context.startActivity(intentShare);
			}
		}
    }
	
	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
		}else{
//			if(adapter.getItemCount() == 1 ){
//				boolean result = adapter.isRecoveryKey(adapter.getItemOff(0));
//				if(result){
//
//					adapter.clearSelections();
//				}
//			}
		}
	}
	
	private void updateActionModeTitle() {
		log("updateActionModeTitle");
		if (actionMode == null || getActivity() == null) {
			return;
		}
		List<MegaOffline> documents = adapter.getSelectedOfflineNodes();
		int folders=0;
		int files=0;
		
		String pathI=null;
		
		if(documents.size()>0){
			if(documents.get(0).getOrigin()==MegaOffline.INCOMING){
				pathI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + documents.get(0).getHandleIncoming() + "/";
			}
			else if(documents.get(0).getOrigin()==MegaOffline.INBOX){
				pathI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/";
			}
			else{
				pathI= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
			}	
			
			for(int i=0; i<documents.size();i++){
				MegaOffline mOff = (MegaOffline) documents.get(i);
				String path = pathI + mOff.getPath() + mOff.getName();			

				File destination = new File(path);
				if (destination.exists()){
					if(destination.isFile()){
						files++;					
					}
					else{
						folders++;					
					}
				}
				else{
					log("File do not exist");
				}		
			}
		}
		
		Resources res = getActivity().getResources();
		/*String format = "%d %s";
		String filesStr = String.format(format, files,
				res.getQuantityString(R.plurals.general_num_files, files));
		String foldersStr = String.format(format, folders,
				res.getQuantityString(R.plurals.general_num_folders, folders));
		String title;
		if (files == 0 && folders == 0) {
			title = foldersStr + ", " + filesStr;
		} else if (files == 0) {
			title = foldersStr;
		} else if (folders == 0) {
			title = filesStr;
		} else {
			title = foldersStr + ", " + filesStr;
		}*/

		String title;
		int sum=files+folders;

		if (files == 0 && folders == 0) {
			title = Integer.toString(sum);
		} else if (files == 0) {
			title = Integer.toString(folders);
		} else if (folders == 0) {
			title = Integer.toString(files);
		} else {
			title = Integer.toString(sum);
		}
		actionMode.setTitle(title);
		try {
			actionMode.invalidate();
		} catch (NullPointerException e) {
			e.printStackTrace();
			log("oninvalidate error");
		}
		// actionMode.
	}
	
	/*
	 * Disable selection
	 */
	public void hideMultipleSelect() {
		adapter.clearSelections();
		adapter.setMultipleSelect(false);

		if (actionMode != null) {
			actionMode.finish();
		}
	}
	
	public int onBackPressed(){
		log("onBackPressed");

		if (adapter == null){
			return 0;
		}

		if (adapter.getPositionClicked() != -1){
			adapter.setPositionClicked(-1);
			adapter.notifyDataSetChanged();
			return 1;
		}
		else if(pathNavigation != null){
			if(pathNavigation.isEmpty()){
				return 0;
			}
			if (!pathNavigation.equals("/")){
				log("onBackPress: "+pathNavigation);
				pathNavigation=pathNavigation.substring(0,pathNavigation.length()-1);
				log("substring: "+pathNavigation);
				int index=pathNavigation.lastIndexOf("/");				
				pathNavigation=pathNavigation.substring(0,index+1);
				
				if (context instanceof ManagerActivityLollipop){
					((ManagerActivityLollipop)context).setPathNavigationOffline(pathNavigation);
					((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
					((ManagerActivityLollipop)context).setToolbarTitle();
				}

//				ArrayList<MegaOffline> mOffListNavigation= new ArrayList<MegaOffline>();
				mOffList = dbH.findByPath(pathNavigation);
				
//				contentText.setText(getInfoFolder(mOffList));
				
				if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
					sortByNameDescending();
				}
				else{
					sortByNameAscending();
				}

				setNodes(mOffList);

				int lastVisiblePosition = 0;
				if(!lastPositionStack.empty()){
					lastVisiblePosition = lastPositionStack.pop();
					log("Pop of the stack "+lastVisiblePosition+" position");
				}
				log("Scroll to "+lastVisiblePosition+" position");

				if(lastVisiblePosition>=0){

					if(((ManagerActivityLollipop)context).isList){
						mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
					else{
						gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
				}

				//adapterList.setNodes(mOffList);
				
				return 2;
			}
			else{
				log("pathNavigation  / ");
				return 0;
			}
		}
		else{
				return 0;
		}
	}
	
	public RecyclerView getRecyclerView(){
		return recyclerView;
	}
	
	public void setNodes(ArrayList<MegaOffline> _mOff){
		log("setNodes");
		this.mOffList = _mOff;

		if (adapter != null){
			adapter.setNodes(mOffList);
			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.ic_empty_offline);
				}
				String textToShow = getString(R.string.context_empty_offline);
				try{
					textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
					textToShow = textToShow.replace("[/A]", "</font>");
					textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
					textToShow = textToShow.replace("[/B]", "</font>");
				}
				catch (Exception e){}
				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}
				emptyTextViewFirst.setText(result);
			}
			else{
				recyclerView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}			
		}
	}
	
	public void setPositionClicked(int positionClicked){
		log("setPositionClicked");
		if (adapter != null){
			adapter.setPositionClicked(positionClicked);
		}
	}
	
	public void notifyDataSetChanged(){
		log("notifyDataSetChanged");
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}
	}

	public void refresh(){
		log("refresh");

		mOffList=dbH.findByPath(pathNavigation);


		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
		if (((ManagerActivityLollipop)context).isList) {
			addSectionTitle(mOffList);
			if (adapter == null) {
				adapter = new MegaOfflineLollipopAdapter(this,context,mOffList,recyclerView,emptyImageView,emptyTextView,aB,MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			} else {
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
				adapter.setNodes(mOffList);
				adapter.notifyDataSetChanged();
				recyclerView.invalidate();
			}
		} else {
			addSectionTitle(mOffList);
			if (adapter == null) {
				adapter = new MegaOfflineLollipopAdapter(this,context,mOffList,recyclerView,emptyImageView,emptyTextView,aB,MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			} else {
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
				adapter.setNodes(mOffList);
                adapter.notifyDataSetChanged();
                recyclerView.invalidate();
			}
		}
		if (adapter.getItemCount() == 0){
			recyclerView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);
			contentTextLayout.setVisibility(View.GONE);
			if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				emptyImageView.setImageResource(R.drawable.offline_empty_landscape);
			}else{
				emptyImageView.setImageResource(R.drawable.ic_empty_offline);
			}
			String textToShow = getString(R.string.context_empty_offline);
			try{
				textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
				textToShow = textToShow.replace("[/A]", "</font>");
				textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
				textToShow = textToShow.replace("[/B]", "</font>");
			}
			catch (Exception e){}
			Spanned result = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
				result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
			} else {
				result = Html.fromHtml(textToShow);
			}
			emptyTextViewFirst.setText(result);
			onBackPressed();

		}else{
			recyclerView.setVisibility(View.VISIBLE);
			contentTextLayout.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
//			contentText.setText(getInfoFolder(mOffList));
		}
		addSectionTitle(mOffList);
		setPositionClicked(-1);
		notifyDataSetChanged();
	}
	
	public void refreshPaths(){
		log("refreshPaths()");
		mOffList=dbH.findByPath("/");	
		
		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
		if (((ManagerActivityLollipop)context).isList) {
			addSectionTitle(mOffList);
			if (adapter == null) {
				adapter = new MegaOfflineLollipopAdapter(this,context,mOffList,recyclerView,emptyImageView,emptyTextView,aB,MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			} else {
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST);
				adapter.setNodes(mOffList);
			}
		} else {
			addSectionTitle(mOffList);
			if (adapter == null) {
				adapter = new MegaOfflineLollipopAdapter(this,context,mOffList,recyclerView,emptyImageView,emptyTextView,aB,MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			} else {
//				adapter.setAdapterType(MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID);
				adapter.setNodes(mOffList);
			}
		}
		addSectionTitle(mOffList);
//		setNodes(mOffList);
		recyclerView.invalidate();
	}
	
	public void refreshPaths(MegaOffline mOff){
		log("refreshPaths(MegaOffline mOff): "+mOff.getName());
		int index=0;
//		MegaOffline retFindPath = null;
		
		//Find in the tree, the last existing node
		String pNav= mOff.getPath();
		
		if(mOff.getType()==DB_FILE){
			
			index=pNav.lastIndexOf("/");				
			pNav=pNav.substring(0,index+1);
			
		}
		else{
			pNav=pNav.substring(0,pNav.length()-1);
		}	
			
		if(pNav.length()==0){
			mOffList=dbH.findByPath("/");
			pNav="/";
		}
		else{
			findPath(pNav);			
		}
				
		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}

		((ManagerActivityLollipop)context).setToolbarTitle();
		log("At the end of refreshPaths the path is: "+pathNavigation);
//		contentText.setText(getInfoFolder(mOffList));
		setNodes(mOffList);
	}
	
	public int getItemCount(){
		log("getItemCount");
		if(adapter != null){
			return adapter.getItemCount();
		}
		return 0;
	}

	public int getItemCountWithoutRK(){
		log("getItemCountWithoutRK");
		if(adapter != null){
			return adapter.getItemCountWithoutRK();
		}
		return 0;
	}
	
	private void findPath (String pNav){
		log("findPath():" + pNav);

		MegaOffline nodeToShow = null;
		
		if(!pNav.equals("/")){
			
			if (pNav.endsWith("/")) {
				pNav = pNav.substring(0, pNav.length() - 1);
				log("NEW findPath:" + pNav);		
			}
			
			int index=pNav.lastIndexOf("/");	
			String pathToShow = pNav.substring(0, index+1);
			log("Path: "+ pathToShow);
			String nameToShow = pNav.substring(index+1, pNav.length());
			log("Name: "+ nameToShow);
			
			nodeToShow = dbH.findbyPathAndName(pathToShow, nameToShow);
			if(nodeToShow!=null){
				//Show the node
				log("findPath:Node: "+ nodeToShow.getName());
				pathNavigation=pathToShow+nodeToShow.getName()+"/";
				log("findPath:pathNavigation: "+pathNavigation);
				return;
			}
			else{
				if(pathNavigation.equals("/")){
					log("Return Path /");
					return;
				}
				else{
					findPath(pathToShow);
				}				
			}
		}
		else{
			pathNavigation="/";
		}		
	}

	public void setPathNavigation(String _pathNavigation){
		log("setPathNavigation(): "+pathNavigation);
		this.pathNavigation = _pathNavigation;
		addSectionTitle(mOffList);
		if (adapter != null){
//			contentText.setText(getInfoFolder(mOffList));
			adapter.setNodes(dbH.findByPath(_pathNavigation));
		}
	}

	public void setOrder(int orderGetChildren){
		log("setOrder");
		this.orderGetChildren = orderGetChildren;
	}
	
	private static void log(String log) {
		Util.log("OfflineFragmentLollipop", log);
	}
	
//	private void dlog(Object o) {
//		String s = (o == null) ? "NULL" : o.toString();
//		Log.e("@#$","---> " + s);
//	}
	
	public String getPathNavigation() {
		log("getPathNavigation");
		return pathNavigation;
	}
}