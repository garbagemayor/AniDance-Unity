package anidance.anidance_android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unity3d.player.R;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import anidance.anidance_android.VisualizerPackage.VisualizerView;
import anidance.anidance_android.VisualizerPackage.VisualizerViewHelper;

public class MainActivity extends UnityPlayerActivity {

    public static String TAG = "MainActivity";

    //Unity背景部分
    private LinearLayout mUnityParent;
    private View mPlaceHolderView;
    private View mOperationView;
    private HackerThread mHackerThread;

    //模式选择部分
    private View mColumnFileView;
    private View mColumnLiveView;
    private View mOperationFileView;
    private View mOperationLiveView;

    //舞种选择部分
    public static int DANCE_TYPE_COUNT = 4;
    public static String[] DANCE_TYPE_NAME = {"Tangle", "Rumba", "Chacha", "Waltz"};
    private int mDanceTypeNow;// = 0,1,2,3
    private CheckBox[] mDanceTypeCheckBox;

    //选择文件部分
    private TextView mFileNameText;
    private Button mFileBrowseBtn;
    private Button mFileStartBtn;
    private Button mFileStopBtn;
    private VisualizerView mFileVisualizerView;
    private MediaController mMediaController;

    //现场演唱部分的节拍器
    private CheckBox mMetronomeCheckBox;
    private EditText mMetronomeEditText;
    private VisualizerView mMetronomeVisualizerView;
    private Button mMetronomeStartBtn;
    private Button mMetronomeStopBtn;
    private MetronomeController mMetronomeController;

    //现场演唱部分的录音机
    private VisualizerView mRecorderVisualizerView;
    private Button mRecorderStartBtn;
    private Button mRecorderStopBtn;
    private RecorderController mRecorderController;

    private DatagramSocket mUnitySocket;
    private InetAddress mUnityAddr;
    private int mUnityPort;
    private Thread mReadAndSendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        //权限检验器
        PermissionsChecker.run(MainActivity.this);

        //文件路径生成器
        FolderGenerator.run();

        //Unity背景部分
        initUnityBackgroundView();

        //模式选择
        initModeSelectView();

        //舞种选择
        initDanceTypeView();

        //选择文件部分
        initFileModeView();

        //现场演唱部分
        initLiveModeMetronome();
        initLiveModeRecorder();

        //准备与Unity通信
        try {
            mUnityPort = 12345;
            mUnitySocket = new DatagramSocket();
            mUnityAddr = InetAddress.getByName("127.0.0.1");
        } catch (SocketException e) {
            Log.e(TAG, "mUnitySocket炸了");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.e(TAG, "mUnityAddr炸了");
            e.printStackTrace();
        }
    }

    //Unity背景部分
    private void initUnityBackgroundView() {
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
        mPlaceHolderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mHackerThread == null) {
                    mHackerThread = new HackerThread();
                    mHackerThread.start();
                } else {
                    mHackerThread.interrupt();
                    mHackerThread = null;
                }
                return true;
            }
        });
    }

    //模式选择部分
    private void initModeSelectView() {
        //模式选择
        mColumnFileView = findViewById(R.id.main_column_file);
        mColumnLiveView = findViewById(R.id.main_column_live);
        mOperationFileView = findViewById(R.id.main_operation_file);
        mOperationLiveView = findViewById(R.id.main_operation_live);
        mOperationLiveView.setVisibility(View.GONE);
        mColumnFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColumnFileView.setBackgroundResource(R.drawable.top_column_bar_active);
                mColumnLiveView.setBackgroundResource(R.drawable.top_column_bar_inactive);
                mOperationFileView.setVisibility(View.VISIBLE);
                mOperationLiveView.setVisibility(View.GONE);
            }
        });
        mColumnLiveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColumnFileView.setBackgroundResource(R.drawable.top_column_bar_inactive);
                mColumnLiveView.setBackgroundResource(R.drawable.top_column_bar_active);
                mOperationFileView.setVisibility(View.GONE);
                mOperationLiveView.setVisibility(View.VISIBLE);
            }
        });
    }

    //舞种选择
    private void initDanceTypeView() {
        mDanceTypeNow = 0;
        mDanceTypeCheckBox = new CheckBox[4];
        mDanceTypeCheckBox[0] = findViewById(R.id.dance_type_T_checkbox);
        mDanceTypeCheckBox[1] = findViewById(R.id.dance_type_R_checkbox);
        mDanceTypeCheckBox[2] = findViewById(R.id.dance_type_C_checkbox);
        mDanceTypeCheckBox[3] = findViewById(R.id.dance_type_W_checkbox);
        for (int i = 0; i < DANCE_TYPE_COUNT; i ++) {
            final int i_f = i;
            mDanceTypeCheckBox[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && mDanceTypeNow != i_f) {
                        int j = mDanceTypeNow;
                        mDanceTypeNow = i_f;
                        mDanceTypeCheckBox[j].setChecked(false);
                    } else if (mDanceTypeNow == i_f) {
                        mDanceTypeCheckBox[i_f].setChecked(true);
                    }
                }
            });
        }
    }

    //选择文件部分
    private void initFileModeView() {
        //显示文件名
        mFileNameText = findViewById(R.id.file_mode_path_text);
        mFileNameText.setMovementMethod(ScrollingMovementMethod.getInstance());

        //浏览文件按钮
        mFileBrowseBtn = findViewById(R.id.file_mode_browse_btn);
        mFileBrowseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        //波形绘制器
        mFileVisualizerView = findViewById(R.id.file_mode_visualizer);
        VisualizerViewHelper.setDefaultLineRender(mFileVisualizerView);

        //开始按钮
        mFileStartBtn = findViewById(R.id.file_mode_start_btn);
        mFileStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaController.start();
            }
        });

        //停止按钮
        mFileStopBtn = findViewById(R.id.file_mode_stop_btn);
        mFileStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaController.stop();
            }
        });

        //播放与跳舞控制器
        mMediaController = new MediaController(MainActivity.this);
        mMediaController.setVisualizerViewCallBack(new VisualizerViewCallBack() {
            @Override
            public VisualizerView getView() {
                return mFileVisualizerView;
            }
        });
        mMediaController.setOnControllerStartStopListener(new OnControllerStartStopListener() {
            @Override
            public void onStartStop(boolean isStart) {
                mFileBrowseBtn.setEnabled(!isStart);
                mFileStartBtn.setEnabled(!isStart);
                mFileStopBtn.setEnabled(isStart);
                mColumnFileView.setEnabled(!isStart);
                mColumnLiveView.setEnabled(!isStart);
                mPlaceHolderView.setEnabled(!isStart);
            }
        });
    }

    //现场演唱的节拍器
    private void initLiveModeMetronome() {
        //节拍器使能
        mMetronomeCheckBox = findViewById(R.id.metronome_checkbox);
        mMetronomeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMetronomeEditText.setEnabled(isChecked);
                if (isChecked) {
                    mMetronomeController.setBpm(Integer.valueOf(mMetronomeEditText.getText().toString()));
                } else {
                    mMetronomeController.setBpm(0);
                }
            }
        });

        //节拍器数字
        mMetronomeEditText = findViewById(R.id.metronome_edittext);
        mMetronomeEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mMetronomeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    int value = Integer.valueOf(s.toString());
                    if (value < 1) {
                        value = 1;
                        mMetronomeEditText.setText("1");
                        mMetronomeEditText.setSelection(1);
                    } else if (value > 600) {
                        value = 600;
                        mMetronomeEditText.setText("600");
                        mMetronomeEditText.setSelection(3);
                    }
                    mMetronomeController.setBpm(value);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //节拍器开始
        mMetronomeStartBtn = findViewById(R.id.metronome_start_btn);
        mMetronomeStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMetronomeController.start();
            }
        });

        //节拍器停止按钮
        mMetronomeStopBtn = findViewById(R.id.metronome_stop_btn);
        mMetronomeStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMetronomeController.stop();
            }
        });

        //节拍器控制器
        mMetronomeController = new MetronomeController(MainActivity.this, Integer.valueOf(mMetronomeEditText.getText().toString()));
        mMetronomeController.setVisualizerViewCallBack(new VisualizerViewCallBack() {
            @Override
            public VisualizerView getView() {
                return mMetronomeVisualizerView;
            }
        });
        mMetronomeController.setOnControllerStartStopListener(new OnControllerStartStopListener() {
            @Override
            public void onStartStop(boolean isStart) {
                mMetronomeCheckBox.setEnabled(!isStart);
                mMetronomeEditText.setEnabled(!isStart && mMetronomeCheckBox.isChecked());
                mMetronomeStartBtn.setEnabled(!isStart);
                mMetronomeStopBtn.setEnabled(isStart);
                mColumnFileView.setEnabled(!isStart && !mRecorderController.isRunning());
                mColumnLiveView.setEnabled(!isStart && !mRecorderController.isRunning());
                mPlaceHolderView.setEnabled(!isStart && !mRecorderController.isRunning());
            }
        });
    }

    //现场演唱的录音机
    private void initLiveModeRecorder() {
        //录音机波形显示
        mRecorderVisualizerView = findViewById(R.id.recorder_visualizer);
        VisualizerViewHelper.setDefaultLineRender(mRecorderVisualizerView);

        //录音机开始按钮
        mRecorderStartBtn = findViewById(R.id.recorder_start_btn);
        mRecorderStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorderController.start();
            }
        });

        //录音机停止按钮
        mRecorderStopBtn = findViewById(R.id.recorder_stop_btn);
        mRecorderStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorderController.stop();
            }
        });

        //录音控制器
        mRecorderController = new RecorderController(MainActivity.this);
        mRecorderController.setVisualizerViewCallBack(new VisualizerViewCallBack() {
            @Override
            public VisualizerView getView() {
                return mRecorderVisualizerView;
            }
        });
        mRecorderController.setOnControllerStartStopListener(new OnControllerStartStopListener() {
            @Override
            public void onStartStop(boolean isStart) {
                mRecorderStartBtn.setEnabled(!isStart);
                mRecorderStopBtn.setEnabled(isStart);
                mColumnFileView.setEnabled(!isStart && !mMetronomeController.isRunning());
                mColumnLiveView.setEnabled(!isStart && !mMetronomeController.isRunning());
                mPlaceHolderView.setEnabled(!isStart && !mMetronomeController.isRunning());
            }
        });
    }

    //浏览文件返回时
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                if (uri != null) {
                    mFileNameText.setText(MainActivity.getDisplayNameFromUri(MainActivity.this, uri));
                    mMediaController.setUri(uri);
                    mFileStartBtn.setEnabled(true);
                }
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
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        return cursor.getString(columnIndex);
    }
}
