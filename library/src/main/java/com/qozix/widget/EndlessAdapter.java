package com.qozix.widget;

import android.support.v7.widget.RecyclerView;

/**
 * Created by michaeldunn on 7/20/16.
 *
 * @param <VH> the type parameter
 */
public abstract class EndlessAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  /**
   * Fill.
   *
   * @param quantity the quantity
   */
  public void fill(int quantity) {
    if (quantity > 0) {
      pad(quantity);
      fetch(quantity);
    }
  }

  /**
   * Pad.
   *
   * @param quantity the quantity
   */
  public abstract void pad(int quantity);

  /**
   * Fetch.
   *
   * @param quantity the quantity
   */
  public abstract void fetch(int quantity);

}
