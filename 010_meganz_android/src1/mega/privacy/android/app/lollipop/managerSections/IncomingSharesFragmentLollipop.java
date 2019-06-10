package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import android.widget.Toast;

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
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.CustomizedGridLayoutManager;
import mega.privacy.android.app.components.NewGridRecyclerView;
import mega.privacy.android.app.components.NewHeaderItemDecoration;
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaNodeAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;


public class IncomingSharesFragmentLollipop extends Fragment{

	public static ImageView imageDrag;

	Context context;
	RecyclerView recyclerView;
	LinearLayoutManager mLayoutManager;
	CustomizedGridLayoutManager gridLayoutManager;
	FastScroller fastScroller;

	ImageView emptyImageView;
	LinearLayout emptyTextView;
	TextView emptyTextViewFirst;

	MegaNodeAdapter adapter;
	IncomingSharesFragmentLollipop incomingSharesFragment = this;
	
	public NewHeaderItemDecoration headerItemDecoration;

	RelativeLayout transfersOverViewLayout;

	Stack<Integer> lastPositionStack;

	MegaApiAndroid megaApi;

	float density;
	DisplayMetrics outMetrics;
	Display display;

	ArrayList<MegaNode> nodes;
	MegaNode selectedNode;

	DatabaseHandler dbH;
	MegaPreferences prefs;
	String downloadLocationDefaultPath = Util.downloadDIR;

	public ActionMode actionMode;
	
	private int placeholderCount;

	public void activateActionMode(){
		if (!adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
		}
	}

	public void updateScrollPosition(int position) {
		log("updateScrollPosition");
		if (adapter != null) {
			if (adapter.getAdapterType() == MegaNodeAdapter.ITEM_VIEW_TYPE_LIST && mLayoutManager != null) {
				mLayoutManager.scrollToPosition(position);
			}
			else if (gridLayoutManager != null) {
				gridLayoutManager.scrollToPosition(position);
			}
		}
	}


	public ImageView getImageDrag(int position) {
		log("getImageDrag");
		if (adapter != null) {
			if (adapter.getAdapterType() == MegaNodeAdapter.ITEM_VIEW_TYPE_LIST && mLayoutManager != null) {
				View v = mLayoutManager.findViewByPosition(position);
				if (v != null) {
					return (ImageView) v.findViewById(R.id.file_list_thumbnail);
				}
			}
			else if (gridLayoutManager != null) {
				View v = gridLayoutManager.findViewByPosition(position);
				if (v != null) {
					return (ImageView) v.findViewById(R.id.file_grid_thumbnail);
				}
			}
		}

		return null;
	}
    
    public void addSectionTitle(List<MegaNode> nodes,int type) {
        Map<Integer, String> sections = new HashMap<>();
        int folderCount = 0;
        int fileCount = 0;
        for (MegaNode node : nodes) {
            if(node == null) {
                continue;
            }
            if (node.isFolder()) {
                folderCount++;
            }
            if (node.isFile()) {
                fileCount++;
            }
        }

        if (type == MegaNodeAdapter.ITEM_VIEW_TYPE_GRID) {
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
		headerItemDecoration.setType(type);
		headerItemDecoration.setKeys(sections);
    }
	
	private class ActionBarCallBack implements ActionMode.Callback {
		
		boolean showRename = false;
		boolean showMove = false;
		boolean showLink = false;
		boolean showCopy = false;
		boolean showTrash =false;

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			List<MegaNode> documents = adapter.getSelectedNodes();
			
			switch(item.getItemId()){
				case R.id.cab_menu_download:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.prepareForDownload(handleList, false);
					break;
				}
				case R.id.cab_menu_trash:{

					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}
					((ManagerActivityLollipop) context).askConfirmationMoveToRubbish(handleList);
					break;
				}
				case R.id.cab_menu_rename:{

					if (documents.size()==1){
						((ManagerActivityLollipop) context).showRenameDialog(documents.get(0), documents.get(0).getName());
					}
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_copy:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToCopyNodes(handleList);
					clearSelections();
					hideMultipleSelect();
					break;
				}	
				case R.id.cab_menu_move:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToMoveNodes(handleList);

					clearSelections();
					hideMultipleSelect();

					break;
				}
				case R.id.cab_menu_share_link:{

					if (documents.size()==1){
						NodeController nC = new NodeController(context);
						nC.exportLink(documents.get(0));
					}
					break;
				}
				case R.id.cab_menu_select_all:{
					selectAll();
					break;
				}
				case R.id.cab_menu_unselect_all:{
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_leave_multiple_share: {
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					((ManagerActivityLollipop) context).showConfirmationLeaveMultipleShares(handleList);
					break;
				}
				case R.id.cab_menu_send_to_chat:{
					log("Send files to chat");
					ArrayList<MegaNode> nodesSelected = adapter.getArrayListSelectedNodes();
					NodeController nC = new NodeController(context);
					nC.checkIfNodesAreMineAndSelectChatsToSendNodes(nodesSelected);
					clearSelections();
					hideMultipleSelect();
					break;
				}
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.file_browser_action, menu);
			((ManagerActivityLollipop)context).hideFabButton();
			((ManagerActivityLollipop) context).showHideBottomNavigationView(true);
			((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ACCENT);
			checkScroll();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			clearSelections();
			adapter.setMultipleSelect(false);
			((ManagerActivityLollipop)context).showFabButton();
			((ManagerActivityLollipop) context).showHideBottomNavigationView(false);
			((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ZERO_DELAY);
			checkScroll();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<MegaNode> selected = adapter.getSelectedNodes();
			MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);

			menu.findItem(R.id.cab_menu_send_to_chat).setIcon(Util.mutateIconSecondary(context, R.drawable.ic_send_to_contact, R.color.white));

			boolean showSendToChat = false;

			if (selected.size() != 0) {

				showCopy = true;
				showMove = false;
				showTrash =false;
				showRename=false;
				if(selected.size()==adapter.getItemCount()){
                    menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
				  	if(selected.size()==1){
				  		showRename=true;
					}else{
						showRename=false;
					}
					showMove = false;
					showTrash=false;

				}else if(selected.size()==1){

                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);

					if((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
						showRename = true;

					}else if(megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK){
						showRename = false;

					}else if(megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READ).getErrorCode() == MegaError.API_OK){
						showRename = false;
					}

 				}else{

                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
					showRename = false;
				}

				if (((ManagerActivityLollipop)context).deepBrowserTreeIncoming == 0){

					showTrash = false;
					showMove = false;
					menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(true);
					menu.findItem(R.id.cab_menu_leave_multiple_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				}else{

					if((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
						showTrash = true;
						showMove = true;
					}else{
						showTrash = false;
						showMove = false;
					}

					menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(false);
					menu.findItem(R.id.cab_menu_leave_multiple_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				}

				boolean areFiles = true;
				for (int i=0; i<selected.size(); i++) {
					if (selected.get(i).isFolder()) {
						areFiles = false;
						break;
					}
				}

				if(areFiles && Util.isChatEnabled()) {
					showSendToChat = true;
				}
				else {
					showSendToChat = false;
				}

				menu.findItem(R.id.cab_menu_send_to_chat).setVisible(showSendToChat);
				menu.findItem(R.id.cab_menu_send_to_chat).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

//				for(int i=0; i<selected.size();i++)	{
//                    if(megaApi.checkMove(selected.get(i), megaApi.getRubbishNode()).getErrorCode() != MegaError.API_OK)	{
//                        showTrash = false;
//						break;
//					}
//				}
			}
			else{
                menu.findItem(R.id.cab_menu_select_all).setVisible(true);
				menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
			}
			
			menu.findItem(R.id.cab_menu_download).setVisible(true);
			menu.findItem(R.id.cab_menu_download).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.findItem(R.id.cab_menu_rename).setVisible(showRename);

			menu.findItem(R.id.cab_menu_copy).setVisible(true);
			menu.findItem(R.id.cab_menu_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.findItem(R.id.cab_menu_move).setVisible(showMove);
			menu.findItem(R.id.cab_menu_share_link).setVisible(showLink);

			menu.findItem(R.id.cab_menu_trash).setVisible(showTrash);

			return false;
		}
	}

	public static IncomingSharesFragmentLollipop newInstance() {
		log("newInstance");
		IncomingSharesFragmentLollipop fragment = new IncomingSharesFragmentLollipop();
		return fragment;
	}
			
	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		dbH = DatabaseHandler.getDbHandler(context);
		prefs = dbH.getPreferences();
		if (prefs != null){
			log("prefs != null");
			if (prefs.getStorageAskAlways() != null){
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					log("askMe==false");
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
						}
					}
				}
			}
		}
		nodes = new ArrayList<MegaNode>();
		lastPositionStack = new Stack<>();

		super.onCreate(savedInstanceState);
		log("onCreate");		
	}

	public void checkScroll () {
		if (recyclerView != null) {
			if ((recyclerView.canScrollVertically(-1) && recyclerView.getVisibility() == View.VISIBLE) || (adapter != null && adapter.isMultipleSelect())) {
				((ManagerActivityLollipop) context).changeActionBarElevation(true);
			}
			else {
				((ManagerActivityLollipop) context).changeActionBarElevation(false);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		log("onCreateView: parentHandle is: "+((ManagerActivityLollipop)context).parentHandleIncoming);
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		if (megaApi.getRootNode() == null){
			return null;
		}
		
		display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;

		((ManagerActivityLollipop)context).showFabButton();

		if (((ManagerActivityLollipop)context).isList){
			View v = inflater.inflate(R.layout.fragment_filebrowserlist, container, false);
			
			recyclerView = (RecyclerView) v.findViewById(R.id.file_list_view_browser);
			fastScroller = (FastScroller) v.findViewById(R.id.fastscroll);

			recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
			recyclerView.setClipToPadding(false);
//			recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
			mLayoutManager = new LinearLayoutManager(context);
			recyclerView.setLayoutManager(mLayoutManager);
			recyclerView.setItemAnimator(new DefaultItemAnimator());
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					checkScroll();
				}
			});

			emptyImageView = (ImageView) v.findViewById(R.id.file_list_empty_image);
			emptyTextView = (LinearLayout) v.findViewById(R.id.file_list_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.file_list_empty_text_first);

			transfersOverViewLayout = (RelativeLayout) v.findViewById(R.id.transfers_overview_item_layout);
			transfersOverViewLayout.setVisibility(View.GONE);

			if (adapter == null){
				adapter = new MegaNodeAdapter(context, this, nodes, ((ManagerActivityLollipop)context).parentHandleIncoming, recyclerView, null, Constants.INCOMING_SHARES_ADAPTER, MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
			}
			else{
				adapter.setListFragment(recyclerView);
				adapter.setAdapterType(MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
			}

			if (((ManagerActivityLollipop)context).parentHandleIncoming == -1){
				log("ParentHandle -1");
				findNodes();
				adapter.setParentHandle(-1);
			}
			else{
				MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming);
				log("ParentHandle to find children: "+((ManagerActivityLollipop)context).parentHandleIncoming);

				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
				addSectionTitle(nodes,adapter.getAdapterType());

				adapter.setNodes(nodes);
			}
			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

			adapter.setMultipleSelect(false);
			
			recyclerView.setAdapter(adapter);
			fastScroller.setRecyclerView(recyclerView);
			visibilityFastScroller();

			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);

				if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleIncoming||((ManagerActivityLollipop)context).parentHandleIncoming==-1) {
					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
					}
					String textToShow = String.format(context.getString(R.string.context_empty_incoming));
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

				}else{

//					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//					emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.ic_zero_landscape_empty_folder);
					}else{
						emptyImageView.setImageResource(R.drawable.ic_zero_portrait_empty_folder);
					}
					String textToShow = String.format(context.getString(R.string.file_browser_empty_folder_new));
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
			}else{
				recyclerView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}

			log("Deep browser tree: "+((ManagerActivityLollipop)context).deepBrowserTreeIncoming);

			return v;
		}
		else{
			log("Grid View");
			View v = inflater.inflate(R.layout.fragment_filebrowsergrid, container, false);
			
			recyclerView = (NewGridRecyclerView) v.findViewById(R.id.file_grid_view_browser);
			fastScroller = (FastScroller) v.findViewById(R.id.fastscroll);

			recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(80, outMetrics));
			recyclerView.setClipToPadding(false);
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

			emptyImageView = (ImageView) v.findViewById(R.id.file_grid_empty_image);
			emptyTextView = (LinearLayout) v.findViewById(R.id.file_grid_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.file_grid_empty_text_first);
			addSectionTitle(nodes,MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);

			if (adapter == null){
				adapter = new MegaNodeAdapter(context, this, nodes, ((ManagerActivityLollipop)context).parentHandleIncoming, recyclerView, null, Constants.INCOMING_SHARES_ADAPTER, MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
//				adapterList.setNodes(nodes);
			}
			else{
				adapter.setParentHandle(((ManagerActivityLollipop)context).parentHandleIncoming);
				adapter.setListFragment(recyclerView);
				adapter.setAdapterType(MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
//				adapterList.setNodes(nodes);
			}

			if (((ManagerActivityLollipop)context).parentHandleIncoming == -1){
				log("ParentHandle -1");
				findNodes();
			}
			else{
				MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming);
				log("ParentHandle: "+((ManagerActivityLollipop)context).parentHandleIncoming);

				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
				addSectionTitle(nodes,adapter.getAdapterType());

				adapter.setNodes(nodes);
			}

			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

			adapter.setMultipleSelect(false);
			
			recyclerView.setAdapter(adapter);
			fastScroller.setRecyclerView(recyclerView);
			visibilityFastScroller();
			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);

				if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleIncoming||((ManagerActivityLollipop)context).parentHandleIncoming==-1) {

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
					}
					String textToShow = String.format(context.getString(R.string.context_empty_incoming));
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

				}else{
//					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//					emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.ic_zero_landscape_empty_folder);
					}else{
						emptyImageView.setImageResource(R.drawable.ic_zero_portrait_empty_folder);
					}
					String textToShow = String.format(context.getString(R.string.file_browser_empty_folder_new));
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
			}
			else{
				recyclerView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}	
//			setNodes(nodes);	
			
			return v;
		}		
	}

//	public void refresh(){
//		log("refresh");
//		findNodes();
//	}
	

	public void refresh (){
		log("refresh");
		MegaNode parentNode = null;
		if (((ManagerActivityLollipop)context).parentHandleIncoming == -1){
			log("ParentHandle is -1");
			findNodes();
		}
		else{
			if (megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming) == null){
				findNodes();
			}
			else {
				parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming);

				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
				addSectionTitle(nodes,adapter.getAdapterType());

				adapter.setNodes(nodes);
			}
		}
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

		visibilityFastScroller();
		clearSelections();
		hideMultipleSelect();
		//If folder has no files
		if (adapter.getItemCount() == 0){
			recyclerView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);

			if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleIncoming||((ManagerActivityLollipop)context).parentHandleIncoming==-1) {

				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
				}
				String textToShow = String.format(context.getString(R.string.context_empty_incoming));
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

			}else{

				//emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//				emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.ic_zero_landscape_empty_folder);
				}else{
					emptyImageView.setImageResource(R.drawable.ic_zero_portrait_empty_folder);
				}
				String textToShow = String.format(context.getString(R.string.file_browser_empty_folder_new));
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
		}else{
			recyclerView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		}
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public void itemClick(int position, int[] screenPosition, ImageView imageView) {
    	log("itemClick");

		if (adapter.isMultipleSelect()){
			log("multiselect ON");
			adapter.toggleSelection(position);

			List<MegaNode> selectedNodes = adapter.getSelectedNodes();
			if (selectedNodes.size() > 0){
				updateActionModeTitle();
			}
		}
		else{
			if (nodes.get(position).isFolder()){
				((ManagerActivityLollipop)context).increaseDeepBrowserTreeIncoming();
				log("Is folder deep: "+((ManagerActivityLollipop)context).deepBrowserTreeIncoming);
				MegaNode n = nodes.get(position);

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

				((ManagerActivityLollipop)context).parentHandleIncoming = n.getHandle();
				((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
				((ManagerActivityLollipop)context).setToolbarTitle();
				
				MegaNode infoNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming);
				nodes = megaApi.getChildren(nodes.get(position), ((ManagerActivityLollipop)context).orderCloud);
				addSectionTitle(nodes,adapter.getAdapterType() );

				adapter.setNodes(nodes);
				recyclerView.scrollToPosition(0);
				visibilityFastScroller();

				//If folder has no files
				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);

					if (megaApi.getRootNode().getHandle()==n.getHandle()) {

						if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
							emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
						}else{
							emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
						}
						String textToShow = String.format(context.getString(R.string.context_empty_incoming));
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
					} else {

//						emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//						emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
						if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
							emptyImageView.setImageResource(R.drawable.ic_zero_landscape_empty_folder);
						}else{
							emptyImageView.setImageResource(R.drawable.ic_zero_portrait_empty_folder);
						}
						String textToShow = String.format(context.getString(R.string.file_browser_empty_folder_new));
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
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
				}
				checkScroll();
				((ManagerActivityLollipop) context).showFabButton();
			}
			else{
				//Is file
				if (MimeTypeList.typeForName(nodes.get(position).getName()).isImage()){
					Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
                    //Put flag to notify FullScreenImageViewerLollipop.
                    intent.putExtra("placeholder", placeholderCount);
					intent.putExtra("position", position);
					intent.putExtra("adapterType", Constants.INCOMING_SHARES_ADAPTER);
					intent.putExtra("isFolderLink", false);
					if (megaApi.getParentNode(nodes.get(position)).getType() == MegaNode.TYPE_ROOT){
						intent.putExtra("parentNodeHandle", -1L);
					}
					else{
						intent.putExtra("parentNodeHandle", megaApi.getParentNode(nodes.get(position)).getHandle());
					}

					if(((ManagerActivityLollipop)context).parentHandleIncoming==-1){
						intent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderOthers);
					}
					else{
						intent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderCloud);
					}

					intent.putExtra("fromShared", true);
					intent.putExtra("screenPosition", screenPosition);
					context.startActivity(intent);
					((ManagerActivityLollipop) context).overridePendingTransition(0,0);
					imageDrag = imageView;
				}
				else if (MimeTypeList.typeForName(nodes.get(position).getName()).isVideoReproducible() || MimeTypeList.typeForName(nodes.get(position).getName()).isAudio() ){
					MegaNode file = nodes.get(position);

					String mimeType = MimeTypeList.typeForName(file.getName()).getType();
					log("FILENAME: " + file.getName());

					Intent mediaIntent;
					boolean internalIntent;
					boolean opusFile = false;
					if (MimeTypeList.typeForName(file.getName()).isVideoNotSupported() || MimeTypeList.typeForName(file.getName()).isAudioNotSupported()){
						mediaIntent = new Intent(Intent.ACTION_VIEW);
						internalIntent = false;
						String[] s = file.getName().split("\\.");
						if (s != null && s.length > 1 && s[s.length-1].equals("opus")) {
							opusFile = true;
						}
					}
					else {
						internalIntent = true;
						mediaIntent = new Intent(context, AudioVideoPlayerLollipop.class);
					}
					mediaIntent.putExtra("position", position);
					if (megaApi.getParentNode(nodes.get(position)).getType() == MegaNode.TYPE_ROOT){
						mediaIntent.putExtra("parentNodeHandle", -1L);
					}
					else{
						mediaIntent.putExtra("parentNodeHandle", megaApi.getParentNode(nodes.get(position)).getHandle());
					}

					if(((ManagerActivityLollipop)context).parentHandleIncoming==-1){
						mediaIntent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderOthers);
					}
					else{
						mediaIntent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderCloud);
					}
                    mediaIntent.putExtra("placeholder", placeholderCount);
					mediaIntent.putExtra("screenPosition", screenPosition);
					mediaIntent.putExtra("adapterType", Constants.INCOMING_SHARES_ADAPTER);

					mediaIntent.putExtra("fromShared", true);
					mediaIntent.putExtra("HANDLE", file.getHandle());
					mediaIntent.putExtra("FILENAME", file.getName());
					imageDrag = imageView;
					boolean isOnMegaDownloads = false;
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					File f = new File(downloadLocationDefaultPath, file.getName());
					if(f.exists() && (f.length() == file.getSize())){
						isOnMegaDownloads = true;
					}
					if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(file) != null && megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath))))){
						File mediaFile = new File(localPath);
						//mediaIntent.setDataAndType(Uri.parse(localPath), mimeType);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
							mediaIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						else{
							mediaIntent.setDataAndType(Uri.fromFile(mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					}
					else {
						if (megaApi.httpServerIsRunning() == 0) {
							megaApi.httpServerStart();
						}

						ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
						ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
						activityManager.getMemoryInfo(mi);

						if(mi.totalMem>Constants.BUFFER_COMP){
							log("Total mem: "+mi.totalMem+" allocate 32 MB");
							megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
						}
						else{
							log("Total mem: "+mi.totalMem+" allocate 16 MB");
							megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
						}

						String url = megaApi.httpServerGetLocalLink(file);
						mediaIntent.setDataAndType(Uri.parse(url), mimeType);
					}
					if (opusFile){
						mediaIntent.setDataAndType(mediaIntent.getData(), "audio/*");
					}
					if (internalIntent) {
						context.startActivity(mediaIntent);
					}
					else {
						if (MegaApiUtils.isIntentAvailable(context, mediaIntent)) {
							context.startActivity(mediaIntent);
						}
						else {
							((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.intent_not_available), -1);
							adapter.notifyDataSetChanged();
							ArrayList<Long> handleList = new ArrayList<Long>();
							handleList.add(nodes.get(position).getHandle());
							NodeController nC = new NodeController(context);
							nC.prepareForDownload(handleList, true);
						}
					}
					((ManagerActivityLollipop) context).overridePendingTransition(0,0);
				}else if (MimeTypeList.typeForName(nodes.get(position).getName()).isPdf()){
					MegaNode file = nodes.get(position);

					String mimeType = MimeTypeList.typeForName(file.getName()).getType();
					log("FILENAME: " + file.getName() + "TYPE: "+mimeType);

					Intent pdfIntent = new Intent(context, PdfViewerActivityLollipop.class);

					pdfIntent.putExtra("fromShared", true);
					pdfIntent.putExtra("inside", true);
					pdfIntent.putExtra("adapterType", Constants.INCOMING_SHARES_ADAPTER);
					boolean isOnMegaDownloads = false;
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					File f = new File(downloadLocationDefaultPath, file.getName());
					if(f.exists() && (f.length() == file.getSize())){
						isOnMegaDownloads = true;
					}
					if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(file) != null && megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath))))){
						File mediaFile = new File(localPath);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
							pdfIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						else{
							pdfIntent.setDataAndType(Uri.fromFile(mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					}
					else {
						if (megaApi.httpServerIsRunning() == 0) {
							megaApi.httpServerStart();
						}

						ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
						ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
						activityManager.getMemoryInfo(mi);

						if(mi.totalMem>Constants.BUFFER_COMP){
							log("Total mem: "+mi.totalMem+" allocate 32 MB");
							megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
						}
						else{
							log("Total mem: "+mi.totalMem+" allocate 16 MB");
							megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
						}

						String url = megaApi.httpServerGetLocalLink(file);
						pdfIntent.setDataAndType(Uri.parse(url), mimeType);
					}
					pdfIntent.putExtra("HANDLE", file.getHandle());
					pdfIntent.putExtra("screenPosition", screenPosition);
					imageDrag = imageView;
					if (MegaApiUtils.isIntentAvailable(context, pdfIntent)){
						startActivity(pdfIntent);
					}
					else{
						Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();

						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(nodes.get(position).getHandle());
						NodeController nC = new NodeController(context);
						nC.prepareForDownload(handleList, true);
					}
					((ManagerActivityLollipop) context).overridePendingTransition(0,0);
				}
				else if (MimeTypeList.typeForName(nodes.get(position).getName()).isURL()) {
					log("Is URL file");
					MegaNode file = nodes.get(position);

					boolean isOnMegaDownloads = false;
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					File f = new File(downloadLocationDefaultPath, file.getName());
					if (f.exists() && (f.length() == file.getSize())) {
						isOnMegaDownloads = true;
					}
					log("isOnMegaDownloads: " + isOnMegaDownloads);
					if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(file) != null && megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath))))) {
						File mediaFile = new File(localPath);
						InputStream instream = null;

						try {
							// open the file for reading
							instream = new FileInputStream(f.getAbsolutePath());

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
									Intent intent = new Intent(Intent.ACTION_VIEW);
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
										intent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", f), "text/plain");
									} else {
										intent.setDataAndType(Uri.fromFile(f), "text/plain");
									}
									intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

									if (MegaApiUtils.isIntentAvailable(context, intent)) {
										startActivity(intent);
									} else {
										ArrayList<Long> handleList = new ArrayList<Long>();
										handleList.add(nodes.get(position).getHandle());
										NodeController nC = new NodeController(context);
										nC.prepareForDownload(handleList, true);
									}
								}
							}
						} catch (Exception ex) {

							Intent intent = new Intent(Intent.ACTION_VIEW);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								intent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", f), "text/plain");
							} else {
								intent.setDataAndType(Uri.fromFile(f), "text/plain");
							}
							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

							if (MegaApiUtils.isIntentAvailable(context, intent)) {
								startActivity(intent);
							} else {
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(nodes.get(position).getHandle());
								NodeController nC = new NodeController(context);
								nC.prepareForDownload(handleList, true);
							}

						} finally {
							// close the file.
							try {
								instream.close();
							} catch (IOException e) {
								log("EXCEPTION closing InputStream");
							}
						}
					} else {
						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(nodes.get(position).getHandle());
						NodeController nC = new NodeController(context);
						nC.prepareForDownload(handleList, true);
					}
				}
				else{
					adapter.notifyDataSetChanged();
					ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(nodes.get(position).getHandle());
					NodeController nC = new NodeController(context);
					nC.prepareForDownload(handleList, true);
				}
			}
		}
	}

	public void findNodes(){
		log("findNodes");
		nodes=megaApi.getInShares();
		for(int i=0;i<nodes.size();i++){
			log("NODE: "+nodes.get(i).getName());
		}

		if(((ManagerActivityLollipop)context).orderOthers == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByMailDescending();
		}
		addSectionTitle(nodes,adapter.getAdapterType() );
		adapter.setNodes(nodes);

		if (adapter.getItemCount() == 0){
			log("adapter.getItemCount() = 0");
			recyclerView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);

			if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleIncoming||((ManagerActivityLollipop)context).parentHandleIncoming==-1) {

				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
				}
				String textToShow = String.format(context.getString(R.string.context_empty_incoming));
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

			}else{

//				emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//				emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.ic_zero_landscape_empty_folder);
				}else{
					emptyImageView.setImageResource(R.drawable.ic_zero_portrait_empty_folder);
				}
				String textToShow = String.format(context.getString(R.string.file_browser_empty_folder_new));
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

		}
		else{
			log("adapter.getItemCount() != 0");
			recyclerView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		}
	}

	public void setOrderNodes(){
		log("setOrderNodes: "+((ManagerActivityLollipop)context).parentHandleIncoming);

		if(((ManagerActivityLollipop)context).parentHandleIncoming==-1){
			if(((ManagerActivityLollipop)context).orderOthers == MegaApiJava.ORDER_DEFAULT_DESC){
				sortByMailDescending();
			}
			else{
				nodes=megaApi.getInShares();
			}
		}
		else{
			MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming);
			nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
		}
		addSectionTitle(nodes,adapter.getAdapterType() );
		adapter.setNodes(nodes);
	}

	public void selectAll(){
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
		if (adapter != null){
			return adapter.isMultipleSelect();
		}
		
		return false;
	}
			
	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		log("clearSelections");
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
		}
	}
		
	private void updateActionModeTitle() {
		if (actionMode == null || getActivity() == null) {
			return;
		}
		List<MegaNode> documents = adapter.getSelectedNodes();
		int files = 0;
		int folders = 0;
		for (MegaNode document : documents) {
			if (document.isFile()) {
				files++;
			} else if (document.isFolder()) {
				folders++;
			}
		}
		Resources res = getActivity().getResources();

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
		log("hideMultipleSelect");
		adapter.setMultipleSelect(false);
		if (actionMode != null) {
			actionMode.finish();
		}
	}
	
	public int onBackPressed(){
		log("onBackPressed deepBrowserTree:"+((ManagerActivityLollipop)context).deepBrowserTreeIncoming);

		if (adapter == null){
			return 0;
		}


        if (((ManagerActivityLollipop) context).comesFromNotifications && ((ManagerActivityLollipop) context).comesFromNotificationsLevel == (((ManagerActivityLollipop) context).deepBrowserTreeIncoming)) {
			((ManagerActivityLollipop) context).comesFromNotifications = false;
			((ManagerActivityLollipop) context).comesFromNotificationsLevel = 0;
			((ManagerActivityLollipop) context).comesFromNotificationHandle = -1;
            ((ManagerActivityLollipop) context).selectDrawerItemLollipop(ManagerActivityLollipop.DrawerItem.NOTIFICATIONS);
			((ManagerActivityLollipop) context).setDeepBrowserTreeIncoming(((ManagerActivityLollipop) context).comesFromNotificationDeepBrowserTreeIncoming);
			((ManagerActivityLollipop) context).comesFromNotificationDeepBrowserTreeIncoming = -1;
			((ManagerActivityLollipop) context).setParentHandleIncoming(((ManagerActivityLollipop) context).comesFromNotificationHandleSaved);
			((ManagerActivityLollipop) context).comesFromNotificationHandleSaved = -1;
			((ManagerActivityLollipop) context).refreshIncomingShares();

			return 4;
        }
		else {
			((ManagerActivityLollipop)context).decreaseDeepBrowserTreeIncoming();
			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

			if(((ManagerActivityLollipop)context).deepBrowserTreeIncoming==0){
				//In the beginning of the navigation

				log("deepBrowserTree==0");
				((ManagerActivityLollipop) context).parentHandleIncoming = -1;
				((ManagerActivityLollipop) context).setToolbarTitle();
				findNodes();
				visibilityFastScroller();
				//				adapterList.setNodes(nodes);
				recyclerView.setVisibility(View.VISIBLE);
				int lastVisiblePosition = 0;
				if (!lastPositionStack.empty()) {
					lastVisiblePosition = lastPositionStack.pop();
					log("Pop of the stack " + lastVisiblePosition + " position");
				}
				log("Scroll to " + lastVisiblePosition + " position");


				if (lastVisiblePosition >= 0) {

					if (((ManagerActivityLollipop) context).isList) {
						mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
					else {
						gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
				}

				((ManagerActivityLollipop) context).showFabButton();

				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);

				return 3;
			}
			else if (((ManagerActivityLollipop)context).deepBrowserTreeIncoming>0){
				log("deepTree>0");

				MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleIncoming));

				if (parentNode != null){
					recyclerView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);

					((ManagerActivityLollipop)context).parentHandleIncoming = parentNode.getHandle();

					((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
					((ManagerActivityLollipop)context).setToolbarTitle();

					nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
					addSectionTitle(nodes,adapter.getAdapterType() );

					adapter.setNodes(nodes);
					visibilityFastScroller();
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
				}

				((ManagerActivityLollipop) context).showFabButton();
				return 2;
			}
			else{
				log("ELSE deepTree");
//			((ManagerActivityLollipop)context).setParentHandleBrowser(megaApi.getRootNode().getHandle());
				recyclerView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
				((ManagerActivityLollipop)context).deepBrowserTreeIncoming=0;
				return 0;
			}

		}
	}

	public RecyclerView getRecyclerView(){
		return recyclerView;
	}
	

	public void notifyDataSetChanged(){
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	public void setNodes(ArrayList<MegaNode> nodes) {
		log("setNodes");
		this.nodes = nodes;
		if (((ManagerActivityLollipop)context).isList) {
			addSectionTitle(nodes,MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
		} else {
			addSectionTitle(nodes,MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
		}
		adapter.setNodes(nodes);
	}
	
	public void sortByMailDescending(){
		log("sortByNameDescending");
		ArrayList<MegaNode> folderNodes = new ArrayList<MegaNode>();
		ArrayList<MegaNode> fileNodes = new ArrayList<MegaNode>();

		for (int i=0;i<nodes.size();i++){
			if(nodes.get(i) == null) {
				continue;
			}
			if (nodes.get(i).isFolder()){
				folderNodes.add(nodes.get(i));
			}
			else{
				fileNodes.add(nodes.get(i));
			}
		}

		Collections.reverse(folderNodes);
		Collections.reverse(fileNodes);

		nodes.clear();
		nodes.addAll(folderNodes);
		nodes.addAll(fileNodes);
	}

	public int getItemCount(){
		if(adapter != null){
			return adapter.getItemCount();
		}
		return 0;
	}

	public int getDeepBrowserTree(){
		return ((ManagerActivityLollipop)context).deepBrowserTreeIncoming;
	}

	public boolean isMultipleselect(){
		return adapter.isMultipleSelect();
	}
	
	private static void log(String log) {
		Util.log("IncomingSharesFragmentLollipop", log);
	}

	public void visibilityFastScroller(){
		if(adapter == null){
			fastScroller.setVisibility(View.GONE);
		}else{
			if(adapter.getItemCount() < Constants.MIN_ITEMS_SCROLLBAR){
				fastScroller.setVisibility(View.GONE);
			}else{
				fastScroller.setVisibility(View.VISIBLE);
			}
		}

	}


}
