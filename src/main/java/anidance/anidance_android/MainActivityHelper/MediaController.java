package anidance.anidance_android.MainActivityHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anidance.anidance_android.MainActivity;
import anidance.anidance_android.MfccPackage.WaveFileAnalyzer;
import anidance.anidance_android.beats.Beats;

public class MediaController extends BaseController {

    public static String TAG = "MediaController";

    private Context mContext;

    private Uri mUri;
    private MediaPlayer mMediaPlayer;

    private WaveFileAnalyzer mAnalyzer;
    private Thread mReadThread;

    public MediaController(Context context) {
        super();
        mContext = context;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(false);
    }

    public void setUri(Uri uri) {
        if (mUri != null) {
            mMediaPlayer.reset();
        }
        mUri = uri;
        try {
            mMediaPlayer.setDataSource(mContext, mUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        mVisualizerViewCallBack.getView().link(mMediaPlayer);
        mVisualizerViewCallBack.getView().setOutsideOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MediaController.this.stop();
            }
        });
        mOnControllerStartStopListener.onStartStop(true);
        mReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mAnalyzer = new WaveFileAnalyzer(getRealFilePath(mUri));

                //计算节拍
                Log.d(TAG, "取前若干秒，获取节拍");
                mAnalyzer.skip(WaveFileAnalyzer.NEAREST_SECONDS);
                Beats.BeatsGener beatsGener = mAnalyzer.analysisBeats();
                Log.d(TAG, "beatsGener = (" + beatsGener.offset + ", " + beatsGener.duration + ")");
                mAnalyzer.resetHead();
                mAnalyzer.skip(beatsGener.offset);
                //触发播放器和开始时间
                long playerStartTime = System.nanoTime() / 1000000;
                mMediaPlayer.start();
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MediaController.super.start();
                    }
                });
                try {
                    //发送与计算迭代
                    while (mAnalyzer.hasNext(beatsGener.duration)) {
                        //计算下一拍
                        double stepStartTime = mAnalyzer.getCurrentTime();
                        double[] maskingMfcc = mAnalyzer.analysisNext(beatsGener.duration);
                        //转化为可以发送的数据
                        List<Double> maskingMfccList = new ArrayList<>();
                        for (int i = 0; i < maskingMfcc.length; i ++) {
                            maskingMfccList.add(maskingMfcc[i]);
                        }
                        String stepName = MainActivity.TABLE_MANAGER[MainActivity.DANCE_TYPE_NOW].next(maskingMfccList);
                        int stepBeats = MainActivity.TABLE_MANAGER[MainActivity.DANCE_TYPE_NOW].getStepBeats(stepName);
                        Log.d(TAG, "stepName = " + stepName + ", stepBeats = " + stepBeats);
                        long playerCurrentTime = System.nanoTime() / 1000000 - playerStartTime;
                        long sleepTime = Math.max(0, Math.round(stepStartTime * 1000) - playerCurrentTime);
                        //等待然后发送
                        Thread.sleep(sleepTime);
                        if (SendMoveThread.SEND_THREAD != null && SendMoveThread.SEND_THREAD.isAlive()) {
                            SendMoveThread.SEND_THREAD.interrupt();
                        }
                        SendMoveThread.SEND_THREAD = new SendMoveThread(stepName, stepBeats * beatsGener.duration);
                        SendMoveThread.SEND_THREAD.start();
                        //跳过动作剩余节拍
                        mAnalyzer.skip((stepBeats - 1) * beatsGener.duration);
                        beatsGener = mAnalyzer.analysisBeats();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mReadThread.start();
    }

    @Override
    public void stop() {
        super.stop();
        mReadThread.interrupt();
        if (SendMoveThread.SEND_THREAD != null && SendMoveThread.SEND_THREAD.isAlive()) {
            SendMoveThread.SEND_THREAD.interrupt();
        }
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        mVisualizerViewCallBack.getView().release();
        mOnControllerStartStopListener.onStartStop(false);
        SendMoveThread.sendTerminal();
    }

    //从Uri获取绝对路径
    private String getRealFilePath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        String res = null;
        if (scheme == null) {
            res = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            res = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{MediaStore.Audio.AudioColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                    if (index > -1) {
                        res = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return res;
    }
}
