package com.tapc.android.controller;

import com.tapc.android.helper.AppSettings;
import com.tapc.android.helper.RandomValueGenerator;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HeartController extends GenericMessageHandler {
	RandomValueGenerator mRandomHeartRateGenerator;
	public int recvCommandCount = 0;

	public HeartController(Handler uihandler) {
		super(uihandler);

		mRandomHeartRateGenerator = new RandomValueGenerator(60, 150, 2000);

		mTransferPacket = new TransferPacket(Commands.GET_HR_HAND);

		getPeriodicCommander().addCommandtoList(this.toString(),
				mTransferPacket);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_HR_HAND;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		Bundle bndl = new Bundle();
		if (AppSettings.getLoopbackMode()) {
			bndl.putInt("HEART_RATE", mRandomHeartRateGenerator.nextValue());
		} else {
			bndl.putInt("HEART_RATE",
					Utility.getIntegerFromByteArray(packet.getData()) & 0x00FF);
			recvCommandCount++;
		}
		msg.setData(bndl);
	}
}
