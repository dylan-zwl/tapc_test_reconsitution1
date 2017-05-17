package com.tapc.test.entity;

public enum TestItemType {
    FAN("风扇测试"),
    SAFEKEY("安全锁测试"),
    HEART("有线心跳测试"),
    WIRELESS_HEART("无线心跳测试"),
    CONTROL_COMMUNICATION("下控通讯测试"),
    MCU_COMMUNICATION("与MCU通讯测试"),
    TFT_COLOR("TFT屏色彩测试"),
    KEYBOARD("按键口测试"),
    EARPHONE("耳机测试"),
    SPEAKER("喇叭测试"),
    MP3_IN("MP3 测试"),
    AUDIO_IN("AUDIO IN 测试"),
    TV("TV测试"),
    TF("TF卡测试"),
    USB("USB 测试"),
    BLUETOOTH("蓝牙测试"),
    WIFI("WIFI 测试"),
    TOUCHSCREEN("触摸屏 测试"),
    ERP("待机测试"),
    UDISK("U盘测试"),
    LED("LED测试"),
    BAT("电池测试"),
    BRIGHT("背光测试"),
    RFID("RF测试");

    public String title;

    private TestItemType(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
