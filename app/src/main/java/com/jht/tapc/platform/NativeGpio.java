package com.jht.tapc.platform;

public class NativeGpio {
	static {
		System.loadLibrary("EBWhite_Gpio_App");
	}

	public native static int switchTV();

	public native static int switchMP3();
}
