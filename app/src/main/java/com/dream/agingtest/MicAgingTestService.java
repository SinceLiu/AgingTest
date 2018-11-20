package com.dream.agingtest;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.dream.agingtest.GpsAgingTestService.MyBinder;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MicAgingTestService extends Service {

    String mAudiofilePath;
    static String TAG = "MicAgingTestService";
    MediaRecorder mMediaRecorder;
    MediaPlayer mMediaPlayer;
    boolean isRecording = false;
    AudioManager mAudioManager;
    Context mContext;
    private boolean isExit = false;
	
    private boolean hasCamcorderMic = true;
	private long times_camcorderMic = 0;
	private long wrongTimes_camcorderMic = 0;
	private long times_normalMic = 0;
	private long wrongTimes_normalMic = 0;
	
	
    Timer mTimer ;
    private int mSecond = 0;
    
    private int MicType = -1;
    
    private final int TIMER_UPDATE = 1598;
    
    private boolean isPaused = false;
    
    class sTimerTask extends TimerTask {

        public void run() {
        	if (isRecording||!isPaused) {
        		//正常情况或者pause后继续录音
                mSecond ++;
			}
            if (mSecond >= MainActivity.DELAY_TIME/1000 * (isRecording?1:2)) {
				if (isRecording) {
					stop();
				} else {
					start();
				}
			}
            mHandler.sendEmptyMessage(TIMER_UPDATE);
        }

    }

    private Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case TIMER_UPDATE:

	            if (mDisplayTextCallback != null) {
	            	String type = "";
	            	if (MicType == -1) {
	            		type = "OFF";
					} else if (MicType == MediaRecorder.AudioSource.CAMCORDER) {
						type = "Camcorder mic";
					} else {
						type = "Normal mic";
					}
	            	long size = 0;
	            	if (mAudiofilePath!=null) {
		            	File file = new File(mAudiofilePath);
		            	if (file.exists()) {
		            		size = file.length();
		            		if (!isRecording) {
		            			type = "PLAY";
							}
						} else {
							type = "OFF";
						}
					}
	            	
	            	mDisplayTextCallback.onTextChanged("Mic:"
		            		+type
		            		+", File size:" + size
                    		+", times:" + (times_camcorderMic+times_normalMic),
                    		MainActivity.textColorNormal);
				}
	            
				break;
			default:
				break;
			}
    	};
    };
    
    public void startDisplayText(){
    	if (mDisplayTextCallback != null) {
        	String type = "";
        	if (MicType == -1) {
        		type = "OFF";
			} else if (MicType == MediaRecorder.AudioSource.CAMCORDER) {
				type = "Camcorder mic";
			} else {
				type = "Normal mic";
			}
        	long size = 0;
        	if (mAudiofilePath!=null) {
            	File file = new File(mAudiofilePath);
            	if (file.exists()) {
            		size = file.length();
    			}
			}
        	mDisplayTextCallback.onTextChanged("Mic:"
            		+type
            		+", File size:" + size
            		+", times:" + (times_camcorderMic+times_normalMic),
            		MainActivity.textColorNormal);
		}
    }
    
    public static String ACTION_START_SERVICE = 
			"com.dream.agingtest.MicAgingTestService.action.START_SERVICE";

    private MicTestMusicCallback mMicTestMusicCallback = null;
	public void setMicTestMusicCallback(MicTestMusicCallback micTestMusicCallback) {
		this.mMicTestMusicCallback = micTestMusicCallback;
	}
	
	private DisplayTextCallback mDisplayTextCallback = null;
	public void setDisplayTextCallback(DisplayTextCallback displayTextCallback) {
		this.mDisplayTextCallback = displayTextCallback;
	}
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		
		return new MyBinder();
	}
    
    public class MyBinder extends Binder {
		/**
		 * 获取当前Service的实例
		 * 
		 * @return
		 */
		public MicAgingTestService getService() {
			return MicAgingTestService.this;
		}
	}
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null)// intent可能为null
		{
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
//			MyLog.d(TAG, "onStartCommand action=" + action);
			if (action.equals(ACTION_START_SERVICE )) {
				return START_STICKY;
			}

		}
		return super.onStartCommand(intent, flags, startId);
	}
    
    
    public void onCreate() {


		ResultsInformation r = ResultsInformation.getResultsInformation();
		times_camcorderMic = r.getCamcorderMicAgingTestCount();
		wrongTimes_camcorderMic = r.getCamcorderMicAgingTestWrongCount();
		times_normalMic = r.getNormalMicAgingTestCount();
		wrongTimes_normalMic = r.getNormalMicAgingTestWrongCount();
		
		if (times_camcorderMic >= times_normalMic&&hasCamcorderMic) {
    		MicType = MediaRecorder.AudioSource.CAMCORDER;
		} else {
			MicType = MediaRecorder.AudioSource.MIC;
		}
    	
        mContext = this;
        isRecording = false;

        getService();

        if (mAudioManager.isWiredHeadsetOn())
//            showWarningDialog(getString(R.string.remove_headset));

        setAudio();
        
        start();
        mTimer = new Timer();
        mTimer.schedule(new sTimerTask(), 0, 1000);
//        registerScreenActionReceiver();
    }
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();

//    	unregisterScreenActionReceiver();
    	
    	if (mTimer != null) {
    		mTimer.cancel();
		}
    	
        isExit = true;
        if (isRecording) {
        	if (mMediaRecorder!=null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
			}
            
            File file = new File(mAudiofilePath);
            file.delete();
        }
        
        if (mMediaPlayer!=null) {
        	try {
            	mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
        
        //add for click fail button crash when recoding by song 20140506 end
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        //Add for adding second-mic test by lvhongshan 20140521 start
        mAudioManager.setParameters("second-mic=false");
        //Add for adding second-mic test by lvhongshan 20140521 end
    }
    
    @Override
	public boolean onUnbind(Intent intent) {
    	stopSelf();
		return super.onUnbind(intent);
	}

    void record() throws IllegalStateException, IOException, InterruptedException {

    	mMediaRecorder = new MediaRecorder();
    	
        mMediaRecorder.setAudioSource(MicType);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(this.getCacheDir().getAbsolutePath() + "/test");
        mAudiofilePath = this.getCacheDir().getAbsolutePath() + "/test";
        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }
    
    public void setAudio() {

//        mAudioManager.setMode(AudioManager.MODE_IN_CALL);

    	int p = 1;
//    	int p = 10;
//        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_ALARM)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_MUSIC)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_DTMF)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_RING)/p, 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager
//                .getStreamMaxVolume(AudioManager.STREAM_SYSTEM)/p, 0);
        //Add for adding second-mic test by lvhongshan 20140521 start
        /*mAudioManager.setParameters("second-mic=true");*/
        //Add for adding second-mic test by lvhongshan 20140521 end
    }
    
    void start(){
    	if (!mAudioManager.isWiredHeadsetOn()) {
        	
        	if (MicType == -1 || MicType == MediaRecorder.AudioSource.CAMCORDER) {
        		MicType = MediaRecorder.AudioSource.MIC;
                mAudioManager.setParameters("second-mic=false");
    		} else if(hasCamcorderMic&&MicType == MediaRecorder.AudioSource.MIC) {
    			MicType = MediaRecorder.AudioSource.CAMCORDER;
    	        mAudioManager.setParameters("second-mic=true");
    		} else {
        		MicType = MediaRecorder.AudioSource.MIC;
                mAudioManager.setParameters("second-mic=false");
    		}
        	
            if (MicType == MediaRecorder.AudioSource.MIC) {
            	times_normalMic++;
            	ResultsInformation.getResultsInformation(
        				).setNormalMicAgingTestCount(times_normalMic);
    		} else if (MicType == MediaRecorder.AudioSource.CAMCORDER) {
    			times_camcorderMic++;
    			ResultsInformation.getResultsInformation(
        				).setCamcorderMicAgingTestCount(times_camcorderMic);
    		}
            
            try {
                record();
                isRecording = true;
            } catch (Exception e) {
                e.printStackTrace();
                
                if (MicType == MediaRecorder.AudioSource.MIC) {
                	wrongTimes_normalMic++;
                	ResultsInformation.getResultsInformation(
            				).setNormalMicAgingTestWrongCount(wrongTimes_normalMic);
        		} else if (MicType == MediaRecorder.AudioSource.CAMCORDER) {
        			wrongTimes_camcorderMic++;
        			ResultsInformation.getResultsInformation(
            				).setCamcorderMicAgingTestWrongCount(wrongTimes_camcorderMic);
        		}
            }
			mSecond = 0;
        } 
//    	else
//            showWarningDialog(getString(R.string.remove_headset));
    }

    void stop(){
    	if (isRecording) {
            if (mMediaRecorder!=null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
			}
            
            if (mMediaPlayer!=null) {
            	mMediaPlayer.stop();
            	mMediaPlayer.release();
                mMediaPlayer = null;
			}

        	if (isPaused) {
        		return;
    		}

            isRecording = false;
            
            try {
            	if (!mMicTestMusicCallback.onStart()) {
//            		mMicTestMusicCallback.onStart();
            		mMediaPlayer = new MediaPlayer();
    				mMediaPlayer.setDataSource(mAudiofilePath);
    	            mMediaPlayer.prepare();
    	            mMediaPlayer.start();
    	            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
						
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							if (mMediaPlayer!=null) {
    	                    	mMediaPlayer.stop();
    	                    	mMediaPlayer.release();
    	                        mMediaPlayer = null;
    	        			}
    	                	
    	                    File file = new File(mAudiofilePath);
    	                    file.delete();
    	                    mMicTestMusicCallback.onEnd();
							return true;
						}
					});

    	            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

    	                public void onCompletion(MediaPlayer mPlayer) {
//    	                    mPlayer.stop();
    	                	
    	                	if (mMediaPlayer!=null) {
    	                    	mMediaPlayer.stop();
    	                    	mMediaPlayer.release();
    	                        mMediaPlayer = null;
    	        			}
    	                	
    	                    File file = new File(mAudiofilePath);
    	                    file.delete();
    	                    mMicTestMusicCallback.onEnd();
    	                }
    	            });
				} else {
					
					if (mMediaPlayer!=null) {
		            	mMediaPlayer.stop();
		            	mMediaPlayer.release();
		                mMediaPlayer = null;
					}
					
					File file = new File(mAudiofilePath);
                    file.delete();
                    mMicTestMusicCallback.onEnd();
                    mSecond += MainActivity.DELAY_TIME / 1000;
				}
	            
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
//            File file = new File(mAudiofilePath);
//            Log.d(TAG, file.getName()+":"+file.length());
//            file.delete();

			mSecond = 0;
        } 
//    	else
//            showWarningDialog(getString(R.string.transmitter_receiver_record_first));
    }
    

    public void pause() {
    	isPaused = true;
    	if (mMediaPlayer!=null) {
    		mMediaPlayer.pause();
		}
    }
    public void resume() {
    	isPaused = false;
    	if (mMediaPlayer!=null) {
    		mMediaPlayer.start();
		}
    }
    
    
    void getService() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    public interface MicTestMusicCallback {
    	boolean onStart();
    	boolean onEnd();
    }
}
