package anidance.anidance_android.beats;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Beats {

    public static String TAG = "Beats";

    public static final int HALF_THRESHOLD_WINDOW_SIZE = 11;
    public static final float MULTIPLIER = 1.5f;
    public static final int FRAME_SIZE = 1024;

    public BeatsGener analysisBeats(double[] data, int length, int sampleRate) {
        double[] data2 = new double[length];
        for (int i = 0; i < length; i ++) {
            data2[i] = data[i];
        }
        float[] frameFlux = getFrameFlux(data2, sampleRate);

        //找节奏的最优等差数列
        double frameDuration = 1.0 / (((double) sampleRate) / FRAME_SIZE);
        int minBeatsFrame = (int) Math.ceil(0.5 / frameDuration);
        int maxBeatsFrame = (int) Math.floor(1.0 / frameDuration);
        int bestBf = -1;
        int bestBs = -1;
        double bestScore = Double.MIN_VALUE;
        for (int bf = minBeatsFrame; bf < maxBeatsFrame; bf ++) {
            for (int bs = 0; bs < frameFlux.length && bs < bf; bs ++) {
                int count = 0;
                double score = 0;
                for (int f = bs; f < frameFlux.length; f += bf) {
                    count ++;
                    score += frameFlux[f];
                }
                score /= count;
                if (bestScore < score) {
                    bestScore = score;
                    bestBf = bf;
                    bestBs = bs;
                }
            }
        }

        return new BeatsGener(bestBs * frameDuration, bestBf * frameDuration);
    }

    private float[] getFrameFlux(double[] data, int sampleRate) {

        FFT fft = new FFT(FRAME_SIZE, sampleRate);
        float[] samples;
        float[] spectrum = new float[FRAME_SIZE / 2 + 1];
        float[] lastSpectrum = new float[FRAME_SIZE / 2 + 1];

        //List<Float> spectralFlux = new ArrayList<Float>();
        int frameCnt = (data.length + FRAME_SIZE - 1) / FRAME_SIZE;
        float[] spectralFlux = new float[frameCnt];
        float[] threshold = new float[frameCnt];
        float[] uniqueFlux = new float[frameCnt];

        //获取每帧的特征数据
        for (int start = 0; start < data.length; start += 1024) {
            int len = Math.min(data.length - start, 1024);
            samples = new float[1024];
            for (int i = 0; i < len; ++i) {
                samples[i] = (float) data[i + start];
            }
            fft.forward(samples);
            System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
            System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);
            float flux = 0;
            for (int i = 0; i < spectrum.length; i++) {
                flux += (spectrum[i] - lastSpectrum[i]);
            }
            spectralFlux[start >> 10] = flux;
        }

        //均值降噪
        for (int i = 0; i < spectralFlux.length; i++) {
            int startFrame = Math.max(0, i - HALF_THRESHOLD_WINDOW_SIZE);
            int endFrame = Math.min(spectralFlux.length - 1, i + HALF_THRESHOLD_WINDOW_SIZE);
            float mean = 0;
            for (int j = startFrame; j <= endFrame; j++) {
                mean += spectralFlux[j];
            }
            mean /= (endFrame - startFrame + 1);
            threshold[i] = Math.abs(mean) * MULTIPLIER;
        }
        for (int i = 0; i < threshold.length; i++) {
            if (threshold[i] <= spectralFlux[i]) {
                uniqueFlux[i] = spectralFlux[i] - threshold[i];
            } else {
                uniqueFlux[i] = 0;
            }
        }
        return uniqueFlux;
    }

    public static class BeatsGener {
        public double offset;
        public double duration;

        public BeatsGener(double offset, double duration) {
            this.offset = offset;
            this.duration = duration;
        }

        public void move(double moveDuration) {
            while (offset < moveDuration) {
                offset += duration;
            }
            offset -= moveDuration;
        }
    }
}
