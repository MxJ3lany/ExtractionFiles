package com.pedro.encoder.input.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import com.pedro.encoder.audio.DataTaken;
import java.nio.ByteBuffer;

/**
 * Created by pedro on 19/01/17.
 */

public class MicrophoneManager {

  private final String TAG = "MicrophoneManager";
  private static final int BUFFER_SIZE = 4096;
  private AudioRecord audioRecord;
  private GetMicrophoneData getMicrophoneData;
  private ByteBuffer pcmBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
  private byte[] pcmBufferMuted = new byte[BUFFER_SIZE];
  private boolean running = false;
  private boolean created = false;

  //default parameters for microphone
  private int sampleRate = 32000; //hz
  private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
  private int channel = AudioFormat.CHANNEL_IN_STEREO;
  private boolean muted = false;
  private AudioPostProcessEffect audioPostProcessEffect;
  private Thread thread;

  public MicrophoneManager(GetMicrophoneData getMicrophoneData) {
    this.getMicrophoneData = getMicrophoneData;
  }

  /**
   * Create audio record
   */
  public void createMicrophone() {
    createMicrophone(sampleRate, true, false, false);
    Log.i(TAG, "Microphone created, " + sampleRate + "hz, Stereo");
  }

  /**
   * Create audio record with params
   */
  public void createMicrophone(int sampleRate, boolean isStereo, boolean echoCanceler,
      boolean noiseSuppressor) {
    this.sampleRate = sampleRate;
    if (!isStereo) channel = AudioFormat.CHANNEL_IN_MONO;
    audioRecord =
        new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, channel, audioFormat,
            getPcmBufferSize());
    audioPostProcessEffect = new AudioPostProcessEffect(audioRecord.getAudioSessionId());
    if (echoCanceler) audioPostProcessEffect.enableEchoCanceler();
    if (noiseSuppressor) audioPostProcessEffect.enableNoiseSuppressor();
    String chl = (isStereo) ? "Stereo" : "Mono";
    Log.i(TAG, "Microphone created, " + sampleRate + "hz, " + chl);
    created = true;
  }

  /**
   * Start record and get data
   */
  public void start() {
    init();
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (running && !Thread.interrupted()) {
          DataTaken dataTaken = read();
          if (dataTaken != null) {
            getMicrophoneData.inputPCMData(dataTaken.getPcmBuffer(), dataTaken.getSize());
          } else {
            running = false;
          }
        }
      }
    });
    thread.start();
  }

  private void init() {
    if (audioRecord != null) {
      audioRecord.startRecording();
      running = true;
      Log.i(TAG, "Microphone started");
    } else {
      Log.e(TAG, "Error starting, microphone was stopped or not created, "
          + "use createMicrophone() before start()");
    }
  }

  public void mute() {
    muted = true;
  }

  public void unMute() {
    muted = false;
  }

  public boolean isMuted() {
    return muted;
  }

  /**
   * @return Object with size and PCM buffer data
   */
  private DataTaken read() {
    pcmBuffer.rewind();
    int size = audioRecord.read(pcmBuffer, pcmBuffer.remaining());
    if (size <= 0) {
      return null;
    }
    return new DataTaken(muted ? pcmBufferMuted : pcmBuffer.array(), size);
  }

  /**
   * Stop and release microphone
   */
  public void stop() {
    running = false;
    created = false;
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(100);
      } catch (InterruptedException e) {
        thread.interrupt();
      }
      thread = null;
    }
    if (audioRecord != null) {
      audioRecord.setRecordPositionUpdateListener(null);
      audioRecord.stop();
      audioRecord.release();
      audioRecord = null;
    }
    if (audioPostProcessEffect != null) {
      audioPostProcessEffect.releaseEchoCanceler();
      audioPostProcessEffect.releaseNoiseSuppressor();
    }
    Log.i(TAG, "Microphone stopped");
  }

  /**
   * Get PCM buffer size
   */
  private int getPcmBufferSize() {
    int pcmBufSize =
        AudioRecord.getMinBufferSize(sampleRate, channel, AudioFormat.ENCODING_PCM_16BIT);
    return pcmBufSize * 5;
  }

  public int getMaxInputSize() {
    return BUFFER_SIZE;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  public int getAudioFormat() {
    return audioFormat;
  }

  public int getChannel() {
    return channel;
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isCreated() {
    return created;
  }
}
