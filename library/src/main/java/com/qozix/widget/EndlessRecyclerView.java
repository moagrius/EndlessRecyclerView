package com.qozix.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by michaeldunn on 7/20/16.
 */
public class EndlessRecyclerView extends RecyclerView {

  private enum Orientation {
    VERTICAL, HORIZONTAL, NONE
  }

  private EndlessListener mEndlessListener;
  private int mThreshold;
  private int mDistanceFromBottom;
  private boolean mIsEndless;
  private boolean mCanExpectConsistentItemHeight;
  private int mLastRecordedItemHeight;

  public EndlessRecyclerView(Context context) {
    this(context, null);
  }

  public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mEndlessListener = new EndlessListener(this);
    addOnScrollListener(mEndlessListener);
  }

  public void start(){
    mEndlessListener.onScrolled(this, 0, 0);
  }

  public boolean isEndless(){
    return mIsEndless;
  }

  private void setIsEndless(boolean isEndless){
    if(isEndless != mIsEndless){
      mIsEndless = isEndless;
      if(mIsEndless) {
        addOnScrollListener(mEndlessListener);
      } else {
        removeOnScrollListener(mEndlessListener);
      }
    }
  }

  @Override
  public void setAdapter(Adapter adapter) {
    if (!(adapter instanceof EndlessAdapter)) {
      throw new UnsupportedOperationException("EndlessRecyclerView needs an EndlessAdapter");
    }
    super.setAdapter(adapter);
  }

  public EndlessAdapter getEndlessAdapter(){
    return (EndlessAdapter) getAdapter();
  }

  public int getThreshold(){
    return mThreshold;
  }

  public void setThreshold(int threshold){
    mThreshold = threshold;
    onEndlessVerticalScroll();
  }

  public boolean getCanExpectConsistentItemHeight() {
    return mCanExpectConsistentItemHeight;
  }

  /**
   * Set this to true to skip recomputation of row heights, if you expect rows to be a consistent height.
   * @param canExpectConsistentItemHeight
   */
  public void setCanExpectConsistentItemHeight(boolean canExpectConsistentItemHeight) {
    mCanExpectConsistentItemHeight = canExpectConsistentItemHeight;
  }

  private void computeDistanceFromBottom(){
    mDistanceFromBottom = computeVerticalScrollRange() - computeVerticalScrollOffset() - getHeight();
  }

  private int getDistanceFromBottom(){
    return mDistanceFromBottom;
  }

  private void computeAverageRowHeight(){
    int totalHeight = 0;
    for(int i = 0; i < getChildCount(); i++){
      View child = getChildAt(i);
      totalHeight += child.getHeight();
    }
    mLastRecordedItemHeight = totalHeight / getChildCount();
  }

  private int getAverageRowHeight(){
    if(!mCanExpectConsistentItemHeight || mLastRecordedItemHeight == 0) {
      computeAverageRowHeight();
    }
    return mLastRecordedItemHeight;
  }

  private boolean isPastThreshold(){
    return mDistanceFromBottom < mThreshold;
  }

  private void onThresholdReached(){
    if(getAdapter() == null){
      return;
    }
    int averageRowHeight = getAverageRowHeight();
    if(averageRowHeight > 0){
      int quantity = 1 + mDistanceFromBottom / averageRowHeight;
      getEndlessAdapter().fill(quantity);
    }
  }

  private Orientation getOrientation(){
    return
  }

  private void getDimension(View view) {
    return
  }

  /* package-private */ void onEndlessVerticalScroll(){
    computeDistanceFromBottom();
    if(isPastThreshold()){
      onThresholdReached();
    }
  }

}
