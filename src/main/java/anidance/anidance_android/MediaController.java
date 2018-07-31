package anidance.anidance_android;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class MediaController extends BaseController {

    public static String TAG = "MediaController";

    private Context mContext;

    private Uri mUri;
    private MediaPlayer mMediaPlayer;
    private WaveToMfcc mWaveToMfcc;

    public MediaController(Context context) {
        super();
        mContext = context;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(false);
        //mWaveToMfcc = new WaveToMfcc();
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
                Log.d(TAG, "OutsideOnCompletionListener");
            }
        });
        mOnControllerStartStopListener.onStartStop(true);
        mMediaPlayer.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        mVisualizerViewCallBack.getView().release();
        mOnControllerStartStopListener.onStartStop(false);
    }
}
