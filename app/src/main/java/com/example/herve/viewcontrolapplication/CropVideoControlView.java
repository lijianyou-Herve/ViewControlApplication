package com.example.herve.viewcontrolapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Herve on 2016/4/10.
 */
public class CropVideoControlView extends RelativeLayout {

    private String TAG = getClass().getSimpleName();

    private LinearLayout ll_crop_one_l_button;
    private LinearLayout ll_crop_one_r_button;
    private RelativeLayout rl_photo_edit_tool_bar;
    private LinearLayout ll_video_crop_middle;
    private ImageView iv_crop_one_l_button;
    private ImageView iv_crop_one_r_button;

    /*变量清单*/
    private Context mContext;
    //是否是单滑块
    private boolean singleAble = false;

    private int dmw, dmh;

    /*滑块*/

    private boolean isFirstOnTouch = true;//防止第一次点击的时候误滑动

    //滑块的宽度，总控件的宽度
    private double measuredWidth = 50;
    private double totalWidth = 1000;
    //最长的宽度，最小的宽度
    private double minWidth = 62;
    private double maxWidth = 100;

    //最长的时间，最小的时间
    private double minCurrent = 62;
    private double maxCurrent = 100;

    private double totalCurrent = 1000;

    private ProgressListener progressListener;

    private double finalLeftTime = -1;//最后输出给外部的开始时间
    private double finalRightTime = -1;//最后输出给外部的结束时间

    private double leftMarginChange = 0;//左边的位置改变值，累计到可以改变UI后进行重新的初始化
    private double rightMarginChange = 0;//右边的位置改变值，累计到可以改变UI后进行重新的初始化
    private double scale = 1;//根据屏幕宽度 和 需要裁剪的 总时间长  计算转化值
    //计算滑动距离
    private int lastX = 0;
    private int lastY = 0;

    /*判断类型数值*/
    private int witchView = 0;//判断当前点击的是哪一个滑块
    public static final int LEFT_ON_TOUCH = 0;
    public static final int RIGHT_ON_TOUCH = 1;

    private final int WITCH_SINGLE = 0;//
    private final int WITCH_DOUBLE_LEFT = 1;
    private final int WITCH_DOUBLE_RIGHT = 2;
    /*清单*/

    public CropVideoControlView(Context context) {
        super(context);
        this.mContext = context;
        initView();

    }

    public CropVideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();

    }

    public CropVideoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();

    }

    private void initView() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        dmw = dm.widthPixels;
        dmh = dm.heightPixels;

        View root = LayoutInflater.from(mContext).inflate(R.layout.herve_layout, null);
        ll_crop_one_l_button = (LinearLayout) root.findViewById(R.id.ll_crop_one_l_button);
        ll_crop_one_r_button = (LinearLayout) root.findViewById(R.id.ll_crop_one_r_button);
        ll_video_crop_middle = (LinearLayout) root.findViewById(R.id.ll_video_crop_middle);
        rl_photo_edit_tool_bar = (RelativeLayout) root.findViewById(R.id.rl_photo_edit_tool_bar);
        iv_crop_one_l_button = (ImageView) root.findViewById(R.id.iv_crop_one_l_button);
        iv_crop_one_r_button = (ImageView) root.findViewById(R.id.iv_crop_one_r_button);

        addView(root);

        initUI();
        updateVideoCropPullPosition();

    }

    private void initUI() {
        rl_photo_edit_tool_bar.setLayoutParams(UI.getRelativeLayoutPararmWH(dmw, dmh, UI.ORG_SCREEN_WIDTH, 140));

        double oneCropVideoFrameHeight = dmw * 100 / UI.ORG_SCREEN_WIDTH;

        ll_crop_one_l_button.setLayoutParams(UI.getLinearLayoutPararmWHTrue((int) (oneCropVideoFrameHeight * 0.2), (int) oneCropVideoFrameHeight));
        ll_crop_one_r_button.setLayoutParams(UI.getLinearLayoutPararmWHTrue((int) (oneCropVideoFrameHeight * 0.2), (int) oneCropVideoFrameHeight));

    }

    private void updateVideoCropPullPosition() {

        /*
         * 左滑块
		 */

        ll_crop_one_l_button.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (witchView == RIGHT_ON_TOUCH) {
                    isFirstOnTouch = true;
                }
                iv_crop_one_l_button.setImageResource(R.drawable.crop_leftfreepull_icon_selected);
                iv_crop_one_r_button.setImageResource(R.drawable.crop_rightpull_icon);

                witchView = LEFT_ON_TOUCH;

                return moveAction(event, WITCH_DOUBLE_LEFT);

            }

        });

        ll_crop_one_r_button.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                if (witchView == LEFT_ON_TOUCH) {
                    isFirstOnTouch = true;
                }
                iv_crop_one_l_button.setImageResource(R.drawable.crop_leftpull_icon);
                iv_crop_one_r_button.setImageResource(R.drawable.crop_rightfreepull_icon_selected);

                witchView = RIGHT_ON_TOUCH;

                return moveAction(event, WITCH_DOUBLE_RIGHT);

            }

        });

        ll_video_crop_middle.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                if (singleAble) {
                    return moveAction(event, WITCH_SINGLE);
                }
                return false;

            }

        });

		/*
         * 右滑块
		 */

    }

    private boolean moveAction(MotionEvent event, int witch) {

        View view = ll_crop_one_l_button;

        //检测到触摸事件后 第一时间得到相对于父控件的触摸点坐标 并赋值给x,y
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            //触摸事件中绕不开的第一步，必然执行，将按下时的触摸点坐标赋值给 lastX 和 last Y
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                progressListener.onProgressBefore();
                break;
            //触摸事件的第二步，这时候的x,y已经随着滑动操作产生了变化，用变化后的坐标减去首次触摸时的坐标得到 相对的偏移量
            case MotionEvent.ACTION_MOVE:

                int offsetX = x - lastX;
                int offsetY = y - lastY;
                //使用 layout 进行重新定位

                switch (witch) {
                    case WITCH_SINGLE:
                        if (offsetX <= 0) {

                            view = ll_crop_one_l_button;

                        } else {

                            view = ll_crop_one_r_button;

                        }
                        break;
                    case WITCH_DOUBLE_LEFT:
                        view = ll_crop_one_l_button;
                        break;
                    case WITCH_DOUBLE_RIGHT:
                        view = ll_crop_one_r_button;

                        break;

                }
                Log.e(TAG, "moveAction:滑动offsetX=" + offsetX);

                if (isFirstOnTouch) {
                    if (Math.abs(offsetX) > 2) {//第一点点击要偏移量大于2才允许移动
                        isFirstOnTouch = false;
                    }

                    formatTimeForProgressChanged(finalLeftTime, finalRightTime);

                } else {
                    if (Math.abs(offsetX) > 1) {

                        interactiveView(view, offsetX, true);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                progressListener.onProgressAfter();

                break;

        }
        return true;
    }

    private void interactiveView(View view, double offsetX, boolean isChangeView) {

        int actionType = -1;

        final int ACTIONTYPE_LEFT_PULL = 0;
        final int ACTIONTYPE_LEFT_PUSH = 1;
        final int ACTIONTYPE_RIGHT_PULL = 2;
        final int ACTIONTYPE_RIGHT_PUSH = 3;


        /*左边滑块的布局信息*/
        LinearLayout.LayoutParams leftLayoutParams = (LinearLayout.LayoutParams) ll_crop_one_l_button.getLayoutParams();

        /*左边滑块的布局信息*/
        LinearLayout.LayoutParams rightlayoutParams = (LinearLayout.LayoutParams) ll_crop_one_r_button.getLayoutParams();
        double totalmeasuredWidth = totalWidth - 2 * measuredWidth;
        Log.e(TAG, "interactiveView:关注 leftLayoutParams.leftMargin=" + leftLayoutParams.leftMargin);
        Log.e(TAG, "interactiveView:关注 rightlayoutParams.rightMargin=" + rightlayoutParams.rightMargin);
        Log.e(TAG, "interactiveView:关注 offsetX=" + offsetX);

        if (view == ll_crop_one_l_button) {
            Log.e(TAG, "interactiveView:关注 左边");

            /*左边的最大距离*/
            double minLeftmargin = totalmeasuredWidth - rightlayoutParams.rightMargin - minWidth;

            double maxLeftmargin = totalmeasuredWidth - rightlayoutParams.rightMargin - maxWidth;

            //超过最大允许距离，推动右边
            if (leftLayoutParams.leftMargin + offsetX > minLeftmargin) {
                Log.e(TAG, "interactiveView:关注 推动右边");

                actionType = ACTIONTYPE_LEFT_PUSH;
                if (leftLayoutParams.leftMargin + offsetX >= totalmeasuredWidth - 0 - minWidth) {
                    rightlayoutParams.rightMargin = 0;
                    Log.e(TAG, "interactiveView:关注 推动右边A");

                    leftLayoutParams.leftMargin = (int) (totalmeasuredWidth - 0 - minWidth);

                } else {
                    leftLayoutParams.leftMargin = (int) (leftLayoutParams.leftMargin + offsetX);
                    Log.e(TAG, "interactiveView:关注 推动右边B");
                    Log.e(TAG, "interactiveView:关注 推动右边BminLeftmargin=" + minLeftmargin);

                    rightlayoutParams.rightMargin = (int) (rightlayoutParams.rightMargin - offsetX);

                }
                //超过最大允许距离，拉动右边
            } else if (leftLayoutParams.leftMargin + offsetX <= maxLeftmargin) {
                Log.e(TAG, "interactiveView: 拉动右边");
                actionType = ACTIONTYPE_LEFT_PULL;

                if (leftLayoutParams.leftMargin + offsetX <= 0) {
                    Log.e(TAG, "interactiveView: 拉动右边A");

                    leftLayoutParams.leftMargin = 0;
                    if (rightlayoutParams.rightMargin < totalmeasuredWidth - maxWidth) {

                        rightlayoutParams.rightMargin = (int) (totalmeasuredWidth - maxWidth);
                    }

                } else {
                    Log.e(TAG, "interactiveView: 拉动右边B");

                    leftLayoutParams.leftMargin = (int) (maxLeftmargin + offsetX);

                    rightlayoutParams.rightMargin = (int) (rightlayoutParams.rightMargin - offsetX);

                }

                //越界
            } else if (leftLayoutParams.leftMargin + offsetX < 0) {
                Log.e(TAG, "interactiveView: 越界");

                leftLayoutParams.leftMargin = 0;

                //正常范围
            } else {
                leftLayoutParams.leftMargin = (int) (leftLayoutParams.leftMargin + offsetX);
                //最小距离
            }
            //右边滑块
        } else if (view == ll_crop_one_r_button) {
            Log.e(TAG, "interactiveView: 右边");
            double minRightmargin = totalmeasuredWidth - leftLayoutParams.leftMargin - minWidth;

            double maxRightmargin = totalmeasuredWidth - leftLayoutParams.leftMargin - maxWidth;
            //距离右边最大距离，拉动左边
            if (rightlayoutParams.rightMargin - offsetX <= maxRightmargin) {
                actionType = ACTIONTYPE_RIGHT_PULL;

                if (rightlayoutParams.rightMargin - offsetX <= 0) {
                    if (leftLayoutParams.leftMargin < (totalmeasuredWidth - 0 - maxWidth)) {

                        leftLayoutParams.leftMargin = (int) (totalmeasuredWidth - 0 - maxWidth);
                    }

                    rightlayoutParams.rightMargin = 0;
                } else {

                    rightlayoutParams.rightMargin = (int) (rightlayoutParams.rightMargin - offsetX);
                    leftLayoutParams.leftMargin = (int) (leftLayoutParams.leftMargin + offsetX);

                }

                //距离右边最大距离，推动左边
            } else if (rightlayoutParams.rightMargin - offsetX > minRightmargin) {
                actionType = ACTIONTYPE_RIGHT_PUSH;

                if (rightlayoutParams.rightMargin - offsetX >= totalmeasuredWidth - 0 - minWidth) {

                    leftLayoutParams.leftMargin = 0;

                    rightlayoutParams.rightMargin = (int) (totalmeasuredWidth - 0 - minWidth);
                } else {
                    leftLayoutParams.leftMargin = (int) (leftLayoutParams.leftMargin + offsetX);

                    rightlayoutParams.rightMargin = (int) (minRightmargin - offsetX);
                }

                //越界
            } else if (rightlayoutParams.rightMargin - offsetX < 0) {

                rightlayoutParams.rightMargin = 0;
            } else {

                rightlayoutParams.rightMargin = (int) (rightlayoutParams.rightMargin - offsetX);

                //距离右边最小距离
            }

        }

        if (leftLayoutParams.leftMargin < 0) {
            leftLayoutParams.leftMargin = 0;
        }
        if (rightlayoutParams.rightMargin < 0) {
            rightlayoutParams.rightMargin = 0;
        }
        ll_crop_one_l_button.setLayoutParams(leftLayoutParams);

        ll_crop_one_r_button.setLayoutParams(rightlayoutParams);

        double leftxx = leftLayoutParams.leftMargin * totalCurrent / totalmeasuredWidth;

        double rightxx = (totalmeasuredWidth - rightlayoutParams.rightMargin) * totalCurrent / totalmeasuredWidth;
        Log.e(TAG, "interactiveView: 计算leftMargin=" + leftLayoutParams.leftMargin);
        Log.e(TAG, "interactiveView: 计算rightMargin=" + rightlayoutParams.rightMargin);
        Log.e(TAG, "interactiveView: totalmeasuredWidth=" + totalmeasuredWidth);
        Log.e(TAG, "interactiveView: 计算rightMargin=" + rightlayoutParams.rightMargin);

        if (progressListener != null) {
            if (isChangeView) {
                Log.e(TAG, "interactiveView: 计算leftxx=" + leftxx);
                Log.e(TAG, "interactiveView: 计算rightxx=" + rightxx);
                finalLeftTime = Math.floor(leftxx * 10) / 10;
                finalRightTime = Math.ceil(rightxx * 10) / 10;
                if (finalLeftTime < 0) {
                    finalLeftTime = 0;
                }

                if (finalRightTime > totalCurrent) {
                    finalRightTime = totalCurrent;
                }

                if (finalLeftTime > totalCurrent - minCurrent) {
                    finalLeftTime = totalCurrent - minCurrent;
                    Log.e(TAG + "BUG", "interactiveView: A");
                }

                if (finalRightTime - finalLeftTime < minCurrent) {

                    finalRightTime = finalLeftTime + minCurrent;
                    Log.e(TAG + "BUG", "interactiveView: B");

                }

                switch (actionType) {
                    case ACTIONTYPE_LEFT_PULL:
                        finalRightTime = finalLeftTime + maxCurrent;

                        break;
                    case ACTIONTYPE_LEFT_PUSH:
                        finalRightTime = finalLeftTime + minCurrent;

                        break;
                    case ACTIONTYPE_RIGHT_PULL:
                        finalLeftTime = finalRightTime - maxCurrent;

                        break;
                    case ACTIONTYPE_RIGHT_PUSH:
                        finalLeftTime = finalRightTime - minCurrent;

                        break;

                }

                formatTimeForProgressChanged(finalLeftTime, finalRightTime);

            }

        }

    }

    public boolean initViewForSingle(double startCurrent, double totalCurrent, double unChangeCurrent) {

        if (totalCurrent < startCurrent + unChangeCurrent) {

            Toast.makeText(mContext, "初始化时间:" + startCurrent + "+" + unChangeCurrent + "=" + (startCurrent + unChangeCurrent) + " > " + minWidth, Toast.LENGTH_SHORT).show();

            return false;
        }
        initView(startCurrent, startCurrent + unChangeCurrent, totalCurrent, unChangeCurrent, unChangeCurrent);

        return true;

    }

    public boolean initView(double startCurrent, double endCurrent, double totalCurrent, double minCurrent, double maxCurrent) {

        formatTime(startCurrent);
        formatTime(endCurrent);
        formatTime(totalCurrent);
        formatTime(minCurrent);
        formatTime(maxCurrent);

        double middle = endCurrent - startCurrent;

        RelativeLayout.LayoutParams layoutparms = (LayoutParams) rl_photo_edit_tool_bar.getLayoutParams();
        LinearLayout.LayoutParams leftLayoutParams = (LinearLayout.LayoutParams) ll_crop_one_l_button.getLayoutParams();
        LinearLayout.LayoutParams rightlayoutParams = (LinearLayout.LayoutParams) ll_crop_one_r_button.getLayoutParams();

        this.totalCurrent = totalCurrent;
        this.minCurrent = minCurrent;
        this.maxCurrent = maxCurrent;

        totalWidth = layoutparms.width;

        measuredWidth = leftLayoutParams.width;

        minWidth = (int) ((totalWidth - 2 * measuredWidth) * minCurrent / totalCurrent);

        maxWidth = (int) ((totalWidth - 2 * measuredWidth) * maxCurrent / totalCurrent);

        scale = (totalWidth - 2 * measuredWidth) / totalCurrent;
        Log.e(TAG, "initView:初始化 totalWidth" + totalWidth);
        Log.e(TAG, "initView:初始化 measuredWidth" + measuredWidth);
        Log.e(TAG, "initView:初始化 totalCurrent" + totalCurrent);
        finalLeftTime = startCurrent;
        finalRightTime = endCurrent;

        if (minCurrent > middle && middle > maxCurrent) {

            Toast.makeText(mContext, "不能小于最小值:" + middle + "<" + minWidth, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (minCurrent == maxCurrent) {
            singleAble = true;
        }

        leftLayoutParams.leftMargin = (int) (startCurrent * scale);

        ll_crop_one_l_button.setLayoutParams(leftLayoutParams);

        rightlayoutParams.rightMargin = (int) (totalWidth - 2 * measuredWidth - endCurrent * scale);

        ll_crop_one_r_button.setLayoutParams(rightlayoutParams);

        return true;

    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);

    }

    /*设置左边的滑动*/
    public void setLeftChange(double setLeftChange) {
        leftMarginChange += setLeftChange;

        double change = leftMarginChange * scale;

        getLeftTime(setLeftChange);

        if (Math.abs(change) >= 1) {
            leftMarginChange = 0;
            interactiveView(ll_crop_one_l_button, change, false);

        }

    }

    private void getLeftTime(double setLeftChange) {

        finalLeftTime += setLeftChange;
        if (finalLeftTime <= 0) {
            finalLeftTime = 0;

        } else if (finalLeftTime + minCurrent >= totalCurrent) {

            finalLeftTime = totalCurrent - minCurrent;
            finalRightTime = totalCurrent;

        } else {
            if (finalLeftTime + minCurrent >= finalRightTime) {
                finalRightTime = finalLeftTime + minCurrent;
            } else if (finalRightTime - finalLeftTime > maxCurrent) {

                finalRightTime = finalLeftTime + maxCurrent;
            }

        }
        Log.e(TAG, "getLeftTime时间:finalLeftTime=" + finalLeftTime);
        Log.e(TAG, "getLeftTime时间:finalRightTime=" + finalRightTime);
        formatTimeForProgressChanged(finalLeftTime, finalRightTime);

    }

    /*设置左边的滑动*/
    public void setRightChange(double setLeftChange) {

        rightMarginChange += setLeftChange;

        double change = rightMarginChange * scale;

        getRightTime(setLeftChange);

        if (Math.abs(change) >= 1) {

            rightMarginChange = 0;
            interactiveView(ll_crop_one_r_button, change, false);

        }

    }

    private void getRightTime(double setLeftChange) {
        finalRightTime += setLeftChange;

        if (finalRightTime >= totalCurrent) {
            Log.e(TAG, "getRightTime: 第一种");
            Log.e(TAG, "getRightTime: 第一种finalLeftTime=" + finalLeftTime);
            Log.e(TAG, "getRightTime: 第一种finalRightTime=" + finalRightTime);
            finalRightTime = totalCurrent;
        } else if (finalRightTime - minCurrent <= finalLeftTime) {
            Log.e(TAG, "getRightTime: 第二种");
            finalLeftTime = finalRightTime - minCurrent;
            Log.e(TAG, "getRightTime: 第二种finalLeftTime=" + finalLeftTime);
            Log.e(TAG, "getRightTime: 第二种finalRightTime=" + finalRightTime);
            if (finalLeftTime <= 0) {
                finalLeftTime = 0;
                finalRightTime = minCurrent;
            }

        } else {
            Log.e(TAG, "getRightTime: 第三种");
            Log.e(TAG, "getRightTime: 第三种finalLeftTime=" + finalLeftTime);
            Log.e(TAG, "getRightTime: 第三种finalRightTime=" + finalRightTime);
            Log.e(TAG, "getRightTime: 第三种finalRightTime=" + maxCurrent);
            if (finalRightTime - finalLeftTime > maxCurrent) {

                finalLeftTime = finalRightTime - maxCurrent;
            }
            if (finalRightTime - finalLeftTime > maxCurrent) {

            }

        }
        Log.e(TAG, "getRightTime: 第三种AAAfinalRightTime=" + finalRightTime);
        Log.e(TAG, "getRightTime: 第三种AAAfinalLeftTime=" + finalLeftTime);
        formatTimeForProgressChanged(finalLeftTime, finalRightTime);
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        formatTimeForProgressChanged(finalLeftTime, finalRightTime);

    }

    //统一处理输出后的时间，防止小数点转型产生的误差
    private void formatTimeForProgressChanged(double finalLeftTime, double finalRightTime) {

        finalLeftTime = formatTime(finalLeftTime);
        finalRightTime = formatTime(finalRightTime);

        if (progressListener != null) {
            progressListener.onProgressChanged(this, finalLeftTime, finalRightTime, witchView);

        }

    }

    //格式化时间，统一格式化为 小数点后带一位（可以根据需求修改）
    private double formatTime(double valueTime) {
        Log.e(TAG, "formatTime: valueTime之前=" + valueTime);

        valueTime = (double) Math.round(valueTime * 10) / 10;
        Log.e(TAG, "formatTime: valueTime=" + valueTime);
        return valueTime;
    }

    interface ProgressListener {
        //滑动前
        void onProgressBefore();

        //滑动时
        void onProgressChanged(CropVideoControlView controlView, double startCurrent, double endCurrent, int witchView);

        //滑动后
        void onProgressAfter();
    }
}