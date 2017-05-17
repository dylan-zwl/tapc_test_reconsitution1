package com.tapc.test.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.tapc.android.controller.MachineController;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.utils.IntentUtil;
import com.tapc.test.utils.PreferenceHelper;

import java.io.File;

public class McuBslModel {
    private Activity mAcitvity;
    private MachineController mController;
    private ProgressDialog mProgressDialog;
    private int mNowProgress;
    private String mProgressTitle = "MCU 升级进度 : ";
    private boolean isTestFinish = false;
    private ShowVersionListener mListener;

    public interface ShowVersionListener {
        void version();
    }

    private class ShowType {
        public static final int SHOW = 0;
        public static final int HIDE = 1;
        public static final int NOFILE = 2;
    }

    public McuBslModel(Activity activity, ShowVersionListener listener) {
        mAcitvity = activity;
        mController = MachineController.getInstance();
        mProgressDialog = new ProgressDialog(mAcitvity);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.hide();
        mListener = listener;
    }

    public void startUpdate() {
        new Thread(mRunnable).start();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            isTestFinish = false;
            mProgressHandler.sendEmptyMessage(ShowType.SHOW);
            File file = new File(Config.SD_FILE_PATH + "ROM.bin");
            if (file.exists()) {
                mController.updateMCU(file.getAbsolutePath(), IOUpdateMsg);
                while (true) {
                    if (isTestFinish == true) {
                        SystemClock.sleep(2000);
                        break;
                    } else {
                        SystemClock.sleep(300);
                    }
                }
                mProgressHandler.sendEmptyMessage(ShowType.HIDE);
            } else {
                mProgressHandler.sendEmptyMessage(ShowType.NOFILE);
            }
        }
    };

    Handler IOUpdateMsg = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.getData().containsKey("ERROR")) {
                String errorStr = (String) msg.getData().get("ERROR");
                Log.d("update MCU error", "error info: " + errorStr);
                mProgressDialog.setTitle(mProgressTitle + errorStr);
                if (errorStr.contains("Update Failed") || errorStr.contains("Couldnot enter Update Mode")) {
                    mProgressDialog.setTitle("MCU 烧录失败,将影响下面测试结果");
                    isTestFinish = true;
                }
            } else if (msg.getData().containsKey("INFO")) {
                String infoStr = (String) msg.getData().get("INFO");
                Log.d("update MCU info", "info: " + infoStr);
                mProgressDialog.setTitle(mProgressTitle + infoStr);
                if (infoStr.contains("Update Completed Successfully")) {
                    mProgressDialog.setTitle("MCU 烧录成功可以继续下面测试");
                    isTestFinish = true;
                    PreferenceHelper.write(mAcitvity, Config.SETTING_CONFIG, "mcu_upload", true);
                    if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                IntentUtil.sendBroadcast(mAcitvity, "reload", null);
                            }
                        }, 2000);
                    }
                }
            } else if (msg.getData().containsKey("PROGRESS")) {
                String progressStr = (String) msg.getData().get("PROGRESS");
                if (progressStr != null) {
                    mNowProgress = Integer.valueOf(progressStr).intValue();
                    if (mNowProgress >= 0 && mNowProgress <= 100) {
                        mProgressDialog.setProgress(mNowProgress);
                    }
                }
            }
        }
    };

    Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ShowType.SHOW:
                    mNowProgress = 0;
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.setTitle(mProgressTitle);
                    mProgressDialog.show();
                    break;
                case ShowType.HIDE:
                    mProgressDialog.hide();
                    mListener.version();
                    break;
                case ShowType.NOFILE:
                    mProgressDialog.setTitle("MCU 烧录失败,找不到烧录文件,请查看！");
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                    break;
            }
        }
    };
}
