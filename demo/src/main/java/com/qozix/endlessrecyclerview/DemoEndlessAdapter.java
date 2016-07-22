package com.qozix.endlessrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qozix.endlessrecyclerview.models.JsonResponse;
import com.qozix.endlessrecyclerview.models.MediaItem;
import com.qozix.widget.EndlessAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by michaeldunn on 7/21/16.
 */
public class DemoEndlessAdapter extends EndlessAdapter<DemoEndlessAdapter.ItemHolder> {

  private MockClient mMockClient;
  private List<MediaItem> mMediaItems = new ArrayList<>();
  private Queue<Integer> mNullPositions = new LinkedList<>();
  private View.OnClickListener mOnClickListener;
  private LayoutInflater mLayoutInflater;
  private int mLimit = Integer.MAX_VALUE;
  private boolean mIsFetching;

  public DemoEndlessAdapter(Context context){
    mLayoutInflater = LayoutInflater.from(context);
    mMockClient = new MockClient(context);
  }

  @Override
  public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = mLayoutInflater.inflate(R.layout.endless_row, parent, false);
    return new ItemHolder(itemView);
  }

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
    MediaItem mediaItem = mMediaItems.get(position);
    if(mediaItem == null) {
      holder.mReadyContainer.setOnClickListener(null);
      holder.mWaitingContainer.setVisibility(View.VISIBLE);
      holder.mReadyContainer.setVisibility(View.GONE);
    } else {
      holder.mReadyContainer.setOnClickListener(mOnClickListener);
      holder.mWaitingContainer.setVisibility(View.GONE);
      holder.mReadyContainer.setVisibility(View.VISIBLE);
      holder.mTitleTextView.setText(mediaItem.title);
      boolean isAudioOrVideo = "video".equals(mediaItem.format);
      holder.mMediaTextView.setVisibility(isAudioOrVideo ? View.VISIBLE : View.GONE);
      Picasso.with(holder.itemView.getContext()).load(mediaItem.cover_url).into(holder.mThumbnailImageView);
      holder.mAuthorsTextView.setText(TextUtils.join(", ", mediaItem.authors));
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

  public void setOnItemClickListener(View.OnClickListener listener){
    mOnClickListener = listener;
  }

  public List<MediaItem> getMediaItems(){
    return mMediaItems;
  }

  public static class ItemHolder extends RecyclerView.ViewHolder {
    private ViewGroup mReadyContainer;
    private ViewGroup mWaitingContainer;
    private ImageView mThumbnailImageView;
    private TextView mMediaTextView;
    private TextView mTitleTextView;
    private TextView mAuthorsTextView;
    public ItemHolder(View itemView) {
      super(itemView);
      mReadyContainer = (ViewGroup) itemView.findViewById(R.id.endless_row_ready);
      mWaitingContainer = (ViewGroup) itemView.findViewById(R.id.endless_row_waiting);
      mThumbnailImageView = (ImageView) itemView.findViewById(R.id.imageview_endless_row_thumb);
      mMediaTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_media);
      mTitleTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_title);
      mAuthorsTextView = (TextView) itemView.findViewById(R.id.textview_endless_row_authors);
    }
  }

  @Override
  public void fill(int quantity){
    super.fill(quantity);
    Log.d("DEA", "fill request");
  }

  @Override
  public void pad(int quantity) {
    Log.d("DEA", "pad: " + quantity);
    for(int i = 0; i < quantity; i++){
      if(mMediaItems.size() < mLimit) {
        mNullPositions.add(mMediaItems.size());
        mMediaItems.add(null);
      }
    }
    Log.d("DEA", "requested items, now items owed: " + mNullPositions.size());
    notifyDataSetChanged();
  }

  private boolean canUseMoreDataFromServer(){
    return mMediaItems.size() < mLimit || !mNullPositions.isEmpty();
  }

  /**
   * Since our API only returns 10 at a time, we can't take advantage of the quantity param and have to keep requesting
   * items until we've met our total debt.
   *
   * @param quantity
   */
  @Override
  public void fetch(int quantity) {
    if(!mIsFetching && canUseMoreDataFromServer()) {
      Log.d("DEA", "fetch");
      mIsFetching = true;
      mMockClient.fetch(mResponseReceivedListener);
    }
  }

  private MockClient.ResponseReceivedListener mResponseReceivedListener = new MockClient.ResponseReceivedListener() {
    @Override
    public void onResponse(JsonResponse jsonResponse) {
      Log.d("DEA", "onResponse");
      for(MediaItem mediaItem : jsonResponse.results){
        if(!mNullPositions.isEmpty()){
          int position = mNullPositions.poll();
          mMediaItems.set(position, mediaItem);
        } else {
          if(mMediaItems.size() < mLimit) {
            mMediaItems.add(mediaItem);
          }
        }
      }
      Log.d("DEA", "got items, now items owed: " + mNullPositions.size());
      notifyDataSetChanged();
      mIsFetching = false;
      if(!mNullPositions.isEmpty()){
        Log.d("DEA", "still have placeholders to fill, launch more");
        fetch(0);
      }
    }
  };

}
