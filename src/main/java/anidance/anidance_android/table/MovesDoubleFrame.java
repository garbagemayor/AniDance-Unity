package anidance.anidance_android.table;

import android.util.Log;

import anidance.anidance_android.SendMoveThread;

public class MovesDoubleFrame {

    public static String TAG = "MovesDoubleFrame";
    private static byte[] byteHeader = "{\"center\": ".getBytes();

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
        SendMoveThread.currentCenter.y = 0.98 * SendMoveThread.currentCenter.y + 0.02 * center.y;
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

    private Vector3d calculateCenter(Vector3d lct, Vector3d ct) {
        lct.x = lct.x * 0.98 + ct.x * 0.02;
        lct.y = lct.y * 0.98 + ct.y * 0.02;
        lct.z = lct.z * 0.98 + ct.z * 0.02;
        return lct;
    }
}
