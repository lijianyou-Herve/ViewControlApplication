package com.example.herve.viewcontrolapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.cropVideoControlView)
    CropVideoControlView cropVideoControlView;
    @Bind(R.id.btn_left)
    Button btn_left;
    @Bind(R.id.btn_right)
    Button btn_right;

    @Bind(R.id.tv_endTime)
    TextView tv_endTime;
    @Bind(R.id.tv_startTime)
    TextView tv_startTime;

    @Bind(R.id.seekBar_tg2)
    SeekBarPressure seekBar_tg2;
    public DisplayMetrics dm;

    int k = 0;
    private String TAG = getClass().getSimpleName();
    private int witchViewOnTouch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        cropVideoControlView.initView(9.2, 19.8, 100, 10, 40);
//        cropVideoControlView.initViewForSingle(98.2, 1000, 400);

        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (witchViewOnTouch == 0) {

                    cropVideoControlView.setLeftChange(-0.1);
                } else if (witchViewOnTouch == 1) {
                    cropVideoControlView.setRightChange(-0.1);

                }

            }
        });

        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (witchViewOnTouch == 0) {

                    cropVideoControlView.setLeftChange(0.1);
                } else if (witchViewOnTouch == 1) {
                    cropVideoControlView.setRightChange(0.1);

                }
            }
        });

        btn_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return false;
            }
        });

        cropVideoControlView.setProgressListener(new CropVideoControlView.ProgressListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(CropVideoControlView controlView, double startCurrent, double endCurrent, int witchOnTouch) {

                tv_startTime.setText(startCurrent + "秒");
                tv_endTime.setText(endCurrent + "秒");
                Log.e(TAG, "moveAction: 开始时间=" + startCurrent + "秒");
                Log.e(TAG, "moveAction: 结束时间=" + endCurrent + "秒");
                Log.e(TAG, "moveAction: witchOnTouch=" + witchOnTouch);
                witchViewOnTouch = witchOnTouch;
            }

            @Override
            public void onProgressAfter() {

            }
        });

    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }
}
