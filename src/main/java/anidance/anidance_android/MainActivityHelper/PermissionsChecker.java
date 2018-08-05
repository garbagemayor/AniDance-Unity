package anidance.anidance_android.MainActivityHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionsChecker {

    public static String TAG = "PermissionsChecker";

    private static String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
    };

    public static void run(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(context, "需要权限！", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions((Activity) context, permissions, 1);
                Toast.makeText(context, "申请权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
