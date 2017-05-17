package com.tapc.android.controller;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class KeyboardController extends GenericMessageHandler {
	public final static String KEY_CODE = "KEY_CODE";

	public KeyboardController(Handler uihandler) {
		super(uihandler);
		mTransferPacket = new TransferPacket(Commands.GET_KEY_CODE);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_KEY_CODE
				|| cmd == Commands.GET_KEY_CODE_TEST;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		Bundle bndl = new Bundle();
		bndl.putInt(KeyboardController.KEY_CODE,
				Utility.getIntegerFromByteArray(packet.getData()) & 0x00FF);
		msg.setData(bndl);
	}
}
