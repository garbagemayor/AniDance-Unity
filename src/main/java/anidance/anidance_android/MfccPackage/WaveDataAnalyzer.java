package anidance.anidance_android.MfccPackage;

import android.util.Log;

import java.io.File;

public class WaveDataAnalyzer {

    public static String TAG = "WaveDataAnalyzer";

    public static double[][] analysis(String filePath) {
        //读一个通道的波形
        double oneChannelSample[] = readOneChannelSample(filePath);
        //算mfcc
        MFCC mfcc = new MFCC();
        double[][] mfccResult = mfcc.process(oneChannelSample);
        

        return mfccResult;
    }


    private static double[] readOneChannelSample(String filePath) {
        try {
            WavFile wavFile = WavFile.openWavFile(new File(filePath));
            double[] sample = wavFile.readAllSample()[0];
            wavFile.close();
            return sample;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
