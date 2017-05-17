package com.jht.sample;

public class NativeGpioJohnson {

	static {
		System.loadLibrary("EBWhite_Gpio_Johnson");
	}

	public native static int switchNone();

	public native static int switchBt();

	public native static int switchTV();

	public native static int switchEP9555E();

	public native static int powerOpen();

	public native static int powerClose();

	public native static int checkLineInStatusGpa13();

	public native static int checkLineInStatusGpa14();

	public native static int checkGpe14();
}
