package anidance.anidance_android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.ant.liao.GifView;
import com.unity3d.player.R;

import java.util.Random;

import anidance.anidance_android.MainActivityHelper.FolderGenerator;
import anidance.anidance_android.MainActivityHelper.PermissionsChecker;
import anidance.anidance_android.table.MovesDouble;
import anidance.anidance_android.table.TableManager;

public class WelcomeActivity extends AppCompatActivity {

    public static String TAG = "WelcomeActivity";

    public static String[] HINT_TEXT = {
            "正在跳舞时切换舞种将在当前正在进行的动作结束后生效。",
            "演唱模式下舞蹈将在检测到足够音量持续2秒后开始。",
            "点击动画区域可以隐藏操作界面，再次点击重新弹出。",
            "开发者设计了这条出现概率很低的提示，调戏一下幸运的用户。",
    };
    public static int HINT_PROB_BASE = 100000;

    private ImageView mBackground1;
    private ImageView mBackground2;
    //private GifView mBackground3;
    private TextView mHintText;

    private volatile int mLoadImageFlag;

    private Thread mInitThread;
    private Thread mWaitThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //权限检验器
        PermissionsChecker.run(WelcomeActivity.this);

        //文件路径生成器
        FolderGenerator.run();

        //手动加载文件过大的图
        mLoadImageFlag = 0;
        mBackground1 = findViewById(R.id.welcome_background_1);
        mBackground1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "background1 onGlobalLayout");
                loadLargeBitmapForImageView(mBackground1, R.drawable.welcome_background_1, ImageView.ScaleType.FIT_XY);
                mLoadImageFlag ++;
            }
        });
        mBackground2 = findViewById(R.id.welcome_background_2);
        mBackground2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "background2 onGlobalLayout");
                loadLargeBitmapForImageView(mBackground2, R.drawable.welcome_background_2, ImageView.ScaleType.FIT_CENTER);
                mLoadImageFlag ++;
            }
        });

        /*
        //加载GIF
        mBackground3 = findViewById(R.id.welcome_background_3);
        mBackground3.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBackground3.setGifImage(R.drawable.welcome_background_3);
                int viewW = mBackground3.getWidth();
                int viewH = mBackground3.getHeight();
                Log.d(TAG, "background3 w = " + viewW + ", h = " + viewH);
            }
        });
        */

        //随机选择一条提示词
        mHintText = findViewById(R.id.welcome_hint);
        int hintId = new Random().nextInt(HINT_PROB_BASE * (HINT_TEXT.length - 1) + 1) / HINT_PROB_BASE;
        mHintText.setText("小贴士：" + HINT_TEXT[hintId]);


        //初始化巨大的数据表
        mInitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                MovesDouble.init();
                MainActivity.TABLE_MANAGER = new TableManager[4];
                MainActivity.TABLE_MANAGER[0] = new TableManager("T");
                MainActivity.TABLE_MANAGER[1] = new TableManager("R");
                MainActivity.TABLE_MANAGER[2] = new TableManager("C");
                MainActivity.TABLE_MANAGER[3] = new TableManager("W");
                TableManager.initFinishFlag = true;
                Log.d(TAG, "TableManager init finish");
            }
        });
        mInitThread.start();
        //持续3秒后启动主界面
        mWaitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mLoadImageFlag != 2) {
                        Thread.sleep(10);
                    }
                    Thread.sleep(3000);
                    mInitThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class );
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        });
        mWaitThread.start();
    }

    private void loadLargeBitmapForImageView(ImageView imageView, int resourceId, ImageView.ScaleType scaleType) {
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);
        int resW = bitmap.getWidth();
        int resH = bitmap.getHeight();
        int viewW = imageView.getWidth();
        int viewH = imageView.getHeight();
        int scaleW = 0;
        int scaleH = 0;
        if (scaleType == ImageView.ScaleType.FIT_CENTER) {
            if (viewW * resH < resW * viewH) {
                scaleW = viewW;
                scaleH = Math.round((float) viewW * resH / resW);
            } else {
                scaleH = viewH;
                scaleW = Math.round((float) viewH * resW / resH);
            }
        } else if (scaleType == ImageView.ScaleType.FIT_XY) {
            scaleW = viewW;
            scaleH = viewH;
        }
        Bitmap bitmapOnView = Bitmap.createScaledBitmap(bitmap, scaleW  / 2, scaleH / 2, true);
        bitmap.recycle();
        imageView.setImageBitmap(bitmapOnView);
        imageView.setScaleType(scaleType);
    }

    @Override
    protected void onDestroy() {
        ((BitmapDrawable) mBackground1.getDrawable()).getBitmap().recycle();
        mBackground1.setImageDrawable(null);
        ((BitmapDrawable) mBackground2.getDrawable()).getBitmap().recycle();
        mBackground2.setImageDrawable(null);
        super.onDestroy();
    }
}
