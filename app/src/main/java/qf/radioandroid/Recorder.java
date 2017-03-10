package qf.radioandroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by pavelkuzmin on 04/03/2017.
 */

@Deprecated
public class Recorder {

    public final int minBufferSize;
    public final AudioRecord audioRecord;

    public Recorder() {

        minBufferSize = AudioTrack.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2);
    }
}
