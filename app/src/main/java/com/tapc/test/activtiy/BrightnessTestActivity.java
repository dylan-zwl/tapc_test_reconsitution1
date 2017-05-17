package com.tapc.test.activtiy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tapc.test.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrightnessTestActivity extends Activity {
    @BindView(R.id.brightness_seekbar)
    SeekBar mBrightnessBar;

    private Handler mHandler;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);
        ButterKnife.bind(this);
        mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                setBrightness(BrightnessTestActivity.this, arg1);
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int brightness = mBrightnessBar.getProgress();
                int maxBrightness = mBrightnessBar.getMax();

                switch (index % 3) {
                    case 0:
                        brightness = maxBrightness;
                        break;
                    case 1:
                        brightness = maxBrightness / 2;
                        break;
                    case 2:
                        brightness = 0;
                        break;
                    default:
                        break;
                }

                if (index < 4) {
                    mHandler.sendEmptyMessageDelayed(0, 700);
                } else {
                    index = 0;
                    // testFinish(null);
                    return;
                }
                index++;
                mBrightnessBar.setProgress(brightness);
            }
        };
        index = 0;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        Log.d("lxy", "set  lp.screenBrightness == " + lp.screenBrightness);
        activity.getWindow().setAttributes(lp);
    }

    @OnClick(R.id.bright_test_finish)
    protected void testFinish(View v) {
        Intent intent = new Intent();
        this.setResult(1, intent);
        this.finish();
    }

    @OnClick(R.id.bright_test_again)
    protected void testAgain(View v) {
        index = 0;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }
}
