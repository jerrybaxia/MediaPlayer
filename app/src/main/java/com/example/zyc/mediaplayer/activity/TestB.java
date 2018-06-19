package com.example.zyc.mediaplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.zyc.mediaplayer.utils.LogUtil;

/**
 * @author zyc
 */
public class TestB extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.e("onCreate--");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("onRestart--");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart--");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("onResume--");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop--");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("onDestroy--");
    }
}
