package anidance.anidance_android.MfccPackage;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import anidance.anidance_android.beats.Beats;

public class WaveFileAnalyzer {

    public static String TAG = "WaveFileAnalyzer";

    public static int NEAREST_SECONDS = 10;

    private WaveFileReader mReader;
    private MFCC mMfcc;

    private double[] mNearestSample;//最近10秒的数据
    private int mNearestLength;
    private Beats mBeats;

    public WaveFileAnalyzer(String filePath) {
        mMfcc = new MFCC();
        mBeats = new Beats();
        try {
            mReader = WaveFileReader.openWavFile(new File(filePath));
        } catch (IOException | WaveFileReader.WavFileException e) {
            e.printStackTrace();
        }
        mNearestSample = new double[(int) (mReader.getSampleRate() * NEAREST_SECONDS)];
        mNearestLength = 0;
    }

    public WaveFileReader getReader() {
        return mReader;
    }

    //获取当前已读部分的时长，单位秒
    public double getCurrentTime() {
        int currentFrame = mReader.getCurrentFrame();
        return ((double) currentFrame) / mReader.getSampleRate();
    }

    //分析接下来的duration秒
    public double[] analysisNext(double duration) {
        int durationFrame = (int) Math.round(duration * mReader.getSampleRate());
        //获取音频数据
        double[] sample = mReader.readNext(durationFrame)[0];
        Log.d(TAG, "sample.length = " + sample.length);
        //处理节奏缓冲区
        copySample(sample);
        //计算MFCC
        double[][] mfcc = mMfcc.process(sample);
        double[] maskingMfcc = masking(mfcc);
        return maskingMfcc;
    }

    public Beats.BeatsGener analysisBeats() {
        Beats.BeatsGener beatsGener =  mBeats.analysisBeats(mNearestSample, mNearestLength, (int) mReader.getSampleRate());
        double nearestDuration = ((double)mNearestLength) / mReader.getSampleRate();
        beatsGener.move(nearestDuration);
        return beatsGener;
    }

    private void copySample(double[] sample) {
        if (mNearestLength + sample.length <= mNearestSample.length) {
            for (int i = 0; i < sample.length; i ++) {
                mNearestSample[mNearestLength + i] = sample[i];
            }
            mNearestLength += sample.length;
        } else {
            int offset = mNearestLength + sample.length - mNearestSample.length;
            for (int i = 0; i < mNearestLength - offset; i ++) {
                mNearestSample[i] = mNearestSample[i + offset];
            }
            for (int i = mNearestLength - offset; i < mNearestSample.length; i ++) {
                mNearestSample[i] = sample[i - mNearestLength + offset];
            }
        }
    }

    public boolean hasNext(double duration) {
        int durationFrame = (int) Math.round(duration * mReader.getSampleRate());
        return mReader.hasNext(durationFrame);
    }

    public void skip(double duration) {
        int durationFrame = (int) Math.round(duration * mReader.getSampleRate());
        if (durationFrame > mNearestSample.length) {
            mReader.skip(durationFrame - mNearestSample.length);
            durationFrame = mNearestSample.length;
        }
        double[] sample = mReader.readNext(durationFrame)[0];
        copySample(sample);
    }

    public void resetHead() {
        mNearestLength = 0;
        mReader.resetHead();
    }

    //加Masking
    private double[] masking(double[][] mfcc) {
        double[] maskingMfcc = new double[mfcc.length];
        int maskingSize = Math.min(mfcc[0].length, 10);
        for (int i = 0; i < maskingMfcc.length; i ++) {
            maskingMfcc[i] = 0;
            for (int j = 0; j < maskingSize; j ++) {
                maskingMfcc[i] += mfcc[i][j];
            }
            maskingMfcc[i] /= maskingSize;
        }
        return maskingMfcc;
    }
}
