package com.example.herve.viewcontrolapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.math.BigDecimal;

/**
 * Created by Herve on 2016/6/2.
 */
public class MyTest extends View {

    private Drawable hasScrollBarBg;        //滑动条滑动后背景图
    private Drawable notScrollBarBg;        //滑动条未滑动背景图
    private Drawable mThumbLow;         //前滑块
    private Drawable mThumbHigh;        //后滑块

    private int mScollBarWidth;     //控件宽度=滑动条宽度+滑动块宽度
    private int mScollBarHeight;    //滑动条高度

    private int mThumbWidth;        //滑动块宽度
    private int mThumbHeight;       //滑动块高度

    private double mOffsetLow = 0;     //前滑块中心坐标
    private double mOffsetHigh = 0;    //后滑块中心坐标
    private int mDistance = 0;      //总刻度是固定距离 两边各去掉半个滑块距离

    private int mThumbMarginTop = 30;   //滑动块顶部距离上边框距离，也就是距离字体顶部的距离
    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
            android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };

    public MyTest(Context context) {
        super(context);
        initView();

    }

    public MyTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();

    }

    public MyTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        Resources resources = getResources();
        notScrollBarBg = resources.getDrawable(R.drawable.red);
        hasScrollBarBg = resources.getDrawable(R.drawable.blue);
        mThumbLow = resources.getDrawable(R.drawable.btn_radio_on_holo_dark);
        mThumbHigh = resources.getDrawable(R.drawable.btn_radio_on_holo_dark);

        mThumbLow.setState(STATE_NORMAL);
        mThumbHigh.setState(STATE_NORMAL);

        mScollBarWidth = notScrollBarBg.getIntrinsicWidth();
        mScollBarHeight = notScrollBarBg.getIntrinsicHeight();

        mThumbWidth = mThumbLow.getIntrinsicWidth();
        mThumbHeight = mThumbLow.getIntrinsicHeight();
    }

    private double defaultScreenLow = 0;    //默认前滑块位置百分比
    private double defaultScreenHigh = 100;  //默认后滑块位置百分比

    //默认执行，计算view的宽高,在onDraw()之前
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
//        int height = measureHeight(heightMeasureSpec);
        mScollBarWidth = width;
        mOffsetHigh = width - mThumbWidth / 2;
        mOffsetLow = mThumbWidth / 2;
        mDistance = width - mThumbWidth;

        mOffsetLow = formatDouble(defaultScreenLow / 100 * (mDistance)) + mThumbWidth / 2;
        mOffsetHigh = formatDouble(defaultScreenHigh / 100 * (mDistance)) + mThumbWidth / 2;
        setMeasuredDimension(width, mThumbHeight + mThumbMarginTop + 2);
    }

    public static double formatDouble(double pDouble) {
        BigDecimal bd = new BigDecimal(pDouble);
        BigDecimal bd1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        pDouble = bd1.doubleValue();
        return pDouble;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //wrap_content
        if (specMode == MeasureSpec.AT_MOST) {
        }
        //fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {
        }

        return specSize;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int defaultHeight = 100;
        //wrap_content
        if (specMode == MeasureSpec.AT_MOST) {
        }
        //fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {
            defaultHeight = specSize;
        }

        return defaultHeight;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint text_Paint = new Paint();
        text_Paint.setTextAlign(Paint.Align.CENTER);
        text_Paint.setColor(Color.RED);
        text_Paint.setTextSize(20);

        //前滑块
        mThumbLow.setBounds((int) (mOffsetLow - mThumbWidth / 2), mThumbMarginTop, (int) (mOffsetLow + mThumbWidth / 2), mThumbHeight + mThumbMarginTop);
        mThumbLow.draw(canvas);

        //后滑块
        mThumbHigh.setBounds((int) (mOffsetHigh - mThumbWidth / 2), mThumbMarginTop, (int) (mOffsetHigh + mThumbWidth / 2), mThumbHeight + mThumbMarginTop);
        mThumbHigh.draw(canvas);

    }
}
