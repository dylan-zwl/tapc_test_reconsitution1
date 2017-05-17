package com.tapc.android.uart;

public enum Commands implements ICommand {
	//MACHINE
	SET_CONTROL_TYPE(0x0024,1,0),
	GET_CONTROL_TYPE(0x0024,0,1),
	SET_MCHINE_START(0x005A,4,0),
	SET_MACHINE_STOP(0x005C,2,0),
	SET_MACHINE_PAUSE(0x005B,0,0),
	GET_MACHIE_ERROR(0x0007,0,2),
	GET_STATUS(0x0001,0,2),
	
	//KEYBOARD
	GET_KEY_CODE(0x0006,0,1),
	GET_KEY_CODE_TEST(0x2006,0,1),
	
	//SPEED
	SET_RPM_TARGET(0x0030,2,0),
	GET_RPM_CURRENT(0x0032,0,2),
	
	//INCLINE
	SET_ADC_TARGET(0x0040,2,0),
	GET_ADC_CURRENT(0x0042,0,2),
	SET_INCLNE_CAL(0x00D0,0,1),
	GET_INCLNE_CAL_FINISH(0x00D4,0,1),
	
	//HEART
	GET_HR_HAND(0x0060,0,2),
	
	//FOOT RATE
	GET_FOOT_RATE(0X00D2,0,2),
	
	//MCU UPDATE
	SET_UPDATE_DATA(0x0091, 128, 0),
	ENTER_UPDATE_MODE(0x0090, 0, 0),
	
	//BUZZER
	SET_BUZZER_CNTRL(0x0080,2,0),
	SET_BUZZER_SQNEC(0x0081,2,0),
	
	//FAN
	SET_FAN_CNTRL(0x0086,1,0),
	
	//VOLUME
	SET_EARPHONE_VOLUME(0x008A,1,0),
	
	//SYSTEM SUSPEND
	ENTER_ERP(0x008C, 2, 0),
	
	//VERSION
	GET_MCB_VERSION(0x0020,0,6),
	
	//SET PARM
	SET_MACHINE_PARM(0x8600,10,0),
		
	//TEST
	REGISTER_FAN_AGING(0x2000,3,0),
	REGISTER_SKEY_AGING(0x2001,3,0),
	REGISTER_WLHT_AGING(0x2002,3,0),
	REGISTER_WHT_AGING(0x2003,3,0),
	REGISTER_LCB_COM_AGING(0x2004,3,0),
	REGISTER_ERP_AGING(0x2005,3,0),     
	REGISTER_KEY_AGING(0x2006,3,0),                                             
	REGISTER_NO_IN_EP_AGING(0x2007,3,0),                                          
	REGISTER_AV_IN_EP_AGING(0x2008,3,0),                                          
	REGISTER_AUDIO_IN_EP_AGING(0x2009,3,0),                                       
	REGISTER_NO_IN_HR_AGING(0x2027,3,0),                                        
	REGISTER_AV_IN_HR_AGING(0x2028,3,0),                                         
	REGISTER_AUDIO_IN_HR_AGING(0x2029,3,0),                                       
	REGISTER_I2C_AGING(0x200a,3,0),  	
	REGISTER_CLEAN_ALL_AGING_DATA(0x2100,3,0), 

	REGISTER_BAT_AGING(0x200B,0,0),
	REGISTER_LED_AGING(0x200C,0,0),
	
	GET_REGISTER_VERSION_DB(0x0020,0,2),
	SET_REGISTER_VERSION_DB(0x0020,0,0),
	
	GET_REGISTER_ERP_AGING(0x2205,0,1), 
	
	SET_REGISTER_RFID_AGING(0x00CA,0,0),
	GET_REGISTER_RFID_AGING(0x00CB,0,2),
	
	NULL(0xFFFF,0,0);
	
	private int _commandID;
	private int _sendDataSize;
	private int _receiveDataSize;

	private Commands(int CommandID, int TxDataSize, int RxDataSize)
	{
		this._commandID = CommandID;
		this._sendDataSize = TxDataSize;
		this._receiveDataSize = RxDataSize;
	}
	
	public static Commands getCommandForID(int id)
	{
		for(Commands cmd : Commands.values())
			if(id == cmd.getCommandID()) 
				return cmd;
		
		return Commands.NULL;
	}

	@Override
	public int getCommandID() {
		return this._commandID;
	}

	@Override
	public int getSendPacketDataSize() {
		return this._sendDataSize;
	}

	@Override
	public int getReceivePacketDataSize() { 
		return this._receiveDataSize;
	}

	@Override
	public String getCommandString() { 
		return this.toString();
	}
}