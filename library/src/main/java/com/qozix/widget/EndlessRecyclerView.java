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

  /**
   * Set this to true to skip recomputation of row heights, if you expect rows to be a consistent height.
   *
   * @param canExpectConsistentItemSize
   */
  public void setCanExpectConsistentItemSize(boolean canExpectConsistentItemSize) {
    mCanExpectConsistentItemSize = canExpectConsistentItemSize;
  }

  private void computeDistanceFromVerticalEnd() {
    Log.d("ERV", "content=" + computeVerticalScrollRange() + ", scrollY=" + computeVerticalScrollOffset());
    mDistanceFromVerticalEnd = computeVerticalScrollRange() - computeVerticalScrollOffset() - getHeight();
  }

  private void computeDistanceFromHorizontalEnd() {
    mDistanceFromHorizontalEnd = computeHorizontalScrollRange() - computeHorizontalScrollOffset() - getWidth();
  }

  protected int computeAverageRowHeight() {
    if (getChildCount() == 0) {
      Log.d("ERV", "no children, returning height");
      return getHeight();
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
      return getWidth();
    }
    int totalWidth = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      totalWidth += child.getWidth();
    }
    return totalWidth / getChildCount();
  }

  protected int getAverageRowHeight() {
    if (!mCanExpectConsistentItemSize || mLastRecordedItemHeight == 0) {
      mLastRecordedItemHeight = computeAverageRowHeight();
    }
    return mLastRecordedItemHeight;
  }

  protected int getAverageColumnWidth() {
    if (!mCanExpectConsistentItemSize || mLastRecordedItemWidth == 0) {
      mLastRecordedItemWidth = computeAverageColumnWidth();
    }
    return mLastRecordedItemWidth;
  }

  private boolean isPastVerticalThreshold() {
    return mDistanceFromVerticalEnd < mVerticalThreshold;
  }

  private boolean isPastHorizontalThreshold() {
    return mDistanceFromHorizontalEnd < mHorizontalThreshold;
  }

  private void onVerticalThresholdReached() {
    if (getAdapter() == null) {
      return;
    }
    int averageRowHeight = getAverageRowHeight();
    Log.d("ERV", "avg height=" + averageRowHeight + ", distance=" + mDistanceFromVerticalEnd);
    if (averageRowHeight > 0) {
      int quantity = 1 + (mDistanceFromVerticalEnd / averageRowHeight);
      Log.d("ERV", "quantity=" + quantity);
      getEndlessAdapter().fill(quantity);
    }
  }

  private void onHorizontalThresholdReached() {
    if (getAdapter() == null) {
      return;
    }
    int averageColumnWidth = getAverageColumnWidth();
    if (averageColumnWidth > 0) {
      int quantity = 1 + Math.max(1, mDistanceFromHorizontalEnd / averageColumnWidth);
      getEndlessAdapter().fill(quantity);
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
      if (isPastVerticalThreshold()) {
        onVerticalThresholdReached();
      }
    }
    if (isScrollingHorizontally && layoutManagerCanScrollHorizontally()) {
      computeDistanceFromHorizontalEnd();
      if (isPastHorizontalThreshold()) {
        onHorizontalThresholdReached();
      }
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

  private View.OnLayoutChangeListener mOnLayoutChangedListener = new View.OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      recomputeTerminalDistances();
      trigger();
    }
  };

}
