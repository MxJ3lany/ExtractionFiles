package eu.siacs.conversations.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityRecordingBinding;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.utils.ThemeHelper;

public class RecordingActivity extends Activity implements View.OnClickListener {

	public static String STORAGE_DIRECTORY_TYPE_NAME = "Recordings";

	private ActivityRecordingBinding binding;

	private MediaRecorder mRecorder;
	private long mStartTime = 0;

	private Handler mHandler = new Handler();
	private Runnable mTickExecutor = new Runnable() {
		@Override
		public void run() {
			tick();
			mHandler.postDelayed(mTickExecutor, 100);
		}
	};

	private File mOutputFile;
	private boolean mShouldFinishAfterWrite = false;

	private FileObserver mFileObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeHelper.findDialog(this));
		super.onCreate(savedInstanceState);
		this.binding = DataBindingUtil.setContentView(this,R.layout.activity_recording);
		this.binding.cancelButton.setOnClickListener(this);
		this.binding.shareButton.setOnClickListener(this);
		this.setFinishOnTouchOutside(false);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!startRecording()) {
			this.binding.shareButton.setEnabled(false);
			this.binding.timer.setTextAppearance(this, R.style.TextAppearance_Conversations_Title);
			this.binding.timer.setText(R.string.unable_to_start_recording);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mRecorder != null) {
			mHandler.removeCallbacks(mTickExecutor);
			stopRecording(false);
		}
		if (mFileObserver != null) {
			mFileObserver.stopWatching();
		}
	}

	private boolean startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mRecorder.setAudioEncodingBitRate(96000);
		mRecorder.setAudioSamplingRate(22050);
		setupOutputFile();
		mRecorder.setOutputFile(mOutputFile.getAbsolutePath());

		try {
			mRecorder.prepare();
			mRecorder.start();
			mStartTime = SystemClock.elapsedRealtime();
			mHandler.postDelayed(mTickExecutor, 100);
			Log.d("Voice Recorder", "started recording to " + mOutputFile.getAbsolutePath());
			return true;
		} catch (Exception e) {
			Log.e("Voice Recorder", "prepare() failed " + e.getMessage());
			return false;
		}
	}

	protected void stopRecording(boolean saveFile) {
		mShouldFinishAfterWrite = saveFile;
		try {
			mRecorder.stop();
			mRecorder.release();
		} catch (Exception e) {
			if (saveFile) {
				Toast.makeText(this,R.string.unable_to_save_recording, Toast.LENGTH_SHORT).show();
			}
		} finally {
			mRecorder = null;
			mStartTime = 0;
		}
		if (!saveFile && mOutputFile != null) {
			if (mOutputFile.delete()) {
				Log.d(Config.LOGTAG,"deleted canceled recording");
			}
		}
	}

	private static File generateOutputFilename(Context context) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
		String filename = "RECORDING_" + dateFormat.format(new Date()) + ".m4a";
		return new File(FileBackend.getConversationsDirectory(context, STORAGE_DIRECTORY_TYPE_NAME) + "/" + filename);
	}

	private void setupOutputFile() {
		mOutputFile = generateOutputFilename(this);
		File parentDirectory = mOutputFile.getParentFile();
		if (parentDirectory.mkdirs()) {
			Log.d(Config.LOGTAG, "created " + parentDirectory.getAbsolutePath());
		}
		File noMedia = new File(parentDirectory, ".nomedia");
		if (!noMedia.exists()) {
			try {
				if (noMedia.createNewFile()) {
					Log.d(Config.LOGTAG, "created nomedia file in " + parentDirectory.getAbsolutePath());
				}
			} catch (IOException e) {
				Log.d(Config.LOGTAG, "unable to create nomedia file in " + parentDirectory.getAbsolutePath(), e);
			}
		}
		setupFileObserver(parentDirectory);
	}

	private void setupFileObserver(File directory) {
		mFileObserver = new FileObserver(directory.getAbsolutePath()) {
			@Override
			public void onEvent(int event, String s) {
				if (s != null && s.equals(mOutputFile.getName()) && event == FileObserver.CLOSE_WRITE) {
					if (mShouldFinishAfterWrite) {
						setResult(Activity.RESULT_OK, new Intent().setData(Uri.fromFile(mOutputFile)));
						finish();
					}
				}
			}
		};
		mFileObserver.startWatching();
	}

	@SuppressLint("SetTextI18n")
	private void tick() {
		long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
		int minutes = (int) (time / 60000);
		int seconds = (int) (time / 1000) % 60;
		int milliseconds = (int) (time / 100) % 10;
		this.binding.timer.setText(minutes + ":" + (seconds < 10 ? "0" + seconds : seconds) + "." + milliseconds);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.cancel_button:
				mHandler.removeCallbacks(mTickExecutor);
				stopRecording(false);
				setResult(RESULT_CANCELED);
				finish();
				break;
			case R.id.share_button:
				this.binding.shareButton.setEnabled(false);
				this.binding.shareButton.setText(R.string.please_wait);
				mHandler.removeCallbacks(mTickExecutor);
				mHandler.postDelayed(() -> stopRecording(true), 500);
				break;
		}
	}
}
