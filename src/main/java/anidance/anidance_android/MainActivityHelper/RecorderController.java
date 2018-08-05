package anidance.anidance_android.MainActivityHelper;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import anidance.anidance_android.MainActivity;
import anidance.anidance_android.MfccPackage.RecorderAnalyzer;
import anidance.anidance_android.beats.Beats;

public class RecorderController extends BaseController {

    public static String TAG = "RecorderController";

    public static int SAMPLE_RATE = 44100;
    public static int MAX_BUFFER_SIZE = SAMPLE_RATE * 16;
    public static double WAITTING_BEATS = 2;//等待时每次获取多长时间的录音
    public static int WAIT_BUFFER_SIZE = (int) Math.round(SAMPLE_RATE * WAITTING_BEATS);

    private Context mContext;

    private AudioRecord mAudioRecord;
    private Thread mReadBufferThread;

    private RecorderAnalyzer mAnalyzer;

    public RecorderController(Context context) {
        super();
        mContext = context;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, 1, AudioFormat.ENCODING_PCM_8BIT, MAX_BUFFER_SIZE);
        Log.d(TAG, "init");
        mAnalyzer = new RecorderAnalyzer(SAMPLE_RATE);

    }

    @Override
    public void start() {
        mOnControllerStartStopListener.onStartStop(true);
        Log.d(TAG, "startRecording");
        mAudioRecord.startRecording();
        mReadBufferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[MAX_BUFFER_SIZE];
                try {
                    while (true) {//等待开始唱
                        //获取一段等待的录音
                        int length = mAudioRecord.read(buffer, 0, WAIT_BUFFER_SIZE);
                        if (length != WAIT_BUFFER_SIZE) {
                            throw new RecorderStopException();
                        }
                        mAnalyzer.skip(buffer, WAIT_BUFFER_SIZE);
                        if (mAnalyzer.hasEnoughEnergy()) {
                            break;
                        }
                    }
                    //用最近的数据算出节拍
                    Beats.BeatsGener beatsGener = mAnalyzer.analysisBeats();
                    //跳过一段到达节拍点
                    int skipLength = (int) Math.round(SAMPLE_RATE * beatsGener.offset);
                    int length = mAudioRecord.read(buffer, 0, skipLength);
                    if (length != skipLength) {
                        throw new RecorderStopException();
                    }
                    mAnalyzer.skip(buffer, skipLength);
                    while (true) {//开始了就根本停不下来
                        //用末尾MFCC数据
                        double[] maskingMfcc = mAnalyzer.analysisNearest(beatsGener.duration);
                        //转化为可以发送的数据
                        List<Double> maskingMfccList = new ArrayList<>();
                        for (int i = 0; i < maskingMfcc.length; i++) {
                            maskingMfccList.add(maskingMfcc[i]);
                        }
                        String stepName = MainActivity.TABLE_MANAGER[MainActivity.DANCE_TYPE_NOW].next(maskingMfccList);
                        int stepBeats = MainActivity.TABLE_MANAGER[MainActivity.DANCE_TYPE_NOW].getStepBeats(stepName);
                        Log.d(TAG, "stepName = " + stepName + ", stepBeats = " + stepBeats);
                        //直接发送
                        if (SendMoveThread.SEND_THREAD != null && SendMoveThread.SEND_THREAD.isAlive()) {
                            SendMoveThread.SEND_THREAD.interrupt();
                        }
                        SendMoveThread.SEND_THREAD = new SendMoveThread(stepName, stepBeats * beatsGener.duration);
                        SendMoveThread.SEND_THREAD.start();
                        //跳过动作剩余节拍，每次至多跳MAX_BUFFER_SIZE
                        for (int movesSkip = (int) Math.round(SAMPLE_RATE * stepBeats * beatsGener.duration); movesSkip > 0; movesSkip -= MAX_BUFFER_SIZE) {
                            skipLength = Math.min(movesSkip, MAX_BUFFER_SIZE);
                            length = mAudioRecord.read(buffer, 0, skipLength);
                            if (length < skipLength) {
                                throw new RecorderStopException();
                            }
                        }
                    }
                } catch (RecorderStopException e) {
                    Log.d(TAG, "RecorderStop!");
                    e.printStackTrace();
                    if (SendMoveThread.SEND_THREAD != null && SendMoveThread.SEND_THREAD.isAlive()) {
                        SendMoveThread.SEND_THREAD.interrupt();
                    }
                    SendMoveThread.sendTerminal();
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecorderController.this.realStop();
                        }
                    });
                }
                Log.d(TAG, "ReadThread end");
            }
        });
        mReadBufferThread.start();
        super.start();
    }

    @Override
    public void stop() {
        mAudioRecord.stop();
    }

    private void realStop() {
        super.stop();
        mAudioRecord.release();
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, 1, AudioFormat.ENCODING_PCM_8BIT, MAX_BUFFER_SIZE);
        mOnControllerStartStopListener.onStartStop(false);
    }

    public class RecorderStopException extends Exception {
    }
}
