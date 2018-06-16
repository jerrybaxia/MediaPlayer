package com.example.zyc.mediaplayer.base;

import android.content.Context;
import android.view.View;

import com.example.zyc.mediaplayer.utils.LogUtil;

/**
 * @author zyc
 */
public abstract class BasePager {

    /**
     * 上下文
     */
    public final Context context;

    /**
     * 接收各个页面的实例
     */
    public View rootView;

    /**
     * 是否已经初始化数据
     */
    public boolean isInitData;

    public BasePager(Context context) {
        this.context = context;
        this.rootView = initView();
    }

    /**
     * 强制子类实现初始界面
     * @return
     */
    public abstract View initView();

    /**
     * 当子页面，需要绑定数据
     */
    public void initData() {
        LogUtil.w("请求并绑定数据");

    }
}
