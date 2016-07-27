package com.qozix.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
  private boolean mShouldEstimateHeightUsingAdapter = true;
  private int mEstimatedItemHeightFromAdapter;
  private int mEstimatedItemWidthFromAdapter;
  private Ruler mWidthRuler;
  private Ruler mHeightRuler;

  private enum Orientation {
    HORIZONTAL, VERTICAL
  }

  private interface Ruler {
    int get(View view);
  }

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

  public boolean isShouldEstimateHeightUsingAdapter() {
    return mShouldEstimateHeightUsingAdapter;
  }

  public void setShouldEstimateHeightUsingAdapter(boolean shouldEstimateHeightUsingAdapter) {
    mShouldEstimateHeightUsingAdapter = shouldEstimateHeightUsingAdapter;
  }

  protected int getEstimatedItemHeightFromAdapter() {
    if (mShouldEstimateHeightUsingAdapter && mEstimatedItemHeightFromAdapter == 0) {
      mEstimatedItemHeightFromAdapter = computeItemHeightFromAdapter();
    }
    return mEstimatedItemHeightFromAdapter;
  }

  private int computeDimensionFromAdapter(Orientation orientation){
    if (getAdapter() != null) {
      ViewGroup dummy = new FrameLayout(getContext());
      ViewHolder viewHolder = getAdapter().onCreateViewHolder(dummy, 0);
      View yardstick = viewHolder.itemView;
      int widthMeasureSpec;
      int heightMeasureSpec;
      switch(orientation){
        case VERTICAL:
          widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
          heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
          yardstick.measure(widthMeasureSpec, heightMeasureSpec);
          return yardstick.getMeasuredHeight();
        case HORIZONTAL:
          widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
          heightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST);
          yardstick.measure(widthMeasureSpec, heightMeasureSpec);
          return yardstick.getMeasuredWidth();
      }
    }
    return 0;
  }

  protected int computeItemHeightFromAdapter() {
    return computeDimensionFromAdapter(Orientation.VERTICAL);
  }

  public int getEstimatedItemHeight() {
    if (mEstimatedItemHeight > 0) {
      return mEstimatedItemHeight;
    }
    int estimatedItemHeightFromAdapter = getEstimatedItemHeightFromAdapter();
    if (estimatedItemHeightFromAdapter > 0) {
      return estimatedItemHeightFromAdapter;
    }
    return getHeight();
  }

  public void setEstimatedItemHeight(int estimatedItemHeight) {
    mEstimatedItemHeight = estimatedItemHeight;
  }

  public int getEstimatedItemWidth() {
    if (mEstimatedItemWidth > 0) {
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

  protected int computeAverageItemDimension(Ruler ruler){
    if (getChildCount() == 0) {
      return 0;
    }
    int total = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      total += ruler.get(child);
    }
    return total / getChildCount();
  }

  protected int computeAverageItemDimension(Orientation orientation){
    Ruler ruler = getRulerFromOrientation(orientation);
    return computeAverageItemDimension(ruler);
  }

  private Ruler getRulerFromOrientation(Orientation orientation){
    switch(orientation){
      case VERTICAL:
        return getHeightRuler();
      case HORIZONTAL:
        return getWidthRuler();
    }
    return null;
  }

  private Ruler getWidthRuler(){
    if(mWidthRuler == null){
      mWidthRuler = new Ruler(){
        @Override
        public int get(View view) {
          return view.getWidth();
        }
      };
    }
    return mWidthRuler;
  }

  private Ruler getHeightRuler(){
    if(mHeightRuler == null){
      mHeightRuler = new Ruler() {
        @Override
        public int get(View view) {
          return view.getHeight();  // TODO: will work with adapter measurements when not on screen?
        }
      };
    }
    return mHeightRuler;
  }

  protected int getAverageItemHeight() {
    if (!mCanExpectConsistentItemSize || mLastRecordedItemHeight == 0) {
      mLastRecordedItemHeight = computeAverageItemDimension(Orientation.VERTICAL);
    }
    return mLastRecordedItemHeight;
  }

  protected int getAverageOrEstimatedItemHeight() {
    int averageHeight = getAverageItemHeight();
    if (averageHeight > 0) {
      return averageHeight;
    }
    return getEstimatedItemHeight();
  }

  protected int getAverageOrEstimatedItemWidth() {
    int averageWidth = getAverageItemWidth();
    if (averageWidth > 0) {
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

  protected int computeVerticalSpaceToMeetThreshold(){
    int shouldFill = computeVerticalScrollOffset() + getHeight() + mVerticalThreshold;
    int currentlyFills = getContentHeight();
    return shouldFill - currentlyFills;
  }

  public void populateVertically() {
    if (getAdapter() == null) {
      return;
    }
    int space = computeVerticalSpaceToMeetThreshold();
    if (space > 0) {
      int averageHeight = getAverageOrEstimatedItemHeight();
      if (averageHeight > 0) {
        int quantity = 1 + (space / averageHeight);
        if (quantity > 0) {
          getEndlessAdapter().fill(quantity);
        }
      }
    }
  }

  public void populateHorizontally() {
    // TODO:
  }

  public void populate() {
    if (getLayoutManager() != null && getLayoutManager().canScrollVertically()) {
      populateVertically();
    }
    if (getLayoutManager() != null && getLayoutManager().canScrollHorizontally()) {
      populateHorizontally();
    }
  }

  /* package-private */ void onEndlessScroll(boolean isScrollingHorizontally, boolean isScrollingVertically) {
    if (isScrollingVertically) {
      populateVertically();
    }
    if (isScrollingHorizontally) {
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
    if (changed) {
      recomputeItemHeights();  // probably not needed
      populate();
    }
  }

  // fires when the contents of the ViewGroup change (e.g., children are added)
  private OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      recomputeItemHeights();
    }
  };

}
