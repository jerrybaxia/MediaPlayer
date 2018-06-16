package com.example.zyc.mediaplayer;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author zyc
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    /**
     * 在类成员里面声明的Handle的时候，一定要在OnDestroy中进行removeCallbacksAndMessages，移除掉所有事件
     */
    private Handler handlerMain = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handlerMain.postDelayed(new Runnable() {
            @Override
            public void run() {
                //在这里通知进行跳转到MainActivity
                //当前纯种为
                Log.w(TAG,"当前线程名称为：" + Thread.currentThread().getName());
                startMainActivity();
            }
        }, 2000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG,"onTouchEvent");
        startMainActivity();
        return super.onTouchEvent(event);
    }

    private boolean isStartMainActivity;

    /**
     * 跳转到主页面，并且把当前页面关闭掉，但这里可能是被点击和定时器同时进来，那么要进行一个锁
     */
    private synchronized void startMainActivity() {
        if(!isStartMainActivity) {
            isStartMainActivity = true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //关闭当前页面
            finish();
            Log.w(TAG, "startMainActivity");
        }

    }

    @Override
    protected void onDestroy() {
        //去除掉所有handler事件
        handlerMain.removeCallbacksAndMessages(null);
        super.onDestroy();

    }
}
