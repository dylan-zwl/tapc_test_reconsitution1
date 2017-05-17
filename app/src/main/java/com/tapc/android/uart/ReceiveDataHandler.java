package com.tapc.android.uart;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

enum ReceivePosition {
    PACKET_HEAD,
    RW,
    COMMAND,
    DATALENGTH,
    DATA,
    CRC;
}


public class ReceiveDataHandler extends Observable implements IReceiveDataHandler {
    ReceivePosition R_State = ReceivePosition.PACKET_HEAD;
    int MAX_BUFFER_LENGTH = 255;

    byte Uart_RxBUff[] = new byte[MAX_BUFFER_LENGTH];

    int by_RxPoint = 0;
    boolean StartRx = false;
    boolean FirstByteReceived = false;
    int DataLength = 0;
    int ReceivedCommand = 0;
    int DataStartPosition = 0;
    byte[] Empty_Data = new byte[]{};

    public ReceiveDataHandler() {
    }

    @Override
    public void handleReceivedByte(byte by_Data) {
        switch (R_State) {
            case PACKET_HEAD:
                by_RxPoint = 0;
                StartRx = false;
                if (FirstByteReceived) {
                    if ((by_Data & 0xFF) == 0xaa) {
                        Uart_RxBUff[by_RxPoint++] = (byte) 0x55;
                        Uart_RxBUff[by_RxPoint++] = (byte) 0xaa;

                        StartRx = true;
                        R_State = ReceivePosition.RW;
                    }
                } else if ((by_Data & 0xFF) == 0x55) {
                    FirstByteReceived = true;
                    break;
                }
                FirstByteReceived = false;
                break;
            case RW:
                if (by_Data == 0x01 || by_Data == 0x02) {
                    Uart_RxBUff[by_RxPoint++] = by_Data;
                    StartRx = true;
                    R_State = ReceivePosition.COMMAND;
                } else {
                    R_State = ReceivePosition.PACKET_HEAD;
                    Log.d("PACKET_LOST", "The received packet Read/Write field has wrong value:" + by_Data);
                }
                break;
            case COMMAND:
                Uart_RxBUff[by_RxPoint++] = by_Data;
                if (FirstByteReceived) {
                    byte[] arr = {Uart_RxBUff[by_RxPoint - 2], Uart_RxBUff[by_RxPoint - 1]};
                    ReceivedCommand = Utility.getIntegerFromByteArray(arr); // ByteBuffer.wrap(arr).getInt();
                    R_State = ReceivePosition.DATALENGTH;    //
                }
                FirstByteReceived = !FirstByteReceived;
                break;
            case DATALENGTH:
                Uart_RxBUff[by_RxPoint++] = by_Data;
                if (FirstByteReceived) {
                    DataStartPosition = by_RxPoint;
                    byte[] arr = {Uart_RxBUff[by_RxPoint - 2], Uart_RxBUff[by_RxPoint - 1]};
                    DataLength = Utility.getIntegerFromByteArray(arr); //ByteBuffer.wrap(arr).getInt();

                    //ADD DATALENGTH CHECK HERE, IF EXCEEDS BUFFER THEN LOOK FOR NEXT PACKET
                    if (DataLength > MAX_BUFFER_LENGTH) {
                        R_State = ReceivePosition.PACKET_HEAD;
                        FirstByteReceived = false;
                        Log.d("PACKET_LOST", "The received packet data length exceeded the max data length size.\n");
                        break;
                    }

                    R_State = (DataLength == 0) ? ReceivePosition.CRC : ReceivePosition.DATA;
                }
                FirstByteReceived = !FirstByteReceived;
                break;
            case DATA:
                Uart_RxBUff[by_RxPoint++] = by_Data;
                if (--DataLength == 0)  //
                {
                    R_State = ReceivePosition.CRC;
                    DataLength = by_RxPoint - DataStartPosition;
                }
                break;
            case CRC:
                Uart_RxBUff[by_RxPoint++] = by_Data;
                if (FirstByteReceived) {
                    R_State = ReceivePosition.PACKET_HEAD;
                    StartRx = false;
                    ProcessReceivedPacket();
                    by_RxPoint = 0;
                }
                FirstByteReceived = !FirstByteReceived;
                break;
            default:
                R_State = ReceivePosition.PACKET_HEAD;
                break;
        }
    }

    public void resetProcessingEngine() {
        R_State = ReceivePosition.PACKET_HEAD;
    }

    @SuppressLint("NewApi")
    private void ProcessReceivedPacket() {
        //DO CRC CHECK
        byte[] arr = {Uart_RxBUff[by_RxPoint - 2], Uart_RxBUff[by_RxPoint - 1]};

        if (Utility.getIntegerFromByteArray(arr) == Utility.getCheckSum(Uart_RxBUff, by_RxPoint)) {
            //CRC ERROR
            Log.d("PACKET_LOST", "The received packet CRC mismatch.\n");
            return;
        }

        Commands cmd = Commands.getCommandForID(ReceivedCommand);

        if (cmd == Commands.NULL) {
            Log.d(this.toString(), "ERROR PACKET DUMPING START\n");
            //ERROR MESSAGE RECEIVED, SHOW UNKNOWN COMMAND
            for (byte b : Uart_RxBUff)
                Log.d(this.toString(), "ERROR PACKET BYTE DUMPING: " + b + "\n");

            Log.d(this.toString(), "ERROR PACKET DUMPING FINISH\n");

            return;
        }

        ReceivePacket received_packet = new ReceivePacket(cmd);
        if (DataLength == 0)
            received_packet.setData(Empty_Data);
        else
            received_packet.setData(
                    Arrays.copyOfRange(Uart_RxBUff,
                            DataStartPosition,
                            DataStartPosition + DataLength
                    )
            );
        this.setChanged();
        this.notifyObservers(received_packet);
    }


    @Override
    public void subscribeDataReceivedNotification(Observer o) {
        this.addObserver(o);
    }


    @Override
    public void unsubscribeDataReceivedNotification(Observer o) {
        this.deleteObserver(o);
    }

    @Override
    public int getObserversCount() {
        return this.countObservers();
    }
}
