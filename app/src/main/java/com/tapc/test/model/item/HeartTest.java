package com.tapc.test.model.item;

import android.os.SystemClock;

import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.entity.TestWay;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class HeartTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_WHT_AGING;
    private TestItemType mType;
    private int mHearRate = 0;

    public void init(TestItemType type) {
        mType = type;
        switch (type) {
            case HEART:
                mTestCommand = Commands.REGISTER_WHT_AGING;
                break;
            case WIRELESS_HEART:
                mTestCommand = Commands.REGISTER_WLHT_AGING;
                break;
        }
    }

    @Override
    public TestItemType getTestItemType() {
        return mType;
    }

    @Override
    public void start() {
        super.start();
        new Thread(mRunnable).start();
    }

    @Override
    public void stop() {
        super.stop();
        //有线测试结束立刻启动一下无线测试。
        if (mType == TestItemType.HEART) {
            selectOpenHeartTest(TestItemType.WIRELESS_HEART);
        } else {
            selectOpenHeartTest(TestItemType.HEART);
        }
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHearRate = 0;
            int min = 0;
            int max = 0;
            if (mTestCommand == Commands.REGISTER_WHT_AGING) {
                if (Config.IS_8935_PLATFORM) {
                    min = 50;
                    if (Config.BOARD_TYPE == BoardType.G029) {
                        max = 80;
                    } else {
                        max = 70;
                    }
                } else {
                    min = 70;
                    max = 120;
                }
            } else {
                if (Config.IS_8935_PLATFORM) {
                    min = 90;
                    max = 110;
                } else {
                    min = 70;
                    max = 120;
                }
            }
            testHearRate(min, max, TestWay.NONE);
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

    private void testHearRate(int min, int max, int way) {
        testHeartRateOpen(way);
        for (int i = 0; i < 40; i++) {
            SystemClock.sleep(500);
            mHearRate = MachineController.getInstance().getHeartRate();
            //            getUsbCtl().sendProgressDialogMsg("心跳值：" + mHearRate);
            if (mHearRate >= min && mHearRate <= max) {
                break;
            }
        }
        if (mHearRate >= min && mHearRate <= max) {
            finished(TestResult.SUCCESS);
        } else {
            finished(TestResult.FAIL);
        }
    }

    private void testHeartRateOpen(int data) {
        getUsbCtl().sendStartTestCommand(mTestCommand, data, 0);
    }

    private void testHeartRateOpen(Commands command) {
        getUsbCtl().sendStartTestCommand(command, TestWay.NONE, 0);
    }

    private void testHeartRateClose() {
        if (mTestCommand != null) {
            getUsbCtl().sendStopTestCommand(mTestCommand);
        }
    }

    public void selectOpenHeartTest(TestItemType type) {
        if (Config.IS_8935_PLATFORM) {
            switch (type) {
                case HEART:
                    testHeartRateOpen(Commands.REGISTER_WHT_AGING);
                    break;
                case WIRELESS_HEART:
                    testHeartRateOpen(Commands.REGISTER_WLHT_AGING);
                    break;
            }
        }
    }
}
