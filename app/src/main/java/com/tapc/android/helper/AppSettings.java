package com.tapc.android.helper;

import com.tapc.android.uart.UARTController;


public class AppSettings {
	public static final int HeartRateSamplePeriod = 1000;
	public static final int RPMSamplePeriod = 1000;

	// FOR LOOPBACK SETTINGS
	public static final int RandomHeartRateGenerationPeriod = 1000;
	public static final int RandomRPMGenerationPeriod = 1000;

	public static final long SamplePeriod = 1000;

	private static boolean _loopbackMode = false;

	public static void setLoopbackMode(boolean value) {
		_loopbackMode = value;
	}

	public static boolean getLoopbackMode() {
		return _loopbackMode;
	}

	public static void dumpException(Exception e) {
		// Log.d(e.print, msg)
	}

	public static UARTController getUARTController() {
		return UARTController.getInstance(AppSettings.getLoopbackMode());
	}

	public static String getStackTraceString(StackTraceElement[] ste) {
		StringBuilder builder = new StringBuilder();

		for (StackTraceElement s : ste) {
			builder.append(s.toString());
		}

		return builder.toString();
	}
}
