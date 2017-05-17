package com.tapc.android.uart;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.tapc.android.uart.Commands;
import com.tapc.android.uart.GenericMessageHandler;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.android.uart.TransferPacket;
import com.tapc.android.uart.Utility;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IOUpdateController extends GenericMessageHandler {

	final public int MAX_RESEND_ATTEMPT = 3;
	TransferPacket _transferPacket;
	Thread _updateThread = null;

	private enum Update_Status {
		IN_PROGRESS, SUCCESS, FAILED, CANCELLED
	};

	IOUpdateHelper _updateHelper = new IOUpdateHelper();

	public IOUpdateController(Handler uihandler) {
		super(uihandler);
		_transferPacket = new TransferPacket(Commands.SET_UPDATE_DATA);
	}

	public int updateIO(String filepath) {
		if (_updateHelper.getLastUpdateStatus() == Update_Status.IN_PROGRESS) {
			throw new RuntimeException("Another update is already in progress.");
		}

		_updateThread = new Thread(_updateHelper.setBinaryPath(filepath));

		// start update
		_updateThread.start();

		return 0;
	}

	public int cancelUpdate() {
		if (_updateThread != null && _updateThread.isAlive()) {
			_updateThread.interrupt();
			try {
				_updateThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return 0;
	}

	@Override
	public boolean shouldHandleCommand(Commands cmd) {
		return cmd == Commands.SET_UPDATE_DATA
				|| cmd == Commands.ENTER_UPDATE_MODE;
	}

	@Override
	public void handlePacket(ReceivePacket packet, Message msg) {
		_updateHelper.ackReceivedCallBack(packet);
	}

	// helper class for doing the update
	private class IOUpdateHelper implements Runnable {

		Utility.ThreadSignaller _ackWait = new Utility.ThreadSignaller();

		public final int ACK_TIMEOUT = 1 * 1000;

		private boolean _ackReceived = false;

		private int _lastSentPacketNumber = 0;

		private int _ackPacketNumber = 0;

		int max_data_read_buffer_size = 128;

		byte[] data = new byte[max_data_read_buffer_size];

		private String _romBinary;

		private Update_Status _lastUpdateStatus;

		private boolean lcbInUpdateMode = false;

		private IOUpdateHelper() {
		}

		public Update_Status getLastUpdateStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		public IOUpdateHelper setBinaryPath(String filepath) {
			this._romBinary = filepath;
			return this;
		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void run() {
			Date start = Calendar.getInstance().getTime();
			_lastUpdateStatus = Update_Status.IN_PROGRESS;
			FileInputStream fis = null;
			_lastSentPacketNumber = 0;
			_ackPacketNumber = 0;
			_ackReceived = false;
			TransferPacket enterUpdateModeCommand = new TransferPacket(
					Commands.ENTER_UPDATE_MODE);
			enterUpdateModeCommand.setData(null);
			boolean updateComplete = false;
			lcbInUpdateMode = false;
			try {
				// get the file inputstream to read the file
				File f = new File(this._romBinary);
				fis = new FileInputStream(f);

				long totalFileSize = f.length();

				int attemptTimes = 10;

				while (!lcbInUpdateMode && attemptTimes-- > 0) {
					// send the command to enter update mode to LCB
					// wait until get back the ack, once the ack is received
					// send the data packets
					send(enterUpdateModeCommand);
					Thread.sleep(1000);
				}

				if (attemptTimes < 0) {
					sendMessageToUI("ERROR", "Couldnot enter Update Mode");
					_lastUpdateStatus = Update_Status.FAILED;
					return;
				}

				_lastUpdateStatus = Update_Status.IN_PROGRESS;

				int dataReadSize = 0;
				int readProcessed = 0;
				// until reach end of stream while reading the ROM
				// the first 1 byte of the data field is reserved for the
				// packet sequence number so, load the data after the offset 1
				while ((dataReadSize = fis.read(data, 1,
						max_data_read_buffer_size - 1)) != -1) {
					readProcessed += dataReadSize;
					// sendMessageToUI("PROGRESS",
					// ""+(readProcessed*100)/totalFileSize);
					// feed the packet sequence number in the data field
					_lastSentPacketNumber = (_lastSentPacketNumber + 1) % 256;
					byte[] seqno = Utility.getByteArrayFromInteger(
							_lastSentPacketNumber, 1);
					data[0] = seqno[0];

					// put the data in the transfer packet
					if (dataReadSize == max_data_read_buffer_size - 1)
						_transferPacket.setData(data);
					else {
						_transferPacket.setData(Arrays.copyOf(data,
								dataReadSize));
					}

					// send the packet
					if (sendPacket() > 0) {
						// error occurred or failed
						sendMessageToUI("ERROR", "Update Failed");
						return;
					}
				}

				// reached here means, all the data was sent successfully
				// so send the signal that data sent complete
				_transferPacket.setData(null);
				sendPacket();

				sendMessageToUI(
						"INFO",
						"Update Completed Successfully, Time taken: "
								+ (Math.abs(Calendar.getInstance()
										.getTimeInMillis() - start.getTime()) / 10000)
								+ 1 + " Second(s).");

				updateComplete = true;
			} catch (FileNotFoundException e) {
				sendMessageToUI("ERROR", "File not found: " + e.getMessage());
			} catch (IOException e) {
				sendMessageToUI("ERROR",
						"IOException occurred: " + e.getMessage());
			} catch (InterruptedException e) {
				sendMessageToUI("ERROR", "Update Failed, thread interrupted");
				_lastUpdateStatus = Update_Status.CANCELLED;
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				if (updateComplete)
					_lastUpdateStatus = Update_Status.SUCCESS;
				else
					_lastUpdateStatus = Update_Status.FAILED;
			}
		}

		public Integer sendPacket() throws InterruptedException {

			// every time while sending/resending the packet, increment the
			// resend_attempt count.
			// The resend_attempt count gets incremented on every timeout event
			int resend_attempt = -1;

			_ackReceived = false;

			// try sending and resending the packet until an acknowledgement is
			// received
			while (!_ackReceived) {

				if (resend_attempt++ >= MAX_RESEND_ATTEMPT)
					break;

				// send the prepared packet, In every timeout event, it resends
				// the packet again
				// if the ack from IO is lost and IO receives duplicate packet
				// then it ignores
				// the duplicate packet because while sending back the ack, it
				// sends back
				// the next expected packet sequence number. If that number
				// doesnot match the sequence number in the received packet then
				// the IO drops that packet and
				// sends back an ack with the next expected packet sequence
				// number
				send(_transferPacket);

				// log
				Log.d("IOUPDATE", "Send pckt number: " + _lastSentPacketNumber);
				if (resend_attempt > 0) {
					sendMessageToUI("ERROR", "ReSent packet no: "
							+ _lastSentPacketNumber + ", attempt: "
							+ resend_attempt);
				}

				// wait for ack to arrive, it blocks over here
				if (!_ackReceived)
					_ackWait.doWait(ACK_TIMEOUT);
			}

			return _ackReceived ? 0 : 1;
		}

		public void ackReceivedCallBack(ReceivePacket packet) {
			if (packet.getCommand() == Commands.SET_UPDATE_DATA) {

				_ackPacketNumber = Utility.getIntegerFromByteArray(packet
						.getData());

				// Log.d("IOUPDATE", "Ack pckt number: " + _ackPacketNumber
				// + " Last sent packet: " + _lastSentPacketNumber);

				if (((_lastSentPacketNumber + 1) % 256) == _ackPacketNumber) {
					_ackReceived = true;
					_ackWait.doNotify();
				}
			} else if (packet.getCommand() == Commands.ENTER_UPDATE_MODE) {
				lcbInUpdateMode = true;
				sendMessageToUI("INFO", "LCB entered update mode");
			}
		}
	}
}
