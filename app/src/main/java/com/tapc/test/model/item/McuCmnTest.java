package com.tapc.test.model.item;

import android.os.SystemClock;

import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;

import java.util.Observable;

public class McuCmnTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_WHT_AGING;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.MCU_COMMUNICATION;
    }

    @Override
    public void start() {
        super.start();
        new Thread(mRunnable).start();
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int recvCommandCount = 0;
            MachineController.getInstance().setRecvCommandCount(0);
            MachineController.getInstance().sendCommands(Commands.GET_HR_HAND, null);
            SystemClock.sleep(1000);
            recvCommandCount = MachineController.getInstance().getRecvCommandCount();
            if (recvCommandCount > 0) {
                finished(TestResult.SUCCESS);
            } else {
                finished(TestResult.FAIL);
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
                        break;
                    case RecvTestResult.ATS_FAIL:
                    case RecvTestResult.COMUNI_ERR:
                        getTestCallback().finish(getIndex(), TestResult.FAIL);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
