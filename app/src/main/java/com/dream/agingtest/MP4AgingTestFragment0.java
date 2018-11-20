package com.dream.agingtest;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class MP4AgingTestFragment0 extends Fragment {

	private FinishCallback mCallback;
	
	private CountChangeCallback mCountChangeCallback;
    public CountChangeCallback getCountChangeCallback() {
		return mCountChangeCallback;
	}
	public void setCountChangeCallback(CountChangeCallback countChangeCallback) {
		mCountChangeCallback = countChangeCallback;
	}
	
	private Context mContext;
	private Uri video_uri;
	private long restTimeLong;
	private long maxTimeLong = 5*60*1000;
//	private long maxTimeLong = 1*1000;
	private Timer mTimer;
	private long startTime;
	private long ct;
	private VideoView videoView;
	private int per;
	
	private int oldVolume = -1;
	private float oldBrightness = -1;
	
	private boolean isCreating = false;
	
	private Calendar calendar;
	
	final static String TAG = "mp4AgingTest";
	
	public static MP4AgingTestFragment0 newInstance() {
		return new MP4AgingTestFragment0();
	}
	
	public MP4AgingTestFragment0() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		int layoutId = R.layout.mp4;
		super.onCreate(savedInstanceState);

		View view = inflater.inflate(layoutId, container, false);
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		mContext = getActivity();

    	calendar = Calendar.getInstance();
    	
		((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(
				AudioManager.STREAM_MUSIC, 
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 
//				0,
				0);
		
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		oldBrightness = lp.screenBrightness;
		lp.screenBrightness = 1.0f;
		((Activity) mContext).getWindow().setAttributes(lp);
		
		videoView = (VideoView) view.findViewById(R.id.videoView);
		
		Resources r = getResources();
		video_uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
				+ r.getResourcePackageName(R.raw.video) + "/"
				+ r.getResourceTypeName(R.raw.video) + "/"
				+ r.getResourceEntryName(R.raw.video));
		
//		video_uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
//				+ r.getResourcePackageName(R.raw.video1) + "/"
//				+ r.getResourceTypeName(R.raw.video1) + "/"
//				+ r.getResourceEntryName(R.raw.video1));
		
		videoView.setVideoURI(video_uri);
//		videoView.setMediaController(new MediaController(this));
		videoView.start();
		videoView.requestFocus();
		
		if (restTimeLong <= 0) {
			restTimeLong = maxTimeLong;
		}
		
		startTime = System.currentTimeMillis();
		ct = startTime;
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				Log.d("timeLong", "-----------------");
//				Log.d("timeLong", ""+((System.currentTimeMillis() - ct)/1000));
//				Log.d("timeLong", ""+(System.currentTimeMillis() - ct));
//				Log.d("timeLong", ""+restTimeLong);
				if (System.currentTimeMillis() - ct >= restTimeLong) {
					restTimeLong = 0;
					mTimer.cancel();
					mHandler.sendEmptyMessage(END);
				}
			}
		}, 0,500);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mCountChangeCallback!=null) {
	        		mCountChangeCallback.onCountChange(1, 1, MainActivity.textColorNormal);
				}
			}
		}, 50);
		
		
//		videoView.setOnCompletionListener(new OnCompletionListener() {
//			
//			@Override
//			public void onCompletion(MediaPlayer mediaPlayer) {
//				mediaPlayer.reset();
//				mediaPlayer.start();
//			}
//		});
		
		videoView.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				mediaPlayer.setLooping(true);
			}
		});
		
		isCreating = true;
		
		return view;
	}
	
	
	@Override
	public void onPause() {
		if (oldVolume != -1) {
			AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(
					AudioManager.STREAM_MUSIC, 
					oldVolume, 
					0);
		}
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		lp.screenBrightness = oldBrightness;
		((Activity) mContext).getWindow().setAttributes(lp);
		if (videoView != null) {
			videoView.pause();
			per = videoView.getCurrentPosition();
		}
		if (mTimer != null) {
			restTimeLong = restTimeLong - (System.currentTimeMillis() - ct);
			mTimer.cancel();
		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("onResume", "onResume");
		if (isCreating) {
			isCreating = false;
			return;
		}
		
		AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
		oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(
				AudioManager.STREAM_MUSIC, 
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 
//				0,
				0);
		
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		oldBrightness = lp.screenBrightness;
		lp.screenBrightness = 1.0f;
		((Activity) mContext).getWindow().setAttributes(lp);
		
		if (videoView != null) {
			videoView.seekTo(per);
			videoView.start();
		}
		if (mTimer != null) {
			mTimer.cancel();
			ct = System.currentTimeMillis();
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
//					Log.d("timeLong", "-----------------");
//					Log.d("timeLong", ""+((System.currentTimeMillis() - ct)/1000));
//					Log.d("timeLong", ""+(System.currentTimeMillis() - ct));
//					Log.d("timeLong", ""+restTimeLong);
					if (System.currentTimeMillis() - ct >= restTimeLong) {
						restTimeLong = 0;
						mTimer.cancel();
						mHandler.sendEmptyMessage(END);
					}
				}
			}, 0,500);
		}
	}
	
	private final int END = 1478;
	private Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case END:
				end();
				break;
			default:
				break;
			}
    	};
    };
	
	public FinishCallback getCallback() {
		return mCallback;
	}

	public void setCallback(FinishCallback callback) {
		mCallback = callback;
	}
	
	void end() {
    	if (mCallback != null) {
    		mCallback.onTestFinish(MainActivity.TEST_PASS);
		}
    	
    	calendar.setTimeInMillis(startTime);
    	String startTimeStr = calendar.getTime().toString();
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	String endTimeStr = calendar.getTime().toString();
    	ResultsInformation.getResultsInformation().addMp4AgingTestResult(false, 
    			startTimeStr + " to " + endTimeStr);
    }
	
	@Override
    public void onDestroy() {
		if (oldVolume != -1) {
			AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(
					AudioManager.STREAM_MUSIC, 
					oldVolume, 
					0);
		}
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		lp.screenBrightness = oldBrightness;
		((Activity) mContext).getWindow().setAttributes(lp);
		if (videoView != null) {
			videoView.pause();
			per = videoView.getCurrentPosition();
		}
		if (mTimer != null) {
			restTimeLong = restTimeLong - (System.currentTimeMillis() - ct);
			mTimer.cancel();
			
			calendar.setTimeInMillis(startTime);
	    	String startTimeStr = calendar.getTime().toString();
	    	calendar.setTimeInMillis(System.currentTimeMillis());
	    	String endTimeStr = calendar.getTime().toString();
	    	ResultsInformation.getResultsInformation().addMp4AgingTestResult(false, 
	    			startTimeStr + " to " + endTimeStr);
		}
		((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	super.onDestroy();
    }
}
