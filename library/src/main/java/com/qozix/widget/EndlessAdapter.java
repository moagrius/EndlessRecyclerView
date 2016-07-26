package com.qozix.widget;

import android.support.v7.widget.RecyclerView;

/**
 * Created by michaeldunn on 7/20/16.
 */
public abstract class EndlessAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  public void fill(int quantity){
    if(quantity > 0){
      pad(quantity);
      fetch(quantity);
    }
  }

  public abstract void pad(int quantity);
  public abstract void fetch(int quantity);

}
