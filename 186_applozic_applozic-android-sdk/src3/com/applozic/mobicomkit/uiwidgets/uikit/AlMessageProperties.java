package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This class returns the properties of a message for your Recycler view's adapter.
 * for e.g the receiver's name from a message object.
 */

public class AlMessageProperties {
    private Context context;
    private BaseContactService contactService;
    private ChannelDatabaseService channelService;
    private MessageDatabaseService messageDatabase;
    private Message message;
    private Contact contact;
    private Channel channel;

    /**
     * This constructor should be initialised only once. You can do this in the constructor of your Adapter.
     *
     * @param context pass the calling Context
     */
    public AlMessageProperties(final Context context) {
        this.context = context;
        contactService = new AppContactService(context);
        messageDatabase = new MessageDatabaseService(context);
        channelService = ChannelDatabaseService.getInstance(context);
    }

    /**
     * This method is used to set message to this object. This will save you headache passing message object into every method.
     * This has to called in your bindView/getView method of your adapter.
     * This method creates a contact object, if message is from a user and a channel object if the message is from a group.
     *
     * @param message Pass the current Message object from the position.
     * @return Instance of this.
     */
    public AlMessageProperties setMessage(Message message) {
        this.message = message;
        if (message.getGroupId() == null) {
            contact = contactService.getContactById(message.getContactIds());
            channel = null;
        } else {
            channel = channelService.getChannelByChannelKey(message.getGroupId());
            contact = null;
        }
        return this;
    }

    /**
     * This method returns the receiver's name from the message object that this instance holds currently.
     *
     * @return If the message belongs to group it will return group's name and if it belongs to user, the display name of the user will be returned.
     */
    public String getReceiver() {
        if (message.getGroupId() == null) {
            return contact.getDisplayName();
        } else {
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    Contact withUserContact = contactService.getContactById(ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey()));
                    if (withUserContact != null) {
                        return withUserContact.getDisplayName();
                    }
                } else {
                    return ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(context).getUserId());
                }
            }
        }
        return null;
    }

    /**
     * Returns the Message string that needs to be displayed in the conversation list item.
     *
     * @return Message string if the message is of type text.
     * File's name if the message has attachment.
     * "Location" if the message is of type location etc
     */
    public void setMessageAndAttchmentIcon(TextView messageTv, ImageView attachmentIcon) {
        if (message.hasAttachment() && !Message.ContentType.TEXT_URL.getValue().equals(message.getContentType())) {
            messageTv.setText(message.getFileMetas() == null && message.getFilePaths() != null ? message.getFilePaths().get(0).substring(message.getFilePaths().get(0).lastIndexOf("/") + 1) :
                    message.getFileMetas() != null ? message.getFileMetas().getName() : "");
            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.VISIBLE);
                attachmentIcon.setImageResource(R.drawable.applozic_ic_action_attachment);
            }
        } else if (Message.ContentType.LOCATION.getValue().equals(message.getContentType())) {
            messageTv.setText(context.getString(R.string.Location));
            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.VISIBLE);
                attachmentIcon.setImageResource(R.drawable.mobicom_notification_location_icon);
            }
        } else if (Message.ContentType.TEXT_HTML.getValue().equals(message.getContentType())) {
            messageTv.setText(message.getMessage());
            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }
        } else if (Message.ContentType.PRICE.getValue().equals(message.getContentType())) {
            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }
            //return EmoticonUtils.getSmiledText(context, ConversationUIService.FINAL_PRICE_TEXT + message.getMessage(), emojiconHandler)
        } else {
            messageTv.setText((!TextUtils.isEmpty(message.getMessage()) ? message.getMessage().substring(0, Math.min(message.getMessage().length(), 50)) : ""));
            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets the unread count to a textView for the current conversation.
     *
     * @param textView The TextView to display the unread count.
     */
    public void setUnreadCount(TextView textView) {
        int unreadCount = 0;
        if (message.getGroupId() == null) {
            unreadCount = messageDatabase.getUnreadMessageCountForContact(message.getContactIds());
        } else {
            unreadCount = messageDatabase.getUnreadMessageCountForChannel(message.getGroupId());
        }

        if (unreadCount > 0) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(unreadCount));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the formatted time for the conversation.
     *
     * @return Formatted time as String.
     */
    public String getCreatedAtTime() {
        return DateUtils.getFormattedDateAndTime(context, message.getCreatedAtTime(), R.string.JUST_NOW, R.plurals.MINUTES, R.plurals.HOURS);
    }

    /**
     * This method loads the image for a Contact into the ImageView. If the user does not have image url set, it will create an alphabeticText image.
     * This will automatically check if the image is set and handle the views visibility by itself.
     *
     * @param imageView CircularImageView which loads the image for the user.
     * @param textView  TextView which will display the alphabeticText image.
     * @param contact   The Contact object whose image is to be displayed.
     */
    public void loadContactImage(CircleImageView imageView, TextView textView, Contact contact) {
        try {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            String contactNumber = "";
            char firstLetter = 0;
            contactNumber = contact.getDisplayName().toUpperCase();
            firstLetter = contact.getDisplayName().toUpperCase().charAt(0);

            if (firstLetter != '+') {
                textView.setText(String.valueOf(firstLetter));
            } else if (contactNumber.length() >= 2) {
                textView.setText(String.valueOf(contactNumber.charAt(1)));
            }

            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            GradientDrawable bgShape = (GradientDrawable) textView.getBackground();
            bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));

            if (contact.isDrawableResources()) {
                textView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                imageView.setImageResource(drawableResourceId);
            } else if (contact.getImageURL() != null) {
                loadImage(imageView, textView, contact.getImageURL(), 0);
            } else {
                textView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }
    }

    /**
     * @return Returns the channel object if the message is from channel, null otherwise.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @return Returns the Contact object if the message is from a user, null otherwise.
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * This method loads the channel's image into the ImageView
     *
     * @param imageView CircularImageView in which the image is to be loaded
     * @param textView  Although we do not display alphabeticImage for a group, but this is needed to handle the visibility for recycler view.
     * @param channel   Channel object whose image is to be loaded
     */
    public void loadChannelImage(CircleImageView imageView, TextView textView, Channel channel) {
        textView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        if (channel.getImageUrl() != null) {
            loadImage(imageView, textView, channel.getImageUrl(), R.drawable.applozic_group_icon);
        } else {
            imageView.setImageResource(R.drawable.applozic_group_icon);
        }
    }

    /**
     * This methods saves you a lot of work by check. Use this method in your bindView/getView.
     *
     * @param imageView CircularImageView to load the image
     * @param textView  TextView to display AlphabeticImage
     */
    public void loadProfileImage(CircleImageView imageView, TextView textView) {
        if (channel != null) {
            loadChannelImage(imageView, textView, channel);
        } else if (contact != null) {
            loadContactImage(imageView, textView, contact);
        }
    }

    /**
     * This method loads the image into ImageView using Glide.
     *
     * @param imageView        CircularImageView
     * @param textImage        TextView
     * @param imageUrl         Image Url
     * @param placeholderImage The res id for the placeholder image
     */
    private void loadImage(final CircleImageView imageView, final TextView textImage, String imageUrl, int placeholderImage) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(placeholderImage)
                .error(placeholderImage);


        Glide.with(context).load(imageUrl).apply(options).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (textImage != null) {
                    textImage.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.VISIBLE);
                return false;
            }
        }).into(imageView);
    }

    /**
     * This method launches the Message thread from conversation click.
     *
     * @param message
     */
    public void handleConversationClick(Message message) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("takeOrder", true);
        if (message.getGroupId() == null) {
            intent.putExtra("userId", message.getContactIds());
        } else {
            intent.putExtra("groupId", message.getGroupId());
        }
        context.startActivity(intent);
    }
}
