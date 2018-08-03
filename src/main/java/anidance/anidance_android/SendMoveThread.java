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

public class SendMoveThread extends Thread {

    public static String TAG = "SendMoveThread";

    private static String termin = "{\"skeletons\": [{\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}, {\"z\": 10000, \"x\": 10000, \"y\": 10000}], \"center\": {\"z\": 10000, \"x\": 10000, \"y\": 10000}}";
    private String mStepName;
    private double mStepDuration;
    private List<String> mStepMoves;

    private static DatagramSocket mUnitySocket;
    private static InetAddress mUnityAddr;
    private static int mUnityPort;

    public static SendMoveThread SEND_THREAD = null;

    public SendMoveThread(String stepName, double stepDuration) {
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
        if (mStepMoves == null) {
            mStepMoves = new ArrayList<>();
            mStepMoves.add("");
        }
    }

    @Override
    public void run() {
        long frameDuration = (long) Math.floor(mStepDuration * 1000 / mStepMoves.size() * 0.98);
        try {
            for (int i = 0; i < mStepMoves.size(); i ++) {
                if (i % 25 == 0) {
                    Log.d(TAG, mStepName + "[" + i + "]");
                }
                if (!mStepMoves.get(i).equals("")) {
                    byte data[] = mStepMoves.get(i).getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, mUnityAddr, mUnityPort);
                    try {
                        mUnitySocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(frameDuration);
            }
            Log.d(TAG, "send finish.");
        } catch (InterruptedException e) {
            Log.d(TAG, "send interrupted.");
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
