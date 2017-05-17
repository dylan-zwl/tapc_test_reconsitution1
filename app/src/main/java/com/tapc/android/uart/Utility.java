package com.tapc.android.uart;

public class Utility {
	public static class ThreadSignaller {
		private class MonitorObject {
		}

		MonitorObject myMonitorObject = new MonitorObject();

		public void doWait(long durationInMilliSec) throws InterruptedException {
			synchronized (myMonitorObject) {
				myMonitorObject.wait(durationInMilliSec);
			}
		}

		public void doNotify() {
			synchronized (myMonitorObject) {
				myMonitorObject.notify();
			}
		}
	}

	public static byte[] getByteArrayFromInteger(int value, int arrayLength) {
		byte[] result = new byte[arrayLength];

		int shift_var = 0;

		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (value >> shift_var);
			shift_var += 8;
		}

		return result;
	}

	public static int getIntegerFromByte(byte data) {
		return getIntegerFromByteArray(new byte[] { data });
	}

	public static int getIntegerFromByteArray(byte[] data) {
		int result = 0;
		int shift_var = 0;
		for (byte b : data) {
			result = result + (((b & 0xFF) << shift_var));
			shift_var += 8;
		}

		return result;
	}

	public static int getCheckSum(byte[] buffer, int length) {
		int crc = 0xFFFF;

		int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

		for (int j = 0; j < length; j++) {
			byte b = buffer[j];
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b >> (7 - i) & 1) == 1);
				boolean c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= polynomial;
			}
		}

		crc &= 0xffff;

		return crc;
	}
}
