package com.qozix.endlessrecyclerview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.qozix.endlessrecyclerview.demo.network.MockNetworkDemoActivity;
import com.qozix.endlessrecyclerview.R;
import com.qozix.endlessrecyclerview.demo.simple.SimpleDemoActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void startSimpleDemo(View view) {
    Intent intent = new Intent(this, SimpleDemoActivity.class);
    startActivity(intent);
  }

  public void startMockNetworkDemo(View view) {
    Intent intent = new Intent(this, MockNetworkDemoActivity.class);
    startActivity(intent);
  }

}
