package com.qozix.endlessrecyclerview;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.qozix.widget.EndlessRecyclerView;

public class MainActivity extends AppCompatActivity {

  private EndlessRecyclerView mEndlessRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final DemoEndlessAdapter demoEndlessAdapter = new DemoEndlessAdapter(this);
    demoEndlessAdapter.setOnItemClickListener(mOnItemClickListener);
    demoEndlessAdapter.setOnFillCompleteListener(mOnFillCompleteListener);
    demoEndlessAdapter.setLimit(1000);

    mEndlessRecyclerView = (EndlessRecyclerView) findViewById(R.id.endlessrecyclerview_main);
    mEndlessRecyclerView.setCanExpectConsistentItemSize(true);
    mEndlessRecyclerView.setAdapter(demoEndlessAdapter);
    mEndlessRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    mEndlessRecyclerView.addOnLayoutChangeListener(mOnLayoutChangeListener);
    //mEndlessRecyclerView.start(30);

  }

  private void updateEndlessRecyclerViewThreshold(){
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

  private DemoEndlessAdapter.OnFillCompleteListener mOnFillCompleteListener = new DemoEndlessAdapter.OnFillCompleteListener() {
    @Override
    public void onFillComplete() {
      mEndlessRecyclerView.rebuild();
      Log.d("MA", "onFillComplete on main thread? " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
    }
  };

}
