package anidance.anidance_android.MfccPackage;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import anidance.anidance_android.beats.Beats;

public class RecorderAnalyzer {

    public static String TAG = "RecorderAnalyzer";

    public static int NEAREST_SECONDS = 10;

    private MFCC mMfcc;
    private int mSampleRate;
    private byte mLastByte;

    private double[] mNearestSample;//最近10秒的数据
    private int mNearestLength;
    private Beats mBeats;

    public RecorderAnalyzer(int sampleRate) {
        mMfcc = new MFCC();
        mSampleRate = sampleRate;
        mBeats = new Beats();
        mNearestSample = new double[sampleRate * NEAREST_SECONDS];
        mNearestLength = 0;
        mLastByte = 0;
    }

    //对最近5秒的数据进行能量检查，满足条件就开始分析
    public boolean hasEnoughEnergy() {
        double avg = 0;
        int judgeLength = mSampleRate * 2;
        for (int i = mNearestLength - judgeLength; i < mNearestLength; i ++) {
            avg += Math.abs(mNearestSample[i]);
        }
        avg = avg / judgeLength;
        Log.d(TAG, "hasEnoughEnergy:  judgeLength = " + judgeLength + ", avg = " + avg);
        return avg > 0.05;
    }

    //对最近5秒的数据进行能量检查，不满足条件就开始停止
    public boolean notHasEnoughEnergy() {
        double avg = 0;
        int judgeLength = Math.min(mNearestLength, mSampleRate * 10);
        for (int i = mNearestLength - judgeLength; i < mNearestLength; i ++) {
            avg += Math.abs(mNearestSample[i]);
        }
        avg = avg / judgeLength;
        Log.d(TAG, "notHasEnoughEnergy:  judgeLength = " + judgeLength + ", avg = " + avg);
        return avg < 0.02;
    }

    //插入数据
    public void skip(byte[] wave, int length) {
        //获取浮点音频数据
        double[] sample = waveToSample(wave, length);
        //处理节奏缓冲区
        copySample(sample);
    }

    //分析最近的duration秒的数据
    public double[] analysisNearest(double duration) {
        int durationFrame = (int) Math.round(duration * mSampleRate);
        //获取浮点音频数据
        double[] sample = new double[durationFrame];
        for (int i = 0; i < durationFrame; i ++) {
            sample[i] = mNearestSample[mNearestLength - durationFrame + i];
        }
        //计算MFCC
        double[][] mfcc = mMfcc.process(sample);
        double[] maskingMfcc = masking(mfcc);
        return maskingMfcc;
    }

    //分析节拍
    public Beats.BeatsGener analysisBeats() {
        Beats.BeatsGener beatsGener =  mBeats.analysisBeats(mNearestSample, mNearestLength, mSampleRate);
        double nearestDuration = ((double)mNearestLength) / mSampleRate;
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

    //输入数据的差分浮点化
    private double[] waveToSample(byte[] wave, int length) {
        double[] sample = new double[length];
        sample[0] = wave[0] - mLastByte;
        double tmp = 1.0 / 256;
        for (int i = 1; i < length; i ++) {
            sample[i] = (wave[i] - wave[i - 1]) * tmp;
        }
        mLastByte = wave[length - 1];
        return sample;
    }

    //尾部Masking
    private double[] masking(double[][] mfcc) {
        double[] maskingMfcc = new double[mfcc.length];
        int maskingSize = Math.min(mfcc[0].length, 10);
        for (int i = 0; i < maskingMfcc.length; i ++) {
            maskingMfcc[i] = 0;
            for (int j = mfcc[0].length - maskingSize; j < mfcc[0].length; j ++) {
                maskingMfcc[i] += mfcc[i][j];
            }
            maskingMfcc[i] /= maskingSize;
        }
        return maskingMfcc;
    }
}
