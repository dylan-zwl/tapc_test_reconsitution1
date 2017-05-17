package com.tapc.test.model.item;

import android.os.SystemClock;

import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class FanTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_FAN_AGING;

    private int mNowSpeedLevel = 0;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.FAN;
    }

    @Override
    public void start() {
        super.start();
//        new Thread(mRunnable).start();
        testFanSpeed(0);
        SystemClock.sleep(1000);
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            testFanSpeed(0);
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
                        if (mNowSpeedLevel == 3) {
                            finished(TestResult.SUCCESS);
                        } else {
                            testFanSpeed(3);
                        }
                        break;
                    case RecvTestResult.ATS_FAIL:
                    case RecvTestResult.COMUNI_ERR:
                        finished(TestResult.FAIL);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void testFanSpeed(int speedLevel) {
        mNowSpeedLevel = speedLevel;
        MachineController.getInstance().setFanLevel(speedLevel);
        getUsbCtl().sendStartTestCommand(mTestCommand, speedLevel, 0);
    }
}
