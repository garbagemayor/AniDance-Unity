package anidance.anidance_android.MfccPackage;

public class MyMatmul {

    public static String TAG = "MyMatmul";
    public static int THREAD_COUNT = 8;

    private double[][] mMatA;
    private double[][] mMatB;
    private double[][] mMatC;
    private Thread[] mThreads;

    public MyMatmul(double[][] matA, double[][] matB) {
        mMatA = matA;
        mMatB = matB;
        mMatC = new double[matA.length][matB[0].length];
        mThreads = new Thread[matA.length];
        for (int i = 0, j = 0; i < mThreads.length; i ++) {
            final int ii = i;
            mThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < mMatB.length; k ++) {
                        double tmpA = mMatA[ii][k];
                        double[] tmpB = mMatB[k];
                        double[] tmpC = mMatC[ii];
                        for (int j = 0; j < mMatB[0].length; j ++) {
                            tmpC[j] += tmpA * tmpB[j];
                        }
                    }
                }
            });
            mThreads[i].start();
            if (i + 1 < mThreads.length){
                if (i - j + 1 >= THREAD_COUNT) {
                    try {
                        mThreads[j].join();
                        j++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                while (j < mThreads.length) {
                    try {
                        mThreads[j].join();
                        j ++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public double[][] getResult() {
        try{
            for (int i = 0; i < mThreads.length; i ++) {
                mThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mMatC;
    }
}
