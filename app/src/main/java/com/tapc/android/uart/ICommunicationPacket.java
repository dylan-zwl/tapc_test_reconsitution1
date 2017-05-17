package com.tapc.android.uart;

public interface ICommunicationPacket
{
	public CommandReadWriteMode getReadWriteMode();
	public Commands getCommand();
	
	public void setData(byte[] data);
	public byte[] getData();
	
	public byte[] getPacketByteArray();
}