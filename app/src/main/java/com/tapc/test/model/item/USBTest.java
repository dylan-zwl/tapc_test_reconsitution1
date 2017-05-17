package com.tapc.test.model.item;

import android.os.SystemClock;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.usb.UsbCtl;

import java.util.Observable;
import java.util.Observer;

public class USBTest extends BaseTest {
    private Commands mTestCommand = Commands.REGISTER_WHT_AGING;

    private int isTestSucess = 0;
    private int testCount = 6;

    @Override
    public TestItemType getTestItemType() {
        return TestItemType.USB;
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

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            isTestSucess = 0;
            for (int i = 0; i < testCount; i++) {
                getUsbCtl().sendStartTestCommand(mTestCommand, 0, 0);
                SystemClock.sleep(100);
            }
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                if (startTestUsb2() == false) {
                    finished(TestResult.FAIL);
                    return;
                }
            }
            if (isTestSucess >= (testCount - 2)) {
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
            isTestSucess++;
        }
    }

    /**
     * 测试usb2
     */
    private int isTestSucessUsb2 = 0;
    private UsbCtl mUsb2Ctl;

    private boolean startTestUsb2() {
        if (mUsb2Ctl == null) {
            mUsb2Ctl = new UsbCtl(getActivity(), false);
            for (int i = 0; i < 3; i++) {
                mUsb2Ctl.init(1);
                SystemClock.sleep(1000);
                if (mUsb2Ctl.isUsbConnect()) {
                    break;
                }
            }
        }
        isTestSucessUsb2 = 0;
        if (mUsb2Ctl.isUsbConnect()) {
            mUsb2Ctl.subscribeDataReceivedNotification(new Observer() {
                @Override
                public void update(Observable observable, Object o) {
                    ReceivePacket receivePacket = (ReceivePacket) o;
                    if (receivePacket == null) {
                        return;
                    }
                    if (receivePacket.getCommand() == mTestCommand) {
                        isTestSucessUsb2++;
                    }
                }
            });
            for (int i = 0; i < testCount; i++) {
                mUsb2Ctl.sendStartTestCommand(mTestCommand, 0, 0);
                SystemClock.sleep(100);
            }
            if (isTestSucessUsb2 >= (testCount - 2)) {
                return true;
            }
        }
        return false;
    }
}
