package com.tapc.android.uart;

public class CommunicationPacket implements ICommunicationPacket {
	protected CommandReadWriteMode _readWriteMode;
	protected Commands _command;
	protected int _headerLength = 2 + 1 + 2 + 2 + 2; // PACKETHEAD+RW+CMD+DTLENGTH+CRC

	private byte[] _emptyData = new byte[0];

	protected byte[] _dataBuffer;
	protected byte[] _packetByteArray;
	protected int _checkSum = -1;

	public CommunicationPacket(Commands Command) {
		this._command = Command;

		if (Command.toString().startsWith("GET"))
			_readWriteMode = CommandReadWriteMode.READ_MODE;
		else
			_readWriteMode = CommandReadWriteMode.WRITE_MODE;
	}

	public int getHeaderLength() {
		return _headerLength;
	}

	@Override
	public CommandReadWriteMode getReadWriteMode() {
		return _readWriteMode;
	}

	@Override
	public Commands getCommand() {
		return this._command;
	}

	@Override
	public void setData(byte[] data) {
		this._dataBuffer = data;
		this._packetByteArray = null;
	}

	@Override
	public byte[] getData() {
		if (_dataBuffer == null)
			return _emptyData;

		return _dataBuffer;
	}

	@Override
	public byte[] getPacketByteArray() {
		int currentindex = 0;
		if (_packetByteArray == null) {
			if (this._dataBuffer == null)
				this._dataBuffer = new byte[0];

			int length = this._headerLength + this._dataBuffer.length;

			_packetByteArray = new byte[length];
			// ADD PACKET HEADER 0xaa55
			_packetByteArray[currentindex++] = (byte) 0x55;
			_packetByteArray[currentindex++] = (byte) 0xaa;

			// ADD RW
			_packetByteArray[currentindex++] = (byte) ((_readWriteMode == CommandReadWriteMode.READ_MODE) ? 2
					: 1);

			// ADD COMMAND
			byte[] tmp = Utility.getByteArrayFromInteger(
					_command.getCommandID(), 2); // ByteBuffer.allocate(2).putInt(_command.getCommandID()).array();
			_packetByteArray[currentindex++] = tmp[0];
			_packetByteArray[currentindex++] = tmp[1];

			// ADD DATA LENGTH
			tmp = Utility.getByteArrayFromInteger(this._dataBuffer.length, 2); // /*_command.getSendPacketDataSize()*/,
																				// 2);
																				// //
																				// ByteBuffer.allocate(2).putInt(_command.getSendPacketDataSize()).array();
			_packetByteArray[currentindex++] = tmp[0];
			_packetByteArray[currentindex++] = tmp[1];

			// ADD DATA
			for (byte val : _dataBuffer) {
				_packetByteArray[currentindex++] = val;
			}

			// ADD CRC
			this._checkSum = Utility
					.getCheckSum(_packetByteArray, currentindex);
			tmp = Utility.getByteArrayFromInteger(this._checkSum, 2); // ByteBuffer.allocate(2).putInt(getCheckSum()).array();
			_packetByteArray[currentindex++] = tmp[0];
			_packetByteArray[currentindex++] = tmp[1];
		}

		return _packetByteArray;
	}
}
