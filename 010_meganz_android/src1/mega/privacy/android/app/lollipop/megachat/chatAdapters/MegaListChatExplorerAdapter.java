package mega.privacy.android.app.lollipop.megachat.chatAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.MarqueeTextView;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.components.scrollBar.SectionTitleProvider;
import mega.privacy.android.app.components.twemoji.EmojiTextView;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatUserAvatarListener;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerFragment;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerListItem;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatRoom;

public class MegaListChatExplorerAdapter extends RecyclerView.Adapter<MegaListChatExplorerAdapter.ViewHolderChatExplorerList> implements View.OnClickListener, View.OnLongClickListener, SectionTitleProvider {

    DisplayMetrics outMetrics;
    
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    ChatController cC;
    DatabaseHandler dbH = null;

    ViewHolderChatExplorerList holder;
    RecyclerView listView;

    Context context;
    ArrayList<ChatExplorerListItem> items;
    Object fragment;

    SparseBooleanArray selectedItems;

    boolean isSearchEnabled;
    SparseBooleanArray searchSelectedItems;

    public MegaListChatExplorerAdapter(Context _context, Object _fragment, ArrayList<ChatExplorerListItem> _items, RecyclerView _listView) {
        log("new adapter");
        this.context = _context;
        this.items = _items;
        this.fragment = _fragment;
        this.listView = _listView;

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        selectedItems = new SparseBooleanArray();

        cC = new ChatController(context);
    }

    public static class ViewHolderChatExplorerList extends RecyclerView.ViewHolder {
        public ViewHolderChatExplorerList(View arg0) {
            super(arg0);
        }

        RelativeLayout itemLayout;
        RoundedImageView avatarImage;
        TextView initialLetter;
        EmojiTextView titleText;
        ImageView stateIcon;
        MarqueeTextView lastSeenStateText;
        EmojiTextView participantsText;

        String email;

        public String getEmail() {
            return email;
        }

        public void setAvatarImage(Bitmap avatarImage) {
            this.avatarImage.setImageBitmap(avatarImage);
            initialLetter.setVisibility(View.GONE);
        }
    }

    @Override
    public MegaListChatExplorerAdapter.ViewHolderChatExplorerList onCreateViewHolder(ViewGroup parent, int viewType) {

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        dbH = DatabaseHandler.getDbHandler(context);

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_explorer_list, parent, false);
        holder = new ViewHolderChatExplorerList(v);

        holder.itemLayout = (RelativeLayout) v.findViewById(R.id.chat_explorer_list_item_layout);
        holder.avatarImage = (RoundedImageView) v.findViewById(R.id.chat_explorer_list_avatar);
        holder.initialLetter = (TextView) v.findViewById(R.id.chat_explorer_list_initial_letter);
        holder.titleText = (EmojiTextView) v.findViewById(R.id.chat_explorer_list_title);
        holder.stateIcon = (ImageView) v.findViewById(R.id.chat_explorer_list_contact_state);
        holder.lastSeenStateText = (MarqueeTextView) v.findViewById(R.id.chat_explorer_list_last_seen_state);
        holder.participantsText = (EmojiTextView) v.findViewById(R.id.chat_explorer_list_participants);

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            holder.titleText.setEmojiSize(Util.scaleWidthPx(10, outMetrics));
            holder.participantsText.setEmojiSize(Util.scaleWidthPx(10, outMetrics));
        }
        else{
            holder.titleText.setEmojiSize(Util.scaleWidthPx(20, outMetrics));
            holder.participantsText.setEmojiSize(Util.scaleWidthPx(20, outMetrics));
        }

        v.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(MegaListChatExplorerAdapter.ViewHolderChatExplorerList holder, int position) {

        ChatExplorerListItem item = getItem(position);
        MegaChatListItem chat = item.getChat();

        holder.titleText.setText(item.getTitle());

        if (chat != null && chat.isGroup()) {
            if (item.getTitle().length() > 0){
                String chatTitle = item.getTitle().trim();
                String firstLetter = chatTitle.charAt(0) + "";
                firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                holder.initialLetter.setText(firstLetter);
            }
            if((isItemChecked(position) && !isSearchEnabled()) || (isSearchEnabled() && isSearchItemChecked(position))){
                holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.new_multiselect_color));
                holder.avatarImage.setImageResource(R.drawable.ic_select_avatar);
                holder.initialLetter.setVisibility(View.GONE);
            }
            else{
                holder.itemLayout.setBackgroundColor(Color.WHITE);
                createGroupChatAvatar(holder);
            }
            holder.stateIcon.setVisibility(View.GONE);
            holder.lastSeenStateText.setVisibility(View.GONE);
            holder.participantsText.setVisibility(View.VISIBLE);
            MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());
            long peerCount = chatRoom.getPeerCount() + 1;
            holder.participantsText.setText(context.getResources().getQuantityString(R.plurals.subtitle_of_group_chat, (int) peerCount, peerCount));
        }
        else {

            holder.participantsText.setVisibility(View.GONE);
            MegaContactAdapter contact = item.getContact();

            long handle = -1;

            if (chat != null) {
                holder.email = megaChatApi.getContactEmail(chat.getPeerHandle());
                log("email: "+holder.email);
            }
            else if (contact != null && contact.getMegaUser() != null) {
                holder.email = contact.getMegaUser().getEmail();
            }

            if (contact != null && contact.getMegaUser() != null) {
                handle = contact.getMegaUser().getHandle();
            }

            String userHandleEncoded = MegaApiAndroid.userHandleToBase64(handle);

            if((isItemChecked(position) && !isSearchEnabled()) || (isSearchEnabled() && isSearchItemChecked(position))){
                holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.new_multiselect_color));
                holder.avatarImage.setImageResource(R.drawable.ic_select_avatar);
                holder.initialLetter.setVisibility(View.GONE);
            }
            else{
                holder.itemLayout.setBackgroundColor(Color.WHITE);
                setUserAvatar(holder, userHandleEncoded);
            }
            
            if(Util.isChatEnabled()){
                if (megaChatApi != null){
                    int userStatus = megaChatApi.getUserOnlineStatus(handle);
                    if(userStatus == MegaChatApi.STATUS_ONLINE){
                        log("This user is connected");
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_online));
                        holder.lastSeenStateText.setText(context.getString(R.string.online_status));
                        holder.lastSeenStateText.setVisibility(View.VISIBLE);
                    }
                    else if(userStatus == MegaChatApi.STATUS_AWAY){
                        log("This user is away");
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_away));
                        holder.lastSeenStateText.setText(context.getString(R.string.away_status));
                        holder.lastSeenStateText.setVisibility(View.VISIBLE);
                    }
                    else if(userStatus == MegaChatApi.STATUS_BUSY){
                        log("This user is busy");
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_busy));
                        holder.lastSeenStateText.setText(context.getString(R.string.busy_status));
                        holder.lastSeenStateText.setVisibility(View.VISIBLE);
                    }
                    else if(userStatus == MegaChatApi.STATUS_OFFLINE){
                        log("This user is offline");
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_offline));
                        holder.lastSeenStateText.setText(context.getString(R.string.offline_status));
                        holder.lastSeenStateText.setVisibility(View.VISIBLE);
                    }
                    else if(userStatus == MegaChatApi.STATUS_INVALID){
                        log("INVALID status: "+userStatus);
                        holder.stateIcon.setVisibility(View.GONE);
                        holder.lastSeenStateText.setVisibility(View.GONE);
                    }
                    else{
                        log("This user status is: "+userStatus);
                        holder.stateIcon.setVisibility(View.GONE);
                        holder.lastSeenStateText.setVisibility(View.GONE);
                    }

                    if(userStatus != MegaChatApi.STATUS_ONLINE && userStatus != MegaChatApi.STATUS_BUSY && userStatus != MegaChatApi.STATUS_INVALID){
                        if(contact != null && !contact.getLastGreen().isEmpty()){
                            holder.lastSeenStateText.setText(contact.getLastGreen());
                            holder.lastSeenStateText.isMarqueeIsNecessary(context);
                        }
                    }
                }
            }
            else{
                holder.stateIcon.setVisibility(View.GONE);
            }
        }
        
        if (chat != null) {
            if(chat.getOwnPrivilege()==MegaChatRoom.PRIV_RM || chat.getOwnPrivilege()==MegaChatRoom.PRIV_RO){
                holder.avatarImage.setAlpha(.4f);
                holder.itemLayout.setOnClickListener(null);
                holder.itemLayout.setOnLongClickListener(null);

                holder.titleText.setTextColor(context.getResources().getColor(R.color.text_secondary));
                holder.lastSeenStateText.setTextColor(context.getResources().getColor(R.color.text_secondary));
                holder.participantsText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
            else{
                holder.avatarImage.setAlpha(1.0f);
                holder.itemLayout.setOnClickListener(this);
                holder.itemLayout.setOnLongClickListener(this);

                holder.titleText.setTextColor(ContextCompat.getColor(context, R.color.file_list_first_row));
                holder.lastSeenStateText.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
                holder.participantsText.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
            }
        }
    }

    public void createGroupChatAvatar(ViewHolderChatExplorerList holder){
        log("createGroupChatAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(ContextCompat.getColor(context,R.color.divider_upgrade_account));

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        holder.avatarImage.setImageBitmap(defaultAvatar);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        String firstLetter = holder.initialLetter.getText().toString();

        if(firstLetter.trim().isEmpty()){
            holder.initialLetter.setVisibility(View.INVISIBLE);
        }
        else{
            log("Group chat initial letter is: "+firstLetter);
            if(firstLetter.equals("(")){
                holder.initialLetter.setVisibility(View.INVISIBLE);
            }
            else{
                holder.initialLetter.setText(firstLetter);
                holder.initialLetter.setTextColor(Color.WHITE);
                holder.initialLetter.setVisibility(View.VISIBLE);
                holder.initialLetter.setTextSize(24);
            }
        }
    }

    public void setUserAvatar(ViewHolderChatExplorerList holder, String userHandle){
		log("setUserAvatar ");
		createDefaultAvatar(holder, userHandle);

		ChatUserAvatarListener listener = new ChatUserAvatarListener(context, holder);
		File avatar = null;

		if(holder.email == null){
			if (context.getExternalCacheDir() != null) {
				avatar = new File(context.getExternalCacheDir().getAbsolutePath(), userHandle + ".jpg");
			}else {
				avatar = new File(context.getCacheDir().getAbsolutePath(), userHandle + ".jpg");
			}
		}
		else{
			if (context.getExternalCacheDir() != null){
				avatar = new File(context.getExternalCacheDir().getAbsolutePath(), holder.email + ".jpg");
			}else{
				avatar = new File(context.getCacheDir().getAbsolutePath(), holder.email + ".jpg");
			}
		}

		Bitmap bitmap = null;
		if (avatar.exists()){
			if (avatar.length() > 0){
				BitmapFactory.Options bOpts = new BitmapFactory.Options();
				bOpts.inPurgeable = true;
				bOpts.inInputShareable = true;
				bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
				if (bitmap == null) {
					avatar.delete();

					if(megaApi==null){
						log("setUserAvatar: megaApi is Null in Offline mode");
						return;
					}

					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(holder.email, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
					}
					else{
						megaApi.getUserAvatar(holder.email, context.getCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
					}
				}else{
					holder.initialLetter.setVisibility(View.GONE);
					holder.avatarImage.setImageBitmap(bitmap);
				}
			}else{

				if(megaApi==null){
					log("setUserAvatar: megaApi is Null in Offline mode");
					return;
				}

				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(holder.email, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
				}else{
					megaApi.getUserAvatar(holder.email, context.getCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
				}
			}
		}else{

			if(megaApi==null){
				log("setUserAvatar: megaApi is Null in Offline mode");
				return;
			}

			if (context.getExternalCacheDir() != null){
				megaApi.getUserAvatar(holder.email, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
			}
			else{
				megaApi.getUserAvatar(holder.email, context.getCacheDir().getAbsolutePath() + "/" + holder.email + ".jpg", listener);
			}
		}
	}

    public void createDefaultAvatar(ViewHolderChatExplorerList holder, String userHandle){
        log("createDefaultAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);

        String color = megaApi.getUserAvatarColor(userHandle);
        if(color!=null){
            log("The color to set the avatar is "+color);
            p.setColor(Color.parseColor(color));
        }
        else{
            log("Default color to the avatar");
            p.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        holder.avatarImage.setImageBitmap(defaultAvatar);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        boolean setInitialByMail = false;

        String fullName = holder.titleText.getText().toString();

        if (fullName != null){
            if (fullName.trim().length() > 0){
                String firstLetter = fullName.charAt(0) + "";
                firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                holder.initialLetter.setText(firstLetter);
                holder.initialLetter.setTextColor(Color.WHITE);
                holder.initialLetter.setVisibility(View.VISIBLE);
            }else{
                setInitialByMail=true;
            }
        }
        else{
            setInitialByMail=true;
        }
        if(setInitialByMail){
            if (holder.email != null){
                if (holder.email.length() > 0){
                    log("email TEXT: " + holder.email);
                    log("email TEXT AT 0: " + holder.email.charAt(0));
                    String firstLetter = holder.email.charAt(0) + "";
                    firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                    holder.initialLetter.setText(firstLetter);
                    holder.initialLetter.setTextColor(Color.WHITE);
                    holder.initialLetter.setVisibility(View.VISIBLE);
                }
            }
        }
        holder.initialLetter.setTextSize(24);
    }

    public ChatExplorerListItem getItem(int position) {
        return items.get(position);
    }

    public ArrayList<ChatExplorerListItem> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public int getPosition (ChatExplorerListItem item) {
        log("getPosition");
        return items.indexOf(item);
    }

    @Override
    public void onClick(View v) {
        setClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        setClick(v);

        return true;
    }

    void setClick (View v) {
        ViewHolderChatExplorerList holder = (ViewHolderChatExplorerList) v.getTag();

        if (v.getId() == R.id.chat_explorer_list_item_layout) {
            ((ChatExplorerFragment) fragment).itemClick(holder.getAdapterPosition());
        }
    }

    @Override
    public String getSectionTitle(int position) {
        if (items != null) {
            if (position >= 0 && position < items.size()) {
                String name = items.get(position).getTitle();
                if (name != null && !name.isEmpty()) {
                    return ""+name.charAt(0);
                }
            }
        }
        return "";
    }

    public void setItems (ArrayList<ChatExplorerListItem> items) {
        this.items =  items;
        notifyDataSetChanged();
    }

    public void updateItemContactStatus(int position){
        log("updateContactStatus: "+position);

        notifyItemChanged(position);
    }

    private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

    private boolean isSearchItemChecked(int position) {
        return searchSelectedItems.get(position);
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

        MegaListChatExplorerAdapter.ViewHolderChatExplorerList view = (MegaListChatExplorerAdapter.ViewHolderChatExplorerList) listView.findViewHolderForLayoutPosition(pos);
        if(view!=null){
            log("Start animation: "+pos);
            Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
            flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    Hide multipleselect
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.avatarImage.startAnimation(flipAnimation);
        }
        else {
//            Hide multipleselect
        }
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.isSearchEnabled = searchEnabled;
    }

    public boolean isSearchEnabled () {
        return isSearchEnabled;
    }

    public void setSearchSelectedItems (SparseBooleanArray searchSelectedItems) {
        this.searchSelectedItems = searchSelectedItems;
    }

    private static void log(String log) {
        Util.log("MegaListChatExplorerAdapter", log);
    }
}
