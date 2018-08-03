package anidance.anidance_android.MfccPackage;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WaveFileReader {

    public static String TAG = "WaveFileReader";

    private enum IOState {READING, WRITING, CLOSED}

    private final static int BUFFER_SIZE = 16 << 20;

    private final static int FMT_CHUNK_ID = 0x20746D66;
    private final static int DATA_CHUNK_ID = 0x61746164;
    private final static int RIFF_CHUNK_ID = 0x46464952;
    private final static int RIFF_TYPE_ID = 0x45564157;

    private File file;                        // File that will be read from or written to
    private IOState ioState;                // Specifies the IO State of the Wav File (used for snaity checking)
    private int bytesPerSample;            // Number of bytes required to store a single sample
    private long numFrames;                    // Number of frames within the data section
    private FileOutputStream oStream;    // Output stream used for writting data
    private FileInputStream iStream;        // Input stream used for reading data
    private double floatScale;                // Scaling factor used for int <-> float conversion
    private double floatOffset;            // Offset factor used for int <-> float conversion
    private boolean wordAlignAdjust;        // Specify if an extra byte at the end of the data chunk is required for word alignment

    // Wav Header
    private int numChannels;                // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private long sampleRate;                // 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF (4,294,967,295)
    // Although a java int is 4 bytes, it is signed, so need to use a long
    private int blockAlign;                    // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private int validBits;                    // 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)

    // Buffering
    private byte[] buffer;                    // Local buffer used for IO
    private int bufferPointer;                // Points to the current position in local buffer
    private int bytesRead;                    // Bytes read after last read into local buffer
    private long frameCounter;                // Current number of frames read or written

    //当前读取位置
    private byte[] mDataBuffer;//所有波形数据
    private int mDataPointer;
    private int mCurrentFrame;//当前读了多少帧
    private double mNow;//当前读了多少秒

    // Cannot instantiate WaveFileReader directly, must either use newWavFile() or openWavFile()
    public WaveFileReader() {
        buffer = new byte[BUFFER_SIZE];
    }

    public int getNumChannels() {
        return numChannels;
    }

    public long getNumFrames() {
        return numFrames;
    }

    public long getFramesRemaining() {
        return numFrames - frameCounter;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public int getValidBits() {
        return validBits;
    }

    public static WaveFileReader openWavFile(File file) throws IOException, WavFileException {
        // Instantiate new Wavfile and store the file reference
        WaveFileReader waveFileReader = new WaveFileReader();
        waveFileReader.file = file;

        // Create a new file input stream for reading file data
        waveFileReader.iStream = new FileInputStream(file);

        // Read the first 12 bytes of the file
        int bytesRead = waveFileReader.iStream.read(waveFileReader.buffer, 0, 12);
        if (bytesRead != 12) {
            throw new WavFileException("Not enough wav file bytes for header");
        }

        // Extract parts from the header
        long riffChunkID = getLE(waveFileReader.buffer, 0, 4);
        long chunkSize = getLE(waveFileReader.buffer, 4, 4);
        long riffTypeID = getLE(waveFileReader.buffer, 8, 4);

        // Check the header bytes contains the correct signature
        if (riffChunkID != RIFF_CHUNK_ID) {
            throw new WavFileException("Invalid Wav Header data, incorrect riff chunk ID");
        }
        if (riffTypeID != RIFF_TYPE_ID) {
            throw new WavFileException("Invalid Wav Header data, incorrect riff type ID");
        }

        // Check that the file size matches the number of bytes listed in header
        if (file.length() != chunkSize + 8) {
            throw new WavFileException("Header chunk size (" + chunkSize + ") does not match file size (" + file.length() + ")");
        }

        boolean foundFormat = false;
        boolean foundData = false;

        // Search for the Format and Data Chunks
        while (true) {
            // Read the first 8 bytes of the chunk (ID and chunk size)
            bytesRead = waveFileReader.iStream.read(waveFileReader.buffer, 0, 8);
            if (bytesRead == -1) {
                throw new WavFileException("Reached end of file without finding format chunk");
            }
            if (bytesRead != 8) {
                throw new WavFileException("Could not read chunk header");
            }

            // Extract the chunk ID and Size
            long chunkID = getLE(waveFileReader.buffer, 0, 4);
            chunkSize = getLE(waveFileReader.buffer, 4, 4);

            // Word align the chunk size
            // chunkSize specifies the number of bytes holding data. However,
            // the data should be word aligned (2 bytes) so we need to calculate
            // the actual number of bytes in the chunk
            long numChunkBytes = (chunkSize % 2 == 1) ? chunkSize + 1 : chunkSize;

            if (chunkID == FMT_CHUNK_ID) {
                // Flag that the format chunk has been found
                foundFormat = true;

                // Read in the header info
                bytesRead = waveFileReader.iStream.read(waveFileReader.buffer, 0, 16);

                // Check this is uncompressed data
                int compressionCode = (int) getLE(waveFileReader.buffer, 0, 2);
                if (compressionCode != 1) {
                    throw new WavFileException("Compression Code " + compressionCode + " not supported");
                }

                // Extract the format information
                waveFileReader.numChannels = (int) getLE(waveFileReader.buffer, 2, 2);
                waveFileReader.sampleRate = getLE(waveFileReader.buffer, 4, 4);
                waveFileReader.blockAlign = (int) getLE(waveFileReader.buffer, 12, 2);
                waveFileReader.validBits = (int) getLE(waveFileReader.buffer, 14, 2);

                if (waveFileReader.numChannels == 0) {
                    throw new WavFileException("Number of channels specified in header is equal to zero");
                }
                if (waveFileReader.blockAlign == 0) {
                    throw new WavFileException("Block Align specified in header is equal to zero");
                }
                if (waveFileReader.validBits < 2) {
                    throw new WavFileException("Valid Bits specified in header is less than 2");
                }
                if (waveFileReader.validBits > 64) {
                    throw new WavFileException("Valid Bits specified in header is greater than 64, this is greater than a long can hold");
                }

                // Calculate the number of bytes required to hold 1 sample
                waveFileReader.bytesPerSample = (waveFileReader.validBits + 7) / 8;
                if (waveFileReader.bytesPerSample * waveFileReader.numChannels != waveFileReader.blockAlign) {
                    throw new WavFileException("Block Align does not agree with bytes required for validBits and number of channels");
                }

                // Account for number of format bytes and then skip over
                // any extra format bytes
                numChunkBytes -= 16;
                if (numChunkBytes > 0) {
                    waveFileReader.iStream.skip(numChunkBytes);
                }
            } else if (chunkID == DATA_CHUNK_ID) {
                // Check if we've found the format chunk,
                // If not, throw an exception as we need the format information
                // before we can read the data chunk
                if (foundFormat == false) {
                    throw new WavFileException("Data chunk found before Format chunk");
                }

                // Check that the chunkSize (wav data length) is a multiple of the
                // block align (bytes per frame)
                if (chunkSize % waveFileReader.blockAlign != 0) {
                    throw new WavFileException("Data Chunk size is not multiple of Block Align");
                }

                // Calculate the number of frames
                waveFileReader.numFrames = chunkSize / waveFileReader.blockAlign;
                Log.d(TAG, "blockAlign = " + waveFileReader.blockAlign);
                Log.d(TAG, "chunkSize = " + chunkSize);
                Log.d(TAG, "numFrames = " + waveFileReader.numFrames);

                // Flag that we've found the wave data chunk
                foundData = true;

                break;
            } else {
                // If an unknown chunk ID is found, just skip over the chunk data
                waveFileReader.iStream.skip(numChunkBytes);
            }
        }

        // Throw an exception if no data chunk has been found
        if (foundData == false) {
            throw new WavFileException("Did not find a data chunk");
        }

        // Calculate the scaling factor for converting to a normalised double
        if (waveFileReader.validBits > 8) {
            // If more than 8 validBits, data is signed
            // Conversion required dividing by magnitude of max negative value
            waveFileReader.floatOffset = 0;
            waveFileReader.floatScale = 1 << (waveFileReader.validBits - 1);
        } else {
            // Else if 8 or less validBits, data is unsigned
            // Conversion required dividing by max positive value
            waveFileReader.floatOffset = -1;
            waveFileReader.floatScale = 0.5 * ((1 << waveFileReader.validBits) - 1);
        }
        Log.d(TAG, "floatOffset = " + waveFileReader.floatOffset);
        Log.d(TAG, "floatScale = " + waveFileReader.floatScale);

        waveFileReader.bufferPointer = 0;
        waveFileReader.bytesRead = 0;
        waveFileReader.frameCounter = 0;
        waveFileReader.ioState = IOState.READING;

        //我的代码！
        waveFileReader.mDataBuffer =  new byte[(int) (waveFileReader.numFrames * waveFileReader.blockAlign)];
        waveFileReader.mDataPointer = 0;
        waveFileReader.mCurrentFrame = 0;
        waveFileReader.mNow = 0;
        waveFileReader.iStream.read(waveFileReader.mDataBuffer);

        return waveFileReader;
    }

    // Get and Put little endian data from local buffer
    // ------------------------------------------------
    private static long getLE(byte[] buffer, int pos, int numBytes) {
        numBytes--;
        pos += numBytes;

        long val = buffer[pos] & 0xFF;
        for (int b = 0; b < numBytes; b++) {
            val = (val << 8) + (buffer[--pos] & 0xFF);
        }

        return val;
    }

    public void close() throws IOException {
        // Close the input stream and set to null
        if (iStream != null) {
            iStream.close();
            iStream = null;
        }

        if (oStream != null) {
            // Write out anything still in the local buffer
            if (bufferPointer > 0) {
                oStream.write(buffer, 0, bufferPointer);
            }

            // If an extra byte is required for word alignment, add it to the end
            if (wordAlignAdjust) {
                oStream.write(0);
            }

            // Close the stream and set to null
            oStream.close();
            oStream = null;
        }

        // Flag that the stream is closed
        ioState = IOState.CLOSED;
    }

    public static class WavFileException extends Exception {
        public WavFileException() {
            super();
        }

        public WavFileException(String message) {
            super(message);
        }

        public WavFileException(String message, Throwable cause) {
            super(message, cause);
        }

        public WavFileException(Throwable cause) {
            super(cause);
        }
    }

    //下面是我写的！！！
    public int getCurrentFrame() {
        return mCurrentFrame;
    }

    public boolean hasNext(int durationFrame) {
        return mCurrentFrame + durationFrame <= numFrames;
    }

    public double[][] readNext(int durationFrame) {
        durationFrame = (int) Math.min(durationFrame, numFrames - mCurrentFrame);
        double[][] sample = new double[numChannels][durationFrame];
        double tmp = 1.0 / floatScale;
        for (int i = 0; i < durationFrame; i ++) {
            for (int j = 0; j < numChannels; j ++) {
                long val = 0;
                for (int k = 0; k < bytesPerSample; k++) {
                    long v = mDataBuffer[mDataPointer ++];
                    v = (k < bytesPerSample - 1 || bytesPerSample == 1) ? v & 0xff : v;
                    val |= v << (k << 3);
                }
                sample[j][i] = val * tmp + floatOffset;
            }
        }
        mCurrentFrame += durationFrame;
        mNow = ((double) mCurrentFrame) / sampleRate;
        return sample;
    }

    public void skip(int durationFrame) {
        if (durationFrame > 0) {
            durationFrame = (int) Math.min(durationFrame, numFrames - mCurrentFrame);
            mCurrentFrame += durationFrame;
            mDataPointer += durationFrame * numChannels * bytesPerSample;
        }
    }

    public void resetHead() {
        mCurrentFrame = 0;
        mDataPointer = 0;
    }
}
