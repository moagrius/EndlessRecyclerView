package com.qozix.widget;

import android.support.v7.widget.RecyclerView;

/**
 * Created by michaeldunn on 7/20/16.
 *
 * @param <VH> the type parameter
 */
public abstract class EndlessAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  /**
   * Fill the dataset with enough items to meet the specified threshold.
   *
   * @param quantity the quantity
   */
  public abstract void fill(int quantity);

}
