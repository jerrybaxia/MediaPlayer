package com.example.zyc.mediaplayer.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zyc.mediaplayer.R;
import com.example.zyc.mediaplayer.domain.MediaItem;
import com.example.zyc.mediaplayer.utils.LogUtil;
import com.example.zyc.mediaplayer.utils.Utils;
import com.example.zyc.mediaplayer.view.VitamioVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * @author zyc
 */
public class VitamioVideoPlayer extends Activity implements View.OnClickListener {

    private static final int FULL_SCREEN = 1;
    private static final int DEFAULT_SCREEN = 2;

    private Uri uri;
    private ArrayList<MediaItem> listMediaItem;
    private int position;
    private Utils utils;
    private MyReceive myReceive;

    private RelativeLayout rl_media_controller;
    private VitamioVideoView videoView;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;

    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private TextView tv_laoding_netspeed;
    private LinearLayout ll_loading;


    /**
     * 1.定义手势识别器
     */
    private GestureDetector detector;
    /**
     * 是否全屏
     */
    private boolean isFullScreen;

    /**
     * 屏幕的宽
     */
    private int screenWidth = 0;
    /**
     * 屏幕的高
     */
    private int screenHeight = 0;

    /**
     * 调节声音
     */
    private AudioManager am;

    /**
     * 当前的音量
     */
    private int currentVoice;

    /**
     * 最大的音量 0-15
     */
    private int maxVoice;

    /**
     * 是否为网络视频
     */
    private boolean isNetUri;

    /**
     * 是否使用系统自常
     */
    private boolean isUseSystem = true;

    /**
     * 上次播放到的位置
     */
    private int precurrentPosition;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.e("onCreate--");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitamio_video_player);

        initData();

        findViews();

        getData();


        setListener();


    }

    /**
     * 从父对象中获取数据
     */
    private void getData() {
        //得到播放地址 会传进来
        uri = getIntent().getData();

        listMediaItem = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videoList");
        position = getIntent().getIntExtra("position", 0);

        setData();
    }

    private void setData() {
        if (listMediaItem != null && listMediaItem.size() > 0) {
            //如果列表有数据,优先取列表的数据
            //设置视频名称
            MediaItem mediaItem = listMediaItem.get(position);
            tvName.setText(mediaItem.getName());
            videoView.setVideoURI(Uri.parse(mediaItem.getData()));
            isNetUri = utils.isNetUri(mediaItem.getData());

        } else if (uri != null) {
            videoView.setVideoURI(uri);
            isNetUri = utils.isNetUri(uri.toString());
        } else {
            //没有数据
            Toast.makeText(this, "没有传递数据", Toast.LENGTH_SHORT).show();
        }

        setButtonState();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Vitamio.isInitialized(this);
        utils = new Utils();

        //注册电量广播
        myReceive = new MyReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        //注册接收广播,注意:有注册就要有取消
        registerReceiver(myReceive, intentFilter);

        //2.实例化手势识别器,并且重写双击,点击,长按
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                //                Toast.makeText(SystemVideoPlayer.this, "onLongPress", Toast.LENGTH_SHORT).show();
                btnVideoStartPause.callOnClick();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //Toast.makeText(SystemVideoPlayer.this, "onDoubleTap", Toast.LENGTH_SHORT).show();
                setFullScreenAndDefault();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //                Toast.makeText(SystemVideoPlayer.this, "onSingleTapConfirmed", Toast.LENGTH_SHORT).show();
                if (isShowMediaController) {
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                }
                return super.onSingleTapConfirmed(e);

            }
        });

        //得到屏幕的宽和高
        //过时的方法
        //        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        //        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN:
                //全屏
                //1.设置视频画面的大小，屏幕多大就有多大
                //屏幕有多大就有多大
                videoView.setVideoSize(screenWidth, screenHeight);
                //2.设置按钮的状态-默认
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN:
                //默认
                //1.根据比例来算出最适全的大小
                int width = 0;
                int height = 0;
                MediaPlayer mediaPlayer = (MediaPlayer) videoView.getTag();
                int videoWidth = mediaPlayer.getVideoWidth();
                int videoHeight = mediaPlayer.getVideoHeight();
                float widthMod = (float) screenWidth / videoWidth;
                float heightMod = (float) screenHeight / videoHeight;
                if (widthMod > heightMod) {
                    height = screenHeight;
                    width = Math.round(videoWidth * heightMod);
                } else {
                    width = screenWidth;
                    height = Math.round(videoWidth * widthMod);
                }
                videoView.setVideoSize(width, height);
                //2.设置按钮的状态-默认
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen = false;
                break;
            default:
                break;
        }
    }


    /**
     * 广播接收
     */
    private class MyReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBttery(level);
        }
    }

    /**
     * 设置电量
     *
     * @param level
     */
    private void setBttery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void setListener() {

        btnVoice.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);

        //准备好的监听
        videoView.setOnPreparedListener(new MyOnPreparedListener());
        //播放出错的监听
        videoView.setOnErrorListener(new MyOnErrorListener());
        //播放完成的监听
        videoView.setOnCompletionListener(new MyOnCompletetionListener());

//        设置控制面版
//        videoView.setMediaController(new MediaController(this));

        //设置SeekBar状态变化监听
        seekbarVideo.setOnSeekBarChangeListener(new MySeekBarVideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new MySeekBarVoiceOnSeekBarChangeListener());


        if (isUseSystem) {
            //监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoView.setOnInfoListener(new MyOnInfoListener());
            }
        }
    }


    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    //                    Toast.makeText(SystemVideoPlayer.this, "卡了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    //                    Toast.makeText(SystemVideoPlayer.this, "卡结束了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-06-18 11:33:18 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        videoView = (VitamioVideoView) findViewById(R.id.videoView);
        rl_media_controller = (RelativeLayout) findViewById(R.id.rl_media_controller);

        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwichPlayer = (Button) findViewById(R.id.btn_swich_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = (Button) findViewById(R.id.btn_video_siwch_screen);

        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_laoding_netspeed = (TextView) findViewById(R.id.tv_laoding_netspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

//        MediaController mediaController = new MediaController(this);
//        mediaController.setVisibility(View.GONE);
//        videoView.setMediaController(mediaController);

        //设置声音的最大
        seekbarVoice.setMax(maxVoice);
        //设置当前音量
        seekbarVoice.setProgress(currentVoice);

        //开始更新网速
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-06-18 11:33:18 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            boolean isMute = false;
            if (btnVoice.getTag() == null) {
                isMute = true;
            } else {
                isMute = (boolean) btnVoice.getTag();
            }
            updateVoice(currentVoice, isMute);
            btnVoice.setTag(!isMute);

        } else if (v == btnSwichPlayer) {
            // Handle clicks for btnSwichPlayer
                        showSwichPlayerDialog();
        } else if (v == btnExit) {
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            if (videoView.isPlaying()) {
                videoView.pause();
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
            } else {
                videoView.start();
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            }
        } else if (v == btnVideoNext) {
            playNextVideo();
        } else if (v == btnVideoSiwchScreen) {
            setFullScreenAndDefault();
        }
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
    }


    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("万能播放器提醒您");
        builder.setMessage("当您播放一个视频，有花屏的是，可以尝试使用系统播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSystemPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }



    private void startSystemPlayer() {
        if(videoView != null){
            videoView.stopPlayback();
        }


        Intent intent = new Intent();
        if(listMediaItem != null && listMediaItem.size() > 0){
            intent.setDataAndType(Uri.parse(listMediaItem.get(position).getData()), "Video/*");
        }else if(uri != null){
            intent.setData(uri);
        }
        startActivity(intent);

        finish();//关闭页面
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (listMediaItem != null && listMediaItem.size() > 0) {
            //播放上一个视频
            position--;
            if (position >= 0) {
                //                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = listMediaItem.get(position);
                tvName.setText(mediaItem.getName());
                //                isNetUri = utils.isNetUri(mediaItem.getData());
                videoView.setVideoPath(mediaItem.getData());
                isNetUri = utils.isNetUri(mediaItem.getData());

                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (listMediaItem != null && listMediaItem.size() > 0) {
            //播放下一个
            position++;
            if (position < listMediaItem.size()) {
                //                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = listMediaItem.get(position);
                tvName.setText(mediaItem.getName());
                //                isNetUri = utils.isNetUri(mediaItem.getData());
                videoView.setVideoPath(mediaItem.getData());
                isNetUri = utils.isNetUri(mediaItem.getData());

                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }

    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            //默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            //全屏
            setVideoType(FULL_SCREEN);
        }
    }

    private void setButtonState() {
        if (listMediaItem != null && listMediaItem.size() > 0) {
            if (listMediaItem.size() == 1) {
                setEnable(false);
            } else if (listMediaItem.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);

                } else if (position == listMediaItem.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);

                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == listMediaItem.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置灰色
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }

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

    /**
     * 注意释放资源的时候,先释放子类,再释放父类
     */
    @Override
    protected void onDestroy() {
        LogUtil.e("onDestroy--");
        handler.removeCallbacksAndMessages(null);
        if (myReceive != null) {
            unregisterReceiver(myReceive);
            myReceive = null;
        }
        super.onDestroy();
    }


    /**
     * 视频进度Message
     */
    private static final int PROGRESS = 0;
    private static final int HIDE_MEDIACONTROLLER = 1;
    private static final int SHOW_SPEED = 2;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case  SHOW_SPEED:

                    //显示网速
                    String netSpeed = utils.getNetSpeed(VitamioVideoPlayer.this);
                    tv_laoding_netspeed.setText("加载中..." + " " + netSpeed);
                    tv_buffer_netspeed.setText("缓冲中..." + " " + netSpeed);

                    handler.removeMessages(SHOW_SPEED);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
                case PROGRESS:
                    //1.得到当前的视频的播放进度
                    int currentPosition = (int)videoView.getCurrentPosition();
                    //2.设置SeekBar的位置
                    seekbarVideo.setProgress(currentPosition);
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));

                    //因为这里每一秒种会执行,所以把系统时间也设置进来
                    tvSystemTime.setText(getSystemTime());

                    //缓冲进度更新
                    if (isNetUri) {
                        //只有网络资源才有缓冲效果
                        int buffer = videoView.getBufferPercentage(); //这里为百分比，为0-100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        seekbarVideo.setSecondaryProgress(0);
                    }



                    //监听卡
                    if (!isUseSystem) {
                        //因为currentPosition在播放直播视频源的时候，会为0的情况
                        if(videoView.isPlaying() && currentPosition != 0){
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                //视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                            }
                        }else{
                            ll_buffer.setVisibility(View.GONE);
                        }

                    }

                    //3.每秒更新一次
                    //先把里面的通知移除掉,再添加,这样安全一点
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    break;
                case HIDE_MEDIACONTROLLER:
                    //隐藏控制面版
                    hideMediaController();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private String getSystemTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * 当低层解码准备好的时候
     */
    private class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();

            ll_loading.setVisibility(View.GONE);

            //1.视频总长度
            int duration = (int)mp.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));
            //2.发消息
            handler.sendEmptyMessage(PROGRESS);

            //在这里根据Android设备的宽高来设置视频的宽高
            videoView.setTag(mp);
            setVideoType(DEFAULT_SCREEN);

            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    Toast.makeText(VitamioVideoPlayer.this,"拖动完成了", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * 出错监听
     */
    private class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        /**
         * 出错监听
         *
         * @param mp
         * @param what
         * @param extra
         * @return 在重载的方法里面，一般返回false后面会继续处理，返回true就是拦截处理
         */
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //Toast.makeText(VitamioVideoPlayer.this, "播放出错", Toast.LENGTH_SHORT).show();
            showErrorDialog();
            //这里返回false的话后续的事件还会处理这个错误（会弹出提示框），但如果返回true则这个事件会被拦截销毁掉
            //1.播放的视频格式不支持 -- 利用或跳转到万能播放器继续播放
            //2.播放网络视频的时候，网络中断 -- 1.如果网络确认断了，可以提示网络断了，2.网络断断续续，重新播放
            //3.播放的时候本地文件中间有空白 -- 让下载做完成
            return true;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("抱歉，无法播放该视频！！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    /**
     * 播放完成监听
     */
    private class MyOnCompletetionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //            Toast.makeText(SystemVideoPlayer.this, "播放完成" + uri, Toast.LENGTH_SHORT).show();
            playNextVideo();
        }
    }


    private class MySeekBarVideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当手指滑动的时候,会引起seekBar进度变化,会回调这个方法
         *
         * @param seekBar  对象
         * @param progress 位置
         * @param fromUser 是否由用户触发(这里特别注意)
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                tvCurrentTime.setText(utils.stringForTime(progress));
            }
        }

        /**
         * 当手指触碰开始的时候
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指触碰结束的时候
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            videoView.seekTo(seekBar.getProgress());
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 按下声音的时候记录大小
     */
    private int mVol;
    /**
     * 位置
     */
    float startY, endY;
    int touchRang;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3.把gkwrh传递给手势识别器
        detector.onTouchEvent(event);

        //这里处理声音大小事件
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                //这里取出当前移动上下的最大位置，应该可能有横坚屏问题
                touchRang = Math.min(screenWidth, screenHeight);
                break;
            //手指移动
            case MotionEvent.ACTION_MOVE:
                endY = event.getY();
                //移动位移
                float distanceY = startY - endY;
                //改变的音量 = (滑动屏幕的距离：总距离) * 音量最大值
                float delta = (distanceY / touchRang) * maxVoice;
                int voice = Math.min(maxVoice, Math.max(currentVoice += delta, 0));//max是防止小于0
                updateVoice(voice, false);
                break;
            //手指放开
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }


    /**
     * 是否已经显示
     */
    private boolean isShowMediaController = false;

    /**
     * 显示控制面版
     */
    private void showMediaController() {
        rl_media_controller.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    private void hideMediaController() {
        rl_media_controller.setVisibility(View.GONE);
        LogUtil.w(rl_media_controller.getVisibility() + "");
        isShowMediaController = false;
    }

    private class MySeekBarVoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                //发出更新音量
                updateVoice(progress, false);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //在这里同样要不让控制器隐藏
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 设置每夜媒体声音大小
     *
     * @param value
     */
    private void updateVoice(int value, boolean isMute) {
        if (value > 0) {
            btnVoice.setTag(true);
        }
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 1);
        } else {
            //参数1类型，参数2音量，flags如果是1的话，会显示系统的调音，如果为0则不会
            am.setStreamVolume(AudioManager.STREAM_MUSIC, value, 1);
            currentVoice = value;
        }
        if (seekbarVideo.getProgress() != value && !isMute) {
            seekbarVoice.setProgress(value);
        }
    }


    /**
     * 处理实体按钮事件 监听手机的调整声音大小
     *
     * @param keyCode
     * @param event
     * @return 使用return false时，onKeyDown代码会继续执行，使用return true时，onKeyDown代码不会执行 区别在于要不要把这个事件吃掉。
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
        return super.onKeyDown(keyCode, event);
    }
}
