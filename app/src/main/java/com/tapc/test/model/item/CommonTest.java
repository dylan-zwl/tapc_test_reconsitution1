package com.tapc.test.model.item;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class CommonTest extends BaseTest {
    private Commands mTestCommand;
    private TestItemType mType;

    public void init(TestItemType type) {
        mType = type;
        switch (type) {
            case LED:
                mTestCommand = Commands.REGISTER_LED_AGING;
                break;
            case BAT:
                mTestCommand = Commands.REGISTER_BAT_AGING;
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
        getUsbCtl().sendStartTestCommand(mTestCommand, 0, 0);
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
            byte[] mtestResultData = receivePacket.getData();
            if (mtestResultData != null && mtestResultData.length > 0) {
                switch (mtestResultData[0]) {
                    case RecvTestResult.ATS_SUCC:
                        finished(TestResult.SUCCESS);
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
}
