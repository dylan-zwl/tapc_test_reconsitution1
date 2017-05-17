package com.tapc.test.model;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.LinkedList;

public class VoiceInput {
    private static VoiceInput sVoiceInput;
    protected int mInbufsize;
    private float mVoiceVolume = 1f;
    private AudioRecord mInrec;
    private byte[] mInbytes;
    private LinkedList<byte[]> mInq;
    private int mOutbufsize;
    private AudioTrack mOuttrk;
    private byte[] mOutbytes;
    private Thread mRecord;
    private Thread mPlay;
    private boolean start = false;
    private boolean hasInput = true;

    public static VoiceInput getInstance() {
        if (sVoiceInput == null) {
            sVoiceInput = new VoiceInput();
        }
        return sVoiceInput;
    }

    public VoiceInput() {
        initVoice();
        mRecord = new Thread(new recordSound());
        mPlay = new Thread(new playRecord());
        mRecord.start();
        mPlay.start();
    }

    public void setInput(boolean flag) {
        hasInput = flag;
    }

    public void start() {
        start = true;
        Log.d("VoiceInput", "start");
    }

    public void stop() {
        start = false;
        Log.d("VoiceInput", "stop");
    }

    private void initVoice() {
        mInbufsize = AudioRecord
                .getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mInrec = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, mInbufsize);
        mInbytes = new byte[mInbufsize];
        mInq = new LinkedList<byte[]>();

        mOutbufsize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        mOuttrk = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, mOutbufsize, AudioTrack.MODE_STREAM);
        mOuttrk.setStereoVolume(mVoiceVolume, mVoiceVolume);
        mOutbytes = new byte[mOutbufsize];
    }

    class recordSound implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] bytes_pkg;
                    mInrec.startRecording();
                    while (true) {
                        if (start) {
                            if (mInq.size() < 1000) {
                                mInrec.read(mInbytes, 0, mInbufsize);
                                bytes_pkg = mInbytes.clone();
                                synchronized (mInq) {
                                    mInq.add(bytes_pkg);
                                }
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mInrec.stop();
            }
        }
    }

    class playRecord implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] bytes_pkg = null;
                    mOuttrk.play();
                    while (true) {
                        if ((start) && (mInq.size() > 0)) {
                            synchronized (mInq) {
                                mOutbytes = mInq.getFirst();
                                bytes_pkg = mOutbytes.clone();
                                mInq.removeFirst();
                            }
                            mOuttrk.write(bytes_pkg, 0, bytes_pkg.length);
                        } else {
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mOuttrk.stop();
            }
        }
    }
}
