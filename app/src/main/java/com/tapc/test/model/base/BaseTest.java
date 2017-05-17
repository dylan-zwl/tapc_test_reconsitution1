package com.tapc.test.model.base;

import android.app.Activity;

import com.tapc.android.uart.Commands;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.usb.UsbCtl;

import java.util.Observable;

import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/5/4.
 */

public abstract class BaseTest implements ITest {
    private Activity mActivity;
    private TestResult mTestResult = TestResult.NOT_TEST;
    private UsbCtl mUsbCtl;
    private int mIndex;
    private ITestCallback mTestCallback;
    private Disposable mDisposable;
    private boolean mNeedOrderTest = false;

    @Override
    public TestItemType getTestItemType() {
        return null;
    }

    @Override
    public void init(Activity activity, UsbCtl usbCtl, int index) {
        mActivity = activity;
        mUsbCtl = usbCtl;
        mUsbCtl.subscribeDataReceivedNotification(this);
        mIndex = index;
    }

    @Override
    public void start() {
        setTestResult(TestResult.NOT_TEST);
    }

    public abstract Commands getCommand();

    @Override
    public void stop() {
        if (mUsbCtl != null) {
            Commands commands = getCommand();
            if (commands != null) {
                mUsbCtl.sendStopTestCommand(commands);
            }
        }
    }

    @Override
    public TestResult getTestResult() {
        return mTestResult;
    }

    @Override
    public void setTestResult(TestResult testResult) {
        mTestResult = testResult;
    }

    @Override
    public void setTestCallback(ITestCallback testCallback) {
        mTestCallback = testCallback;
    }

    @Override
    public boolean isOrderTest() {
        return mNeedOrderTest;
    }

    @Override
    public void setOrderTest(boolean needOrderTest) {
        mNeedOrderTest = needOrderTest;
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    public Activity getActivity() {
        return mActivity;
    }

    public UsbCtl getUsbCtl() {
        return mUsbCtl;
    }

    public int getIndex() {
        return mIndex;
    }

    public ITestCallback getTestCallback() {
        return mTestCallback;
    }

    public Disposable getDisposable() {
        return mDisposable;
    }

    public void finished(TestResult testResult) {
        if (mTestCallback != null && mTestResult == TestResult.NOT_TEST) {
            mTestCallback.finish(mIndex, testResult);
            stop();
        }
    }

    public void setMsg(String title, String text) {
        if (mTestCallback != null) {
            mTestCallback.setMsg(title, text);
        }
    }
}
