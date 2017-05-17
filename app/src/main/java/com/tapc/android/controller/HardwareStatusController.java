package com.tapc.android.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

public class HardwareStatusController extends GenericMessageHandler {
    public final static String KEY_WORKOUT_STATUS = "HARDWARE_STATUS";
    public final static String KEY_WORKOUT_SAFEKEY = "SAFEKEY_STATUS";
    public final static int ERROR_MASK_VALUE = 0x0001;
    public final static int SAFEKEY_MASK_VALUE = 0x0008;
    public final static int KEY_MASK_VALUE = 0x0002;
    public final static int WDT_OVERFLOW_MASK_VALUE = 0x0004;
    public final static int STATUS_BIT_INVERTER_ERR_MASK_VALUE = 0x0010;
    public final static int STATUS_BIT_ERR_MASK_VALUE = 0xff00;

    private int mLastStatus = -1;

    public HardwareStatusController(Handler uihandler) {
        super(uihandler);

        mTransferPacket = new TransferPacket(Commands.GET_STATUS);

        getPeriodicCommander().addCommandtoList(this.toString(), mTransferPacket);
    }

    public int getSendPacketSize() {
        return Commands.GET_STATUS.getSendPacketDataSize() + mTransferPacket.getHeaderLength();
    }

    public int getReceivePacketSize() {
        return Commands.GET_STATUS.getReceivePacketDataSize();
    }

    @Override
    public boolean shouldHandleCommand(Commands cmd) {
        return cmd == Commands.GET_STATUS;
    }

    @Override
    public void handlePacket(ReceivePacket packet, Message msg) {
        Bundle bndl = new Bundle();
        int hardwareStatus = Utility.getIntegerFromByteArray(packet.getData());
        if (hardwareStatus == 0x110) {
            bndl.putInt(KEY_WORKOUT_STATUS, hardwareStatus);
            msg.setData(bndl);
            return;
        }
        hardwareStatus = hardwareStatus & SAFEKEY_MASK_VALUE;
        if (mLastStatus != hardwareStatus) {
            mLastStatus = hardwareStatus;
            bndl.putInt(KEY_WORKOUT_STATUS, hardwareStatus);
            msg.setData(bndl);
        }
    }

    public int getSafeKeyStatus() {
        return mLastStatus;
    }
}
