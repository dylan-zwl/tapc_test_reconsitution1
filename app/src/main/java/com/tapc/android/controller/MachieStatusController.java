package com.tapc.android.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

public class MachieStatusController extends GenericMessageHandler {
	public final static int STATUS_BIT_INVERTER_ERR_MASK_VALUE = 0xEE0000;
	private TransferPacket mMachineError;
	private TransferPacket mStartCommand;
	private TransferPacket mStopCommand;
	private TransferPacket mPauseCommand;
	private TransferPacket mFancontrol;
	private TransferPacket mEnterERP;
	private TransferPacket mGetMachineVersion;
	private TransferPacket mSetCtl;
	private TransferPacket mSetRfCommand;
	private TransferPacket mGetRfCommand;
	private int mFanSpeedLeverl;
	private String mMachineVersion;
	private byte[] mMachineVersionBytes;
	private int mRfStatus;

	public MachieStatusController(Handler uihandler) {
		super(uihandler);

		mStartCommand = new TransferPacket(Commands.SET_MCHINE_START);
		mStopCommand = new TransferPacket(Commands.SET_MACHINE_STOP);
		mPauseCommand = new TransferPacket(Commands.SET_MACHINE_PAUSE);
		mMachineError = new TransferPacket(Commands.GET_MACHIE_ERROR);
		mEnterERP = new TransferPacket(Commands.ENTER_ERP);
		mFancontrol = new TransferPacket(Commands.SET_FAN_CNTRL);
		mSetRfCommand = new TransferPacket(Commands.SET_REGISTER_RFID_AGING);
		mGetRfCommand = new TransferPacket(Commands.GET_REGISTER_RFID_AGING);
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.GET_MACHIE_ERROR || cmd == Commands.ENTER_ERP || cmd == Commands.GET_MCB_VERSION
				|| cmd == Commands.GET_REGISTER_RFID_AGING;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		if (packet.getCommand() == Commands.GET_MACHIE_ERROR) {
			int hardwareStatus = Utility.getIntegerFromByteArray(packet.getData()) & 0xFFFF
					| MachieStatusController.STATUS_BIT_INVERTER_ERR_MASK_VALUE;
			Bundle bndl = new Bundle();
			bndl.putInt(HardwareStatusController.KEY_WORKOUT_STATUS, hardwareStatus);
			msg.setData(bndl);
		} else if (packet.getCommand() == Commands.ENTER_ERP) {
			Bundle b = new Bundle();
			b.putString("ERP", "Enter ERP started");
			msg.setData(b);
		} else if (packet.getCommand() == Commands.GET_MCB_VERSION) {
			byte[] data = packet.getData();
			if (data != null && data.length > 0) {
				setMachineVersionBytes(data);
				String version = "";
				for (int i = 0; i < data.length; i++) {
					if (i == (data.length - 1)) {
						version = version + (data[i] & 0xff);
					} else {
						version = version + (data[i] & 0xff) + ".";
					}
				}
				mMachineVersion = version;
			}
		} else if (packet.getCommand() == Commands.GET_REGISTER_RFID_AGING) {
			mRfStatus = Utility.getIntegerFromByteArray(packet.getData()) & 0xFFFF;
		}
	}

	public void startMachine(int speed, int incline) {
		byte[] speedDatabyte = Utility.getByteArrayFromInteger(speed, 2);
		byte[] inclineDatabyte = Utility.getByteArrayFromInteger(incline, 2);
		byte[] databyte = new byte[4];
		System.arraycopy(speedDatabyte, 0, databyte, 0, 2);
		System.arraycopy(inclineDatabyte, 0, databyte, 2, 2);
		mStartCommand.setData(databyte);
		send(mStartCommand);
	}

	public void stopMachine(int incline) {
		mStopCommand
				.setData(Utility.getByteArrayFromInteger(incline, Commands.SET_MACHINE_STOP.getSendPacketDataSize()));
		send(mStopCommand);
	}

	public void pauseMachine() {
		send(mPauseCommand);
	}

	public void enterERP(int time) {
		mEnterERP.setData(Utility.getByteArrayFromInteger(time, Commands.ENTER_ERP.getSendPacketDataSize()));
		send(mEnterERP);
	}

	public void sendRfidCmd() {
		mRfStatus = -1;
		send(mSetRfCommand);
	}

	public int getRfidStatus() {
		return mRfStatus;
	}

	public void setFanSpeedLevel(int spdlvl) {
		mFanSpeedLeverl = spdlvl;
		mFancontrol.setData(Utility.getByteArrayFromInteger(mFanSpeedLeverl,
				Commands.SET_FAN_CNTRL.getSendPacketDataSize()));
		send(mFancontrol);
	}

	public int getFanSpeedLevel() {
		return mFanSpeedLeverl;
	}

	public void sendMachineErrorCmd() {
		send(mMachineError);
	}

	public void sendCtlVersionCmd() {
		if (mGetMachineVersion == null) {
			mGetMachineVersion = new TransferPacket(Commands.GET_MCB_VERSION);
		}
		mMachineVersion = "";
		send(mGetMachineVersion);
	}

	public String getCtlVersionValue() {
		return mMachineVersion;
	}

	public void sendCommands(Commands commands, byte[] data) {
		TransferPacket command = new TransferPacket(commands);
		if (data != null) {
			command.setData(data);
		}
		send(command);
	}

	public byte[] getMachineVersionBytes() {
		return mMachineVersionBytes;
	}

	public void setMachineVersionBytes(byte[] machineVersionBytes) {
		this.mMachineVersionBytes = machineVersionBytes;
	}
}
