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
  private int mDistanceFromVerticalEnd;
  private int mDistanceFromHorizontalEnd;
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
    //addOnScrollListener(mEndlessListener);
  }

  public void start(int quantity) {
    getEndlessAdapter().fill(quantity);
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
      throw new UnsupportedOperationException("EndlessRecyclerView needs an EndlessAdapter");
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

  private void computeDistanceFromVerticalEnd() {
    mDistanceFromVerticalEnd = getContentHeight() - getVerticalEnd();
  }

  // TODO: match vertical
  private void computeDistanceFromHorizontalEnd() {
    mDistanceFromHorizontalEnd = computeHorizontalScrollRange() - computeHorizontalScrollOffset() - getWidth();
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
    int emptySpace = mVerticalThreshold + getVerticalEnd() - getContentHeight();
    Log.d("ERV", "emptySpace=" + emptySpace);
    if(emptySpace > 0){
      Log.d("ERV", "emptySpace gt 0, proceeding...");
      int averageHeight = getAverageOrEstimatedItemHeight();
      Log.d("ERV", "avg height=" + averageHeight + ", distance=" + mDistanceFromVerticalEnd);
      if(averageHeight > 0) {
        Log.d("ERV", "averageHeight gt 0, proceeding...");
        int quantity = 1 + (emptySpace / averageHeight);
        Log.d("ERV", "number of rows to render=" + quantity);
        if (quantity > 0) {
          Log.d("ERV", "quantity gt 0, populating");
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

  // TODO: can scroll horizontally

  /* package-private */ void onEndlessScroll(boolean isScrollingHorizontally, boolean isScrollingVertically) {
    if (isScrollingVertically && layoutManagerCanScrollVertically()) {
      computeDistanceFromVerticalEnd();
      populateVertically();
    }
    if (isScrollingHorizontally && layoutManagerCanScrollHorizontally()) {
      computeDistanceFromHorizontalEnd();
      populateHorizontally();
    }
  }

  public void trigger() {
    onEndlessScroll(true, true);
  }

  private void recomputeTerminalDistances() {
    if (layoutManagerCanScrollVertically()) {
      computeDistanceFromVerticalEnd();
    }
    if (layoutManagerCanScrollHorizontally()) {
      computeDistanceFromHorizontalEnd();
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if(changed){
      Log.d("ERV", "onLayoutChange, height=" + getHeight() + ", itemHeight=" + getAverageOrEstimatedItemHeight());
      recomputeTerminalDistances();
      trigger();
    }
  }


}
