package com.qozix.endlessrecyclerview.demo.network;

import android.content.Context;

import com.qozix.endlessrecyclerview.demo.CommonDemoEndlessAdapter;
import com.qozix.endlessrecyclerview.demo.models.JsonResponse;
import com.qozix.endlessrecyclerview.demo.models.MediaItem;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by michaeldunn on 7/21/16.
 */
public class MockNetworkDemoEndlessAdapter extends CommonDemoEndlessAdapter {

  public interface OnFillCompleteListener {
    void onFillComplete(boolean expectsMore);
  }

  private MockClient mMockClient;
  private Queue<Integer> mNullPositions = new LinkedList<>();
  private boolean mIsFetching;
  private OnFillCompleteListener mOnFillCompleteListener;

  public MockNetworkDemoEndlessAdapter(Context context) {
    super(context);
    mMockClient = new MockClient(context);
  }

  public void setOnFillCompleteListener(OnFillCompleteListener onFillCompleteListener) {
    mOnFillCompleteListener = onFillCompleteListener;
  }

  private boolean canUseMoreDataFromServer() {
    return getMediaItems().size() < getLimit() || !mNullPositions.isEmpty();
  }

  /**
   *
   * @param quantity
   */
  @Override
  public void fill(int quantity) {
    for (int i = 0; i < quantity; i++) {
      if (getMediaItems().size() < getLimit()) {
        int position = getMediaItems().size();
        mNullPositions.add(position);
        getMediaItems().add(null);
        notifyItemInserted(position);
      }
    }
    fetch(quantity);
  }

  /**
   * Since our API only returns 10 at a time, we can't take advantage of the quantity param and have to keep requesting
   * items until we've met our total debt.
   *
   * @param quantity
   */
  public void fetch(int quantity) {
    if (!mIsFetching && canUseMoreDataFromServer()) {
      mIsFetching = true;
      mMockClient.fetch(mResponseReceivedListener);
    }
  }

  private MockClient.ResponseReceivedListener mResponseReceivedListener = new MockClient.ResponseReceivedListener() {
    @Override
    public void onResponse(JsonResponse jsonResponse) {
      for (MediaItem mediaItem : jsonResponse.results) {
        if (mNullPositions.size() > 0) {
          int position = mNullPositions.poll();
          getMediaItems().set(position, mediaItem);
          notifyItemChanged(position);
        } else {
          if (getMediaItems().size() < getLimit()) {
            int position = getMediaItems().size();
            getMediaItems().add(mediaItem);
            notifyItemInserted(position);
          }
        }
      }
      mIsFetching = false;
      // strangely, if the initial estimated item height is get height
      // by not providing an estimate, and explicitly disallowing computation from the adapter
      // the granular notification methods commented out in this method will fail to requestLayout
      // dispatch it here manually
      boolean expectAnotherFetch = mNullPositions.size() > 0;
      if (mOnFillCompleteListener != null) {
        mOnFillCompleteListener.onFillComplete(expectAnotherFetch);
      }
      if (expectAnotherFetch) {  // TODO: probably should not have this here
        fetch(mNullPositions.size());
      }
    }
  };

}
