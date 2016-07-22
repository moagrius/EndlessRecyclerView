package com.qozix.endlessrecyclerview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.qozix.endlessrecyclerview.models.JsonResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Created by michaeldunn on 7/21/16.
 */
public class MockClient {

  private Random mRandom = new Random();
  private Handler mHandler;
  private Context mContext;
  private int mPage;

  public MockClient(Context context){
    mContext = context;
    mHandler = new Handler(Looper.getMainLooper());
  }

  private String readAssetFile(String fileName){
    try {
      InputStream inputStream = mContext.getAssets().open(fileName);
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      StringBuilder stringBuilder = new StringBuilder();
      String line = bufferedReader.readLine();
      while(line != null){
        stringBuilder.append(line);
        line = bufferedReader.readLine();
      }
      return stringBuilder.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void increment(){
    mPage++;
    if(mPage > 10){
      mPage = 1;
    }
  }

  private String getMockUri(){
    return "json/mock-search-" + mPage + ".json";
  }

  private String getNextResult(){
    return readAssetFile(getMockUri());
  }

  public void fetch(final ResponseReceivedListener responseReceivedListener){
    increment();
    new Thread(new Runnable(){
      @Override
      public void run() {
        int delay = 500 + mRandom.nextInt(700);
        Log.d(MockClient.class.getSimpleName(), "latency=" + delay);
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        String json = getNextResult();
        final JsonResponse jsonResponse = new Gson().fromJson(json, JsonResponse.class);
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            responseReceivedListener.onResponse(jsonResponse);
          }
        });
      }
    }).start();
  }

  public interface ResponseReceivedListener {
    void onResponse(JsonResponse jsonResponse);
  }

}
