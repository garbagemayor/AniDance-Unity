/**
 * Copyright 2011, Felix Palmer
 * <p>
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package anidance.anidance_android.VisualizerPackage;

// Data class to explicitly indicate that these bytes are the FFT of audio data
public class FFTData {
    public FFTData(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] bytes;
}
