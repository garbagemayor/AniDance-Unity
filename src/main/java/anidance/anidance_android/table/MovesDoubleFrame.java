package anidance.anidance_android.table;

import android.util.Log;

import anidance.anidance_android.SendMoveThread;

public class MovesDoubleFrame {

    public static String TAG = "MovesDoubleFrame";
    private static byte[] byteHeader = "{\"center\": ".getBytes();
    private static double KEEP_CONSTANT = 0.998;

    public Vector3d center;
    public String skeletons;

    public MovesDoubleFrame(Vector3d center, String skeletons) {
        this.center = center;
        this.skeletons = skeletons;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"center\": ");
        center.addToStringBuilder(builder);
        builder.append(skeletons);
        return builder.toString();
    }

    public int toByteBuffer(byte[] buffer, int offset) {
        int pos = offset;
        for (int i = 0; i < byteHeader.length; i ++) {
            buffer[pos ++] = byteHeader[i];
        }
        if (SendMoveThread.lastCenter != null && Math.abs(center.x - SendMoveThread.lastCenter.x) + Math.abs(center.y - SendMoveThread.lastCenter.y) + Math.abs(center.z - SendMoveThread.lastCenter.z) < 10) {
            SendMoveThread.currentCenter.x += center.x - SendMoveThread.lastCenter.x;
            SendMoveThread.currentCenter.y += center.y - SendMoveThread.lastCenter.y;
            SendMoveThread.currentCenter.z += center.z - SendMoveThread.lastCenter.z;
        }
        SendMoveThread.lastCenter = center;
        //SendMoveThread.currentCenter.y = 0.98 * SendMoveThread.currentCenter.y + 0.02 * center.y;
        keepCenter(center);
        /*
        SendMoveThread.py_ss = SendMoveThread.py_ss * 0.98 + (v.y - 85) * 0.02;
        v.y -= SendMoveThread.py_ss;
        */
        pos += SendMoveThread.currentCenter.toByteBuffer(buffer, pos);
        buffer[pos ++] = ',';
        buffer[pos ++] = ' ';
        for (int i = 0; i < skeletons.length(); i ++) {
            buffer[pos ++] = (byte) skeletons.charAt(i);
        }
        return pos - offset;
    }

    private void keepCenter(Vector3d ct) {
        Vector3d cct = SendMoveThread.currentCenter;
        cct.x = cct.x * KEEP_CONSTANT + ct.x * (1 - KEEP_CONSTANT);
        cct.y = cct.y * KEEP_CONSTANT + ct.y * (1 - KEEP_CONSTANT);
        cct.z = cct.z * KEEP_CONSTANT + ct.z * (1 - KEEP_CONSTANT);
    }
}
