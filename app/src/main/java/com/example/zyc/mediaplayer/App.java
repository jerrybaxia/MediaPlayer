package com.example.zyc.mediaplayer;

import android.app.Application;

import com.example.zyc.mediaplayer.utils.LogUtil;

/**
 * @author zyc
 */
public class App extends Application {
    @Override
    public void onCreate() {
        LogUtil.isSaveLog = false;
        super.onCreate();
    }
}
