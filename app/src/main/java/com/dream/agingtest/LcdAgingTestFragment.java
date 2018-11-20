package com.dream.agingtest;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.MotionEvent;

public class LcdAgingTestFragment extends Fragment{

	private FinishCallback mCallback;
	
	private CountChangeCallback mCountChangeCallback;
    public CountChangeCallback getCountChangeCallback() {
		return mCountChangeCallback;
	}
	public void setCountChangeCallback(CountChangeCallback countChangeCallback) {
		mCountChangeCallback = countChangeCallback;
	}
	
	private LcdView lcdvLcd;
	private TextView tvLcdIllustration;
	
	private int count = 0;
    private int max = 10;
	final static String TAG = "LcdAgingTest";
	
	private boolean isFirst = true;
	private Timer mTimer;
	
	private final static int DATA_CHANGED = 0X0;

	public static LcdAgingTestFragment newInstance() {
		return new LcdAgingTestFragment();
	}
	
	public LcdAgingTestFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
//		openBrightNessModeAuto();
		
		getScreenMetries();
		int layoutId = R.layout.lcd;
		super.onCreate(savedInstanceState);

		View view = inflater.inflate(layoutId, container, false);
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		
		lcdvLcd = (LcdView)view.findViewById(R.id.lcdv_lcd);
		tvLcdIllustration = (TextView)view.findViewById(R.id.txt_lcd_illustration);
		
		tvLcdIllustration.setVisibility(View.INVISIBLE);
		lcdvLcd.setVisibility(View.VISIBLE);

		mTimer = new Timer();
		
//		view.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				final int mAction = event.getAction();
//		        if ((mAction == MotionEvent.ACTION_UP)) {
//
//		            if (isFirst) {
////		                lcdvLcd.setVisibility(View.VISIBLE);
//		                tvLcdIllustration.setVisibility(View.INVISIBLE);
//		                isFirst = false;
////		                return true;
//		            }
//		            
//		            count ++;
//		        	if (count > 20) {
//		        		end();
//		        		return true;
//		    		}
//		            
//		            lcdvLcd.biListIndex ++;
//		            lcdvLcd.invalidate();
//		            if (lcdvLcd.isLastColor) {
//		                llPF.setVisibility(View.VISIBLE);
//		            }
//
//		        }
//		        return true;
//			}
//		});
		
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(DATA_CHANGED);
			}
		}, 0,1000);
		
		return view;
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DATA_CHANGED:
				if (isFirst) {
					tvLcdIllustration.setVisibility(View.INVISIBLE);
	                isFirst = false;
	            }
//	            Log.d(TAG, ""+count);
	            count ++;
	        	if (count > max) {
	        		count = 0;
	            	mTimer.cancel();
	        		end();
	        		return;
	    		} else {
					ResultsInformation.getResultsInformation(
		    				).addLcdAgingTestResult(false, "");
				}
	        	if (mCountChangeCallback!=null) {
	        		mCountChangeCallback.onCountChange(max, count, MainActivity.textColorNormal);
				}
	            
	            lcdvLcd.biListIndex ++;
	            lcdvLcd.invalidate();
//	            if (lcdvLcd.isLastColor) {
//	                llPF.setVisibility(View.VISIBLE);
//	            }
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private void getScreenMetries() {
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
		
		LcdView.screen_X = displaysMetrics.widthPixels;
		LcdView.screen_Y = displaysMetrics.heightPixels;
	}
	

	public FinishCallback getCallback() {
		return mCallback;
	}

	public void setCallback(FinishCallback callback) {
		mCallback = callback;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mTimer!=null) {
			mTimer.cancel();
			mTimer = null;
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(DATA_CHANGED);
			}
		}, 0,1000);
	}
	
	@Override
	public void onPause() {
		if (mTimer!=null) {
			mTimer.cancel();
			mTimer = null;
		}
		super.onPause();
	}
	
	@Override
    public void onDestroy() {
		count = 0;
		if (mTimer!=null) {
			mTimer.cancel();
			mTimer = null;
		}
//    	closeBrightNessModeAuto();
    	super.onDestroy();
    }
    
    void end() {
    	if (mCallback != null) {
    		mCallback.onTestFinish(MainActivity.TEST_PASS);
		}

    }
    
    private boolean isBrightNessModeChanged = false;
    void openBrightNessModeAuto(){
    	try {
			if (Settings.System.getInt(getActivity().getContentResolver(), 
					Settings.System.SCREEN_BRIGHTNESS_MODE) 
					== Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
				Settings.System.putInt(getActivity().getContentResolver(), 
					Settings.System.SCREEN_BRIGHTNESS_MODE, 
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
				isBrightNessModeChanged = true;
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//    	Log.d(TAG, ""+isBrightNessModeChanged);
    }
    
    void closeBrightNessModeAuto(){
    	if (isBrightNessModeChanged) {
    		Settings.System.putInt(getActivity().getContentResolver(), 
					Settings.System.SCREEN_BRIGHTNESS_MODE, 
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		}
    }
}
