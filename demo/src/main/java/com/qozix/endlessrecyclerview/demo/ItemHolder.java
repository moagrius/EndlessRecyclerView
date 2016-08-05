package com.qozix.endlessrecyclerview.demo;

/**
 * Created by michaeldunn on 8/5/16.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qozix.endlessrecyclerview.R;

public class ItemHolder extends RecyclerView.ViewHolder {
  public ViewGroup readyContainer;
  public ViewGroup waitingContainer;
  public ImageView thumbnailImageView;
  public TextView mediaTextView;
  public TextView titleTextView;
  public TextView authorsTextView;

  public ItemHolder(View itemView) {
    super(itemView);
    readyContainer = (ViewGroup) itemView.findViewById(R.id.endless_row_ready);
    waitingContainer = (ViewGroup) itemView.findViewById(R.id.endless_row_waiting);
    thumbnailImageView = (ImageView) itemView.findViewById(R.id.imageview_endless_row_thumb);
    mediaTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_media);
    titleTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_title);
    authorsTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_authors);
  }
}