package com.qozix.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by michaeldunn on 7/20/16.
 */
public class EndlessRecyclerView extends RecyclerView {

  private EndlessListener mEndlessListener;
  private int mVerticalThreshold;
  private int mHorizontalThreshold;
  private int mDistanceFromVerticalEnd;
  private int mDistanceFromHorizontalEnd;
  private boolean mIsEndless;
  private boolean mCanExpectConsistentItemSize;
  private int mLastRecordedItemHeight;
  private int mLastRecordedItemWidth;

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
    addOnLayoutChangeListener(mOnLayoutChangedListener);
  }

  public void start(int quantity){
    getEndlessAdapter().fill(quantity);
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

  public int getVerticalThreshold(){
    return mVerticalThreshold;
  }

  public int getHorizontalThreshold(){
    return mHorizontalThreshold;
  }

  public void setVerticalThreshold(int threshold){
    mVerticalThreshold = threshold;
    onEndlessScroll(false, true);
  }

  public void setHorizontalThreshold(int threshold){
    mHorizontalThreshold = threshold;
    onEndlessScroll(true, false);
  }

  public boolean getCanExpectConsistentItemSize() {
    return mCanExpectConsistentItemSize;
  }

  /**
   * Set this to true to skip recomputation of row heights, if you expect rows to be a consistent height.
   * @param canExpectConsistentItemSize
   */
  public void setCanExpectConsistentItemSize(boolean canExpectConsistentItemSize) {
    mCanExpectConsistentItemSize = canExpectConsistentItemSize;
  }

  private void computeDistanceFromVerticalEnd(){
    mDistanceFromVerticalEnd = computeVerticalScrollRange() - computeVerticalScrollOffset();
  }

  private void computeDistanceFromHorizontalEnd(){
    mDistanceFromHorizontalEnd = computeHorizontalScrollRange() - computeHorizontalScrollOffset() - getWidth();
  }

  private void computeAverageRowHeight(){
    if(getChildCount() == 0){
      mLastRecordedItemHeight = 0;
      return;
    }
    int totalHeight = 0;
    for(int i = 0; i < getChildCount(); i++){
      View child = getChildAt(i);
      totalHeight += child.getHeight();
    }
    mLastRecordedItemHeight = totalHeight / getChildCount();
  }

  private void computeAverageColumnWidth(){
    if(getChildCount() == 0){
      mLastRecordedItemWidth = 0;
      return;
    }
    int totalWidth = 0;
    for(int i = 0; i < getChildCount(); i++){
      View child = getChildAt(i);
      totalWidth += child.getWidth();
    }
    mLastRecordedItemWidth = totalWidth / getChildCount();
  }

  private int getAverageRowHeight(){
    if(!mCanExpectConsistentItemSize || mLastRecordedItemHeight == 0) {
      computeAverageRowHeight();
    }
    return mLastRecordedItemHeight;
  }

  private int getAverageColumnWidth(){
    if(!mCanExpectConsistentItemSize || mLastRecordedItemWidth == 0) {
      computeAverageColumnWidth();
    }
    return mLastRecordedItemWidth;
  }

  private boolean isPastVerticalThreshold(){
    return mDistanceFromVerticalEnd < mVerticalThreshold;
  }

  private boolean isPastHorizontalThreshold(){
    return mDistanceFromHorizontalEnd < mHorizontalThreshold;
  }

  private void onVerticalThresholdReached(){
    if(getAdapter() == null){
      return;
    }
    int averageRowHeight = getAverageRowHeight();
    if(averageRowHeight > 0){
      int quantity = 1 + getHeight() / averageRowHeight;
      getEndlessAdapter().fill(quantity);
    }
  }

  private void onHorizontalThresholdReached(){
    if(getAdapter() == null){
      return;
    }
    int averageColumnWidth = getAverageColumnWidth();
    if(averageColumnWidth > 0){
      int quantity = 1 + mDistanceFromHorizontalEnd / averageColumnWidth;
      getEndlessAdapter().fill(quantity);
    }
  }

  /* package-private */ void onEndlessScroll(boolean isScrollingHorizontally, boolean isScrollingVertically){
    if(isScrollingVertically) {
      computeDistanceFromVerticalEnd();
      if (isPastVerticalThreshold()) {
        onVerticalThresholdReached();
      }
    }
    if(isScrollingHorizontally){
      computeDistanceFromHorizontalEnd();
      if(isPastHorizontalThreshold()){
        onHorizontalThresholdReached();
      }
    }
  }

  public void trigger(){
    onEndlessScroll(mHorizontalThreshold > 0, mVerticalThreshold > 0);
  }

  private View.OnLayoutChangeListener mOnLayoutChangedListener = new View.OnLayoutChangeListener(){
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      trigger();
    }
  };

}
