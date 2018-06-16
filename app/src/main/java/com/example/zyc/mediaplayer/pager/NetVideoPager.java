package com.example.zyc.mediaplayer.pager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.example.zyc.mediaplayer.base.BasePager;
import com.example.zyc.mediaplayer.utils.LogUtil;

/**
 * @author zyc
 * 网络视频
 */
public class NetVideoPager extends BasePager {

    private static final String TAG = VideoPager.class.getSimpleName();
    private TextView textView;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.w("网络视频页面，被初始化了");
        textView = new TextView(this.context);
        textView.setTextSize(25);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.RED);
        return textView;
    }

    @Override
    public void initData() {
        textView.setText("网络视频");
        LogUtil.w("网络视频页面，加载数据...");
    }
}