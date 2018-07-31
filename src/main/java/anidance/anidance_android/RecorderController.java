package anidance.anidance_android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class RecorderController extends BaseController {

    public static String TAG = "RecorderController";

    private Context mContext;

    private AudioRecord mAudioRecord;
    private Thread mReadBufferThread;

    public RecorderController(Context context) {
        super();
        mContext = context;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, 1, AudioFormat.ENCODING_PCM_8BIT, 44100 * 4);
        mReadBufferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long lastBeat = System.currentTimeMillis();
                byte[] buffer = new byte[44100 * 4];
                while (!Thread.interrupted()) {
                    mAudioRecord.read(buffer, 0, 44100);
                    StringBuilder tmp = new StringBuilder("buffer:");
                    for (byte b: buffer) {
                        tmp.append(" ");
                        tmp.append(b);
                    }
                    Log.d(TAG, "buffer: " + tmp.toString());
                }
            }
        });
    }

    @Override
    public void start() {
        mOnControllerStartStopListener.onStartStop(true);
        mAudioRecord.startRecording();
        mReadBufferThread.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        mReadBufferThread.interrupt();
        mAudioRecord.stop();
        mOnControllerStartStopListener.onStartStop(false);
    }
}
