package com.dream.agingtest;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class MP4AgingTestFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener{

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
//	private long maxTimeLong = 10*1000;
	private Timer mTimer;
	private long startTime;
	private long ct;

	private boolean isFragmentPause = false;
	private boolean isPlayerPause = false;
	private boolean isEnded = false;
	
//	private VideoView videoView;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private MediaPlayer player;
	private int per;
	
	private View recording_Layout;
	
	private int oldVolume = -1;
	private int testVolume = -1;
	private float oldBrightness = -1;
	
	private Calendar calendar;
	
	final static String TAG = "mp4AgingTest";
	
	public static MP4AgingTestFragment newInstance() {
		return new MP4AgingTestFragment();
	}
	
	public MP4AgingTestFragment() {
	}
	
	private boolean isjustCreated = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		isjustCreated = true;
		isPlayerPause = getArguments().getBoolean("isPlayingRecoding");
		
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
		
		if (restTimeLong <= 0) {
			restTimeLong = maxTimeLong;
		}
		
		AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		testVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		testVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10;
//		audioManager.setStreamVolume(
//				AudioManager.STREAM_MUSIC, 
//				testVolume, 
////				testVolume / 2, 
//				0);
		
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		oldBrightness = lp.screenBrightness;
		lp.screenBrightness = 1.0f;
		((Activity) mContext).getWindow().setAttributes(lp);
		
		recording_Layout = view.findViewById(R.id.recording_Layout);
		
		surfaceView = (SurfaceView) view.findViewById(R.id.video_surface);
		// 给SurfaceView添加CallBack监听
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		// 为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// 下面开始实例化MediaPlayer对象
		player = MediaPlayer.create(getActivity(), R.raw.video);
		Log.e(TAG,"1111video mediaPlayer: "+player);
		int testCount = 0;
		while (player==null&&testCount<10) {
			testCount++;
			Log.e(TAG, "player==null");
			player = MediaPlayer.create(getActivity(), R.raw.video);
			Log.e(TAG,"2222video mediaPlayer: "+player);
		}
//		player = new MediaPlayer();
//		AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.video);
//		try {
//			player.setDataSource(afd);
//			player.prepare();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} finally {
//			try {
//				afd.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		if (player==null) {
			mHandler.sendEmptyMessage(END);
		} else {
			player.setLooping(true);
			player.setOnErrorListener(this);
			
			if (!isPlayerPause) {
				if (!isjustCreated) {
					player.start();
				}
				
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				ct = System.currentTimeMillis();
				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
//						Log.d("timeLong", "-----------------");
//						Log.d("timeLong", ""+((System.currentTimeMillis() - ct)/1000));
//						Log.d("timeLong", ""+(System.currentTimeMillis() - ct));
//						Log.d("timeLong", ""+restTimeLong);
						if (System.currentTimeMillis() - ct >= restTimeLong) {
							restTimeLong = 0;
							mTimer.cancel();
							mHandler.sendEmptyMessage(END);
						}
					}
				}, 0,500);
				
				recording_Layout.post(new Runnable() {
					@Override
					public void run() {
						recording_Layout.setVisibility(View.GONE);
					}
				});
			} else {
				recording_Layout.post(new Runnable() {
					@Override
					public void run() {
						recording_Layout.setVisibility(View.VISIBLE);
					}
				});
			}
			
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mCountChangeCallback!=null) {
		        		mCountChangeCallback.onCountChange(1, 1, MainActivity.textColorNormal);
					}
				}
			}, 50);
		}
		
		return view;
	}
	

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (player!=null) {
			player.stop();
			player.release();
			player = null;
		}
		player = MediaPlayer.create(getActivity(), R.raw.video);
		Log.e(TAG,"3333 video mediaPlayer: "+player);
		int testCount = 0;
		while (player==null&&testCount<10) {
			testCount++;
			Log.e(TAG, "player==null");
			player = MediaPlayer.create(getActivity(), R.raw.video);
			Log.e(TAG,"video mediaPlayer: "+player);
		}
//		player = new MediaPlayer();
//		AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.video);
//		try {
//			player.setDataSource(afd);
//			player.prepare();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			mHandler.sendEmptyMessage(END);
//		} finally {
//			try {
//				afd.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		if (player==null) {
			mHandler.sendEmptyMessage(END);
		} else {
			player.setLooping(true);
			player.setOnErrorListener(this);
			
			if (!isPlayerPause) {
				if (!isjustCreated) {
					player.start();
				}
				
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				ct = System.currentTimeMillis();
				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						if (System.currentTimeMillis() - ct >= restTimeLong) {
							restTimeLong = 0;
							mTimer.cancel();
							mHandler.sendEmptyMessage(END);
						}
					}
				}, 0,500);
				
				recording_Layout.post(new Runnable() {
					@Override
					public void run() {
						recording_Layout.setVisibility(View.GONE);
					}
				});
			} else {
				recording_Layout.post(new Runnable() {
					@Override
					public void run() {
						recording_Layout.setVisibility(View.VISIBLE);
					}
				});
			}
		}
		return true;
	}
	
	@Override
	public void onPause() {
		isFragmentPause = true;
		if (oldVolume != -1) {
//			AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
//			audioManager.setStreamVolume(
//					AudioManager.STREAM_MUSIC, 
//					oldVolume, 
//					0);
		}
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		lp.screenBrightness = oldBrightness;
		((Activity) mContext).getWindow().setAttributes(lp);
//		if (videoView != null) {
//			videoView.pause();
//			per = videoView.getCurrentPosition();
//		}
		if (player != null) {
			player.pause();
//			per = player.getCurrentPosition();
		}
		if (mTimer != null) {
			restTimeLong = restTimeLong - (System.currentTimeMillis() - ct);
			mTimer.cancel();
			mTimer = null;
		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("onResume", "onResume");
		
//		AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
//		audioManager.setStreamVolume(
//				AudioManager.STREAM_MUSIC, 
//				testVolume, 
////				testVolume / 2, 
//				0);
		
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		oldBrightness = lp.screenBrightness;
		lp.screenBrightness = 1.0f;
		((Activity) mContext).getWindow().setAttributes(lp);
		
//		if (videoView != null) {
//			videoView.seekTo(per);
//			videoView.start();
//		}
		Log.d(TAG, "onResume:isPlayerPause:"+isPlayerPause);
		if (!isPlayerPause) {
//			player.seekTo(per);
			if (!isjustCreated) {
				player.start();
			}
			
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
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
			
			recording_Layout.post(new Runnable() {
				@Override
				public void run() {
					recording_Layout.setVisibility(View.GONE);
				}
			});
		} else {
			recording_Layout.post(new Runnable() {
				@Override
				public void run() {
					recording_Layout.setVisibility(View.VISIBLE);
				}
			});
		}
		
		
		isFragmentPause = false;
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
		isEnded = true;
    	if (mCallback != null) {
    		mCallback.onTestFinish(MainActivity.TEST_PASS);
		}
    	
    	calendar.setTimeInMillis(startTime);
    	String startTimeStr = calendar.getTime().toString();
    	long endTime = System.currentTimeMillis();
    	calendar.setTimeInMillis(endTime);
    	String endTimeStr = calendar.getTime().toString();
    	ResultsInformation.getResultsInformation().addMp4AgingTestResult(
    			player==null, 
    			startTimeStr + " to " + endTimeStr);
    	Log.d(getTag(), "end and " + ((player==null)?"fail":"pass"));
    	if (player != null) {
			player.stop();
		}
    }
	
	@Override
    public void onDestroy() {
		if (oldVolume != -1) {
//			AudioManager audioManager = (AudioManager) ((Activity) mContext).getSystemService(Context.AUDIO_SERVICE);
//			audioManager.setStreamVolume(
//					AudioManager.STREAM_MUSIC, 
//					oldVolume, 
//					0);
		}
		WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
		lp.screenBrightness = oldBrightness;
		((Activity) mContext).getWindow().setAttributes(lp);
//		if (videoView != null) {
//			videoView.pause();
//			per = videoView.getCurrentPosition();
//		}
		if (player != null) {
			player.stop();
			player.release();
			player = null;
		}
		if (mTimer != null) {
			restTimeLong = restTimeLong - (System.currentTimeMillis() - ct);
			mTimer.cancel();
			mTimer = null;
			
			if (!isEnded) {
				calendar.setTimeInMillis(startTime);
		    	String startTimeStr = calendar.getTime().toString();
		    	calendar.setTimeInMillis(System.currentTimeMillis());
		    	String endTimeStr = calendar.getTime().toString();
		    	ResultsInformation.getResultsInformation().addMp4AgingTestResult(false, 
		    			startTimeStr + " to " + endTimeStr);
			}
		}
		((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	super.onDestroy();
    }
	
	
	public void playerPause(){
		isPlayerPause = true;
		if (isFragmentPause) {
			return;
		}
		if (player!=null) {
			player.pause();
			
			if (mTimer != null) {
				restTimeLong = restTimeLong - (System.currentTimeMillis() - ct);
				mTimer.cancel();
				mTimer = null;
			}
			
			recording_Layout.post(new Runnable() {
				@Override
				public void run() {
					recording_Layout.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
	public void playerResume(){
		isPlayerPause = false;
		if (isFragmentPause) {
			return;
		}
		
		if (player!=null) {
			player.start();
		}

		recording_Layout.post(new Runnable() {
			@Override
			public void run() {
				recording_Layout.setVisibility(View.GONE);
			}
		});
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		ct = System.currentTimeMillis();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (System.currentTimeMillis() - ct >= restTimeLong) {
					restTimeLong = 0;
					mTimer.cancel();
					mHandler.sendEmptyMessage(END);
				}
			}
		}, 0,500);
	}
	
	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				if (player!=null) {
//					Log.d(TAG, "surfaceCreated:setDisplay");
//					player.setDisplay(holder);
//				}
//			}
//		}, 100);
		if (player!=null) {
			Log.d(TAG, "surfaceCreated:setDisplay");
			player.setDisplay(holder);
			
			if (!isPlayerPause&&isjustCreated) {
				isjustCreated = false;
				player.start();
			}
		}
	}
	@Override
	public void surfaceChanged(final SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				if (player!=null) {
//					Log.d(TAG, "surfaceCreated:setDisplay");
//					player.setDisplay(holder);
//				}
//			}
//		}, 100);
		if (player!=null) {
			Log.d(TAG, "surfaceCreated:setDisplay");
			player.setDisplay(holder);
			
			if (!isPlayerPause&&isjustCreated) {
				isjustCreated = false;
				player.start();
			}
		}
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
}
