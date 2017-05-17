package com.tapc.android.uart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;

public class UARTController {
	private static UARTController _instance;
	private static UARTController _loopbackInstance;

	private FileInputStream _fileInputStream;
	private FileOutputStream _fileOutputStream;
	private IReceiveDataHandler _dataHandler;
	// 8935
	// private String DEVICE_NAME = "/dev/ttyTCC3";
	// rk3188
	private String DEVICE_NAME = "/dev/ttyS3";
	private boolean _loopbackMode = false;

	private ArrayBlockingQueue<Byte> _loopbackData = new ArrayBlockingQueue<Byte>(
			255, true);

	byte[] dataArray = new byte[1];

	private boolean _running = false;

	public void setReceiveDataHandler(IReceiveDataHandler rph) {
		this._dataHandler = rph;
		Log.d(this.toString(), "Receive Handler Set Successfully.");
	}

	public IReceiveDataHandler getReceiveHandler() {
		return this._dataHandler;
	}

	private UARTController() {
		if (!isDeviceExit(DEVICE_NAME)) {
			DEVICE_NAME = "/dev/ttyTCC3";
		}
	}

	private boolean isDeviceExit(String devicePath) {
		if (new File(devicePath).exists()) {
			return true;
		}
		return false;
	}

	public int start() {
		if (!_running) {
			Log.d(this.toString(), "UARTController Starting....");
			Log.d(this.toString(), "Loopback mode: " + _loopbackMode);
			try {
				if (!_loopbackMode) {
					_fileInputStream = new FileInputStream(DEVICE_NAME);
					_fileOutputStream = new FileOutputStream(DEVICE_NAME);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					while (_running) {
						if (_loopbackMode) {
							try {
								Byte byt = _loopbackData.take();
								if (byt != null)
									_dataHandler.handleReceivedByte(byt);
								else
									Log.d("UART_LOOPBACK",
											"The item came to be null in the received queue of uartcontroller");
							} catch (InterruptedException e) {
								Log.d("UART_LOOPBACK",
										"ERROR OCCURRED WHILE HANDLING DATA");
							} catch (Exception t) {

							}
							continue;
						}

						try {
							if (_dataHandler == null) {
								// Log.d("UART_LOOPBACK",
								// "Error is handled, no need to worry, but the case is, packet would have been received before packet processor being assigned. Don't worry, issue handled.:\\)");
								continue;
							}

							if (_fileInputStream.read(dataArray, 0, 1) != 0) {
								_dataHandler.handleReceivedByte(dataArray[0]);
							}
						} catch (IOException e) {
						} catch (Exception t) {
							_dataHandler.resetProcessingEngine();
						}
					}
				}
			});
			t.setName("UART Controller Byte Receiver thread");
			_running = true;

			t.start();
			Log.d(this.toString(),
					"Receive Packet Handler Thread Started Successfully");
			Log.d(this.toString(), "UARTController started successfully");
		} else {
			Log.d(this.toString(),
					"UARTController already started and running...");
		}

		return 1;
	}

	public static UARTController getInstance() {
		return getInstance(false);
	}

	public static UARTController getInstance(boolean loopbackInstance) {
		if (loopbackInstance) {
			if (_loopbackInstance == null) {
				_loopbackInstance = new UARTController();
				_loopbackInstance._loopbackMode = true;
				if (!_loopbackInstance._running)
					_loopbackInstance.start();
			}

			return _loopbackInstance;
		}

		if (_instance == null)
			_instance = new UARTController();

		if (!_instance._running)
			_instance.start();

		return _instance;
	}

	public int send(ICommunicationPacket p) {
		// Log.d(this.toString(), "UART Controller received a packet to send");
		if (_loopbackMode) {
			for (byte byt : p.getPacketByteArray()) {
				try {
					_loopbackData.put(new Byte(byt));
				} catch (InterruptedException e) {
					Log.d(this.toString(),
							"Interrupted exception while receiving a packet");
				}
			}
			return 0;
		}

		try {
			_fileOutputStream.write(p.getPacketByteArray());
		} catch (IOException e) {
		}
		return 0;
	}

	public boolean isRunning() {
		return _running;
	}

	public int stop(boolean forced) {

		if (!forced) {
			if (_dataHandler.getObserversCount() > 0)
				return 0;
		}

		_running = false;
		Log.d(this.toString(), "UARTController stopped");
		return 1;
	}
}
