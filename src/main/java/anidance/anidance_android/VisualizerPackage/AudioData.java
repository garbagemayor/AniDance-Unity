/**
 * Copyright 2011, Felix Palmer
 * <p>
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package anidance.anidance_android.VisualizerPackage;

// Data class to explicitly indicate that these bytes are raw audio data
public class AudioData {
    public AudioData(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] bytes;
}
