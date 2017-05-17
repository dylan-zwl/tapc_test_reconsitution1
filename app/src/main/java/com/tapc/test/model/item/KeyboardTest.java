package com.tapc.test.model.item;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.tapc.android.controller.KeyboardController;
import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.KeyEventModel;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.utils.AlertDialogUtils;

import java.util.Observable;

public class KeyboardTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_I2C_AGING;

    private int mKeyValue;
    private Handler mHandler;
    private boolean isMCUTestKeyboard = true;
    private KeyboardReceiver mKeyboardReceiver;

    public void init() {
        mHandler = new Handler();
    }

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.KEYBOARD;
    }

    @Override
    public void start() {
        super.start();
        setAlertDialog(false);
        new Thread(mRunnable).start();
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                SystemClock.sleep(1000);
            }
            KeyEventModel.getInstance().getKeyboardEvent().setKeyHandler(mKeyboardHandler);
            if (!Config.IS_8935_PLATFORM || Config.BOARD_TYPE == BoardType.G029) {
                isMCUTestKeyboard = true;
                if (mKeyboardReceiver == null) {
                    mKeyboardReceiver = new KeyboardReceiver();
                    IntentFilter keyFilter = new IntentFilter(MachineController.MSG_KEY_BOARD);
                    getActivity().registerReceiver(mKeyboardReceiver, keyFilter);
                }
            } else {
                isMCUTestKeyboard = false;
            }

            getUsbCtl().sendStartTestCommand(mTestCommand, 0, 0);

            if (isMCUTestKeyboard) {
                MachineController.getInstance().sendCommands(Commands.GET_KEY_CODE_TEST, null);
                SystemClock.sleep(3000);
                if (getTestResult() == TestResult.NOT_TEST) {
                    finished(TestResult.FAIL);
                }
            } else {
                boolean result = false;
                mKeyValue = -1;
                for (int i = 0; i < 20; i++) {
                    SystemClock.sleep(100);
                    if (isMCUTestKeyboard) {
//                        getUsbCtl().sendProgressDialogMsg("按键值 ：" + mKeyValue);
                    }
                    if (mKeyValue != -1) {
                        result = true;
                        break;
                    }
                }
                if (result) {
                    finished(TestResult.SUCCESS);
                } else {
                    finished(TestResult.FAIL);
                }
            }
            setAlertDialog(true);
        }
    };

    private void setAlertDialog(final boolean visibility) {
        //G022,G012 Activity需显示最上层，否则无法接收到按键值。
        if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
            String title = AlertDialogUtils.getInstance().getTitle();
            if (visibility && !TextUtils.isEmpty(title) && title.contains("手动")) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialogUtils.getInstance().setProgressDialogVisibility(visibility);
                }
            });
        }
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    public class KeyboardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keycode = intent.getIntExtra(KeyboardController.KEY_CODE, 0);
            Log.e("keyboard", "keycode : " + keycode);
            if (keycode == 0) {
                finished(TestResult.FAIL);
            } else if (keycode == 0xff) {
                finished(TestResult.SUCCESS);
            }
        }
    }

    public void setKeyValue(int keyValue) {
        mKeyValue = keyValue;
    }

    @SuppressLint("HandlerLeak")
    private Handler mKeyboardHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int key = msg.what;
            mKeyValue = key;
            Log.d("keyboard value", "" + key);
            super.handleMessage(msg);
        }
    };
}
