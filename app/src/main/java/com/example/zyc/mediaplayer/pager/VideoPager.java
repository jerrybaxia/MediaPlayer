package com.example.zyc.mediaplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zyc.mediaplayer.R;
import com.example.zyc.mediaplayer.activity.SystemVideoPlayer;
import com.example.zyc.mediaplayer.adapter.VideoPagerAdapter;
import com.example.zyc.mediaplayer.base.BasePager;
import com.example.zyc.mediaplayer.domain.MediaItem;
import com.example.zyc.mediaplayer.utils.LogUtil;
import com.example.zyc.mediaplayer.utils.ThreadPoolProxyFactory;

import java.util.ArrayList;

/**
 * @author zyc
 * 本地视频页面
 */
public class VideoPager extends BasePager {

    private ListView ls_list;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> listMediaItem;


    private static final int LIST_VIEW_DATA_COMPLETE = 0;

    private Handler handler = new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case LIST_VIEW_DATA_COMPLETE:
                            if (listMediaItem != null && listMediaItem.size() > 0) {
                                //有数据
                                //设置适配器
                                ls_list.setAdapter(new VideoPagerAdapter(context, listMediaItem));
                                //文本隐藏
                                tv_nomedia.setVisibility(View.GONE);
                            } else {
                                //没有数据
                                //文本显示
                                tv_nomedia.setVisibility(View.VISIBLE);
                            }
                            //progressBar隐藏
                            pb_loading.setVisibility(View.GONE);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.w("本地视频页面，被初始化了");
        View view = View.inflate(context, R.layout.video_pager, null);

        ls_list = (ListView) view.findViewById(R.id.ls_list);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);

        //设置ListView的Item点击事件
        ls_list.setOnItemClickListener(new MyListViewOnItemClickListener());

        return view;
    }


    private class MyListViewOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MediaItem mediaItem = listMediaItem.get(position);
            //            Toast.makeText(context, "MediaItem:" + mediaItem.toString(), Toast.LENGTH_SHORT).show();
            //1.调起系统所有的播放器 -
            //            Intent intent = new Intent();
            //            intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
            //            context.startActivity(intent);
            //显示调用
            //            Intent intent = new Intent(context, SystemVideoPlayer.class);
            //            intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
            //            context.startActivity(intent);
            //传列表
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            //Bundle传递的对象一定是要序列化的,如果MediaItem没有支持序列化,则会报错
            //Parcel: unable to marshal value MediaItem
            //只要报错出现Parcel,那肯定就是没有序列化
            //Parcelable 和 Serializable
            //当只是在内存时用 Parcelable 当要保存到磁盘时用 Serializable
            bundle.putSerializable("videoList", listMediaItem);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
            context.startActivity(intent);
        }
    }

    @Override
    public void initData() {
        LogUtil.w("本地视频页面，加载数据...");
        //加载本地视频数据
        getDataFromLocal();
    }

    /**
     * 加载本地的SDCard数据
     * 1:遍历SDCard,后缀名为.mp4,.avi,.wmv
     * 2:从内容提供者里面获取视频信息(当SD卡可用的时候,Android自动会进行扫描,并以内容提供者方式把内容暴露出去)
     * 3:如果是6.0以后的Android,那则需要添加动态权限
     */
    private void getDataFromLocal() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                //取得权限
                //                isGrantExternalRW((Activity) context);
                //声明一个内容观察者
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        //视频文件在SDCard的名称
                        MediaStore.Video.Media.DISPLAY_NAME,
                        //视频总时长
                        MediaStore.Video.Media.DURATION,
                        //视频的文件大小
                        MediaStore.Video.Media.SIZE,
                        //视频的绝对地址
                        MediaStore.Video.Media.DATA,
                        //歌曲的演唱者
                        MediaStore.Video.Media.ARTIST,
                };
                listMediaItem = new ArrayList<>();
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            //视频名称
                            String name = cursor.getString(0);
                            //视频时长
                            long duration = cursor.getLong(1);
                            //视频的文件大小
                            long size = cursor.getLong(2);
                            //视频的播放地址
                            String data = cursor.getString(3);
                            //歌曲的演唱者
                            String artist = cursor.getString(4);

                            MediaItem mediaItem = new MediaItem();
                            mediaItem.setName(name);
                            mediaItem.setDuration(duration);
                            mediaItem.setSize(size);
                            mediaItem.setData(data);
                            mediaItem.setArtist(artist);
                            listMediaItem.add(mediaItem);
                        }
                    } catch (Exception e) {
                    } finally {
                        cursor.close();
                    }
                    handler.sendEmptyMessage(LIST_VIEW_DATA_COMPLETE);
                }

            }
        });
    }
}
