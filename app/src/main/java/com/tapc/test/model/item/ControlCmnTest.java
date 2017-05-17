package com.tapc.test.model.item;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import com.tapc.android.controller.HardwareStatusController;
import com.tapc.android.controller.MachieStatusController;
import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class ControlCmnTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_LCB_COM_AGING;

    private boolean hasErrCode;
    private boolean isUTest;
    private byte mCtlType;
    private StatusReceiver mStatusReceiver;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.CONTROL_COMMUNICATION;
    }

    @Override
    public void start() {
        super.start();
        new Thread(mRunnable).start();
    }

    @Override
    public void stop() {
        super.stop();
        if (mStatusReceiver != null) {
            mStatusReceiver = null;
        }
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
//            根据下控的版本号测试不同的下控，默认0x07
            byte[] datas = MachineController.getInstance().getMachineVersionBytes();
            if (datas != null && datas.length == 1 && datas[0] == 31) {
                mCtlType = 0x07;
            }
            if (datas != null && datas.length >= 3) {
                mCtlType = datas[2];
            }
            if (mCtlType == 0) {
                mCtlType = 0x07;
            }

//            旧版测试架mcu软件G022 G012发0x30
//            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
//                mCtlType = 0x30;
//            }

            MachineController.getInstance().stopMachine(0);
            MachineController.getInstance().startMachine(100, 100);

            hasErrCode = false;
            testControlCmn();
            int testCount = 0;
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                testCount = 80;
            } else {
                testCount = 40;
            }
            while (true) {
                testCount = testCount - 1;
                if (hasErrCode && isUTest) {
                    finished(TestResult.SUCCESS);
                    break;
                } else {
                    if (testCount <= 0) {
                        finished(TestResult.FAIL);
                        break;
                    }
                }
                SystemClock.sleep(200);
            }
        }
    };

    @Override
    public void update(Observable observable, Object o) {
        ReceivePacket receivePacket = (ReceivePacket) o;
        if (receivePacket == null) {
            return;
        }
        if (receivePacket.getCommand() == mTestCommand) {
            byte[] resultData = receivePacket.getData();
            if (resultData != null && resultData.length > 0) {
                switch (resultData[0]) {
                    case RecvTestResult.ATS_SUCC:
                        isUTest = true;
                        break;
                    case RecvTestResult.ATS_FAIL:
                    case RecvTestResult.COMUNI_ERR:
                        isUTest = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void testControlCmn() {
        getUsbCtl().sendStartTestCommand(mTestCommand, mCtlType, 0);
    }

    public void unregisterReceiver() {
        getActivity().unregisterReceiver(mStatusReceiver);
    }

    public void registerReceiver(Activity activity) {
        mStatusReceiver = new StatusReceiver();
        IntentFilter statusFilter = new IntentFilter(MachineController.MSG_WORKOUT_STATUS);
        activity.registerReceiver(mStatusReceiver, statusFilter);
    }


    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(HardwareStatusController.KEY_WORKOUT_STATUS, 0);
            Log.e("StatusReceiver", "onReceive " + String.format("%x", status));
            if ((status & MachieStatusController.STATUS_BIT_INVERTER_ERR_MASK_VALUE) == MachieStatusController
                    .STATUS_BIT_INVERTER_ERR_MASK_VALUE || ((status & 0x10) != 0)) {
                if ((status & 0x3fff) == 0) {
                    return;
                }
                hasErrCode = true;
                Log.e("StatusReceiver", "isHasErrCode true");
            }
        }
    }
}
