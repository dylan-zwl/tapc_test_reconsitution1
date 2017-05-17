package com.tapc.test.model.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.tapc.android.uart.Commands;
import com.tapc.test.activtiy.BrightnessTestActivity;
import com.tapc.test.activtiy.TVActivity;
import com.tapc.test.activtiy.TftTestActivity;
import com.tapc.test.activtiy.TouchTestActivity;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.utils.AlertDialogUtils;


public class ManualTest extends BaseTest {
    private Activity mActivity;
    private TestItemType mType;
    private static Handler sHandler;

    public void init(TestItemType type) {
        mType = type;
        if (sHandler == null) {
            sHandler = new Handler();
        }
    }

    @Override
    public TestItemType getTestItemType() {
        return mType;
    }

    @Override
    public void start() {
        super.start();
        if (mActivity == null) {
            mActivity = getActivity();
        }
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.getInstance().hideProgressDialog();
            }
        });
        Intent intent = new Intent();
        switch (mType) {
            case TFT_COLOR:
                intent.setClass(mActivity, TftTestActivity.class);
                break;
            case TOUCHSCREEN:
                intent.setClass(mActivity, TouchTestActivity.class);
                break;
            case BRIGHT:
                intent.setClass(mActivity, BrightnessTestActivity.class);
                break;
            case TV:
                intent.setClass(mActivity, TVActivity.class);
                break;
        }
        mActivity.startActivityForResult(intent, getIndex());
    }

    @Override
    public Commands getCommand() {
        return null;
    }
}
