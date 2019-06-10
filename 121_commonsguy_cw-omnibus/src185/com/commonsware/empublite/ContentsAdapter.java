package com.commonsware.empublite;

import android.app.Activity;
import android.app.Fragment;
import android.support.v13.app.FragmentStatePagerAdapter;

public class ContentsAdapter extends FragmentStatePagerAdapter {
  final BookContents contents;

  public ContentsAdapter(Activity ctxt, BookContents contents) {
    super(ctxt.getFragmentManager());

    this.contents=contents;
  }

  @Override
  public Fragment getItem(int position) {
    String path=contents.getChapterFile(position);

    return(SimpleContentFragment.newInstance("file:///android_asset/book/"
      + path));
  }

  @Override
  public int getCount() {
    return(contents.getChapterCount());
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return(contents.getChapterTitle(position));
  }
}