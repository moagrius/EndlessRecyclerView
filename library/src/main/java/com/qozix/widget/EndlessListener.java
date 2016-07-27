package com.qozix.widget;

import android.support.v7.widget.RecyclerView;

/**
 * Created by michaeldunn on 7/20/16.
 */
public class EndlessListener extends RecyclerView.OnScrollListener {

  private EndlessRecyclerView mEndlessRecyclerView;

  /**
   * Instantiates a new Endless listener.
   *
   * @param endlessRecyclerView the endless recycler view
   */
  public EndlessListener(EndlessRecyclerView endlessRecyclerView) {
    mEndlessRecyclerView = endlessRecyclerView;
  }

  @Override
  public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    mEndlessRecyclerView.onEndlessScroll(dx > 0, dy > 0);
  }

}
