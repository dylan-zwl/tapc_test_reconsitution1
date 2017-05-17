/* * AUTHOR: 		Rupesh Acharya
 * Created Date: 	9/25/2013
 * 					A generic controller class that provides a framework for basic communication with 
 * 					uart/uartcontroller. Specific controller need to extend this class and provide 
 * 					the controller specific functions.
 */

package com.tapc.android.uart;

import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;

import com.tapc.android.helper.AppSettings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public abstract class GenericMessageHandler implements Runnable, Observer {
	protected TransferPacket mTransferPacket;

	// BLOCKING QUEUE TO QUEUE THE RECEIVED PACKETS
	private ArrayBlockingQueue<ReceivePacket> _queue;

	// THREAD THE KEEPS ON LOOKING FOR RECEIVE PACKETS FROM THE UARTCONTROLLER
	private Thread _thread;

	// FLAG WHETHER THE ABOVE THREAD IS RUNNING OR CAN SAY A CONTROL TO STOP
	// THREAD.
	protected boolean _running = false;

	// THE UI HANDLER THAT NEEDS TO BE UPDATED ABOUT THE RECEIVED PAKCET
	// INFORMATION
	protected Handler _uihandler;

	protected static UARTController _mainController = UARTController
			.getInstance(AppSettings.getLoopbackMode());

	private static PeriodicCommander _periodicTransmitter = null;

	// CONSTRUCTOR
	public GenericMessageHandler(Handler uihandler) {
		this._uihandler = uihandler;

		_periodicTransmitter = PeriodicCommander.getInstance();
		Log.d(this.toString(), "A " + this.toString() + " instance is created.");
	}

	// INITIALIZE THE QUEUE TO STORE THE RECEIVED PACKETS TO PROCESS AND
	// SUBSCRIBE TO DataReceivedNotification EVENT
	// AND START A THREAD TO PROCESS THE PACKETS STORED IN THE QUEUE
	public void start() {
		if (this._queue == null)
			this._queue = new ArrayBlockingQueue<ReceivePacket>(20, true);

		this._queue.clear();

		if (_mainController.getReceiveHandler() == null)
			_mainController.setReceiveDataHandler(new ReceiveDataHandler());

		_mainController.getReceiveHandler().subscribeDataReceivedNotification(
				this);

		_thread = new Thread(this);
		_thread.setName("Receive handler thread of Controller: "
				+ this.toString());
		_thread.start();
		this._running = true;

		_mainController.start();

		_periodicTransmitter.start();

		Log.d(this.toString(),
				"Received Packet Handling Thread Created and Started successfully.");
	}

	// STOP THE PACKET PROCESSING THREAD AND UNREGISTER THIS CONTROLLER
	// FROM THE PACKETRECEIVED NOTIFICATION, SO THAT IT IS NO MORE NOTIFIED
	public void stop() {
		Log.d(this.toString(), "Stopping the Controller....");
		this._running = false;

		_periodicTransmitter.removeCommandFromList(this.toString());

		// UNREGISTER FROM GETTING PACKET RECEIVED NOTIFICATION
		IReceiveDataHandler _rhandler = UARTController.getInstance(
				AppSettings.getLoopbackMode()).getReceiveHandler();

		if (_rhandler != null) {
			_rhandler.unsubscribeDataReceivedNotification(this);
			Log.d(this.toString(),
					"Unsubscribed from receiving Received Packet Notification");
		}

		// STOP THE THREAD, NEED TO BE CAREFUL HERE, SINCE THE THREAD IS IN
		// BLOCKING MODE
		// WAITING FOR THE PACKET TO QUEUE UP IN THE QUEUE SO JUST SETTING
		// _running TO
		// FALSE MAY NOT STOP THE THREAD.
		try {
			this._running = false;
			_queue.add(new ReceivePacket(Commands.NULL)); // ADDING A POISON TO
															// STOP THREAD
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception ee) {
			Log.d(this.toString(), "This was the cause of error !!!");
		}

		Log.d(this.toString(), "Controller Stopped.");
	}

	public void pause() {
		_periodicTransmitter.pause();
	}

	public void resume() {
		_periodicTransmitter.resume();
	}

	public PeriodicCommander getPeriodicCommander() {
		return _periodicTransmitter;
	}

	// THIS FUNCTION IS CALLED WHENEVER A PACKET IS RECEIVED FROM UARTCONTROLLER
	// THIS SHOULD JUST DO THE TASK OF ADDING THE RECEIVED PACKET TO THE QUEUE
	// IF
	// THIS CONTROLLER IS SUPPOSED TO HANDLE THAT PACKET. SINCE HANDLING THE
	// PACKET FROM THIS THREAD WILL DELAY THE UART CONTROLLER, SO THE PACKETS
	// ARE PROCESSED BY INDIVIDUAL PACKET PROCESSING THREAD
	@Override
	public void update(java.util.Observable observable, Object data) {
		// EXTRACT THE RECEIVED PACKET
		ReceivePacket receivedPacket = (ReceivePacket) data;

		// IF IT IS CORRECT PACKET, NEED TO BE HANDLED BY THIS CONTROLLER
		// THEN ADD THE PACKET TO THE QUEUE
		if (shouldHandleCommand(receivedPacket.getCommand())) {
			// CURRENTLY THE PACKET IS DROPPED IF THE QUEUE IS FULL
			try {
				// Log.d(this.toString(), "Queued a packet for processing.");
				_queue.put(receivedPacket);
			} catch (InterruptedException d) {
			}
		}
	};

	// THE THREAD CONTINOUSLY PROCESSING THE PACKET RECEIVED AND SENDING
	// MESSAGE TO THE UI THREAD
	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		while (this._running) {
			try {
				Message msg = this._uihandler.obtainMessage();

				// _queue.take() IS A BLOCKING METHOD SO, THE THREAD IS WAITING
				// UNTIL A PACKET IS ADDED TO THE QUEUE
				// WHEN THE QUEUE IS EMPTY, THE THREAD DOESN'T GET KILLED UNTIL
				// A PACKET IS ADDED TO THE QUEUE
				// IN ORDER TO KILL THIS THREAD FIRST IT NEEDS TO BE INVODED
				// FROM BLOCKING MODE, FOR THIS
				// WE NEED TO ADD A DUMMY PACKET IN THE QUEUE SO THAT IT INVOKES
				// FROM BLOCKING MODE AND THEN SHUTDOWNS
				// THIS IS CALLED Poison Pill Shutdown METHOD
				handlePacket(_queue.take(), msg);
				// Log.d(this.toString(),
				// "Dequeued a packet for processing...");

				this._uihandler.sendMessage(msg);

			} catch (InterruptedException e) {
				break;
			}
		}
	}

	protected void sendMessageToUI(String key, String value) {
		Message msg = this._uihandler.obtainMessage();

		Bundle bndl = new Bundle();

		bndl.putString(key, value);

		msg.setData(bndl);

		this._uihandler.sendMessage(msg);
	}

	// ABSTACT FUNCTION THAT DETERMINES WHETHER THIS CONTROLLER NEEDS TO HANDLE
	// THE PACKET OR NOT
	public abstract boolean shouldHandleCommand(Commands cmd);

	// ABSTRACT METHOD THAT DOES THE TASK OF HANDLING THE PACKET
	// WHICH IS
	public abstract void handlePacket(ReceivePacket packet, Message msg);

	public static void send(ICommunicationPacket packet) {
		_mainController.send(packet);
	}

	// public abstract ICommunicationPacket getQueryPacket(String packetType);

	// RETURN THE CLASS NAME
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}