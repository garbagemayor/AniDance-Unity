package anidance.anidance_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.R;

import anidance.anidance_android.MainActivityHelper.FolderGenerator;
import anidance.anidance_android.MainActivityHelper.PermissionsChecker;
import anidance.anidance_android.table.MovesDouble;
import anidance.anidance_android.table.TableManager;

public class WelcomeActivity extends AppCompatActivity {

    public static String TAG = "WelcomeActivity";

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
}
