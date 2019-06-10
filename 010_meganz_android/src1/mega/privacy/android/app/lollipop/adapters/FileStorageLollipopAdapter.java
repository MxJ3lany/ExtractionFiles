package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop.FileDocument;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop.Mode;
import mega.privacy.android.app.lollipop.adapters.MegaSharedFolderLollipopAdapter.OnItemClickListener;
import mega.privacy.android.app.utils.Util;

/*
 * Adapter for FilestorageActivity list
 */
public class FileStorageLollipopAdapter extends RecyclerView.Adapter<FileStorageLollipopAdapter.ViewHolderFileStorage> implements OnClickListener {
		
	private Context context;
	private List<FileDocument> currentFiles;
	private Mode mode;
	OnItemClickListener mItemClickListener;
	RecyclerView listFragment;
	private SparseBooleanArray selectedItems;
	int positionClicked;
	boolean multipleSelect;
	
	public FileStorageLollipopAdapter(Context context, RecyclerView listView, Mode mode2) {
		this.mode = mode2;
		this.listFragment = listView;
		this.context = context;
	}	
	
	/*private view holder class*/
    class ViewHolderFileStorage extends RecyclerView.ViewHolder implements View.OnClickListener{
    	
    	public ImageView imageView;
    	public TextView textViewFileName;
    	public TextView textViewFileSize;
    	public RelativeLayout itemLayout;
    	public FileDocument document;    	
    	
    	public ViewHolderFileStorage(View itemView) {
			super(itemView);
            itemView.setOnClickListener(this);
		}
    	
    	@Override
		public void onClick(View v) {
			if(mItemClickListener != null){
				mItemClickListener.onItemClick(v, getPosition());
			}			
		}
    }
	
	@Override
	public ViewHolderFileStorage onCreateViewHolder(ViewGroup parent, int viewType) {
		
		listFragment = (RecyclerView) parent;
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;		
	    float scaleW = Util.getScaleW(outMetrics, density);
		
	    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_explorer, parent, false);
	    ViewHolderFileStorage holder = new ViewHolderFileStorage(v);

		holder.itemLayout = (RelativeLayout) v.findViewById(R.id.file_explorer_item_layout);
		holder.itemLayout.setOnClickListener(this);
		holder.imageView = (ImageView) v.findViewById(R.id.file_explorer_thumbnail);
		holder.textViewFileName = (TextView) v.findViewById(R.id.file_explorer_filename);
		holder.textViewFileName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		holder.textViewFileName.getLayoutParams().width = Util.px2dp((260*scaleW), outMetrics);
		holder.textViewFileSize = (TextView) v.findViewById(R.id.file_explorer_filesize);
			
		v.setTag(holder);
		return holder;
	}	
	
	@Override
	public void onBindViewHolder(ViewHolderFileStorage holder, int position){

		FileDocument document = currentFiles.get(position);

		holder.textViewFileName.setText(document.getName());

		if(document.isFolder()){
			String items = Util.getNumberItemChildren(document.getFile());
			holder.textViewFileSize.setText(items);
		}
		else{
			long documentSize = document.getSize();
			holder.textViewFileSize.setText(Util.getSizeString(documentSize));
		}
		
		if(mode == Mode.PICK_FILE)
		{
			if(document.getFile().canRead() == false){
				Util.setViewAlpha(holder.imageView, .4f);
				holder.textViewFileName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));

				RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
				params1.setMargins(36, 0, 0, 0);
				holder.imageView.setLayoutParams(params1);

				if (document.isFolder()){
					holder.imageView.setImageResource(R.drawable.ic_folder_list);
				}
				else{
					//Document is FILE
					holder.imageView.setImageResource(MimeTypeList.typeForName(document.getName()).getIconResourceId());
				}
			}	
			else{
				Util.setViewAlpha(holder.imageView, 1);
				holder.textViewFileName.setTextColor(ContextCompat.getColor(context, android.R.color.black));

				RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
				params1.setMargins(36, 0, 0, 0);
				holder.imageView.setLayoutParams(params1);

				if (document.isFolder()){

					if (multipleSelect) {
						if(this.isItemChecked(position)){
							holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_multiselect_color));
							holder.imageView.setImageResource(R.drawable.ic_select_folder);
						}
						else{
							holder.imageView.setImageResource(R.drawable.ic_folder_list);
							holder.itemLayout.setBackgroundColor(Color.WHITE);
						}
					}
					else{
						holder.imageView.setImageResource(R.drawable.ic_folder_list);
						holder.itemLayout.setBackgroundColor(Color.WHITE);
					}
				}
				else{
					//Document is FILE

					if (multipleSelect) {
						if(this.isItemChecked(position)){
							holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_multiselect_color));
							holder.imageView.setImageResource(R.drawable.ic_select_folder);
						}
						else{
							holder.imageView.setImageResource(MimeTypeList.typeForName(document.getName()).getIconResourceId());
							holder.itemLayout.setBackgroundColor(Color.WHITE);
						}
					}
					else{
						holder.imageView.setImageResource(MimeTypeList.typeForName(document.getName()).getIconResourceId());
						holder.itemLayout.setBackgroundColor(Color.WHITE);
					}
				}
			}
		}
		else 
		{
			if(!isEnabled(position)){
				holder.itemLayout.setEnabled(false);
			}
			else{
				log("position: "+position+" is ENABLED");
				holder.itemLayout.setEnabled(true);
			}
			if (document.isFolder()){	
				holder.imageView.setImageResource(R.drawable.ic_folder_list);
				Util.setViewAlpha(holder.imageView, 1);
				holder.textViewFileName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
			}
			else{
				//Document is FILE
				holder.imageView.setImageResource(MimeTypeList.typeForName(document.getName()).getIconResourceId());	
				Util.setViewAlpha(holder.imageView, .4f);
				holder.textViewFileName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
			}			
			
		}		
	}	
	
	// Set new files on folder change
	public void setFiles(List<FileDocument> newFiles) {
		log("setFiles");
		if(newFiles!=null){
			if(newFiles.size()>0){
				listFragment.setVisibility(View.VISIBLE);
				currentFiles = newFiles;
				notifyDataSetChanged();
			}
			else{
				listFragment.setVisibility(View.GONE);
			}
		}
		else{
			listFragment.setVisibility(View.GONE);
		}
	}
	
	public FileDocument getDocumentAt(int position) {
		if(currentFiles == null || position >= currentFiles.size())
		{
			return null;
		}
		
		return currentFiles.get(position);
	}
	
	@Override
	public int getItemCount() {
		if (currentFiles == null) {
			return 0;
		}
		int size = currentFiles.size();
		return size == 0 ? 1 : size;
	}
	
	public int getPositionClicked() {
		return positionClicked;
	}

	public void setPositionClicked(int p) {
		positionClicked = p;
		notifyDataSetChanged();
	}

	public boolean isEnabled(int position) {
		log("isEnabled: position: "+position);
		if (currentFiles.size() == 0) {
			log("1-return");
			return false;
		}
		FileDocument document = currentFiles.get(position);
		if (mode == Mode.PICK_FOLDER && !document.isFolder()) {
			log("2-return");
			return false;
		}
		if (document.getFile().canRead() == false) {
			log("3-return");
			return false;
		}

		return true;
	}
	
	public void toggleSelection(int pos) {
		log("toggleSelection");
		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}
		notifyItemChanged(pos);

		FileStorageLollipopAdapter.ViewHolderFileStorage view = (FileStorageLollipopAdapter.ViewHolderFileStorage) listFragment.findViewHolderForLayoutPosition(pos);
		if(view!=null){
			log("Start animation: "+pos);
			Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
			flipAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (selectedItems.size() <= 0){
						((FileStorageActivityLollipop) context).hideMultipleSelect();
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			view.imageView.startAnimation(flipAnimation);
		}
	}

	public void toggleAllSelection(int pos) {
		log("toggleSelection: "+pos);
		final int positionToflip = pos;

		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}

		FileStorageLollipopAdapter.ViewHolderFileStorage view = (FileStorageLollipopAdapter.ViewHolderFileStorage) listFragment.findViewHolderForLayoutPosition(pos);
		if(view!=null){
			log("Start animation: "+pos);
			Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
			flipAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (selectedItems.size() <= 0){
						((FileStorageActivityLollipop) context).hideMultipleSelect();

					}
					notifyItemChanged(positionToflip);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			view.imageView.startAnimation(flipAnimation);
		}
		else{
			log("NULL view pos: "+positionToflip);
			notifyItemChanged(pos);
		}
	}
	
	public void selectAll(){
		for (int i= 0; i<this.getItemCount();i++){
			if(!isItemChecked(i)){
				toggleSelection(i);
			}
		}
	}

	public void clearSelections() {
		log("clearSelections");
		for (int i= 0; i<this.getItemCount();i++){
			if(isItemChecked(i)){
				toggleAllSelection(i);
			}
		}
	}
	
	private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<Integer>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); i++) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}	
	
	/*
	 * Get list of all selected nodes
	 */
	public List<FileDocument> getSelectedDocuments() {
		ArrayList<FileDocument> nodes = new ArrayList<FileDocument>();
		
		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				FileDocument document = getDocumentAt(selectedItems.keyAt(i));
				if (document != null){
					nodes.add(document);
				}
			}
		}
		return nodes;
	}
	
	/*
	 * Get list of all selected nodes
	 */
	public int getSelectedCount() {
		
		if (selectedItems!=null){
			return selectedItems.size();
		}

		return -1;
	}
	
	public boolean isMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
		}
		if(this.multipleSelect)
		{
			selectedItems = new SparseBooleanArray();
		}
	}
	
	public Object getItem(int position) {
		return currentFiles.get(position);
	}
	
	@Override
	public void onClick(View v) {
		log("click!");
		ViewHolderFileStorage holder = (ViewHolderFileStorage) v.getTag();

		int currentPosition = holder.getAdapterPosition();
		final FileDocument doc = (FileDocument) getItem(currentPosition);
		log(" in position: "+currentPosition+" document: "+doc.getName());

		switch (v.getId()) {		
			case R.id.file_explorer_item_layout:{
				((FileStorageActivityLollipop) context).itemClick(currentPosition);
				break;
			}
		}
	}	

	private static void log(String message) {
		Util.log("FileStorageLollipopAdapter", message);
	}
}
