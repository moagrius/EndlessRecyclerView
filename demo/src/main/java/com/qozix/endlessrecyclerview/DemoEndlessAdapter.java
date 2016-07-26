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

  public interface OnFillCompleteListener {
    void onFillComplete();
  }

  private MockClient mMockClient;
  private List<MediaItem> mMediaItems = new ArrayList<>();
  private Queue<Integer> mNullPositions = new LinkedList<>();
  private View.OnClickListener mOnClickListener;
  private LayoutInflater mLayoutInflater;
  private int mLimit = Integer.MAX_VALUE;
  private boolean mIsFetching;
  private OnFillCompleteListener mOnFillCompleteListener;

  public DemoEndlessAdapter(Context context){
    mLayoutInflater = LayoutInflater.from(context);
    mMockClient = new MockClient(context);
    registerAdapterDataObserver(mAdapterDataObserver);
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

  public void setOnFillCompleteListener(OnFillCompleteListener onFillCompleteListener){
    mOnFillCompleteListener = onFillCompleteListener;
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

  private boolean canUseMoreDataFromServer(){
    return mMediaItems.size() < mLimit || !mNullPositions.isEmpty();
  }

  @Override
  public void fill(int quantity){
    super.fill(quantity);
    Log.d("DEA", "fill(" + quantity + ")");
  }

  @Override
  public void pad(int quantity) {
    Log.d("DEA", "pad: " + quantity);
    for(int i = 0; i < quantity; i++){
      if(mMediaItems.size() < mLimit) {
        int position = mMediaItems.size();
        mNullPositions.add(position);
        mMediaItems.add(null);
        //notifyItemInserted(position);
      }
    }
    notifyDataSetChanged();
    Log.d("DEA", "requested items, now items owed: " + mNullPositions.size());
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
      int startSize = mMediaItems.size();
      Log.d("DEA", "onResponse, results.length=" + jsonResponse.results.size() + ", placeholders.length=" + mNullPositions.size() + ", items.length=" + mMediaItems.size());
      for(MediaItem mediaItem : jsonResponse.results){
        if(mNullPositions.size() > 0){
          int position = mNullPositions.poll();
          Log.d("DEA", "polling, size should be reduced by 1...  new size=" + mNullPositions.size());
          mMediaItems.set(position, mediaItem);
          notifyItemChanged(position);
        } else {
          if(mMediaItems.size() < mLimit) {
            mMediaItems.add(mediaItem);
            notifyItemInserted(mMediaItems.size() - 1);
            notifyItemInserted(mMediaItems.size());
          }
        }
      }
      Log.d("DEA", "got items, now items owed: " + mNullPositions.size());
      Log.d("DEA", "notifyDataSetChanged, was " + startSize + ", now is " + mMediaItems.size());
      notifyDataSetChanged();
      mIsFetching = false;
      if(mNullPositions.size() == 0) {
        if(mOnFillCompleteListener != null){
          //mOnFillCompleteListener.onFillComplete();
        }
      } else {
        Log.d("DEA", "still have placeholders to fill, launch more");
        fetch(0);
      }
    }
  };


  private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
    @Override
    public void onChanged() {
      Log.d("ADO", "onChanged");
      super.onChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      Log.d("ADO", "onItemRangeChanged");
      super.onItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
      Log.d("ADO", "onItemRangeChanged");
      super.onItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      Log.d("ADO", "onItemRangeInserted");
      super.onItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      Log.d("ADO", "onItemRangeMoved");
      super.onItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      Log.d("ADO", "onItemRangeRemoved");
      super.onItemRangeRemoved(positionStart, itemCount);
    }
  };

}
