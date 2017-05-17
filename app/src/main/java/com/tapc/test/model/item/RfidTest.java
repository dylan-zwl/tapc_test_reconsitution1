package com.tapc.test.model.item;

import android.os.SystemClock;
import android.util.Log;

import com.tapc.android.controller.MachineController;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;

import java.util.Observable;

public class RfidTest extends BaseTest {
    private Commands mTestCommand = Commands.SET_REGISTER_RFID_AGING;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.RFID;
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
            TestResult testResult = TestResult.FAIL;
            starTestRfid();
            int status = -1;
            for (int i = 0; i < 20; i++) {
                status = getRfidStatus();
                Log.d("Rfid test result", "" + status);
                if (status == 3) {
                    testResult = TestResult.SUCCESS;
                    break;
                } else if (status == 2) {
                    testResult = TestResult.FAIL;
                    break;
                } else if (status == 4) {
                    testResult = TestResult.FAIL;
//                    getUsbCtl().sendProgressDialogMsg("RF 网络阻塞中\n需要重测!!");
                    SystemClock.sleep(3000);
                    break;
                }
                SystemClock.sleep(1000);
            }
            finished(testResult);
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

    private void starTestRfid() {
        MachineController.getInstance().sendRfidCommand();
    }

    private int getRfidStatus() {
        return MachineController.getInstance().getRfidStatus();
    }
}
