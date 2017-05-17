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

public class FootRateController extends GenericMessageHandler {
	RandomValueGenerator mRandomHeartRateGenerator;

	public FootRateController(Handler uihandler) {
		super(uihandler);

		mRandomHeartRateGenerator = new RandomValueGenerator(60, 150, 2000);

		mTransferPacket = new TransferPacket(Commands.GET_FOOT_RATE);

		getPeriodicCommander().addCommandtoList(this.toString(),
				mTransferPacket);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_FOOT_RATE;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		Bundle bndl = new Bundle();
		if (AppSettings.getLoopbackMode()) {
			bndl.putInt("FOOT_RATE", mRandomHeartRateGenerator.nextValue());
		} else {
			bndl.putInt("FOOT_RATE",
					Utility.getIntegerFromByteArray(packet.getData()) & 0x00FF);
		}
		msg.setData(bndl);
	}
}
