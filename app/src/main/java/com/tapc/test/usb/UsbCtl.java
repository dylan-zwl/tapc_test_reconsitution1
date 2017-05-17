package com.tapc.test.usb;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.CommunicationPacket;
import com.tapc.android.uart.ReceiveDataHandler;
import com.tapc.test.entity.TestUMcuCmd;

import java.util.Observer;

public class UsbCtl {
    private static D2xxManager ftD2xx = null;
    private Activity mActivity;
    private final String TT = "Trace";
    private FT_Device ftDev;
    private boolean uart_configured = false;
    private final byte XON = 0x11; /* Resume transmission */
    private final byte XOFF = 0x13; /* Pause transmission */
    private int tempDevCount;

    enum DeviceStatus {
        DEV_NOT_CONNECT, DEV_NOT_CONFIG, DEV_CONFIG
    }

    public UsbCtl(Activity activity, boolean isStatic) {
        mActivity = activity;
        try {
            ftD2xx = D2xxManager.getInstance(mActivity);
        } catch (D2xxException e) {
            e.printStackTrace();
        }
    }

    public boolean init(int devIndex) {
        tempDevCount = ftD2xx.createDeviceInfoList(mActivity);
        if (tempDevCount > 0 && tempDevCount >= (devIndex + 1)) {
            if (ftDev == null) {
                if (!isUsbConnect()) {
                    ftDev = ftD2xx.openByIndex(mActivity, devIndex);
                    setConfig(115200, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                    ReadThread readThread = new ReadThread();
                    readThread.start();
                    return true;
                }
            }
        } else {
            ftDev = null;
        }
        return false;
    }

    public boolean sendCommand(Commands commands, byte[] data) {
        CommunicationPacket com = new CommunicationPacket(commands);
        com.setData(data);
        if (DeviceStatus.DEV_CONFIG != checkDevice()) {
            return false;
        }
        byte[] sendDate = com.getPacketByteArray();
        sendData(sendDate.length, sendDate);
        return true;
    }

    public void close() {
        if (ftDev != null && ftDev.isOpen()) {
            ftDev.close();
        }
    }

    public void open() {
        if (ftDev != null) {
            if (!ftDev.isOpen()) {
                ftDev = ftD2xx.openByIndex(mActivity, 0);
                setConfig(115200, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                ReadThread readThread = new ReadThread();
                readThread.start();
            }
        }
    }

    public boolean isUsbConnect() {
        if (ftDev != null && ftDev.isOpen() == true) {
            return true;
        }
        return false;
    }

    private DeviceStatus checkDevice() {
        if (ftDev == null || false == ftDev.isOpen()) {
            return DeviceStatus.DEV_NOT_CONNECT;
        } else if (false == uart_configured) {
            return DeviceStatus.DEV_NOT_CONFIG;
        }

        return DeviceStatus.DEV_CONFIG;
    }

    void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, XON, XOFF);
        uart_configured = true;
    }

    void sendData(int numBytes, byte[] buffer) {
        if (ftDev.isOpen() == false) {
            DLog.e(TT, "SendData: device not open");
            Toast.makeText(mActivity, "Device not open!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numBytes > 0) {
            ftDev.write(buffer, numBytes);
        }
    }

    void sendData(byte buffer) {
        DLog.e(TT, "send buf:" + Integer.toHexString(buffer));
        byte tmpBuf[] = new byte[1];
        tmpBuf[0] = buffer;
        ftDev.write(tmpBuf, 1);
    }

    ReceiveDataHandler mReceiveDataHandler;

    class ReadThread extends Thread {
        ReadThread() {
            this.setPriority(MAX_PRIORITY);
            if (mReceiveDataHandler == null) {
                mReceiveDataHandler = new ReceiveDataHandler();
            }
        }

        public void run() {
            int readcount = 0;
            while (true && isUsbConnect()) {
                readcount = ftDev.getQueueStatus();
                if (readcount > 0) {
                    byte[] usbdata = new byte[readcount];
                    ftDev.read(usbdata, readcount);
                    for (int i = 0; i < readcount; i++) {
                        mReceiveDataHandler.handleReceivedByte(usbdata[i]);
                    }
                }
                SystemClock.sleep(20);
            }
        }
    }

    public void subscribeDataReceivedNotification(Observer o) {
        if (mReceiveDataHandler != null) {
            mReceiveDataHandler.subscribeDataReceivedNotification(o);
        }
    }

    public void sendStartTestCommand(Commands command, int data1, int data2) {
        byte[] data = new byte[3];
        data[0] = TestUMcuCmd.START;
        data[1] = (byte) data1;
        data[2] = (byte) data2;
        sendCommand(command, data);
    }

    public void sendClearTestCommand(Commands command) {
        byte[] data = new byte[3];
        data[0] = TestUMcuCmd.CLEAR_RES;
        sendCommand(command, data);
    }

    public void sendStopTestCommand(Commands command) {
        byte[] data = new byte[3];
        data[0] = TestUMcuCmd.STOP;
        sendCommand(command, data);
    }
}
