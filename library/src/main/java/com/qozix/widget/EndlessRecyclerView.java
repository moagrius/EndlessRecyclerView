package com.qozix.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaeldunn on 7/20/16.
 */
public class EndlessRecyclerView extends RecyclerView {

  private EndlessListener mEndlessListener;
  private OnPopulationListener mOnPopulationListener;
  private boolean mIsEndless;
  private boolean mCanExpectConsistentItemSize;
  private boolean mShouldEstimateFromAdapter = true;
  private Ruler mWidthRuler;
  private Ruler mHeightRuler;
  private Map<Orientation, Integer> mThresholds = new HashMap<>();
  private Map<Orientation, Integer> mEstimatedItemDimensionsFromAdapter = new HashMap<>();
  private Map<Orientation, Integer> mEstimatedItemDimensions = new HashMap<>();
  private Map<Orientation, Integer> mCachedDimensions = new HashMap<>();

  /**
   * The interface On population listener.
   */
  public interface OnPopulationListener {
    /**
     * On population.
     *
     * @param quantity    the quantity
     * @param orientation the orientation
     */
    void onPopulation(int quantity, Orientation orientation);
  }

  /**
   * The enum Orientation.
   */
  public enum Orientation {
    /**
     * Horizontal orientation.
     */
    HORIZONTAL, /**
     * Vertical orientation.
     */
    VERTICAL
  }

  private interface Ruler {
    /**
     * Get int.
     *
     * @param view the view
     * @return the int
     */
    int get(View view);
  }

  /**
   * Instantiates a new Endless recycler view.
   *
   * @param context the context
   */
  public EndlessRecyclerView(Context context) {
    this(context, null);
  }

  /**
   * Instantiates a new Endless recycler view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * Instantiates a new Endless recycler view.
   *
   * @param context  the context
   * @param attrs    the attrs
   * @param defStyle the def style
   */
  public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mEndlessListener = new EndlessListener(this);
    addOnScrollListener(mEndlessListener);
    addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  /**
   * Is endless boolean.
   *
   * @return the boolean
   */
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

  /**
   * Gets endless adapter.
   *
   * @return the endless adapter
   */
  public EndlessAdapter getEndlessAdapter() {
    return (EndlessAdapter) getAdapter();
  }

  /**
   * Sets on population listener.
   *
   * @param onPopulationListener the on population listener
   */
  public void setOnPopulationListener(OnPopulationListener onPopulationListener) {
    mOnPopulationListener = onPopulationListener;
  }

  /**
   * Set this to true to skip re-computation of item dimensions, if you expect rows to be a consistent height or width.
   *
   * @param canExpectConsistentItemSize the can expect consistent item size
   */
  public void setCanExpectConsistentItemSize(boolean canExpectConsistentItemSize) {
    mCanExpectConsistentItemSize = canExpectConsistentItemSize;
  }

  /**
   * Gets threshold from orientation.
   *
   * @param orientation the orientation
   * @return the threshold from orientation
   */
  protected int getThresholdFromOrientation(Orientation orientation) {
    if (mThresholds.containsKey(orientation)) {
      Integer threshold = mThresholds.get(orientation);
      if (threshold != null) {
        return threshold;
      }
    }
    return 0;
  }

  /**
   * Gets vertical threshold.
   *
   * @return the vertical threshold
   */
  public int getVerticalThreshold() {
    return getThresholdFromOrientation(Orientation.VERTICAL);
  }

  /**
   * Gets horizontal threshold.
   *
   * @return the horizontal threshold
   */
  public int getHorizontalThreshold() {
    return getThresholdFromOrientation(Orientation.HORIZONTAL);
  }

  /**
   * Sets vertical threshold.
   *
   * @param threshold the threshold
   */
  public void setVerticalThreshold(int threshold) {
    mThresholds.put(Orientation.VERTICAL, threshold);
    onEndlessScroll(false, true);
  }

  /**
   * Sets horizontal threshold.
   *
   * @param threshold the threshold
   */
  public void setHorizontalThreshold(int threshold) {
    mThresholds.put(Orientation.HORIZONTAL, threshold);
    onEndlessScroll(true, false);
  }

  /**
   * Gets can expect consistent item size.
   *
   * @return the can expect consistent item size
   */
  public boolean getCanExpectConsistentItemSize() {
    return mCanExpectConsistentItemSize;
  }

  /**
   * Is should estimate from adapter boolean.
   *
   * @return the boolean
   */
  public boolean isShouldEstimateFromAdapter() {
    return mShouldEstimateFromAdapter;
  }

  /**
   * Sets should estimate from adapter.
   *
   * @param shouldEstimateFromAdapter the should estimate from adapter
   */
  public void setShouldEstimateFromAdapter(boolean shouldEstimateFromAdapter) {
    mShouldEstimateFromAdapter = shouldEstimateFromAdapter;
  }

  private int computeDimensionFromAdapter(Orientation orientation) {
    if (getAdapter() != null) {
      ViewGroup dummy = new FrameLayout(getContext());
      ViewHolder viewHolder = getAdapter().onCreateViewHolder(dummy, 0);
      View yardstick = viewHolder.itemView;
      int widthMeasureSpec;
      int heightMeasureSpec;
      switch (orientation) {
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

  /**
   * Gets estimated item dimension.
   *
   * @param orientation the orientation
   * @return the estimated item dimension
   */
  public int getEstimatedItemDimension(Orientation orientation) {
    // if we've been provided explicit estimates, use them
    if (mEstimatedItemDimensions.containsKey(orientation)) {
      int estimatedDimension = mEstimatedItemDimensions.get(orientation);
      if (estimatedDimension > 0) {
        return estimatedDimension;
      }
    }
    // do we have saved estimates from the adapter?
    if (mEstimatedItemDimensionsFromAdapter.containsKey(orientation)) {
      return mEstimatedItemDimensionsFromAdapter.get(orientation);
    }
    // if not, let's see what the adapter thinks...
    int estimatedDimensionFromAdapter = computeDimensionFromAdapter(orientation);
    // if it looks valid, let's save it so we don't have to reconstruct those views again
    if (estimatedDimensionFromAdapter > 0) {
      mEstimatedItemDimensionsFromAdapter.put(orientation, estimatedDimensionFromAdapter);
      return estimatedDimensionFromAdapter;
    }
    // nothing has panned out so far, let's return the width or height of this View
    return getDimensionFromView(orientation, this);
  }

  private int getExplicitlyEstimatedItemDimension(Orientation orientation) {
    if (!mEstimatedItemDimensions.containsKey(orientation)) {
      return 0;
    }
    return mEstimatedItemDimensions.get(orientation);
  }

  /**
   * Sets explicitly estimated item height.
   *
   * @param estimatedItemHeight the estimated item height
   */
  public void setExplicitlyEstimatedItemHeight(int estimatedItemHeight) {
    mEstimatedItemDimensions.put(Orientation.VERTICAL, estimatedItemHeight);
  }

  /**
   * Gets explicitly estimated item height.
   *
   * @return the explicitly estimated item height
   */
  public int getExplicitlyEstimatedItemHeight() {
    return getExplicitlyEstimatedItemDimension(Orientation.VERTICAL);
  }

  /**
   * Sets explicitly estimated item width.
   *
   * @param estimatedItemWidth the estimated item width
   */
  public void setExplicitlyEstimatedItemWidth(int estimatedItemWidth) {
    mEstimatedItemDimensions.put(Orientation.HORIZONTAL, estimatedItemWidth);
  }

  /**
   * Gets explicitly estimated item width.
   *
   * @return the explicitly estimated item width
   */
  public int getExplicitlyEstimatedItemWidth() {
    return getExplicitlyEstimatedItemDimension(Orientation.HORIZONTAL);
  }

  /**
   * Gets content size.
   *
   * @param orientation the orientation
   * @return the content size
   */
  public int getContentSize(Orientation orientation) {
    switch (orientation) {
      case VERTICAL:
        return computeVerticalScrollRange();
      case HORIZONTAL:
        return computeHorizontalScrollRange();
    }
    return 0;
  }

  /**
   * Gets scroll position.
   *
   * @param orientation the orientation
   * @return the scroll position
   */
  public int getScrollPosition(Orientation orientation) {
    switch (orientation) {
      case VERTICAL:
        return computeVerticalScrollOffset();
      case HORIZONTAL:
        return computeHorizontalScrollOffset();
    }
    return 0;
  }

  /**
   * Compute average item dimension int.
   *
   * @param ruler the ruler
   * @return the int
   */
  protected int computeAverageItemDimension(Ruler ruler) {
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

  /**
   * Compute average item dimension int.
   *
   * @param orientation the orientation
   * @return the int
   */
  protected int computeAverageItemDimension(Orientation orientation) {
    Ruler ruler = getRulerFromOrientation(orientation);
    return computeAverageItemDimension(ruler);
  }

  private Ruler getRulerFromOrientation(Orientation orientation) {
    switch (orientation) {
      case VERTICAL:
        return getHeightRuler();
      case HORIZONTAL:
        return getWidthRuler();
    }
    return null;
  }

  private Ruler getWidthRuler() {
    if (mWidthRuler == null) {
      mWidthRuler = new Ruler() {
        @Override
        public int get(View view) {
          return view.getWidth();
        }
      };
    }
    return mWidthRuler;
  }

  private Ruler getHeightRuler() {
    if (mHeightRuler == null) {
      mHeightRuler = new Ruler() {
        @Override
        public int get(View view) {
          return view.getHeight();
        }
      };
    }
    return mHeightRuler;
  }

  private int getDimensionFromView(Orientation orientation, View view) {
    Ruler ruler = getRulerFromOrientation(orientation);
    if (ruler == null) {
      return 0;
    }
    return ruler.get(view);
  }

  /**
   * Gets cached dimension.
   *
   * @param orientation the orientation
   * @return the cached dimension
   */
  protected int getCachedDimension(Orientation orientation) {
    if (mCachedDimensions.containsKey(orientation)) {
      Integer cachedDimension = mCachedDimensions.get(orientation);
      if (cachedDimension != null) {
        return cachedDimension;
      }
    }
    return 0;
  }

  /**
   * Gets average item size.
   *
   * @param orientation the orientation
   * @return the average item size
   */
  protected int getAverageItemSize(Orientation orientation) {
    if (mCanExpectConsistentItemSize) {
      int cachedDimension = getCachedDimension(orientation);
      if (cachedDimension > 0) {
        return cachedDimension;
      }
    }
    int computedDimension = computeAverageItemDimension(orientation);
    if (computedDimension > 0 && mCanExpectConsistentItemSize) {
      mCachedDimensions.put(orientation, computedDimension);
    }
    return computedDimension;
  }

  /**
   * Gets average or estimated item size.
   *
   * @param orientation the orientation
   * @return the average or estimated item size
   */
  protected int getAverageOrEstimatedItemSize(Orientation orientation) {
    int averageSize = getAverageItemSize(orientation);
    if (averageSize > 0) {
      return averageSize;
    }
    return getEstimatedItemDimension(orientation);
  }

  /**
   * Compute space to be filled int.
   *
   * @param orientation the orientation
   * @return the int
   */
  protected int computeSpaceToBeFilled(Orientation orientation) {
    return getScrollPosition(orientation) + getDimensionFromView(orientation, this) + getThresholdFromOrientation(orientation);
  }

  /**
   * Compute space to meet threshold int.
   *
   * @param orientation the orientation
   * @return the int
   */
  protected int computeSpaceToMeetThreshold(Orientation orientation) {
    return computeSpaceToBeFilled(orientation) - getContentSize(orientation);
  }

  /**
   * Populate.
   *
   * @param orientation the orientation
   */
  public void populate(Orientation orientation) {
    if (getAdapter() == null) {
      return;
    }
    int space = computeSpaceToMeetThreshold(orientation);
    if (space > 0) {
      int averageItemSize = getAverageOrEstimatedItemSize(orientation);
      if (averageItemSize > 0) {
        int quantity = 1 + (space / averageItemSize);
        if (quantity > 0) {
          getEndlessAdapter().fill(quantity);
          if (mOnPopulationListener != null) {
            mOnPopulationListener.onPopulation(quantity, orientation);
          }
        }
      }
    }
  }

  /**
   * Populate.
   */
  public void populate() {
    if(getLayoutManager() != null) {
      if (getLayoutManager().canScrollVertically()) {
        populate(Orientation.VERTICAL);
      }
      if (getLayoutManager().canScrollHorizontally()) {
        populate(Orientation.HORIZONTAL);
      }
    }
  }

  /**
   * On endless scroll.
   *
   * @param isScrollingHorizontally the is scrolling horizontally
   * @param isScrollingVertically   the is scrolling vertically
   */
  /* package-private */ void onEndlessScroll(boolean isScrollingHorizontally, boolean isScrollingVertically) {
    if (isScrollingVertically) {
      populate(Orientation.VERTICAL);
    }
    if (isScrollingHorizontally) {
      populate(Orientation.HORIZONTAL);
    }
  }

  private void recomputeItemHeights() {
    mCachedDimensions.clear();
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
