package com.tapc.android.uart;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tapc.android.helper.AppSettings;

import android.util.Log;

public class PeriodicCommander implements Runnable {
	Lock _queueAccessLock = new ReentrantLock();

	private HashMap<String, ICommunicationPacket> _queryCommandList = new HashMap<String, ICommunicationPacket>();

	private boolean _running = false;

	UARTController _controller;

	Thread _currentThread;

	String _syncLock = "SYNC_LOCK";

	private boolean _pause;

	private static PeriodicCommander _self;

	private PeriodicCommander() {

	}

	public static PeriodicCommander getInstance() {
		if (_self == null)
			_self = new PeriodicCommander();

		return _self;
	}

	public void addCommandtoList(String key, ICommunicationPacket cmd) {
		if (key != null) {
			_queueAccessLock.lock();
			_queryCommandList.put(key, cmd);
			_queueAccessLock.unlock();
		}
	}

	public void removeCommandFromList(String key) {
		if (key == null)
			return;

		_queueAccessLock.lock();
		if (_queryCommandList.containsKey(key))
			_queryCommandList.remove(key);
		_queueAccessLock.lock();
	}

	@Override
	public void run() {
		_running = true;
		UARTController _controller = AppSettings.getUARTController();

		while (_running) {
			try {
				// if(this._pause)
				// pauseThread();
				if (!this._pause) {
					_queueAccessLock.lock();
					if (_queryCommandList.size() > 0) {
						for (String p : _queryCommandList.keySet())
							_controller.send(_queryCommandList.get(p));
						// Log.d(this.toString(), "Periodic Query Placed!");
					}
					_queueAccessLock.unlock();
				}

				Thread.sleep(AppSettings.SamplePeriod);
			} catch (InterruptedException e) {
				Log.d(this.toString(),
						"The general sampler thread interrupted and exited");
			}
		}
		Log.d(this.toString(), "Periodic Querying Thread stopped.");
	}

	public void start() {
		if (!_running) {
			_running = true;
			_currentThread = new Thread(this);
			_currentThread.setName("Periodic Query Sending Thread");
			_currentThread.start();
		}
	}

	public void restart() {
		while (_running) {
			stop();
		}

		start();
	}

	public void stop() {
		while (_running) {
			_running = false;
			// notifyThreadWakeup();
			_currentThread.interrupt();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		this._pause = true;
	}

	public void resume() {
		this._pause = false;
		// notifyThreadWakeup();
	}

	private void notifyThreadWakeup() {
		synchronized (this) {
			this.notify();
		}
	}

	private void pauseThread() throws InterruptedException {
		synchronized (this) {
			this.wait();
		}
	}
}
