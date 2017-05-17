package com.tapc.test.model.base;

import android.app.Activity;
import android.content.Context;

import com.tapc.test.entity.TestResult;
import com.tapc.test.usb.UsbCtl;
import com.tapc.test.entity.TestItemType;

import java.util.Observer;

/**
 * Created by Administrator on 2017/5/4.
 */

public interface ITest extends Observer {
    TestItemType getTestItemType();

    void init(Activity activity, UsbCtl usbCtl, int index);

    void start();

    void stop();

    TestResult getTestResult();

    void setTestResult(TestResult testResult);

    void setTestCallback(ITestCallback testCallback);

    boolean isOrderTest();

    void setOrderTest(boolean needOrderTest);
}
