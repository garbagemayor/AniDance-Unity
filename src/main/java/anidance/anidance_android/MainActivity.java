package anidance.anidance_android;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.player.R;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends UnityPlayerActivity {

    public static String TAG = "MainActivity";

    private LinearLayout mUnityParent;

    private View mPlaceHolderView;
    private View mOperationView;

    private int mColumnActive;
    private View mColumnFileView;
    private View mColumnLiveView;
    private View mOperationFileView;
    private View mOperationLiveView;

    private TextView mFileNameText;
    private Button mFileBrowseBtn;
    private Uri mFileUri;
    private Button mFileListenBtn;
    private MediaPlayer mMediaPlayer;
    private Button mFileStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        //检查App是否有足够的权限
        requestAllPermissions();

        //Unity背景
        mUnityParent = findViewById(R.id.main_unity_view);
        mUnityParent.addView(mUnityPlayer.getView());

        //操作层点击隐藏
        mPlaceHolderView = findViewById(R.id.main_place_holder_view);
        mOperationView = findViewById(R.id.main_operation_view);
        mPlaceHolderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperationView.getVisibility() == View.VISIBLE) {
                    mOperationView.setVisibility(View.GONE);
                } else {
                    mOperationView.setVisibility(View.VISIBLE);
                }
            }
        });

        //模式选择
        mColumnActive = 0;
        mColumnFileView = findViewById(R.id.main_column_file);
        mColumnLiveView = findViewById(R.id.main_column_live);
        mOperationFileView = findViewById(R.id.main_operation_file);
        mOperationLiveView = findViewById(R.id.main_operation_live);
        mOperationLiveView.setVisibility(View.GONE);
        mColumnFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColumnActive = 0;
                mColumnFileView.setBackgroundResource(R.drawable.top_column_bar_active);
                mColumnLiveView.setBackgroundResource(R.drawable.top_column_bar_inactive);
                mOperationFileView.setVisibility(View.VISIBLE);
                mOperationLiveView.setVisibility(View.GONE);
            }
        });
        mColumnLiveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColumnActive = 1;
                mColumnFileView.setBackgroundResource(R.drawable.top_column_bar_inactive);
                mColumnLiveView.setBackgroundResource(R.drawable.top_column_bar_active);
                mOperationFileView.setVisibility(View.GONE);
                mOperationLiveView.setVisibility(View.VISIBLE);
            }
        });

        //文件名太长时左右滚动
        mFileNameText = findViewById(R.id.file_mode_path_text);
        mFileNameText.setMovementMethod(ScrollingMovementMethod.getInstance());

        //浏览文件按钮
        mFileBrowseBtn = findViewById(R.id.file_mode_browse_btn);
        mFileBrowseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    mFileListenBtn.setText("试听");
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        //试听按钮
        mMediaPlayer = null;
        mFileListenBtn = findViewById(R.id.file_mode_listen_btn);
        mFileListenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileUri != null) {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                        try {
                            mMediaPlayer.setDataSource(MainActivity.this, mFileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mMediaPlayer.setLooping(true);
                                mMediaPlayer.start();
                                mFileListenBtn.setText("停止");
                            }
                        });
                    } else {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                            mFileListenBtn.setText("试听");
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请选择文件！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUnityPlayer.quit();
            }
        });
        super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    //获取权限
    private void requestAllPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
        };
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this, "需要权限！", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
                Toast.makeText(MainActivity.this, "申请权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //浏览文件返回时
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "requestCode = " + resultCode);
            if (requestCode == 1) {
                mFileUri = data.getData();
                String fileDisplayName = getDisplayNameFromUri(MainActivity.this, mFileUri);
                mFileNameText.setText(fileDisplayName);
            }
        }
    }

    //从Uri获取不带路径的文件名
    private static String getDisplayNameFromUri(Context context, Uri uri) {
        if (uri == null || uri.getScheme() == null) {
            return null;
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return uri.getLastPathSegment();
        }
        Cursor cursor = context.getContentResolver().query( uri, new String[] {MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null );
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        return cursor.getString(columnIndex);
    }
}
