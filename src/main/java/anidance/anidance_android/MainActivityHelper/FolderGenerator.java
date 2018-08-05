package anidance.anidance_android.MainActivityHelper;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FolderGenerator {

    private static String TAG = "FolderGenerator";

    public static void run() {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String appFolderPath = sdcardPath + "/AniDance";
        File file = new File(appFolderPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "创建文件失败");
            }
        }
    }
}
