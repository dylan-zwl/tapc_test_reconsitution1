package com.tapc.test.model.item;

import android.os.SystemClock;
import android.util.Log;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;

public class BoardVersion extends BaseTest {
    private Commands mTestCommand = Commands.GET_REGISTER_VERSION_DB;
    private int mBoard = 0;
    private int mVersion = 0;

    @Override
    public void start() {
        super.start();
        mBoard = 0;
        mVersion = 0;
        getUsbCtl().sendCommand(Commands.GET_REGISTER_VERSION_DB, null);
        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(10);
            if (mBoard != 0 && mVersion != 0) {
                return;
            }
        }
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
        if (receivePacket.getCommand() == Commands.GET_MCB_VERSION || receivePacket.getCommand() == mTestCommand) {
            byte[] resultData = receivePacket.getData();
            if (resultData != null && resultData.length == 2) {
                mVersion = resultData[0];
                mBoard = resultData[1];
                Log.d("board version", "board: " + mBoard + " version: " + mVersion);
            }
        }
    }

    public int getBoardValue() {
        return mBoard;
    }

    public int getVersionValue() {
        return mVersion;
    }
}
