package com.qozix.endlessrecyclerview.demo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qozix.endlessrecyclerview.R;
import com.qozix.endlessrecyclerview.demo.models.MediaItem;
import com.qozix.widget.EndlessAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaeldunn on 8/5/16.
 */
public abstract class CommonDemoEndlessAdapter extends EndlessAdapter<ItemHolder> {

  private List<MediaItem> mMediaItems = new ArrayList<>();
  private LayoutInflater mLayoutInflater;
  private View.OnClickListener mOnClickListener;
  private int mLimit = Integer.MAX_VALUE;

  public CommonDemoEndlessAdapter(Context context) {
    mLayoutInflater = LayoutInflater.from(context);
  }

  @Override
  public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = mLayoutInflater.inflate(R.layout.endless_row, parent, false);
    return new ItemHolder(itemView);
  }

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
    MediaItem mediaItem = mMediaItems.get(position);
    if (mediaItem == null) {
      holder.readyContainer.setOnClickListener(null);
      holder.waitingContainer.setVisibility(View.VISIBLE);
      holder.readyContainer.setVisibility(View.GONE);
    } else {
      holder.readyContainer.setOnClickListener(mOnClickListener);
      holder.waitingContainer.setVisibility(View.GONE);
      holder.readyContainer.setVisibility(View.VISIBLE);
      holder.titleTextView.setText(position + ", " + mediaItem.title);
      boolean isAudioOrVideo = "video".equals(mediaItem.format);
      holder.mediaTextView.setVisibility(isAudioOrVideo ? View.VISIBLE : View.GONE);
      Picasso.with(holder.itemView.getContext()).load(mediaItem.cover_url).into(holder.thumbnailImageView);
      holder.authorsTextView.setText(TextUtils.join(", ", mediaItem.authors));
    }
  }

  public int getLimit() {
    return mLimit;
  }

  public void setLimit(int limit) {
    mLimit = limit;
    if (mLimit < mMediaItems.size()) {
      mMediaItems.subList(mLimit, mMediaItems.size()).clear();
    }
  }

  @Override
  public int getItemCount() {
    return mMediaItems.size();
  }

  public void setOnItemClickListener(View.OnClickListener listener) {
    mOnClickListener = listener;
  }

  public List<MediaItem> getMediaItems() {
    return mMediaItems;
  }

}
