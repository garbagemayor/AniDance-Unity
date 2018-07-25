package anidance.anidance_android;

public class WaveToMfcc {

    public static String TAG = "WaveToMfcc";

    private static int INPUT_MAX_SIZE = 1 << 20;
    private static int FRM_LEN = 256;
    private static int FRM_SFT = 80;
    private static int MIN_WORD_LEN = 15;
    private static int MAX_SLIENCE_LEN = 8;
    private static int LEN_PF_MFCC = 256;
    private static int SIZE_X_X_MFCC = 32;
    private static int SIZE_X_Y_MFCC = 140;
    private static int FS_MFCC = 8000;//HZ频率
    private static int N_MFCC = 256;//FFT的长度
    private static int SIZE_DCT_X_MFCC = 13;//dct系数二维数组x大小
    private static int SIZE_DCT_Y_MFCC = 25;//dct系数二维数组y大小
    private static double FH_MFCC = 0.5;
    private static double FL_MFCC = 0;
    private static int P_MFCC = 24;

    private double[] inputWave;
    private int inputSize;

    private double[] fpData;
    private int FrmNum;
    private AudFrame[] audFrame;
    private float[] fltHamm;
    private double fltZcrVadThresh;
    private double fltSteThresh[];
    private double dwZcrThresh[];
    private int WavStart;
    private int WavEnd;
    private double[] working;
    private double[] workingi;
    private double bank[][];
    private double[][] dctcoef;
    private double hamming[];
    private int Pos;
    private double stren_win[];
    private double[] result;

    private double[][] outputMfcc;

    public WaveToMfcc() {
        inputWave = new double[INPUT_MAX_SIZE];
        inputSize = 0;
        working = new double[257];
        workingi = new double[257];
    }

    public void pushData(byte[] data) {
        for (byte x : data) {
            if (inputSize == INPUT_MAX_SIZE) {
                break;
            }
            inputWave[inputSize] = (double) x;
            inputSize++;
        }
    }

    public void calculate() {
        inputNormalization();
        audPreEmphasize();
        audEnframe();
        Hamming();
        audHamming();
        audSte();
        audZcr();
        audNoiseEstimate();
        audVadEstimate();
        audOutput();
    }

    //初始化：mel滤波器组系数，求mel滤波器组系数，并且对其进行归一化,主要参数P_MFCC，LEN_PF_MFCC，FS_MFCC，FH_MFCC，FL_MFCC
    public void melbank() {
        double f0, fn2, lr;
        int b1, b2, b3, b4, k2, k3, k4, mn, mx;
        double bl[] = new double[5];
        double pf[] = new double[LEN_PF_MFCC];
        double fp[] = new double[LEN_PF_MFCC];
        double pm[] = new double[LEN_PF_MFCC];
        double v[] = new double[LEN_PF_MFCC];
        int r[] = new int[LEN_PF_MFCC];
        int c[] = new int[LEN_PF_MFCC];
        int i, j;
        bank = new double[SIZE_X_X_MFCC][SIZE_X_Y_MFCC];
        f0 = (double) 700 / (double) FS_MFCC;
        fn2 = Math.floor((double) N_MFCC / 2);
        lr = Math.log((f0 + FH_MFCC) / (f0 + FL_MFCC)) / (P_MFCC + 1.0);
        bl[1] = N_MFCC * ((f0 + FL_MFCC) * Math.exp(0 * lr) - f0);
        bl[2] = N_MFCC * ((f0 + FL_MFCC) * Math.exp(1 * lr) - f0);
        bl[3] = N_MFCC * ((f0 + FL_MFCC) * Math.exp(P_MFCC * lr) - f0);
        bl[4] = N_MFCC * ((f0 + FL_MFCC) * Math.exp((P_MFCC + 1) * lr) - f0);
        b2 = (int) Math.ceil(bl[2]);
        b3 = (int) Math.floor(bl[3]);
        b1 = (int) Math.floor(bl[1]) + 1;
        b4 = (int) Math.min(fn2, Math.ceil(bl[4])) - 1;
        k2 = b2 - b1 + 1;
        k3 = b3 - b1 + 1;
        k4 = b4 - b1 + 1;
        mn = b1 + 1;
        mx = b4 + 1;
        for (i = 1, j = b1; j <= b4; i++, j++) {
            pf[i] = Math.log(((double) f0 + (double) i / (double) N_MFCC) / (f0 + FL_MFCC)) / lr;
            fp[i] = Math.floor(pf[i]);
            pm[i] = pf[i] - fp[i];
        }
        for (i = 1, j = k2; j <= k4; i++, j++) {
            r[i] = (int) fp[j];
            c[i] = j;
            v[i] = 2 * (1 - pm[j]);
        }
        for (j = 1; j <= k3; j++, i++) {
            r[i] = 1 + (int) fp[j];
            c[i] = j;
            v[i] = 2 * pm[j];
        }
        for (j = 1; j < i; j++) {
            v[j] = 1 - 0.92 / 1.08 * Math.cos(v[j] * Math.PI / 2);
            bank[r[j]][c[j] + mn - 1] = v[j];
        }

        //bank=bank/max(bank(:));
        double buf = 0;
        for (i = 1; i <= 24; i++) {
            for (j = 1; j <= 129; j++) {
                if (bank[i][j] > buf) {
                    buf = bank[i][j];
                }
            }
        }

        for (i = 1; i <= 24; i++) {
            for (j = 1; j <= 129; j++) {
                bank[i][j] = bank[i][j] / buf;
            }
        }


    }

    //初始化：归一化倒谱提升窗口
    public void calStrenWin() {
        stren_win = new double[13];
        double b = 0.0;
        for (int i = 1; i <= 12; i++) {
            stren_win[i] = 1 + 6 * Math.sin(Math.PI * (double) i / (double) 12);
            if (b < stren_win[i]) {
                b = stren_win[i];
            }
        }
        for (int i = 1; i <= 12; i++) {
            stren_win[i] = stren_win[i] / b;
        }
    }

    //处理过程：正规化
    private void inputNormalization() {
        double maxValue = 0;
        for (double x : inputWave) {
            maxValue = Math.max(maxValue, Math.abs(x));
        }
        maxValue = 1.0 / maxValue;
        for (int i = 0; i < inputWave.length; i++) {
            inputWave[i] *= maxValue;
        }
    }

    //处理过程：预加重
    private void audPreEmphasize() {
        fpData = new double[inputSize];
        fpData[0] = inputWave[0];
        for (int i = 1; i < inputSize; i++) {
            fpData[i] = (double) (inputWave[i]) - (double) (inputWave[i - 1]) * 0.9375;
        }
    }

    //处理过程：截断
    public class AudFrame {
        double[] fltFrame;
        double fltSte;
        int dwZcr;
        boolean blVad;
        int AudFrmNext;

    }

    private void audEnframe() {
        FrmNum = (inputSize - (FRM_LEN - FRM_SFT)) / FRM_SFT;
        audFrame = new AudFrame[FrmNum];
        for (int i = 0; i < FrmNum; i++) {
            audFrame[i] = new AudFrame();
        }
        int x = 0;
        for (int i = 0; i < FrmNum; i++) {
            audFrame[i].fltFrame = new double[FRM_LEN];
            for (int j = 0; j < FRM_LEN; j++) {
                audFrame[i].fltFrame[j] = inputWave[x + j];
            }
            x += FRM_SFT;
        }
    }

    //处理过程：算汉明窗
    private void Hamming() {
        fltHamm = new float[FRM_LEN];
        for (int i = 0; i < FRM_LEN; i++) {
            fltHamm[i] = fltHamm[i] = (float) (0.54 - 0.46 * Math.cos((2 * i * Math.PI) / (FRM_LEN - 1)));
        }
    }

    //处理过程：加汉明窗
    private void audHamming() {
        for (int i = 0; i < FrmNum; i++) {
            for (int j = 0; i < FRM_LEN; i++) {
                audFrame[i].fltFrame[j] *= fltHamm[j];
            }
        }
    }

    //处理过程：每段算能量
    private void audSte() {
        for (int i = 0; i < FrmNum; i++) {
            double fltShortEnergy = 0;
            for (int j = 0; j < FRM_LEN; j++) {
                fltShortEnergy += Math.abs(audFrame[i].fltFrame[j]);
            }
            audFrame[i].fltSte = fltShortEnergy;
        }
    }

    //处理过程：不知道
    private void audZcr() {
        fltZcrVadThresh = 0.02;
        for (int i = 0; i < FrmNum; i++) {
            int dwZcrRate = 0;
            for (int j = 0; j < FRM_LEN - 1; j++) {
                if ((audFrame[i].fltFrame[j] * audFrame[i].fltFrame[j + 1] < 0) && ((audFrame[i].fltFrame[j] - audFrame[i].fltFrame[j + 1]) > fltZcrVadThresh)) {
                    dwZcrRate++;
                }
            }
            audFrame[i].dwZcr = dwZcrRate;
        }
    }

    //处理过程：噪声
    private void audNoiseEstimate() {
        fltSteThresh = new double[2];
        dwZcrThresh = new double[2];
//		int ZcrThresh = 0;	//��������ֵ
//		double StrThresh =0.0;	//��ʱ������ֵ
//		int NoiseFrmLen = 0;
//		for(int i = 0; i < FrmNum; i++)
//		{
//			ZcrThresh += audFrame[i].dwZcr;
//			StrThresh += audFrame[i].fltSte;
//			NoiseFrmLen++;
//		}
//		dwZcrThresh[0] = (double)(ZcrThresh) / NoiseFrmLen;
//		dwZcrThresh[1] = (double)(ZcrThresh) / NoiseFrmLen*2.5;
//		fltSteThresh[0] = (double)StrThresh / NoiseFrmLen*0.7;
//		fltSteThresh[1] = (double)(StrThresh / NoiseFrmLen)*0.5;//*0.95;
        dwZcrThresh[0] = 10;
        dwZcrThresh[1] = 5;
        fltSteThresh[0] = 10;
        fltSteThresh[1] = 2;
        double maxSte = 0;
        for (int i = 0; i < FrmNum; i++) {
            if (maxSte < audFrame[i].fltSte) {
                maxSte = audFrame[i].fltSte;
            }
        }
        fltSteThresh[0] = fltSteThresh[0] < (maxSte / 4) ? fltSteThresh[0] : (maxSte / 4);
        fltSteThresh[1] = fltSteThresh[1] < (maxSte / 8) ? fltSteThresh[1] : (maxSte / 8);
    }

    private void audVadEstimate() {
        //Extract Threshold
        double ZcrLow = dwZcrThresh[1];
        double ZcrHigh = dwZcrThresh[0];
        double AmpLow = fltSteThresh[1];
        double AmpHigh = fltSteThresh[0];
        WavStart = 0;
        WavEnd = 0;
        int status = 0;
        int count = 0;
        int silence = 0;
        for (int i = 0; i < FrmNum; i++) {
            switch (status) {
                case 0:
                case 1:
                    if ((audFrame[i].fltSte) > AmpHigh) {
                        WavStart = (i - count - 1) > 1 ? (i - count - 1) : 1;
                        status = 2;
                        silence = 0;
                        count = count + 1;
                    } else if ((audFrame[i].fltSte) > AmpLow || (audFrame[i].dwZcr) > ZcrLow) {
                        status = 1;
                        count = count + 1;
                    } else {
                        status = 0;
                        count = 0;
                    }
                    break;
                case 2: //Speech Section
                    if ((audFrame[i].fltSte > AmpLow) || (audFrame[i].dwZcr > ZcrLow)) {
                        count = count + 1;
                        //WavEnd=i-Silence;
                    } else {
                        silence = silence + 1;
                        if (silence < MAX_SLIENCE_LEN) {
                            count = count + 1;
                        } else if (count < MIN_WORD_LEN) {
                            status = 0;
                            silence = 0;
                            count = 0;
                        } else {
                            status = 3;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        count = count - silence / 2;
        WavEnd = WavStart + count - 1;
    }

    //处理过程：最终结果
    private void audOutput() {
        outputMfcc = new double[FrmNum][24];
        int index1 = 0;
        for (int i = WavStart; i <= WavEnd; i++) {
            for (int j = 0; j < 257; j++) {
                working[j] = (double) fpData[i * 128 + j];
            }

            proc();
            if (i - WavStart > 1 && WavEnd - i > 1) {
                for (int j = 1; j < 25; j++) {
                    outputMfcc[index1][j - 1] = result[j];
                }
                index1++;
            }
        }
    }

    private void proc() {
        int i, j;
        double sum;
        double[] proc_buf1 = new double[25];
        double[][] buf = new double[5][25];
        result = new double[25];
        double[] result = this.result;
        double[] working = this.working;
        for (i = 256; i >= 1; i--) {
            working[i] = working[i] - working[i - 1];
        }
        for (i = 1; i <= 256; i++) {
            //加窗——//s=y'.*hamming(256); 同时错一格。
            working[i - 1] = working[i] * hamming[i];
        }
        //快速傅里叶变换——//t=abs(fft(s));
        fft();
        for (i = 256; i >= 1; i--) {
            working[i] = working[i - 1] * working[i - 1] + workingi[i - 1] * workingi[i - 1];
            workingi[i - 1] = 0;
        }
        for (i = 1; i <= 24; i++) {
            sum = 0;
            for (j = 1; j <= 129; j++) {
                sum = sum + bank[i][j] * working[j];
            }
            if (sum != 0)//求对数
            {
                proc_buf1[i] = Math.log(sum);
            } else {
                proc_buf1[i] = -3000;
            }
        }
        for (i = 1; i <= 12; i++) {
            sum = 0;
            for (j = 1; j <= 24; j++) {
                sum = sum + dctcoef[i][j] * proc_buf1[j]; //dctcoef是DCT系数
            }
            buf[Pos][i] = sum * stren_win[i];
        }
        Pos = (Pos + 1) % 5;
        for (i = 1; i <= 12; i++) {
            result[i] = buf[(Pos + 2) % 5][i];
            //dtm(i,:)=-2*m(i-2,:)-m(i-1,:)+m(i+1,:)+2*m(i+2,:);
            result[i + 12] = -2 * buf[(Pos) % 5][i] - buf[(Pos + 1) % 5][i] + buf[(Pos + 3) % 5][i] + 2 * buf[(Pos + 4) % 5][i];
            result[i + 12] = result[i + 12] / 3;
        }
    }

    //处理过程：快速傅里叶变换
    private void fft() {
        int i, j = 0, k, n, l, le, le1, ip, sign = -1;
        double tr, ti, ur, ui, wr, wi;
        n = 1 << 8; //n = 256
        for (i = 0; i < n - 1; i++) {
            if (i < j) {
                tr = working[j];
                working[j] = working[i];
                working[i] = tr;
            }
            k = n >> 1;
            while (k <= j) {
                j -= k;
                k = k >> 1;
            }
            j += k;
        }
        for (l = 1; l <= 8; l++) {
            le = 1 << l;
            le1 = le / 2;
            ur = 1.;
            ui = 0.;
            wr = Math.cos(Math.PI / le1);
            wi = -Math.sin(Math.PI / le1);
            for (j = 0; j < le1; j++) {
                for (i = j; i < n; i += le) {
                    ip = i + le1;
                    tr = working[ip] * ur - workingi[ip] * ui;
                    ti = working[ip] * ui + workingi[ip] * ur;
                    working[ip] = working[i] - tr;
                    workingi[ip] = workingi[i] - ti;
                    working[i] = working[i] + tr;
                    workingi[i] = workingi[i] + ti;
                }
                tr = ur * wr - ui * wi;
                ti = ur * wi + ui * wr;
                ur = tr;
                ui = ti;
            }
        }
    }
}
