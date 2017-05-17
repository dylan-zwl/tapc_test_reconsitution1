package com.tapc.android.uart;

import java.util.Observer;

public interface IReceiveDataHandler
{
	public void handleReceivedByte(byte by_Data);
	
	public void subscribeDataReceivedNotification(Observer o);
	
	public void unsubscribeDataReceivedNotification(Observer o);

	public int getObserversCount();

	public void resetProcessingEngine();
}