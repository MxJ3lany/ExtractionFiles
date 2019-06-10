package mega.privacy.android.app.lollipop.megachat;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatFileStorageAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;

public class ChatFileStorageFragment extends BottomSheetDialogFragment{

    RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    TextView emptyTextView;
    public ArrayList<String> mPhotoUris;
    public ArrayList<String> imagesPath = new ArrayList<>();
    MegaChatFileStorageAdapter adapter;
    ChatFileStorageFragment fileStorageFragment = this;
    public static int GRID_LARGE = 2;
    MegaChatApiAndroid megaChatApi;
    Context context;
    ActionBar aB;
    float scaleH, scaleW;
    float density;
    DisplayMetrics outMetrics;
    Display display;
    DatabaseHandler dbH;
    MegaPreferences prefs;
    public ActionMode actionMode;
    RelativeLayout rlfragment;
    ArrayList<Integer> posSelected = new ArrayList<>();
    String downloadLocationDefaultPath = Util.downloadDIR;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public static ChatFileStorageFragment newInstance() {
        ChatFileStorageFragment fragment = new ChatFileStorageFragment();
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){

        dbH = DatabaseHandler.getDbHandler(getActivity());

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
        super.onCreate(savedInstanceState);
        log("after onCreate called super");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(!isAdded()){
            return null;
        }

        log("fragment ADDED");

        if (aB == null){
            aB = ((AppCompatActivity)context).getSupportActionBar();
        }

        prefs = dbH.getPreferences();
        display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = getResources().getDisplayMetrics().density;

        scaleW = Util.getScaleW(outMetrics, density);
        scaleH = Util.getScaleH(outMetrics, density);

//        int heightFrag;
//        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            heightFrag = Util.scaleWidthPx(80, outMetrics);
//        }else{
//            heightFrag = Util.scaleWidthPx(240, outMetrics);
//
//        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightFrag = displayMetrics.heightPixels / 2 - getActionBarHeight();

        View v = inflater.inflate(R.layout.fragment_filestorage, container, false);
        rlfragment = (RelativeLayout) v.findViewById(R.id.relative_layout_frag);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, heightFrag);
        rlfragment.setLayoutParams(params);
        emptyTextView = (TextView) v.findViewById(R.id.empty_textview);
        recyclerView = (RecyclerView) v.findViewById(R.id.file_storage_grid_view_browser);
        recyclerView.setClipToPadding(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (recyclerView != null) {

            mPhotoUris = new ArrayList<>();

            int numberOfCells = GRID_LARGE;
            int dimImages = heightFrag / numberOfCells;

            mLayoutManager = new GridLayoutManager(context, numberOfCells, GridLayoutManager.HORIZONTAL,false);
            ((GridLayoutManager) mLayoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return adapter.getSpanSizeOfPosition(position);
                }
            });

            if (adapter == null){
                adapter = new MegaChatFileStorageAdapter(context, this, recyclerView, aB, mPhotoUris, dimImages);
                adapter.setHasStableIds(true);

            }else{
                adapter.setDimensionPhotos(dimImages);
                //adapter.setNodes(mPhotoUris);
                setNodes(mPhotoUris);
            }

            adapter.setMultipleSelect(false);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapter);

            //fetch photos from gallery
            new FetchPhotosTask(fileStorageFragment).execute();

            if (adapter.getItemCount() == 0){
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
            return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        this.context = context;
        aB = ((AppCompatActivity)context).getSupportActionBar();
    }

    public void itemClick(int position) {
        if (adapter.isMultipleSelect()){
            adapter.toggleSelection(position);
        }
    }

    private static void log(String log) {
        Util.log("ChatFileStorageFragment", log);
    }

    public RecyclerView getRecyclerView(){
        return recyclerView;
    }

    public void setNodes(ArrayList<String> mPhotoUris){

        this.mPhotoUris = mPhotoUris;
            if (adapter != null){
                adapter.setNodes(mPhotoUris);

                if (adapter.getItemCount() == 0){
                    recyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }else{
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                }
            }
            else{
                log("grid adapter is NULL");
            }
    }

    public int getItemCount(){

        if(adapter!=null){
            return adapter.getItemCount();
        }
        return 0;
    }


    public boolean showSelectMenuItem(){
        if (adapter != null){
            return adapter.isMultipleSelect();
        }
        return false;
    }

    public void clearSelections() {
        if(adapter != null){
            if(adapter.isMultipleSelect()){
                adapter.clearSelections();
            }
        }
    }

    public void hideMultipleSelect() {
        log("hideMultipleSelect");
        adapter.setMultipleSelect(false);

    }

    public boolean isMultipleselect(){
        if(adapter!=null){
            return adapter.isMultipleSelect();
        }
        return false;
    }

    public void activatedMultiselect(boolean flag){
        ((ChatActivityLollipop) getActivity()).multiselectActivated(flag);
    }

    public void removePosition(Integer pos){
        posSelected.remove(pos);
    }

    public void addPosition(Integer pos){
        posSelected.add(pos);
    }

    public void sendImages(){
        String filePath;
        if(isMultipleselect()){
            for(Integer element:posSelected){
                //filePath = mPhotoUris.get(element);
                filePath = imagesPath.get(element);
                ((ChatActivityLollipop) getActivity()).uploadPicture(filePath);
            }
            clearSelections();
            hideMultipleSelect();
        }
    }

    public static class FetchPhotosTask extends AsyncTask<Void, Void, List<String>> {
        private WeakReference<ChatFileStorageFragment> mContextWeakReference;

        public FetchPhotosTask(ChatFileStorageFragment context) {
            mContextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            ChatFileStorageFragment context = mContextWeakReference.get();
            if (context != null) {
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            ChatFileStorageFragment context = mContextWeakReference.get();

            if (context != null) {
                //get photos from gallery
                String[] projection = new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media._ID
                };

                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String orderBy = MediaStore.Images.Media._ID + " DESC";

                Cursor cursor = context.getActivity().getContentResolver().query(uri, projection, "", null, orderBy);

                if (cursor != null) {
                    int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    List<String> photoUris = new ArrayList<>(cursor.getCount());
                    while (cursor.moveToNext()) {
                        photoUris.add("file://" + cursor.getString(dataColumn));
                        context.createImagesPath(cursor.getString(dataColumn));
                    }
                    cursor.close();

                    return photoUris;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> photoUris) {

            ChatFileStorageFragment context = mContextWeakReference.get();
            if (context != null) {
                //context.mProgressDialog.dismiss();
                if (photoUris != null && photoUris.size() > 0) {
                    context.mPhotoUris.clear();
                    context.mPhotoUris.addAll(photoUris);
                    context.adapter.notifyDataSetChanged();
                }
                if (context.adapter.getItemCount() == 0){
                    context.recyclerView.setVisibility(View.GONE);
                    context.emptyTextView.setVisibility(View.VISIBLE);
                }else{
                    context.recyclerView.setVisibility(View.VISIBLE);
                    context.emptyTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    private int getActionBarHeight() {
        log("getActionBarHeight()");
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context != null && context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    public void createImagesPath(String path){
        imagesPath.add(path);
    }
}