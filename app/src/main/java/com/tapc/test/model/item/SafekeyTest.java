package com.tapc.test.model.item;

import android.os.SystemClock;

import com.tapc.android.controller.HardwareStatusController;
import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class SafekeyTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_SKEY_AGING;

    private int mNowSafekey = 0x00;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.SAFEKEY;
    }

    @Override
    public void start() {
        super.start();
        testSafekey(0x00);
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

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
                        SystemClock.sleep(500);
                        int isSafeOn = MachineController.getInstance().getSafeKeyStatus();
                        if (mNowSafekey == 0xff) {
                            if (isSafeOn == 0) {
                                finished(TestResult.SUCCESS);
                            } else {
                                finished(TestResult.FAIL);
                            }
                        } else {
                            if (isSafeOn == HardwareStatusController.SAFEKEY_MASK_VALUE) {
                                testSafekey(0xff);
                            } else {
                                finished(TestResult.FAIL);
                            }
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

    private void testSafekey(int cmd) {
        mNowSafekey = cmd;
        getUsbCtl().sendStartTestCommand(mTestCommand, cmd, 0);
    }
}
