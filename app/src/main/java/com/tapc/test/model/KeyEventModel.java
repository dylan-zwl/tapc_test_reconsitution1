package com.tapc.test.model;

import com.jht.tapc.jni.KeyEvent;

/**
 * Created by Administrator on 2017/5/8.
 */

public class KeyEventModel {
    private static KeyEventModel sKeyEventModel;
    private KeyEvent mKeyboardEvent;

    public static KeyEventModel getInstance() {
        if (sKeyEventModel == null) {
            sKeyEventModel = new KeyEventModel();
        }
        return sKeyEventModel;
    }

    public void init() {
        mKeyboardEvent = new KeyEvent(null, 200);
        mKeyboardEvent.start();
        mKeyboardEvent.openUinput();
        mKeyboardEvent.initCom();
    }

    public KeyEvent getKeyboardEvent() {
        return mKeyboardEvent;
    }
}
