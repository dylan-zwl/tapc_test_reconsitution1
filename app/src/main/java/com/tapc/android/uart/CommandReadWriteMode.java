package com.tapc.android.uart;

public enum CommandReadWriteMode
{
	READ_MODE(0x02),
	WRITE_MODE(0x01);
	
	private int _value;
	
	private CommandReadWriteMode(int Value)
	{
		this._value = Value;
	}
}
