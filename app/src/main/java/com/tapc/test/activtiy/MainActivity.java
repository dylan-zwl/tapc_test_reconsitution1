package com.tapc.test.activtiy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.tapc.android.controller.MachineController;
import com.tapc.test.R;
import com.tapc.test.adpater.TestGridViewAdapter;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.KeyEventModel;
import com.tapc.test.model.McuBslModel;
import com.tapc.test.model.base.ITest;
import com.tapc.test.model.base.ITestCallback;
import com.tapc.test.model.item.BoardVersion;
import com.tapc.test.model.item.CommonTest;
import com.tapc.test.model.item.ControlCmnTest;
import com.tapc.test.model.item.CopyFileTest;
import com.tapc.test.model.item.ErpTest;
import com.tapc.test.model.item.FanTest;
import com.tapc.test.model.item.HeartTest;
import com.tapc.test.model.item.KeyboardTest;
import com.tapc.test.model.item.ManualTest;
import com.tapc.test.model.item.McuCmnTest;
import com.tapc.test.model.item.RfidTest;
import com.tapc.test.model.item.SafekeyTest;
import com.tapc.test.model.item.SoundTest;
import com.tapc.test.model.item.USBTest;
import com.tapc.test.service.MenuService;
import com.tapc.test.usb.UsbCtl;
import com.tapc.test.utils.AlertDialogUtils;
import com.tapc.test.utils.IntentUtil;
import com.tapc.test.utils.PreferenceHelper;
import com.tapc.test.utils.RxjavaUtil;
import com.tapc.test.utils.SysUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 *
 */
public class MainActivity extends Activity {
    @BindView(R.id.test_gridview)
    GridView mTestGridview;
    @BindView(R.id.test_result_txt)
    Button mTestResultTxt;
    @BindView(R.id.erp_test)
    Button mErpBtn;
    @BindView(R.id.title_text)
    TextView mTitleText;
    @BindView(R.id.test_va_copy)
    Button mTestVaCopy;
    @BindView(R.id.partitions)
    Button mPartitions;

    private McuBslModel mMcuBslModel;
    private USBTest mUsbTest;
    private McuCmnTest mMcuCmnTest;
    private SafekeyTest mSafekeyTest;
    private KeyboardTest mKeyboardTest;
    private FanTest mFanTest;
    private HeartTest mHeartTest;
    private HeartTest mWirelessHeartTest;
    private SoundTest mAudioInTest;
    private SoundTest mMp3InTest;
    private SoundTest mSpeakerTest;
    private SoundTest mEarphoneTest;
    private ControlCmnTest mContrCmnTest;
    private RfidTest mRfidTest;
    private ManualTest mBrightnessTest;
    private ManualTest mTouchScreenTest;
    private ManualTest mTftColor;
    private ManualTest mTvTest;
    private CopyFileTest mUdiskTest;
    private CopyFileTest mTfTest;
    private CommonTest mLedTest;
    private CommonTest mBatTest;

    private UsbCtl mUsbCtl;
    private TestGridViewAdapter mAdapter;
    private List<ITest> mListItem;
    private Activity mActivity;
    private Handler mHandler;
    private boolean isAutoTesting = false;
    private Disposable mDisposable;
    private AutoTestProgressListener mAutoTestProgressListener;

    private String mBoardVersion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mActivity = this;
        initView();
    }

    private void initView() {
        mHandler = new Handler();
        initUsbCtl();
        initConfig();
        initKeyboardEvent();
        initMachineControl();
        initTestItem();
        initBroadReciver();
        mcuUpdate();
    }

    private void initUsbCtl() {
        mUsbCtl = new UsbCtl(mActivity, false);
    }

    private void initConfig() {
        File sdFile = new File("/mnt/external_sd/");
        if (sdFile.exists()) {
            Config.SD_FILE_PATH = "/mnt/external_sd/";
            Config.USB_FILE_PATH = "/mnt/usb_storage/";
        } else {
            Config.SD_FILE_PATH = "/storage/sdcard1/";
            Config.USB_FILE_PATH = "/storage/";
            Config.IS_8935_PLATFORM = true;
        }
    }

    private void initKeyboardEvent() {
        KeyEventModel.getInstance().init();
    }

    private void initMachineControl() {
        MachineController controller = MachineController.getInstance();
        controller.setReceiveBroadcast(this);
        controller.initController();
        controller.start();
    }

    private void mcuUpdate() {
        boolean isMcuUpdated = PreferenceHelper.readBoolean(this, Config.SETTING_CONFIG, "mcu_upload", false);
        if (!isMcuUpdated) {
            mMcuBslModel.startUpdate();
        }
    }

    private void initTestItem() {
        //列表里测试项
        mUsbTest = new USBTest();
        mMcuCmnTest = new McuCmnTest();
        mFanTest = new FanTest();
        mSafekeyTest = new SafekeyTest();
        mHeartTest = new HeartTest();
        mHeartTest.init(TestItemType.HEART);
        mWirelessHeartTest = new HeartTest();
        mWirelessHeartTest.init(TestItemType.WIRELESS_HEART);
        mWirelessHeartTest.setOrderTest(true);
        mKeyboardTest = new KeyboardTest();
        mKeyboardTest.init();
        mContrCmnTest = new ControlCmnTest();
        mContrCmnTest.registerReceiver(this);
        mRfidTest = new RfidTest();

        mAudioInTest = new SoundTest();
        mAudioInTest.initTest(TestItemType.AUDIO_IN);
        mAudioInTest.setOrderTest(true);
        mMp3InTest = new SoundTest();
        mMp3InTest.initTest(TestItemType.MP3_IN);
        mMp3InTest.setOrderTest(true);
        mSpeakerTest = new SoundTest();
        mSpeakerTest.initTest(TestItemType.SPEAKER);
        mSpeakerTest.setOrderTest(true);
        mEarphoneTest = new SoundTest();
        mEarphoneTest.initTest(TestItemType.EARPHONE);
        mEarphoneTest.setOrderTest(true);

        mLedTest = new CommonTest();
        mLedTest.init(TestItemType.LED);
        mBatTest = new CommonTest();
        mBatTest.init(TestItemType.BAT);

        mTftColor = new ManualTest();
        mTftColor.init(TestItemType.TFT_COLOR);
        mTftColor.setOrderTest(true);
        mTouchScreenTest = new ManualTest();
        mTouchScreenTest.init(TestItemType.TOUCHSCREEN);
        mTouchScreenTest.setOrderTest(true);
        mBrightnessTest = new ManualTest();
        mBrightnessTest.init(TestItemType.BRIGHT);
        mBrightnessTest.setOrderTest(true);
        mTvTest = new ManualTest();
        mTvTest.init(TestItemType.TV);
        mTvTest.setOrderTest(true);

        mUdiskTest = new CopyFileTest();
        mUdiskTest.init(TestItemType.UDISK);

        mTfTest = new CopyFileTest();
        mTfTest.init(TestItemType.TF);

        mMcuBslModel = new McuBslModel(mActivity, new McuBslModel.ShowVersionListener() {
            @Override
            public void version() {
                setMcuVersion();
            }
        });

        AlertDialogUtils.getInstance().initProgressDialog(this, new AlertDialogUtils.StopCallback() {
            @Override
            public void stop() {
                if (isAutoTesting) {
                    close();
                }
            }
        });
    }

    private ITestCallback mITestCallback = new ITestCallback() {
        @Override
        public void setMsg(String title, String text) {

        }

        @Override
        public void finish(int index, TestResult result) {
            mListItem.get(index).setTestResult(result);
            finished(index);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isAutoTesting == false) {
                        AlertDialogUtils.getInstance().hideProgressDialog();
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void addListItem(ITest item) {
        item.init(this, mUsbCtl, mListItem.size());
        item.setTestCallback(mITestCallback);
        if (Config.IS_8935_PLATFORM) {
//            item.setOrderTest(true);
        }
        mListItem.add(item);
    }

    private void initTestItemView() {
        if (mListItem == null) {
            mListItem = new ArrayList<ITest>();
        } else {
            for (ITest item : mListItem) {
                item.setTestResult(TestResult.NOT_TEST);
            }
            mListItem.clear();
        }

        if (Config.IS_8935_PLATFORM) {
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                mTestVaCopy.setVisibility(View.GONE);
            }
            mPartitions.setVisibility(View.VISIBLE);
        } else {
            mPartitions.setVisibility(View.GONE);
        }

        addListItem(mUsbTest);
        addListItem(mMcuCmnTest);
        addListItem(mFanTest);
        addListItem(mSafekeyTest);
        if (Config.BOARD_TYPE == BoardType.G028 || Config.BOARD_TYPE == BoardType.G029) {
            mSafekeyTest.setOrderTest(true);
        }
        addListItem(mHeartTest);
        //先启动一次有线测试，稳定测试。
        mHeartTest.selectOpenHeartTest(TestItemType.HEART);
        mHeartTest.setOrderTest(true);

        addListItem(mContrCmnTest);
        if (Config.BOARD_TYPE == BoardType.G028 || Config.BOARD_TYPE == BoardType.G029) {
            //为不跟安全锁起冲突。
            mContrCmnTest.setOrderTest(true);
        }
        if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
            addListItem(mRfidTest);
        }

        addListItem(mSpeakerTest);
        addListItem(mEarphoneTest);

        if (Config.BOARD_TYPE != BoardType.G012 && Config.BOARD_TYPE != BoardType.G029) {
            addListItem(mAudioInTest);
        }
        addListItem(mKeyboardTest);
        if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
            mKeyboardTest.setOrderTest(true);
        }
        if (Config.IS_8935_PLATFORM) {
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                addListItem(mLedTest);
                TestItemType.MP3_IN.setTitle("BT声音测试");
                if (Config.BOARD_TYPE == BoardType.G022) {
                    addListItem(mBatTest);
                }
            }
            addListItem(mMp3InTest);
            addListItem(mWirelessHeartTest);
            if (Config.BOARD_TYPE == BoardType.G028) {
                addListItem(mTvTest);
            }
        }

        if (Config.IS_8935_PLATFORM) {
            if (Config.BOARD_TYPE == BoardType.G028) {
                addListItem(mTvTest);
            }
        }
        addListItem(mTftColor);
        addListItem(mTouchScreenTest);
        addListItem(mBrightnessTest);

        if (Config.BOARD_TYPE == BoardType.G022) {
            addListItem(mUdiskTest);
        }
        addListItem(mTfTest);

        if (mAdapter == null) {
            mAdapter = new TestGridViewAdapter(this, mListItem);
        } else {
            mAdapter.setListItem(mListItem);
        }

        mTestGridview.setAdapter(mAdapter);
        mTestGridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                mListItem.get(position).start();
                AlertDialogUtils.getInstance().setProgressDialogMsg("正在手动测试", mListItem.get(position).getTestItemType
                        ().getTitle());
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private String getMCUVersion() {
        MachineController.getInstance().sendCtlVersionCmd();
        SystemClock.sleep(150);
        return MachineController.getInstance().getCtlVersionValue();
    }

    public void setMcuVersion() {
        Config.MCU_VERSION = getMCUVersion();
        String mcuVersion = "\nMCU: " + Config.MCU_VERSION;
        String version = mBoardVersion + mcuVersion;
        mTitleText.setText(version);
    }

    private void setVersionTitle() {
        String testVersion = "TAPC 测试软件  V" + SysUtils.getLocalVersionCode(this);
        String osVersion = "\nOS: " + android.os.Build.DISPLAY + "_" + android.os.Build.VERSION.INCREMENTAL;

        String boardStr = getBoardStr(Config.BOARD_TYPE);
        String boardVersion = "\n主板: " + boardStr + " -V" + Config.VERSION;

        mBoardVersion = testVersion + boardVersion + osVersion;
        mTitleText.setText(mBoardVersion + Config.MCU_VERSION);

        setMcuVersion();
    }

    private void cancelDisposable() {
        RxjavaUtil.dispose(mDisposable);
        mDisposable = null;
    }

    private void connectUsb() {
        if (mUsbCtl.isUsbConnect()) {
            return;
        }
        mDisposable = RxjavaUtil.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                while (true && mDisposable != null && !mDisposable.isDisposed()) {
                    if (!mUsbCtl.isUsbConnect()) {
                        mUsbCtl.init(0);
                    } else {
                        break;
                    }
                    SystemClock.sleep(500);
                }
                while (true && mDisposable != null && !mDisposable.isDisposed()) {
                    getBoardVersion();
                    SystemClock.sleep(100);
                    if (Config.BOARD_TYPE != BoardType.NONE) {
                        break;
                    }
                }
                e.onNext("show");
            }
        }, new Consumer() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                String cmd = (String) o;
                switch (cmd) {
                    case "show":
                        setVersionTitle();
                        initTestItemView();
                        cancelDisposable();
                        break;
                }
            }
        });
    }


    private String getBoardStr(int boardType) {
        String board = "";
        switch (boardType) {
            case BoardType.G012:
                board = "G012";
                break;
            case BoardType.G022:
                board = "G022";
                break;
            case BoardType.G028:
                board = "G028";
                break;
            case BoardType.G029:
                board = "G029";
                break;
            case BoardType.GR01:
                board = "GR01";
                break;
            default:
                board = "未知";
                break;
        }
        return board;
    }

    private void getBoardVersion() {
        final BoardVersion boardVersion = new BoardVersion();
        boardVersion.init(this, mUsbCtl, -1);
        boardVersion.start();
        Config.BOARD_TYPE = boardVersion.getBoardValue();
        Config.VERSION = boardVersion.getVersionValue();
    }

    public void showError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.stat_notify_error).setTitle("警告信息").setMessage(msg).setCancelable(false)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * 自动测试部分
     */
    private void finished(int index) {
        if (isAutoTesting && mAutoTestProgressListener != null && mListItem.get(index).isOrderTest()) {
            mAutoTestProgressListener.testfineshed(index);
        }
    }

    private void setAutoTestProgressListener(AutoTestProgressListener listener) {
        mAutoTestProgressListener = listener;
    }

    private boolean checkTestResult() {
        boolean result = true;
        for (ITest item : mListItem) {
            if (item.getTestResult() != TestResult.SUCCESS) {
                result = false;
                break;
            }
        }
        return result;
    }

    private void showTestResult() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (checkTestResult()) {
                    mTestResultTxt.setText("测试结果: " + "通过");
                } else {
                    mTestResultTxt.setText("测试结果: " + "不通过");
                }
                AlertDialogUtils.getInstance().hideProgressDialog();
            }
        });
    }

    private void autoNextTest() {
        for (ITest iTest : mListItem) {
            if (iTest.isOrderTest() && iTest.getTestResult() == TestResult.NOT_TEST) {
                iTest.start();
                return;
            }
        }
        isAutoTesting = false;
        showTestResult();
    }

    private void autoTest() {
        mDisposable = RxjavaUtil.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                mUsbTest.start();
                while (mUsbTest.getTestResult() == TestResult.NOT_TEST) {
                    SystemClock.sleep(50);
                }
                if (mUsbTest.getTestResult() == TestResult.FAIL) {
                    e.onNext("usb_test_fail");
                    return;
                }
                mMcuCmnTest.start();
                while (mMcuCmnTest.getTestResult() == TestResult.NOT_TEST) {
                    SystemClock.sleep(50);
                }
                if (mMcuCmnTest.getTestResult() == TestResult.FAIL) {
                    e.onNext("mcu_test_fail");
                    return;
                } else {
                    for (ITest iTest : mListItem) {
                        switch (iTest.getTestItemType()) {
                            case USB:
                            case MCU_COMMUNICATION:
                                break;
                            default:
                                if (iTest.isOrderTest() == false && iTest.getTestResult() == TestResult.NOT_TEST) {
                                    iTest.start();
                                }
                                break;
                        }
                    }
                    autoNextTest();
                    setAutoTestProgressListener(new AutoTestProgressListener() {
                        @Override
                        public void testfineshed(int index) {
                            autoNextTest();
                        }
                    });
                }
            }
        }, new Consumer() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                String cmd = (String) o;
                switch (cmd) {
                    case "usb_test_fail":
                        stopAutoTest("usb 通讯故障，将影响后面测试!");
                        break;
                    case "mcu_test_fail":
                        stopAutoTest("mcu 通讯故障，将影响后面测试!");
                        break;
                }
            }
        });
    }

    private void stopAutoTest(String error) {
        isAutoTesting = false;
        cancelDisposable();
        AlertDialogUtils.getInstance().hideProgressDialog();
        showError(error);
    }

    /**
     * 重启app
     */
    private void initBroadReciver() {
        registerReceiver(mReloadReciver, new IntentFilter("reload"));
    }

    private BroadcastReceiver mReloadReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("reload")) {
                reload();
            }
        }
    };

    private void reload() {
        finish();
        IntentUtil.startApp(this, getPackageName());
        System.exit(0);
    }

    @Override
    protected void onActivityResult(final int index, int resultCode, Intent data) {
        AlertDialogUtils.getInstance().showTestResultDialog(this, mListItem.get(index).getTestItemType()
                .getTitle(), new AlertDialogUtils.Callback() {
            @Override
            public void select(boolean isSuccessed) {
                if (isSuccessed) {
                    mListItem.get(index).setTestResult(TestResult.SUCCESS);
                } else {
                    mListItem.get(index).setTestResult(TestResult.FAIL);
                }
                mAdapter.notifyDataSetChanged();
                finished(index);
            }
        });
    }

    /**
     * 按键部分
     */
    @OnClick(R.id.start_test)
    protected void startTestClick(View v) {
        isAutoTesting = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.getInstance().setProgressDialogMsg("正在自动测试", "");
            }
        });
        initTestItemView();
        autoTest();
    }

    @OnClick(R.id.start_test_failed)
    protected void startTestFailedClick(View v) {
    }

    @OnClick(R.id.test_result_txt)
    protected void resultOnClick() {
        showTestResult();
    }

    @OnClick(R.id.mcu_bsl)
    protected void mcubslOnclick(View v) {
        mMcuBslModel.startUpdate();
    }

    @OnClick(R.id.erp_test)
    protected void erplOnclick(View v) {
        ErpTest.start();
    }

    @OnClick(R.id.close)
    protected void close() {
        System.exit(0);
    }

    @OnClick(R.id.uninstall)
    protected void uninstall() {
        IntentUtil.uninstall(this, this.getPackageName());
    }

    @OnClick(R.id.test_va_copy)
    protected void vaCopyOnclick(View v) {
        IntentUtil.startService(this, MenuService.class);
        IntentUtil.startApp(this, "com.estrongs.android.pop");
    }

    @OnClick(R.id.system_setting)
    protected void systemSettingOnclick(View v) {
        IntentUtil.startService(this, MenuService.class);
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    @OnClick(R.id.title_text)
    protected void titleOnclick(View v) {
        AlertDialogUtils.getInstance().showTestMsg(this, "软件信息", mTitleText.getText().toString());
    }

    @OnClick(R.id.partitions)
    protected void showPartitions(View v) {
        try {
            String text = SysUtils.execCommand("cat /proc/partitions");
            if (text != null) {
                AlertDialogUtils.getInstance().showTestMsg(this, "分区查看", text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mKeyboardTest.setKeyValue(keyCode);
        Log.d("keyboard value", "" + keyCode);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectUsb();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReloadReciver != null) {
            unregisterReceiver(mReloadReciver);
        }
        mContrCmnTest.unregisterReceiver();
    }

    private interface AutoTestProgressListener {
        void testfineshed(int index);
    }
}
