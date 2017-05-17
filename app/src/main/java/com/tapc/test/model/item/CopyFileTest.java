package com.tapc.test.model.item;

import com.tapc.android.uart.Commands;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.BaseTest;
import com.tapc.test.utils.SysUtils;

import java.io.File;

public class CopyFileTest extends BaseTest {
    private TestResult mTestResult;
    private TestItemType mType;
    private String mFilePath;

    public void init(TestItemType type) {
        mType = type;
        switch (type) {
            case UDISK:
                mFilePath = Config.USB_FILE_PATH;
                break;
            case TF:
                mFilePath = Config.SD_FILE_PATH;
                break;
        }
    }

    @Override
    public void start() {
        super.start();
        new Thread(mRunnable).start();
    }

    @Override
    public Commands getCommand() {
        return null;
    }

    @Override
    public TestItemType getTestItemType() {
        return mType;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if ((Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012)
                    && mFilePath.equals(Config.USB_FILE_PATH)) {
                boolean usb0Result = false;
                boolean usb1Result = false;
                usb0Result = SysUtils.testCopyFile(getActivity(), Config.TEST_FILE, mFilePath + "/usb0/");
                usb1Result = SysUtils.testCopyFile(getActivity(), Config.TEST_FILE, mFilePath + "/usb1/");
                if (usb0Result && usb1Result) {
                    mTestResult = TestResult.SUCCESS;
                } else {
                    mTestResult = TestResult.FAIL;
                }
            } else {
                if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                    File jhtFile = new File(mFilePath + "/jht_hide_software_installer.txt");
                    if (jhtFile != null && jhtFile.exists()) {
                        mTestResult = TestResult.SUCCESS;
                    } else {
                        mTestResult = TestResult.FAIL;
                    }
                } else {
                    if (SysUtils.testCopyFile(getActivity(), Config.TEST_FILE, mFilePath)) {
                        mTestResult = TestResult.SUCCESS;
                    } else {
                        mTestResult = TestResult.FAIL;
                    }
                }
            }
            finished(mTestResult);
        }
    };
}
