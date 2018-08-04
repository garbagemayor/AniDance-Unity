package anidance.anidance_android;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import anidance.anidance_android.table.MovesDoubleFrame;
import anidance.anidance_android.table.Vector3d;

public class SendMoveThread extends Thread {

    public static String TAG = "SendMoveThread";
    public static SendMoveThread SEND_THREAD = null;
    private static byte[] byteBuffer = new byte[1 << 16];
    private static String termin = "{\"skeletons\": [{\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}], \"center\": {\"z\": 10000, \"x\": 10000, \"y\": 10000}}";
    public static Vector3d currentCenter = new Vector3d(28, 88, 128);
    public static Vector3d lastCenter = null;

    private String mStepName;
    private double mStepDuration;
    private MovesDoubleFrame[] mStepMoves;

    private static DatagramSocket mUnitySocket;
    private static InetAddress mUnityAddr;
    private static int mUnityPort;


    public SendMoveThread(String stepName, double stepDuration) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        if (mUnitySocket == null) {
            try {
                mUnityPort = 12345;
                mUnitySocket = new DatagramSocket();
                mUnityAddr = InetAddress.getByName("127.0.0.1");
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
        mStepName = stepName;
        mStepDuration = stepDuration;
        mStepMoves = MainActivity.TABLE_MANAGER[MainActivity.DANCE_TYPE_NOW].getMoves(stepName);
        Log.d(TAG, "create " + mStepName);
    }

    @Override
    public void run() {
        if (mStepMoves == null || mStepMoves.length == 0) {
            return;
        }
        Log.d(TAG, "start! " + mStepName);
        long frameDuration = (long) Math.floor(mStepDuration * 1000 / mStepMoves.length * 0.98);
        long startTime = System.nanoTime();
        int i = 0;
        try {
            for (i = 0; i < mStepMoves.length; i ++) {
                if (mStepMoves[i] != null) {
                    long st = System.nanoTime();
                    int bufferLength = mStepMoves[i].toByteBuffer(byteBuffer, 0);
                    long en = System.nanoTime();
                    if (i % 100 == 0) {
                        Log.d(TAG, mStepName + "[" + i + "], toString time = " + ((en - st) * 1e-6));
                        //Log.d(TAG, new String(byteBuffer, 0, bufferLength));
                    }
                    DatagramPacket packet = new DatagramPacket(byteBuffer, bufferLength, mUnityAddr, mUnityPort);
                    try {
                        mUnitySocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                long currentTime = System.nanoTime();
                long sleepTime = Math.max((i + 1) * frameDuration - (currentTime - startTime) / 1000000, 0);
                Thread.sleep(sleepTime);
            }
            Log.d(TAG, "send finish.");
        } catch (InterruptedException e) {
            Log.d(TAG, "send interrupted at [" + i + "] in " + mStepMoves.length);
            e.printStackTrace();
        }
    }

    public static void sendTerminal() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mUnitySocket == null) {
                    try {
                        mUnityPort = 12345;
                        mUnitySocket = new DatagramSocket();
                        mUnityAddr = InetAddress.getByName("127.0.0.1");
                    } catch (SocketException | UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                if (SendMoveThread.SEND_THREAD != null && SendMoveThread.SEND_THREAD.isAlive()) {
                    SendMoveThread.SEND_THREAD.interrupt();
                    try {
                        SendMoveThread.SEND_THREAD.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                byte data[] = termin.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, mUnityAddr, mUnityPort);
                try {
                    mUnitySocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
