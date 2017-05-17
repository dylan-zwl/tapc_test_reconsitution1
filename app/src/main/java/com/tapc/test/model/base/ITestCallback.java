package com.tapc.test.model.base;

import com.tapc.test.entity.TestResult;

/**
 * Created by Administrator on 2017/5/4.
 */

public interface ITestCallback {
    void setMsg(String title, String text);

    void finish(int index, TestResult result);
}
