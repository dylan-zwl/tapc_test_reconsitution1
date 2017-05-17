package com.tapc.test.entity;

public class RecvTestResult {
	public final static byte ATS_IDLE = 0x00;
	public final static byte ATS_START = 0x01;
	public final static byte ATS_ING = 0x02;
	public final static byte ATS_RETRY = 0x03;
	public final static byte ATS_FAIL = 0x04;
	public final static byte ATS_SUCC = 0x05;
	public final static byte COMUNI_ERR = 0x06;
	public final static byte PARM_ERR = 0x07;
	public final static byte ATS_DISABLE = 0x08;
}