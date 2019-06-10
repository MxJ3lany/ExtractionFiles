/***
 Copyright (c) 2015-2016 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain	a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _Android's Architecture Components_
 https://commonsware.com/AndroidArch
 */

package com.commonsware.android.recyclerview.videolist;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

class RowController extends RecyclerView.ViewHolder
    implements View.OnClickListener {
  private TextView title;
  private ImageView thumbnail;
  private Uri videoUri;

  RowController(View row) {
    super(row);

    title=(TextView)row.findViewById(android.R.id.text1);
    thumbnail=(ImageView)row.findViewById(R.id.thumbnail);

    row.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    Intent i=
      new Intent(title.getContext(), VideoPlayerActivity.class)
        .setData(videoUri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    title.getContext().startActivity(i);
  }

  void bindModel(Cursor row) {
    title.setText(row.getString(
      row.getColumnIndex(MediaStore.Video.Media.TITLE)));

    videoUri=
        ContentUris.withAppendedId(
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
          row.getInt(row.getColumnIndex(MediaStore.Video.Media._ID)));

    Picasso.with(thumbnail.getContext())
      .load(videoUri.toString())
      .fit().centerCrop()
      .placeholder(R.drawable.ic_media_video_poster)
      .into(thumbnail);
  }
}
