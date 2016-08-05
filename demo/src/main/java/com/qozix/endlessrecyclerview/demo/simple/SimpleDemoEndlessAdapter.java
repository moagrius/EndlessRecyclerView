package com.qozix.endlessrecyclerview.demo.simple;

import android.content.Context;

import com.qozix.endlessrecyclerview.demo.CommonDemoEndlessAdapter;
import com.qozix.endlessrecyclerview.demo.models.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaeldunn on 7/21/16.
 */
public class SimpleDemoEndlessAdapter extends CommonDemoEndlessAdapter {

  public SimpleDemoEndlessAdapter(Context context) {
    super(context);
  }

  @Override
  public void fill(int quantity) {
    for (int i = 0; i < quantity; i++) {
      if (getMediaItems().size() < getLimit()) {
        getMediaItems().add(getDummyMediaItem(getMediaItems().size()));  // TODO
        notifyItemInserted(getMediaItems().size() - 1);
      }
    }
  }

  private List<String> mDummyAuthorsList = new ArrayList<>();
  {
    mDummyAuthorsList.add("Author A");
    mDummyAuthorsList.add("Author B");
  }

  private MediaItem getDummyMediaItem(int position){
    MediaItem mediaItem = new MediaItem();
    mediaItem.title = "Item #" + position;
    mediaItem.cover_url = "file:///android_asset/books.png";
    mediaItem.authors = mDummyAuthorsList;
    mediaItem.format = "book";
    return mediaItem;
  }
}
