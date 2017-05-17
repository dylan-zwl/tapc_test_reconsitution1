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

public class InclineController extends GenericMessageHandler {
	static private String INCLINECAL_FINISH = "inclinecalfinish";
	private int mTargetIncline = 0;

	private TransferPacket mSetInclinePacket;
	private TransferPacket mSetInclinecalPacket;
	private TransferPacket mGetInclineCalFinish;
	private TransferPacket mGetInclinePacket;

	public InclineController(Handler uihandler) {
		super(uihandler);

		mGetInclinePacket = new TransferPacket(Commands.GET_ADC_CURRENT);
		mSetInclinePacket = new TransferPacket(Commands.SET_ADC_TARGET);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_ADC_CURRENT
				|| cmd == Commands.GET_INCLNE_CAL_FINISH;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		if (packet.getCommand() == Commands.GET_ADC_CURRENT) {
			Bundle bndl = new Bundle();
			bndl.putInt("INCLINE_VALUE",
					Utility.getIntegerFromByteArray(packet.getData()));
			msg.setData(bndl);
		} else if (packet.getCommand() == Commands.GET_INCLNE_CAL_FINISH) {
			Bundle bndl = new Bundle();
			bndl.putInt("INCLNE_CAL_FINISH",
					Utility.getIntegerFromByteArray(packet.getData()));
			msg.setData(bndl);
		}
	}

	public void startInclinecal() {
		if (mSetInclinecalPacket == null) {
			mSetInclinecalPacket = new TransferPacket(Commands.SET_INCLNE_CAL);
			mGetInclineCalFinish = new TransferPacket(
					Commands.GET_INCLNE_CAL_FINISH);
		}
		getPeriodicCommander().addCommandtoList(INCLINECAL_FINISH,
				mGetInclineCalFinish);
		send(mSetInclinecalPacket);
	}

	public void stopInclinecal() {
		getPeriodicCommander().removeCommandFromList(INCLINECAL_FINISH);
	}

	public void setIncline(int incline) {
		mTargetIncline = incline;
		Log.d("set incline", "" + incline);
		mSetInclinePacket
				.setData(Utility.getByteArrayFromInteger(mTargetIncline,
						Commands.SET_ADC_TARGET.getSendPacketDataSize()));
		send(mSetInclinePacket);
	}

	public void getIncline() {
		send(mGetInclinePacket);
	}
}
