package anidance.anidance_android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import anidance.anidance_android.MfccPackage.WaveDataAnalyzer;

public class MediaController extends BaseController {

    public static String TAG = "MediaController";

    private Context mContext;

    private Uri mUri;
    private MediaPlayer mMediaPlayer;

    private Thread mReadThread;
    private WaveDataAnalyzer mAnalyzer;

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
        mReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                double[][] mfcc = WaveDataAnalyzer.analysis(getRealFilePath(mUri));
                try {
                    Log.d(TAG, "PrintWriter: print mfcc, shape = (" + mfcc.length + ", " + mfcc[0].length + ")");
                    PrintWriter pr = new PrintWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AniDance/socket_message.txt"));
                    for (int j = 0; j < mfcc[0].length; j ++) {
                        for (int i = 0; i < mfcc.length; i ++) {
                            pr.printf("%16.8f    ", mfcc[i][j]);
                        }
                        pr.println();
                    }
                    pr.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "PrintWriter finish");

            }
        });
        mReadThread.start();
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
