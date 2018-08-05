package anidance.anidance_android.MainActivityHelper;

import android.content.Context;
import android.media.MediaPlayer;

import com.unity3d.player.R;

public class MetronomeController extends BaseController {

    public static String TAG = "MetronomeController";
    public static int MIN_BPM = 30;
    public static int MAX_BPM = 180;

    private Context mContext;

    private int mBpm = 60;//0表示没有
    private MediaPlayer mMediaPlayer;
    private Thread mBeatThread;

    public MetronomeController(Context context, int bpm) {
        super();
        mContext = context;
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.metronome_beat_1);
    }

    public void setBpm(int bpm) {
        mBpm = bpm;
    }

    @Override
    public void start() {
        mOnControllerStartStopListener.onStartStop(true);
        mBeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                double beatSpace = 60.0 / mBpm;
                int beatSpaceMillis = (int) Math.floor(beatSpace * 1000);
                int beatSpaceNanos = (int) Math.floor(beatSpace * 1000000000) % 1000000;
                try {
                    while (!Thread.interrupted()) {
                        mMediaPlayer.start();
                        Thread.sleep(beatSpaceMillis, beatSpaceNanos);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mBeatThread.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        mBeatThread.interrupt();
        mOnControllerStartStopListener.onStartStop(false);
    }
}
