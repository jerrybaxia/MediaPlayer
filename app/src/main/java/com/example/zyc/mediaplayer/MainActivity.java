package com.example.zyc.mediaplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.zyc.mediaplayer.base.BasePager;
import com.example.zyc.mediaplayer.base.ReplaceFragment;
import com.example.zyc.mediaplayer.pager.AudioPager;
import com.example.zyc.mediaplayer.pager.NetAudioPager;
import com.example.zyc.mediaplayer.pager.NetVideoPager;
import com.example.zyc.mediaplayer.pager.VideoPager;
import com.example.zyc.mediaplayer.utils.LogUtil;

import java.util.ArrayList;

/**
 * @author zyc
 */
public class MainActivity extends FragmentActivity {

    private FrameLayout fl_main_content;
    private RadioGroup rg_bottom_tag;


    private ArrayList<BasePager> basePagers;

    /**
     * 定义选择中位置
     */
    private int position = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fl_main_content = (FrameLayout) findViewById(R.id.fl_main_content);
        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);


        //声明fragment
        basePagers = new ArrayList<>();
        //添加本地视频页面 - 0
        basePagers.add(new VideoPager(this));
        //添加本地音乐页面 - 1
        basePagers.add(new AudioPager(this));
        //添加网络视频页面 - 2
        basePagers.add(new NetVideoPager(this));
        //添加网络音乐页面 - 3
        basePagers.add(new NetAudioPager(this));

        //设置RadioGraoup的监听
        rg_bottom_tag.setOnCheckedChangeListener(new MyBottomTagOnCheckedChangeListener());

        //默认选中首页
        //这里会出现调用两次的问题
        //使用RadioGroup.check()会调用onCheckedChanged()方法三次，
        //而我在onCheckedChanged()方法里判断了调用的是哪个id，然后发送请求创建fragmeng，所以只调用了两次
        //rg_bottom_tag.check(R.id.rb_video);
        ((RadioButton)rg_bottom_tag.findViewById(R.id.rb_video)).setChecked(true);
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

    /**
     * 低部按钮监听
     */
    private class MyBottomTagOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            LogUtil.w("MyBottomTagOnCheckedChangeListener:" + checkedId);
            //根据他的ID选择哪个页面
            switch (checkedId) {
                case R.id.rb_video:
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_net_audio:
                    position = 3;
                    break;
                default:
                    position = 0;
            }

            setFragment();
        }
    }

    /**
     * 把页面添加到Fragment中
     */
    private void setFragment() {
        LogUtil.w("setFragment");
        //1.得到FragmentManager
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事务
        FragmentTransaction ft = manager.beginTransaction();
        //3.替换 用新的Fragment来替换掉当前的Fragment
        ft.replace(R.id.fl_main_content, new ReplaceFragment(getBasePager()));
        //4.提交事务
        ft.commit();
    }

    /**
     * 根据方法得到对应的页面
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if(basePager != null && !basePager.isInitData) {
            //联网请求,绑定数据
            basePager.initData();
            basePager.isInitData = true;
        }
        return basePager;
    }

}
