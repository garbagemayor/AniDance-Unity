package anidance.anidance_android;

import android.content.Context;
import android.media.MediaPlayer;

import com.unity3d.player.R;

public class MetronomeController extends BaseController {

    public static String TAG = "MetronomeController";

    private Context mContext;

    private int mBpm = 60;//0表示没有
    private MediaPlayer mMediaPlayer;
    private Thread mBeatThread;

    public MetronomeController(Context context, int bpm) {
        mContext = context;
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.metronome_beat_1);
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
    }

    public void setBpm(int bpm) {
        mBpm = bpm;
    }

    @Override
    public void start() {
        mOnControllerStartStopListener.onStartStop(true);
        mBeatThread.start();
    }

    @Override
    public void stop() {
        mBeatThread.interrupt();
        mOnControllerStartStopListener.onStartStop(false);
    }
}
