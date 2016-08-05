package com.qozix.endlessrecyclerview.demo.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.qozix.endlessrecyclerview.R;
import com.qozix.endlessrecyclerview.demo.MainActivity;
import com.qozix.widget.EndlessRecyclerView;

/**
 * Created by michaeldunn on 8/5/16.
 */
public class SimpleDemoActivity extends AppCompatActivity {

  private EndlessRecyclerView mEndlessRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.endlessrecyclerview);

    SimpleDemoEndlessAdapter simpleDemoEndlessAdapter = new SimpleDemoEndlessAdapter(this);
    simpleDemoEndlessAdapter.setOnItemClickListener(mOnItemClickListener);
    simpleDemoEndlessAdapter.setLimit(5000);

    mEndlessRecyclerView = (EndlessRecyclerView) findViewById(R.id.endlessrecyclerview_main);
    mEndlessRecyclerView.setCanExpectConsistentItemSize(true);
    mEndlessRecyclerView.setAdapter(simpleDemoEndlessAdapter);
    mEndlessRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    mEndlessRecyclerView.addOnLayoutChangeListener(mOnLayoutChangeListener);

  }

  private void updateEndlessRecyclerViewThreshold() {
    mEndlessRecyclerView.setVerticalThreshold(mEndlessRecyclerView.getHeight() * 3);
  }

  private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Log.d(MainActivity.class.getSimpleName(), "clicked!");
    }
  };

  private View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      updateEndlessRecyclerViewThreshold();
    }
  };

}
