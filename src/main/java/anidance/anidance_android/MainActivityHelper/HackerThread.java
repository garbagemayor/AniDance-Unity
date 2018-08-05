package anidance.anidance_android.MainActivityHelper;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class HackerThread extends Thread {

    public static String TAG = "HackerThread";

    private DatagramSocket mUnitySocket;
    private InetAddress mUnityAddr;
    private int mUnityPort;

    public void run() {
        try {
            try {
                mUnityPort = 12345;
                mUnitySocket = new DatagramSocket();
                mUnityAddr = InetAddress.getByName("127.0.0.1");
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
            String messageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AniDance/socket_message.txt";
            FileInputStream fis = new FileInputStream(messageFilePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                String line = br.readLine();
                while (line != null) {
                    byte data[] = line.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, mUnityAddr, mUnityPort);
                    mUnitySocket.send(packet);
                    line = br.readLine();
                    Thread.sleep(40);//25fps
                }
                br.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
