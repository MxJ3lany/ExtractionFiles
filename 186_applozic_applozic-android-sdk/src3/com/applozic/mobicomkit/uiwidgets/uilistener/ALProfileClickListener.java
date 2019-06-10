package com.applozic.mobicomkit.uiwidgets.uilistener;

import android.content.Context;

import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by ashish on 04/06/18.
 */

public interface ALProfileClickListener {

    void onClick(Context context, String userId, Channel channel, boolean isToolbar);
}
