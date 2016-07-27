package com.qozix.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by michaeldunn on 7/20/16.
 */
public class EndlessRecyclerView extends RecyclerView {

  private EndlessListener mEndlessListener;
  private int mVerticalThreshold;
  private int mHorizontalThreshold;
  private boolean mIsEndless;
  private boolean mCanExpectConsistentItemSize;
  private int mLastRecordedItemHeight;
  private int mLastRecordedItemWidth;
  private int mEstimatedItemHeight;
  private int mEstimatedItemWidth;

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
    addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  public boolean isEndless() {
    return mIsEndless;
  }

  private void setIsEndless(boolean isEndless) {
    if (isEndless != mIsEndless) {
      mIsEndless = isEndless;
      if (mIsEndless) {
        addOnScrollListener(mEndlessListener);
      } else {
        removeOnScrollListener(mEndlessListener);
      }
    }
  }

  @Override
  public void setAdapter(Adapter adapter) {
    if (!(adapter instanceof EndlessAdapter)) {
      throw new UnsupportedOperationException("EndlessRecyclerView requires an EndlessAdapter");
    }
    super.setAdapter(adapter);
  }

  public EndlessAdapter getEndlessAdapter() {
    return (EndlessAdapter) getAdapter();
  }

  public int getVerticalThreshold() {
    return mVerticalThreshold;
  }

  public int getHorizontalThreshold() {
    return mHorizontalThreshold;
  }

  public void setVerticalThreshold(int threshold) {
    mVerticalThreshold = threshold;
    onEndlessScroll(false, true);
  }

  public void setHorizontalThreshold(int threshold) {
    mHorizontalThreshold = threshold;
    onEndlessScroll(true, false);
  }

  public boolean getCanExpectConsistentItemSize() {
    return mCanExpectConsistentItemSize;
  }

  public int getEstimatedItemHeight() {
    if(mEstimatedItemHeight > 0){
      return mEstimatedItemHeight;
    }
    return getHeight();
  }

  public void setEstimatedItemHeight(int estimatedItemHeight) {
    mEstimatedItemHeight = estimatedItemHeight;
  }

  public int getEstimatedItemWidth() {
    if (mEstimatedItemWidth > 0){
      return mEstimatedItemWidth;
    }
    return getWidth();
  }

  public void setEstimatedItemWidth(int estimatedItemWidth) {
    mEstimatedItemWidth = estimatedItemWidth;
  }

  /**
   * Set this to true to skip recomputation of row heights, if you expect rows to be a consistent height.
   *
   * @param canExpectConsistentItemSize
   */
  public void setCanExpectConsistentItemSize(boolean canExpectConsistentItemSize) {
    mCanExpectConsistentItemSize = canExpectConsistentItemSize;
  }

  public int getVerticalEnd() {
    return computeVerticalScrollOffset() + getHeight();
  }

  public int getContentHeight() {
    return computeVerticalScrollRange();
  }

  protected int computeAverageRowHeight() {
    if (getChildCount() == 0) {
      return 0;
    }
    int totalHeight = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      totalHeight += child.getHeight();
    }
    return totalHeight / getChildCount();
  }

  protected int computeAverageColumnWidth() {
    if (getChildCount() == 0) {
      return 0;
    }
    int totalWidth = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      totalWidth += child.getWidth();
    }
    return totalWidth / getChildCount();
  }

  protected int getAverageItemHeight() {
    if (!mCanExpectConsistentItemSize || mLastRecordedItemHeight == 0) {
      mLastRecordedItemHeight = computeAverageRowHeight();
    }
    return mLastRecordedItemHeight;
  }

  protected int getAverageOrEstimatedItemHeight() {
    int averageHeight = getAverageItemHeight();
    if(averageHeight > 0){
      return averageHeight;
    }
    return getEstimatedItemHeight();
  }

  protected int getAverageOrEstimatedItemWidth() {
    int averageWidth = getAverageItemWidth();
    if(averageWidth > 0){
      return averageWidth;
    }
    return getEstimatedItemWidth();
  }

  protected int getAverageItemWidth() {
    if (!mCanExpectConsistentItemSize || mLastRecordedItemWidth == 0) {
      mLastRecordedItemWidth = computeAverageColumnWidth();
    }
    return mLastRecordedItemWidth;
  }

  public void populateVertically() {
    if (getAdapter() == null) {
      return;
    }
    int shouldFill = computeVerticalScrollOffset() + getHeight() + mVerticalThreshold;
    int currentlyFills = getContentHeight();
    int difference = shouldFill - currentlyFills;
    if(difference > 0){
      Log.d("ERV", "shouldFill=" + shouldFill + ", currentlyFills=" + currentlyFills + ", difference=" + difference);
      int averageHeight = getAverageOrEstimatedItemHeight();
      if(averageHeight > 0) {
        Log.d("ERV", "avg height=" + averageHeight + ", is > 0, proceeding...");
        int quantity = 1 + (difference / averageHeight);
        Log.d("ERV", "number of rows to render=" + quantity);
        if (quantity > 0) {
          Log.d("ERV", "quantity > 0, populating");
          getEndlessAdapter().fill(quantity);
        }
      }
    }
  }

  public void populateHorizontally() {
    // TODO:
  }

  public void populate() {
    if (layoutManagerCanScrollVertically()) {
      populateVertically();
    }
    if (layoutManagerCanScrollHorizontally()) {
      populateHorizontally();
    }
  }

  private boolean layoutManagerCanScrollVertically() {
    return getLayoutManager() != null && getLayoutManager().canScrollVertically();
  }

  private boolean layoutManagerCanScrollHorizontally() {
    return getLayoutManager() != null && getLayoutManager().canScrollHorizontally();
  }

  private void onHorizontalThresholdReached(){

  }

  /* package-private */ void onEndlessScroll(boolean isScrollingHorizontally, boolean isScrollingVertically){
    if(isScrollingVertically) {
        populateVertically();
    }
    if(isScrollingHorizontally) {
      populateVertically();
    }
  }

  public void trigger() {
    onEndlessScroll(true, true);
  }

  private void recomputeItemHeights() {
    mLastRecordedItemWidth = 0;
    mLastRecordedItemHeight = 0;

  }

  // Fires when this ViewGroup's dimensions are changed
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if(changed){
      recomputeItemHeights();  // probably not needed
      populate();
      Log.d("ERV", "onLayoutChange, height=" + getHeight() + ", itemHeight=" + getAverageOrEstimatedItemHeight());
    }
  }

  // fires when the contents of the ViewGroup change (e.g., children are added)
  private OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      recomputeItemHeights();  // probably not needed
    }
  };


}
