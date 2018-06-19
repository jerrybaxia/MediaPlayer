package com.example.zyc.mediaplayer.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * @author zyc
 */
public class VideoView extends android.widget.VideoView {


    private Context context;

    /**
     * 在代码中实例化该类的时候,使用这个方法
     *
     * @param context
     */
    public VideoView(Context context) {
        this(context, null);
    }

    /**
     * 当在布局文件使用该类的时候,Android系统通过这个构造方法实例化类
     *
     * @param context
     * @param attrs
     */
    public VideoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 当需要设置样式的时候,可以使用该方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public VideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置视频的宽和高
     *
     * @param videoWidth 指定视频的宽
     * @param videoHeight 指定视频的高
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = videoWidth;
        params.height = videoHeight;
        setLayoutParams(params);
        requestLayout();
    }
}
