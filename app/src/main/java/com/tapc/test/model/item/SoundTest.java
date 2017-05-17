package com.tapc.test.model.item;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;

import com.jht.sample.NativeGpioJohnson;
import com.jht.tapc.platform.NativeGpio;
import com.tapc.android.uart.Commands;
import com.tapc.android.uart.ReceivePacket;
import com.tapc.test.R;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.RecvTestResult;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.entity.TestUMcuCmd;
import com.tapc.test.entity.TestVolumeStatus;
import com.tapc.test.entity.TestWay;
import com.tapc.test.model.VoiceInput;
import com.tapc.test.model.base.BaseTest;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

public class SoundTest extends BaseTest {
    private Commands mTestCommand;
    private static int TEST_VOLUME_L = 0;
    private static int TEST_VOLUME_H = 1;
    private AudioManager mAudiomanage;
    private int mMaxVolume;
    private boolean isTestFinish = false;
    private boolean isNeedMusic = false;
    private boolean isSpeakerOutput = false;
    private int mMinVoltage = 100;
    private int mMaxVoltage = 1400;
    private int mSetVolumeL = 0;
    private int mSetVolumeH = 0;
    private VoiceInput mVoiceInput;
    private Timer mTimer;
    private int oldGpa13 = 0;
    private int oldGpa14 = 0;
    private boolean isGpa13Line = false;
    private boolean isGpa14Line = false;
    private TestResult mTestResult;
    private byte[] mtestResultData;
    private static MediaPlayer sPlayer;
    private TestItemType mType;

    @Override
    public TestItemType getTestItemType() {
        return mType;
    }

    @Override
    public void start() {
        super.start();
        new Thread(mRunnable).start();
    }

    @Override
    public Commands getCommand() {
        return mTestCommand;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAudiomanage == null) {
                mAudiomanage = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            }
            init(mType);
            getUsbCtl().sendClearTestCommand(mTestCommand);
            if (Config.IS_8935_PLATFORM) {
                startVoiceInput();
            }
            if (isNeedMusic) {
                startMediaPlayer();
            }

            SystemClock.sleep(2000);
            if (isSpeakerOutput) {
                setVolume(mMaxVolume, TEST_VOLUME_L, TestWay.RIGHT);
            } else {
                setVolume(mMaxVolume, TEST_VOLUME_L, TestWay.LEFT_RIGHT);
            }
        }
    };

    @Override
    public void finished(TestResult testResult) {
        if (mTimer != null) {
            if (testResult == TestResult.SUCCESS) {
                if (isGpa13Line && isGpa14Line) {
                    testResult = TestResult.SUCCESS;
                } else {
                    testResult = TestResult.FAIL;
                }
            }
            mTimer.cancel();
            mTimer = null;
        }
        mTestResult = testResult;
        if (mAudiomanage != null) {
            mAudiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_PLAY_SOUND);
        }
        if (Config.IS_8935_PLATFORM && mVoiceInput != null) {
            mVoiceInput.stop();
        }
        if (isNeedMusic) {
            stopMediaPlayer();
        }
        super.finished(testResult);
    }

    @Override
    public void update(Observable observable, Object o) {
        ReceivePacket receivePacket = (ReceivePacket) o;
        if (receivePacket == null) {
            return;
        }
        if (receivePacket.getCommand() == mTestCommand) {
            mtestResultData = receivePacket.getData();
            if (mtestResultData != null && mtestResultData.length > 0) {
                if (isSpeakerOutput) {
                    switch (mtestResultData[0]) {
                        case RecvTestResult.ATS_SUCC:
                            if (mtestResultData[2] == TestVolumeStatus.SAG_TRG_FIN) {
                                setVolume(mMaxVolume, TEST_VOLUME_H, TestWay.LEFT);
                            } else if (mtestResultData[2] == TestVolumeStatus.SAG_FAIL) {
                                showVolumeData();
                            } else if (mtestResultData[2] == TestVolumeStatus.SAG_SUCC) {
                                showVolumeData();
                            } else {
                                if (mtestResultData[1] == TestVolumeStatus.SAG_TRG_FIN) {
                                    setVolume(mMaxVolume, TEST_VOLUME_H, TestWay.RIGHT);
                                } else if (mtestResultData[1] == TestVolumeStatus.SAG_FAIL) {
                                    setVolume(mMaxVolume, TEST_VOLUME_L, TestWay.LEFT);
                                } else if (mtestResultData[1] == TestVolumeStatus.SAG_SUCC) {
                                    setVolume(mMaxVolume, TEST_VOLUME_L, TestWay.LEFT);
                                }
                            }
                            break;
                        case RecvTestResult.ATS_FAIL:
                        case RecvTestResult.COMUNI_ERR:
                            finished(TestResult.FAIL);
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (mtestResultData[0]) {
                        case RecvTestResult.ATS_SUCC:
                            if (mtestResultData[1] == TestVolumeStatus.SAG_TRG_FIN
                                    && mtestResultData[2] == TestVolumeStatus.SAG_TRG_FIN) {
                                setVolume(mMaxVolume, TEST_VOLUME_H, TestWay.LEFT_RIGHT);
                            } else if (mtestResultData[1] == TestVolumeStatus.SAG_FAIL
                                    || mtestResultData[2] == TestVolumeStatus.SAG_FAIL) {
                                showVolumeData();
                            } else if (mtestResultData[1] == TestVolumeStatus.SAG_SUCC
                                    && mtestResultData[2] == TestVolumeStatus.SAG_SUCC) {
                                showVolumeData();
                            }
                            break;
                        case RecvTestResult.ATS_FAIL:
                        case RecvTestResult.COMUNI_ERR:
                            finished(TestResult.FAIL);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void startCheckGpio() {
        oldGpa13 = 0;
        oldGpa14 = 0;
        isGpa13Line = false;
        isGpa14Line = false;
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                int Gpa13 = NativeGpioJohnson.checkLineInStatusGpa14() & 0xff;
                int Gpa14 = NativeGpioJohnson.checkLineInStatusGpa13() & 0xff;
                if (oldGpa13 >= 0) {
                    if (oldGpa13 == 0) {
                        oldGpa13 = Gpa13;
                    } else if (oldGpa13 != Gpa13) {
                        isGpa13Line = true;
                    }
                }
                if (oldGpa14 >= 0) {
                    if (oldGpa14 == 0) {
                        oldGpa14 = Gpa14;
                    } else if (oldGpa14 != Gpa14) {
                        isGpa14Line = true;
                    }
                }
                // Log.d("Gpio", "A13: " + Gpa13 + " A14: " + Gpa14);
            }
        };
        mTimer.schedule(task, 100, 100);
    }


    private void setVolume(int maxVolume, int level, int flag) {
        int volume = 0;
        switch (level) {
            case 0:
                volume = mSetVolumeL;
                break;
            case 1:
                volume = mSetVolumeH;
                break;
        }
        mAudiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, AudioManager.FLAG_PLAY_SOUND);
        sendStartTestCommand(level, flag);
        Log.e("audio", "maxVolume : " + maxVolume + " volume : " + volume + " level :" + level);
    }


    private boolean showVolumeData() {
        boolean VolumeResult1 = false;
        boolean VolumeResult2 = false;
        boolean result = false;
        String msgStr = "电压值信息\n";

        int leftVolume1L = (int) ((mtestResultData[9] & 0xFF) | ((mtestResultData[10] & 0xFF) << 8));
        int rightVolume1L = (int) ((mtestResultData[5] & 0xFF) | ((mtestResultData[6] & 0xFF) << 8));

        int leftVolume1H = (int) ((mtestResultData[11] & 0xFF) | ((mtestResultData[12] & 0xFF) << 8));
        int rightVolume1H = (int) ((mtestResultData[7] & 0xFF) | ((mtestResultData[8] & 0xFF) << 8));

        msgStr = msgStr + "通道1左声道：（" + leftVolume1L + "~" + leftVolume1H + "）mv\n";
        msgStr = msgStr + "通道1右声道：（" + rightVolume1L + "~" + rightVolume1H + "）mv\n";

        if (leftVolume1L < mMinVoltage && rightVolume1L < mMinVoltage && leftVolume1H > mMaxVoltage
                && rightVolume1H > mMaxVoltage) {
            VolumeResult1 = true;
        }

        if (isSpeakerOutput) {
            int leftVolume2L = (int) ((mtestResultData[17] & 0xFF) | ((mtestResultData[18] & 0xFF) << 8));
            int rightVolume2L = (int) ((mtestResultData[13] & 0xFF) | ((mtestResultData[14] & 0xFF) << 8));

            int leftVolume2H = (int) ((mtestResultData[19] & 0xFF) | ((mtestResultData[20] & 0xFF) << 8));
            int rightVolume2H = (int) ((mtestResultData[15] & 0xFF) | ((mtestResultData[16] & 0xFF) << 8));

            msgStr = msgStr + "通道2左声道：（" + leftVolume2L + "~" + leftVolume2H + "）mv\n";
            msgStr = msgStr + "通道2右声道：（" + rightVolume2L + "~" + rightVolume2H + "）mv\n";
            if (leftVolume2L < mMinVoltage && rightVolume2L < mMinVoltage && leftVolume2H > mMaxVoltage
                    && rightVolume2H > mMaxVoltage) {
                VolumeResult2 = true;
            }
        }
        if (isSpeakerOutput) {
            result = VolumeResult1 & VolumeResult2;
        } else {
            result = VolumeResult1;
        }
        if (result) {
//             SystemClock.sleep(3000);
            finished(TestResult.SUCCESS);
        } else {
//            SystemClock.sleep(3000);
            finished(TestResult.FAIL);
        }
        return result;
    }

    public void initTest(TestItemType type) {
        mType = type;
    }

    private void init(TestItemType type) {
        mType = type;
        switch (type) {
            case AUDIO_IN:
                mTestCommand = Commands.REGISTER_AUDIO_IN_HR_AGING;
                isNeedMusic = false;
                isSpeakerOutput = true;
                if (Config.IS_8935_PLATFORM) {
                    if (Config.BOARD_TYPE != BoardType.G029) {
                        if (NativeGpio.switchTV() == 1) {
                            Log.d("switchTV", "success");
                        } else {
                            Log.d("switchTV", "failed");
                            finished(TestResult.FAIL);
                            return;
                        }
                    }
                    mSetVolumeL = 0;
                    mMinVoltage = 100;
                    mMaxVoltage = 1400;
                    if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                        mSetVolumeH = 5;
                        NativeGpioJohnson.powerOpen();
                        if (NativeGpioJohnson.switchTV() == 1) {
                            Log.d("switchTV", "success");
                        } else {
                            Log.d("switchTV", "failed");
                            finished(TestResult.FAIL);
                            return;
                        }
                        startCheckGpio();
                    } else {
                        mSetVolumeH = 7;
                    }
                } else {
                    mSetVolumeL = 0;
                    mSetVolumeH = 100;
                    mMinVoltage = 100;
                    mMaxVoltage = 1500;
                }
                break;
            case MP3_IN:
                mTestCommand = Commands.REGISTER_AV_IN_HR_AGING;
                isNeedMusic = false;
                isSpeakerOutput = true;
                if (Config.IS_8935_PLATFORM) {
                    if (Config.BOARD_TYPE == BoardType.G029) {
                        int mp3Gpe14 = (NativeGpioJohnson.checkGpe14() & 0xff);
                        Log.d("GPE 14", "" + mp3Gpe14);
                        if (mp3Gpe14 == 0) {
                            finished(TestResult.FAIL);
                            return;
                        }
                    } else {
                        if (NativeGpio.switchMP3() == 1) {
                            Log.d("switchMP3", "success");
                        } else {
                            Log.d("switchMP3", "failed");
                            finished(TestResult.FAIL);
                            return;
                        }
                    }
                    mSetVolumeL = 0;
                    mMinVoltage = 100;
                    mMaxVoltage = 1400;
                    // 奥玛15.6寸声音偏小
                    if (Config.BOARD_TYPE == BoardType.G029 && Config.MCU_VERSION.startsWith("8.6.7.50.1")) {
                        mMaxVoltage = 800;
                    }
                    if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                        NativeGpioJohnson.powerOpen();
                        if (NativeGpioJohnson.switchBt() == 1) {
                            Log.d("switchBt", "success");
                        } else {
                            Log.d("switchBt", "failed");
                            finished(TestResult.FAIL);
                            return;
                        }
                        mSetVolumeH = 5;
                    } else {
                        if (Config.BOARD_TYPE == BoardType.G029) {
                            mSetVolumeH = 5;
                        } else {
                            mSetVolumeH = 6;
                        }
                    }
                } else {
                    mSetVolumeL = 0;
                    mSetVolumeH = 100;
                    mMinVoltage = 100;
                    mMaxVoltage = 1500;
                }
                break;
            case SPEAKER:
                mTestCommand = Commands.REGISTER_NO_IN_HR_AGING;
                isNeedMusic = true;
                isSpeakerOutput = true;
                if (Config.IS_8935_PLATFORM) {
                    mSetVolumeL = 0;
                    mMinVoltage = 100;
                    mMaxVoltage = 1400;
                    // 奥玛15.6寸声音偏小
                    if (Config.BOARD_TYPE == BoardType.G029 && Config.MCU_VERSION.startsWith("8.6.7.50.1")) {
                        mMaxVoltage = 500;
                    }
                    if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                        mSetVolumeH = 6;
                    } else {
                        mSetVolumeH = 5;
                    }
                } else {
                    mSetVolumeL = 0;
                    mSetVolumeH = 100;
                    mMinVoltage = 100;
                    mMaxVoltage = 1500;
                }
                break;
            case EARPHONE:
                mTestCommand = Commands.REGISTER_NO_IN_EP_AGING;
                isNeedMusic = true;
                isSpeakerOutput = false;
                if (Config.IS_8935_PLATFORM) {
                    mSetVolumeL = 0;
                    mSetVolumeH = 7;
                    mMinVoltage = 100;
                    mMaxVoltage = 1000;
                } else {
                    mSetVolumeL = 0;
                    mSetVolumeH = 100;
                    mMinVoltage = 100;
                    mMaxVoltage = 1100;
                }
                break;
            default:
                break;
        }
    }

    private void sendStartTestCommand(int cmd, int flag) {
        byte volumeR = 0x10;
        byte volumeL = 0x20;
        byte[] data = new byte[3];
        data[0] = TestUMcuCmd.START;
        if (flag == 0) {
            data[1] = (byte) (volumeR | volumeL | cmd);
        } else if (flag == 1) {
            data[1] = (byte) (volumeR | cmd);
        } else if (flag == 2) {
            data[1] = (byte) (volumeL | cmd);
        }
        data[2] = (byte) (cmd * 25);
        getUsbCtl().sendCommand(mTestCommand, data);
    }


    private void startVoiceInput() {
        new Runnable() {
            public void run() {
                if (mVoiceInput == null) {
                    mVoiceInput = VoiceInput.getInstance();
                }
                mVoiceInput.start();
            }
        }.run();
    }


    private void stopMediaPlayer() {
        if (sPlayer != null && sPlayer.isPlaying()) {
            sPlayer.pause();
        }
    }

    private void startMediaPlayer() {
        if (sPlayer == null) {
            sPlayer = MediaPlayer.create(getActivity(), R.raw.lr);
            sPlayer.setLooping(true);
            sPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer players) {
                }
            });
            sPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer players, int arg1, int arg2) {
                    sPlayer.release();
                    sPlayer = null;
                    return false;
                }
            });
        }
        if (!sPlayer.isPlaying()) {
            sPlayer.start();
        }
    }
}
