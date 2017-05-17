package com.tapc.android.controller;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ICommunicationPacket;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SpeedController extends GenericMessageHandler {
	private int _targetSpeed = 0;
	private ICommunicationPacket _setSpeedPacket;

	public SpeedController(Handler uihandler) {
		super(uihandler);

		mTransferPacket = new TransferPacket(Commands.GET_RPM_CURRENT);
		_setSpeedPacket = new TransferPacket(Commands.SET_RPM_TARGET);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_RPM_CURRENT;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		if (packet.getCommand() == Commands.GET_RPM_CURRENT) {
			Bundle bndl = new Bundle();
			bndl.putInt("SPEED_VALUE",
					Utility.getIntegerFromByteArray(packet.getData()));
			msg.setData(bndl);
		}
	}

	public void getSpeed() {
		send(mTransferPacket);
	}

	public void setSpeed(int speed) {
		_targetSpeed = speed;
		Log.d("set incline", "" + speed);
		_setSpeedPacket.setData(Utility.getByteArrayFromInteger(_targetSpeed,
				Commands.SET_RPM_TARGET.getSendPacketDataSize()));
		send(_setSpeedPacket);
	}
}
