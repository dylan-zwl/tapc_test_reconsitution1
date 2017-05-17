package com.tapc.test.usb;

import com.tapc.android.uart.ReceivePacket;

public interface UsbRecvListerner {
	void update(ReceivePacket receivePacket);
}
