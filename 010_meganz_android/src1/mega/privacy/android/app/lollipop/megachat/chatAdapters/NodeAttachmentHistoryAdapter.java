package mega.privacy.android.app.lollipop.megachat.chatAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeThumbnail;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.TimeUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaNode;

public class NodeAttachmentHistoryAdapter extends RecyclerView.Adapter<NodeAttachmentHistoryAdapter.ViewHolderBrowser> implements OnClickListener, View.OnLongClickListener {

    public static final int ITEM_VIEW_TYPE_LIST = 0;
    public static final int ITEM_VIEW_TYPE_GRID = 1;

    Context context;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    //	int positionClicked;
    ArrayList<MegaChatMessage> messages;

    Object fragment;
    long parentHandle = -1;
    DisplayMetrics outMetrics;

    private SparseBooleanArray selectedItems;

    RecyclerView listFragment;

    DatabaseHandler dbH = null;
    boolean multipleSelect;

    ChatController cC;

    int adapterType;

    public static class ViewHolderBrowser extends RecyclerView.ViewHolder {

        public ViewHolderBrowser(View v) {
            super(v);
        }

        public ImageView savedOffline;
        public ImageView publicLinkImage;
        public TextView textViewFileName;
        public TextView textViewMessageInfo;
        public long document;
        public RelativeLayout itemLayout;
        String fullNameTitle;
        boolean nameRequestedAction = false;
    }

    public static class ViewHolderBrowserList extends NodeAttachmentHistoryAdapter.ViewHolderBrowser {

        public ViewHolderBrowserList(View v) {
            super(v);
        }
        public ImageView imageView;
        public RelativeLayout threeDotsLayout;
        public ImageView versionsIcon;
        ImageView threeDotsImageView;
    }

    public static class ViewHolderBrowserGrid extends NodeAttachmentHistoryAdapter.ViewHolderBrowser {

        public ViewHolderBrowserGrid(View v) {
            super(v);
        }

        public ImageView imageViewThumb;
        public ImageView imageViewIcon;
        public RelativeLayout thumbLayout;
        public View separator;
        public ImageView imageViewVideoIcon;
        public TextView videoDuration;
        public RelativeLayout videoInfoLayout;
        public ImageButton imageButtonThreeDots;

        public View fileLayout;
        public RelativeLayout thumbLayoutForFile;
        public ImageView fileGridIconForFile;
        public ImageButton imageButtonThreeDotsForFile;
        public TextView textViewFileNameForFile;
        public ImageView fileGridSelected;
    }

    public void toggleAllSelection(int pos) {
        log("toggleAllSelection: " + pos);
        final int positionToflip = pos;

        if (selectedItems.get(pos,false)) {
            log("delete pos: " + pos);
            selectedItems.delete(pos);

        } else {
            log("PUT pos: " + pos);
            selectedItems.put(pos,true);
        }

        if (adapterType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_LIST) {
            log("adapter type is LIST");
            NodeAttachmentHistoryAdapter.ViewHolderBrowserList view = (NodeAttachmentHistoryAdapter.ViewHolderBrowserList)listFragment.findViewHolderForLayoutPosition(pos);
            if (view != null) {
                log("Start animation: " + pos + " multiselection state: " + isMultipleSelect());
                Animation flipAnimation = AnimationUtils.loadAnimation(context,R.anim.multiselect_flip);
                flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        log("onAnimationEnd: " + selectedItems.size());
                        if (selectedItems.size() <= 0) {
                            log("toggleAllSelection: hideMultipleSelect");

                            ((NodeAttachmentHistoryActivity)context).hideMultipleSelect();
                        }
                        log("toggleAllSelection: notified item changed");
                        notifyItemChanged(positionToflip);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.imageView.startAnimation(flipAnimation);
            } else {
                log("NULL view pos: " + positionToflip);
                notifyItemChanged(pos);
            }
        } else {
            log("adapter type is GRID");
            if (selectedItems.size() <= 0) {
                ((NodeAttachmentHistoryActivity)context).hideMultipleSelect();
            }
            notifyItemChanged(positionToflip);
        }
    }

    public void toggleSelection(int pos) {
        log("toggleSelection: " + pos);

        if (selectedItems.get(pos,false)) {
            log("delete pos: " + pos);
            selectedItems.delete(pos);
        } else {
            log("PUT pos: " + pos);
            selectedItems.put(pos,true);
        }
        notifyItemChanged(pos);
        if (adapterType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_LIST) {
            log("adapter type is LIST");
            NodeAttachmentHistoryAdapter.ViewHolderBrowserList view = (NodeAttachmentHistoryAdapter.ViewHolderBrowserList)listFragment.findViewHolderForLayoutPosition(pos);
            if (view != null) {
                log("Start animation: " + pos);
                Animation flipAnimation = AnimationUtils.loadAnimation(context,R.anim.multiselect_flip);
                flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (selectedItems.size() <= 0) {
                            ((NodeAttachmentHistoryActivity)context).hideMultipleSelect();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                view.imageView.startAnimation(flipAnimation);

            } else {
                log("view is null - not animation");
                if (selectedItems.size() <= 0) {
                    ((NodeAttachmentHistoryActivity)context).hideMultipleSelect();
                }
            }
        } else {
            log("adapter type is GRID");

            if (selectedItems.size() <= 0) {
                ((NodeAttachmentHistoryActivity)context).hideMultipleSelect();
            }
        }
    }

    public void selectAll() {
        for (int i = 0;i < messages.size();i++) {
            if (!isItemChecked(i)) {
                //Exlude placeholder.
                if (messages.get(i) != null) {
                    toggleAllSelection(i);
                }
            }
        }
    }

    public void clearSelections() {
        log("clearSelections");
        for (int i = 0;i < messages.size();i++) {
            if (isItemChecked(i)) {
                //Exlude placeholder.
                if (messages.get(i) != null) {
                    toggleAllSelection(i);
                }
            }
        }
    }

    //	public void clearSelections() {
//		if(selectedItems!=null){
//			selectedItems.clear();
//			for (int i= 0; i<this.getItemCount();i++) {
//				if (isItemChecked(i)) {
//					toggleAllSelection(i);
//				}
//			}
//		}
//		notifyDataSetChanged();
//	}
//
    private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0;i < selectedItems.size();i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /*
     * Get list of all selected messages
     */
    public ArrayList<MegaChatMessage> getSelectedMessages() {
        ArrayList<MegaChatMessage> messages = new ArrayList<MegaChatMessage>();

        for (int i = 0;i < selectedItems.size();i++) {
            if (selectedItems.valueAt(i) == true) {
                MegaChatMessage message = getMessageAt(selectedItems.keyAt(i));
                if (message != null) {
                    messages.add(message);
                }
            }
        }
        return messages;
    }

    public NodeAttachmentHistoryAdapter(Context _context, ArrayList<MegaChatMessage> _messages, RecyclerView recyclerView, int adapterType) {

        this.context = _context;
        this.messages = _messages;
        this.adapterType = adapterType;

        dbH = DatabaseHandler.getDbHandler(context);

        this.listFragment = recyclerView;

        if (megaApi == null) {
            megaApi = ((MegaApplication)((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaChatApi();
        }

        cC = new ChatController(context);
    }

    public void setMessages(ArrayList<MegaChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void setAdapterType(int adapterType) {
        this.adapterType = adapterType;
    }

    public int getAdapterType() {
        return adapterType;
    }

    public NodeAttachmentHistoryAdapter.ViewHolderBrowser onCreateViewHolder(ViewGroup parent, int viewType) {
        log("onCreateViewHolder");
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        if (viewType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_LIST) {
            log("onCreateViewHolder -> type: ITEM_VIEW_TYPE_LIST");

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list,parent,false);
            ViewHolderBrowserList holderList = new ViewHolderBrowserList(v);
            holderList.itemLayout = (RelativeLayout)v.findViewById(R.id.file_list_item_layout);
            holderList.imageView = (ImageView)v.findViewById(R.id.file_list_thumbnail);
            holderList.savedOffline = (ImageView)v.findViewById(R.id.file_list_saved_offline);
            holderList.publicLinkImage = (ImageView)v.findViewById(R.id.file_list_public_link);
            holderList.textViewFileName = (TextView)v.findViewById(R.id.file_list_filename);
            holderList.textViewMessageInfo = (TextView)v.findViewById(R.id.file_list_filesize);
            holderList.threeDotsLayout = (RelativeLayout)v.findViewById(R.id.file_list_three_dots_layout);
            holderList.threeDotsImageView = (ImageView) v.findViewById(R.id.file_list_three_dots);
            holderList.versionsIcon = (ImageView) v.findViewById(R.id.file_list_versions_icon);
            holderList.textViewMessageInfo.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams paramsThreeDotsIcon = (RelativeLayout.LayoutParams) holderList.threeDotsImageView.getLayoutParams();
            paramsThreeDotsIcon.leftMargin = Util.scaleWidthPx(8, outMetrics);
            holderList.threeDotsImageView.setLayoutParams(paramsThreeDotsIcon);

            holderList.textViewMessageInfo.setSelected(true);
            holderList.textViewMessageInfo.setHorizontallyScrolling(true);
            holderList.textViewMessageInfo.setFocusable(true);
            holderList.textViewMessageInfo.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holderList.textViewMessageInfo.setMarqueeRepeatLimit(-1);
            holderList.textViewMessageInfo.setSingleLine(true);
            holderList.textViewMessageInfo.setHorizontallyScrolling(true);

            holderList.savedOffline.setVisibility(View.INVISIBLE);
            holderList.versionsIcon.setVisibility(View.GONE);
            holderList.publicLinkImage.setVisibility(View.GONE);

            holderList.itemLayout.setTag(holderList);
            holderList.itemLayout.setOnClickListener(this);
            holderList.itemLayout.setOnLongClickListener(this);

            holderList.threeDotsLayout.setTag(holderList);
            holderList.threeDotsLayout.setOnClickListener(this);

            v.setTag(holderList);
            return holderList;
        } else if (viewType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_GRID) {
            log("onCreateViewHolder -> type: ITEM_VIEW_TYPE_GRID");

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_grid_new,parent,false);
            NodeAttachmentHistoryAdapter.ViewHolderBrowserGrid holderGrid = new NodeAttachmentHistoryAdapter.ViewHolderBrowserGrid(v);

            holderGrid.fileLayout = v.findViewById(R.id.item_file_grid_file);
            holderGrid.itemLayout = (RelativeLayout)v.findViewById(R.id.file_grid_item_layout);
            holderGrid.imageViewThumb = (ImageView)v.findViewById(R.id.file_grid_thumbnail);
            holderGrid.imageViewIcon = (ImageView)v.findViewById(R.id.file_grid_icon);
            holderGrid.fileGridIconForFile = (ImageView)v.findViewById(R.id.file_grid_icon_for_file);
            holderGrid.thumbLayout = (RelativeLayout)v.findViewById(R.id.file_grid_thumbnail_layout);
            holderGrid.thumbLayoutForFile = (RelativeLayout)v.findViewById(R.id.file_grid_thumbnail_layout_for_file);
            holderGrid.textViewFileName = (TextView)v.findViewById(R.id.file_grid_filename);
            holderGrid.textViewFileNameForFile = (TextView)v.findViewById(R.id.file_grid_filename_for_file);
            holderGrid.imageButtonThreeDotsForFile = (ImageButton)v.findViewById(R.id.file_grid_three_dots_for_file);
            holderGrid.textViewMessageInfo = (TextView)v.findViewById(R.id.file_grid_filesize);
            holderGrid.imageButtonThreeDots = (ImageButton)v.findViewById(R.id.file_grid_three_dots);
            holderGrid.savedOffline = (ImageView)v.findViewById(R.id.file_grid_saved_offline);
            holderGrid.publicLinkImage = (ImageView)v.findViewById(R.id.file_grid_public_link);
            holderGrid.separator = (View)v.findViewById(R.id.file_grid_separator);

            holderGrid.imageViewVideoIcon = (ImageView)v.findViewById(R.id.file_grid_video_icon);
            holderGrid.videoDuration = (TextView)v.findViewById(R.id.file_grid_title_video_duration);
            holderGrid.videoInfoLayout = (RelativeLayout)v.findViewById(R.id.item_file_videoinfo_layout);
            holderGrid.fileGridSelected = (ImageView)v.findViewById(R.id.file_grid_selected);

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holderGrid.textViewMessageInfo.setMaxWidth(Util.scaleWidthPx(70,outMetrics));
            } else {
                holderGrid.textViewMessageInfo.setMaxWidth(Util.scaleWidthPx(130,outMetrics));
            }

            if (holderGrid.textViewMessageInfo != null) {
                holderGrid.textViewMessageInfo.setVisibility(View.VISIBLE);
            } else {
                log("textViewMessageInfo is NULL");
            }

            holderGrid.savedOffline.setVisibility(View.INVISIBLE);
            holderGrid.publicLinkImage.setVisibility(View.GONE);

            holderGrid.itemLayout.setTag(holderGrid);
            holderGrid.itemLayout.setOnClickListener(this);
            holderGrid.itemLayout.setOnLongClickListener(this);

            holderGrid.imageButtonThreeDots.setTag(holderGrid);
            holderGrid.imageButtonThreeDots.setOnClickListener(this);
            holderGrid.imageButtonThreeDotsForFile.setTag(holderGrid);
            holderGrid.imageButtonThreeDotsForFile.setOnClickListener(this);
            v.setTag(holderGrid);

            return holderGrid;
        } else {
            return null;
        }
    }

    public void onBindViewHolder(NodeAttachmentHistoryAdapter.ViewHolderBrowser holder, int position) {
        log("onBindViewHolder");

        if (adapterType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_LIST) {
            NodeAttachmentHistoryAdapter.ViewHolderBrowserList holderList = (NodeAttachmentHistoryAdapter.ViewHolderBrowserList)holder;
            onBindViewHolderList(holderList,position);
        } else if (adapterType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_GRID) {
            NodeAttachmentHistoryAdapter.ViewHolderBrowserGrid holderGrid = (NodeAttachmentHistoryAdapter.ViewHolderBrowserGrid)holder;
            onBindViewHolderGrid(holderGrid,position);
        }
    }

    public void onBindViewHolderGrid(ViewHolderBrowserGrid holder,int position) {
        log("onBindViewHolderGrid");
        MegaChatMessage m = (MegaChatMessage)getItem(position);
        MegaNode node = m.getMegaNodeList().get(0);

        holder.document = node.getHandle();
        Bitmap thumb = null;

        log("Node : " + position + " " + node.getName());

        holder.textViewFileName.setText(node.getName());
        holder.textViewMessageInfo.setText("");
        holder.videoInfoLayout.setVisibility(View.GONE);

        holder.itemLayout.setVisibility(View.VISIBLE);

        holder.imageViewThumb.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.imageViewThumb.setVisibility(View.GONE);
        holder.fileLayout.setVisibility(View.VISIBLE);
        holder.textViewFileName.setVisibility(View.VISIBLE);
        holder.textViewMessageInfo.setVisibility(View.GONE);

        holder.textViewFileNameForFile.setText(node.getName());
        long nodeSize = node.getSize();
        holder.textViewMessageInfo.setText(Util.getSizeString(nodeSize));

        holder.fileGridIconForFile.setVisibility(View.VISIBLE);
        holder.fileGridIconForFile.setImageResource(MimeTypeThumbnail.typeForName(node.getName()).getIconResourceId());
        holder.thumbLayoutForFile.setBackgroundColor(Color.TRANSPARENT);

        if (multipleSelect && isItemChecked(position)) {
//                    holder.itemLayout.setForeground(ContextCompat.getDrawable(context,R.drawable.background_item_grid_selected));
            holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid_selected));
            holder.fileGridSelected.setVisibility(View.VISIBLE);

        } else {
//                    holder.itemLayout.setForeground(new ColorDrawable());
            holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid));
            holder.fileGridSelected.setVisibility(View.GONE);
        }

        if (Util.isVideoFile(node.getName())) {
            holder.videoInfoLayout.setVisibility(View.VISIBLE);
            holder.videoDuration.setVisibility(View.GONE);
            log(node.getName() + " DURATION: " + node.getDuration());
            int duration = node.getDuration();
            if (duration > 0) {
                int hours = duration / 3600;
                int minutes = (duration % 3600) / 60;
                int seconds = duration % 60;

                String timeString;
                if (hours > 0) {
                    timeString = String.format("%d:%d:%02d",hours,minutes,seconds);
                } else {
                    timeString = String.format("%d:%02d",minutes,seconds);
                }

                log("The duration is: " + hours + " " + minutes + " " + seconds);

                holder.videoDuration.setText(timeString);
                holder.videoDuration.setVisibility(View.VISIBLE);
            }
        }

        if (node.hasThumbnail()) {

//				DisplayMetrics dm = new DisplayMetrics();
//				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, dm);
//
//				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
//				params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//				params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//				holder.imageView.setLayoutParams(params);

            Bitmap temp = ThumbnailUtils.getThumbnailFromCache(node);

            if (temp != null) {
                thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,temp,2);
                holder.fileGridIconForFile.setVisibility(View.GONE);
                holder.imageViewThumb.setVisibility(View.VISIBLE);
                holder.imageViewThumb.setImageBitmap(thumb);
                holder.thumbLayoutForFile.setBackgroundColor(ContextCompat.getColor(context,R.color.new_background_fragment));

            } else {
                temp = ThumbnailUtils.getThumbnailFromFolder(node,context);

                if (temp != null) {
                    thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,temp,2);
                    holder.fileGridIconForFile.setVisibility(View.GONE);
                    holder.imageViewThumb.setVisibility(View.VISIBLE);
                    holder.imageViewThumb.setImageBitmap(thumb);
                    holder.thumbLayoutForFile.setBackgroundColor(ContextCompat.getColor(context,R.color.new_background_fragment));

                } else {
                    try {
                        temp = ThumbnailUtilsLollipop.getThumbnailFromMegaGrid(node,context,holder,megaApi,this);

                    } catch (Exception e) {
                    } // Too many AsyncTasks

                    if (temp != null) {
                        thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,temp,2);
                        holder.imageViewIcon.setVisibility(View.GONE);
                        holder.imageViewThumb.setVisibility(View.VISIBLE);
                        holder.imageViewThumb.setImageBitmap(thumb);
                        holder.thumbLayoutForFile.setBackgroundColor(ContextCompat.getColor(context,R.color.new_background_fragment));
                    }
                }
            }
        } else {
            Bitmap temp = ThumbnailUtils.getThumbnailFromCache(node);

//				thumb = ThumbnailUtils.getThumbnailFromCache(node);
            if (temp != null) {
                thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,temp,2);
                holder.fileGridIconForFile.setVisibility(View.GONE);
                holder.imageViewThumb.setVisibility(View.VISIBLE);
                holder.imageViewThumb.setImageBitmap(thumb);
                holder.thumbLayoutForFile.setBackgroundColor(ContextCompat.getColor(context,R.color.new_background_fragment));
            } else {
                temp = ThumbnailUtils.getThumbnailFromFolder(node,context);

                if (temp != null) {
                    thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,temp,2);
                    holder.fileGridIconForFile.setVisibility(View.GONE);
                    holder.imageViewThumb.setVisibility(View.VISIBLE);
                    holder.imageViewThumb.setImageBitmap(thumb);
                    holder.thumbLayoutForFile.setBackgroundColor(ContextCompat.getColor(context,R.color.new_background_fragment));
                } else {
                    try {
                        ThumbnailUtilsLollipop.createThumbnailGrid(context,node,holder,megaApi,this);
                    } catch (Exception e) {
                    } // Too many AsyncTasks
                }
            }
        }
    }

    public void onBindViewHolderList(ViewHolderBrowserList holder,int position) {
        log("onBindViewHolderList: " + position);
        MegaChatMessage m = (MegaChatMessage)getItem(position);
        MegaNode node = m.getMegaNodeList().get(0);

        holder.document = node.getHandle();
        Bitmap thumb = null;

        holder.textViewFileName.setText(node.getName());
        holder.textViewMessageInfo.setText("");

        String date = TimeUtils.formatDateAndTime(context,m.getTimestamp(), TimeUtils.DATE_LONG_FORMAT);

        if (m.getUserHandle() == megaChatApi.getMyUserHandle()) {
            log("MY message handle!!: " + m.getMsgId());
            holder.fullNameTitle = megaChatApi.getMyFullname();
        }
        else{

            long userHandle = m.getUserHandle();
            log("Contact message!!: " + userHandle);

            if (((NodeAttachmentHistoryActivity)context).chatRoom.isGroup()) {

                holder.fullNameTitle = cC.getFullName(userHandle, ((NodeAttachmentHistoryActivity)context).chatRoom);

                if (holder.fullNameTitle == null) {
                    holder.fullNameTitle = "";
                }

                if (holder.fullNameTitle.trim().length() <= 0) {

                    log("NOT found in DB - ((ViewHolderMessageChat)holder).fullNameTitle");
                    holder.fullNameTitle = "Unknown name";
                    if (!(holder.nameRequestedAction)) {
                        log("3-Call for nonContactName: " + m.getUserHandle());
                        holder.nameRequestedAction = true;
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, userHandle, ((NodeAttachmentHistoryActivity)context).chatRoom.isPreview());
                        megaChatApi.getUserFirstname(userHandle, ((NodeAttachmentHistoryActivity)context).chatRoom.getAuthorizationToken(), listener);
                        megaChatApi.getUserLastname(userHandle, ((NodeAttachmentHistoryActivity)context).chatRoom.getAuthorizationToken(), listener);
                        megaChatApi.getUserEmail(userHandle, listener);
                    } else {
                        log("4-Name already asked and no name received: " + m.getUserHandle());
                    }
                }

            } else {
                holder.fullNameTitle = ((NodeAttachmentHistoryActivity)context).chatRoom.getTitle();
            }
        }

        String secondRowInfo = context.getString(R.string.second_row_info_item_shared_file_chat, holder.fullNameTitle, date);

        holder.textViewMessageInfo.setText(secondRowInfo);
        holder.textViewMessageInfo.setVisibility(View.VISIBLE);

        if (!multipleSelect) {
            log("Not multiselect");
            holder.itemLayout.setBackgroundColor(Color.WHITE);
            holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
            params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,48,context.getResources().getDisplayMetrics());
            params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,48,context.getResources().getDisplayMetrics());
            params.setMargins(0,0,0,0);
            holder.imageView.setLayoutParams(params);

            log("Check the thumb");

            if (node.hasThumbnail()) {
                log("Node has thumbnail");
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                params1.setMargins(left,0,0,0);

                holder.imageView.setLayoutParams(params1);

                thumb = ThumbnailUtils.getThumbnailFromCache(node);
                if (thumb != null) {

                    holder.imageView.setImageBitmap(thumb);

                } else {
                    thumb = ThumbnailUtils
                            .getThumbnailFromFolder(node,context);
                    if (thumb != null) {
                        holder.imageView.setImageBitmap(thumb);

                    } else {
                        try {
                            thumb = ThumbnailUtilsLollipop.getThumbnailFromMegaList(node,context,holder,megaApi,this);
                        } catch (Exception e) {
                        } // Too many AsyncTasks

                        if (thumb != null) {
                            holder.imageView.setImageBitmap(thumb);
                        }
                    }
                }
            } else {
                log("Node NOT thumbnail");
                thumb = ThumbnailUtils.getThumbnailFromCache(node);
                if (thumb != null) {
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                    params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                    params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                    int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                    params1.setMargins(left,0,0,0);

                    holder.imageView.setLayoutParams(params1);
                    holder.imageView.setImageBitmap(thumb);


                } else {
                    thumb = ThumbnailUtils.getThumbnailFromFolder(node,context);
                    if (thumb != null) {
                        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                        params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                        params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                        int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                        params1.setMargins(left,0,0,0);

                        holder.imageView.setLayoutParams(params1);
                        holder.imageView.setImageBitmap(thumb);

                    } else {
                        try {
                            ThumbnailUtilsLollipop.createThumbnailList(context,node,holder,megaApi,this);
                        } catch (Exception e) {
                        } // Too many AsyncTasks
                    }
                }
            }
        } else {
            log("Multiselection ON");
            if (this.isItemChecked(position)) {
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.new_multiselect_color));
                RelativeLayout.LayoutParams paramsMultiselect = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                paramsMultiselect.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,48,context.getResources().getDisplayMetrics());
                paramsMultiselect.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,48,context.getResources().getDisplayMetrics());
                paramsMultiselect.setMargins(0,0,0,0);
                holder.imageView.setLayoutParams(paramsMultiselect);
                holder.imageView.setImageResource(R.drawable.ic_select_folder);
            } else {
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.white));

                log("Check the thumb");

                if (node.hasThumbnail()) {
                    log("Node has thumbnail");
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                    params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                    params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                    int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                    params1.setMargins(left,0,0,0);

                    holder.imageView.setLayoutParams(params1);

                    thumb = ThumbnailUtils.getThumbnailFromCache(node);
                    if (thumb != null) {

                        holder.imageView.setImageBitmap(thumb);

                    } else {
                        thumb = ThumbnailUtils
                                .getThumbnailFromFolder(node,context);
                        if (thumb != null) {
                            holder.imageView.setImageBitmap(thumb);

                        } else {
                            try {
                                thumb = ThumbnailUtilsLollipop.getThumbnailFromMegaList(node,context,holder,megaApi,this);
                            } catch (Exception e) {
                            } // Too many AsyncTasks

                            if (thumb != null) {
                                holder.imageView.setImageBitmap(thumb);
                            }
                        }
                    }
                } else {
                    log("Node NOT thumbnail");

                    thumb = ThumbnailUtils.getThumbnailFromCache(node);
                    if (thumb != null) {
                        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                        params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                        params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                        int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                        params1.setMargins(left,0,0,0);

                        holder.imageView.setLayoutParams(params1);
                        holder.imageView.setImageBitmap(thumb);


                    } else {
                        thumb = ThumbnailUtils.getThumbnailFromFolder(node,context);
                        if (thumb != null) {
                            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.imageView.getLayoutParams();
                            params1.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                            params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,36,context.getResources().getDisplayMetrics());
                            int left = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
                            params1.setMargins(left,0,0,0);

                            holder.imageView.setLayoutParams(params1);
                            holder.imageView.setImageBitmap(thumb);

                        } else {
                            log("NOT thumbnail");
                            holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
                            try {
                                ThumbnailUtilsLollipop.createThumbnailList(context,node,holder,megaApi,this);
                            } catch (Exception e) {
                            } // Too many AsyncTasks
                        }
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return adapterType;
    }

    public Object getItem(int position) {
        if (messages != null) {
            return messages.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        log("onClick");
        ((MegaApplication)((Activity)context).getApplication()).sendSignalPresenceActivity();

        ViewHolderBrowser holder = (ViewHolderBrowser)v.getTag();
        int currentPosition = holder.getAdapterPosition();

        log("onClick -> Current position: " + currentPosition);

        if (currentPosition < 0) {
            log("Current position error - not valid value");
            return;
        }

        final MegaChatMessage m = (MegaChatMessage)getItem(currentPosition);
        if (m == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.file_list_three_dots_layout:
            case R.id.file_grid_three_dots:{
                threeDotsClicked(currentPosition,m);
                break;
            }
            case R.id.file_grid_three_dots_for_file: {
                threeDotsClicked(currentPosition,m);
                break;
            }
            case R.id.file_list_item_layout:
            case R.id.file_grid_item_layout: {
                int[] screenPosition = new int[2];
                ImageView imageView;
                if (adapterType == NodeAttachmentHistoryAdapter.ITEM_VIEW_TYPE_LIST) {
                    imageView = (ImageView)v.findViewById(R.id.file_list_thumbnail);
                } else {
                    imageView = (ImageView)v.findViewById(R.id.file_grid_thumbnail);
                }
                imageView.getLocationOnScreen(screenPosition);

                int[] dimens = new int[4];
                dimens[0] = screenPosition[0];
                dimens[1] = screenPosition[1];
                dimens[2] = imageView.getWidth();
                dimens[3] = imageView.getHeight();

                ((NodeAttachmentHistoryActivity)context).itemClick(currentPosition);
                break;
            }
        }
    }

    public void loadPreviousMessages(ArrayList<MegaChatMessage> messages, int counter) {
        log("loadPreviousMessages: " + counter);
        this.messages = messages;
        notifyItemRangeInserted(messages.size()-counter, counter);
    }

    public void addMessage(ArrayList<MegaChatMessage> messages, int position) {
        log("addMessage: " + position);
        this.messages = messages;
        notifyItemInserted(position);
        if (position == messages.size()) {
            log("No need to update more");
        } else {
            log("Update until end");
            int itemCount = messages.size() - position;
            log("itemCount: " + itemCount);
            notifyItemRangeChanged(position, itemCount + 1);
        }
    }

    public void removeMessage(int position, ArrayList<MegaChatMessage> messages) {
        log("removeMessage: size: " + messages.size());
        this.messages = messages;
        notifyItemRemoved(position);

        if (position == messages.size() - 1) {
            log("No need to update more");
        } else {
            log("Update until end");
            int itemCount = messages.size() - position;
            log("itemCount: " + itemCount);
            notifyItemRangeChanged(position, itemCount);
        }
    }

    private void threeDotsClicked(int currentPosition,MegaChatMessage m) {
        log("onClick: file_list_three_dots: " + currentPosition);
        ((NodeAttachmentHistoryActivity)context).showNodeAttachmentBottomSheet(m, currentPosition);
    }

    @Override
    public boolean onLongClick(View view) {
        log("OnLongCLick");
        ((MegaApplication)((Activity)context).getApplication()).sendSignalPresenceActivity();

        ViewHolderBrowser holder = (ViewHolderBrowser)view.getTag();
        int currentPosition = holder.getAdapterPosition();
//        Toast.makeText(context,"pos:" + currentPosition ,Toast.LENGTH_SHORT ).show();

        ((NodeAttachmentHistoryActivity)context).activateActionMode();
        ((NodeAttachmentHistoryActivity)context).itemClick(currentPosition);

        return true;
    }

    public MegaChatMessage getMessageAt(int position) {
        try {
            if (messages != null) {
                return messages.get(position);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }

    public boolean isMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(boolean multipleSelect) {
        log("setMultipleSelect: " + multipleSelect);
        if (this.multipleSelect != multipleSelect) {
            this.multipleSelect = multipleSelect;
        }
        if (this.multipleSelect) {
            selectedItems = new SparseBooleanArray();
        }
    }

    private static void log(String log) {
        Util.log("NodeAttachmentHistoryAdapter",log);
    }
}