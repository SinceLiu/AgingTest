package com.dream.agingtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class SensorAgingTestFragment extends Fragment {
	private FinishCallback mCallback;
	
	private long maxTimeLong = 10*1000;

    final static String TAG = "SensorAgingTest";
    
	// Add SensorCheck for LightSensor by xiasiping 20140626 start
	private long SensorchangeTimes = 0L;
	private float mValue_lux;
	// Add SensorCheck for LightSensor by xiasiping 20140626 end
	private TextView txt_1,txt_2,txt_3,txt_4,txt_5,txt_6;
	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private int[] luxes = new int[] { 0, 50, 100, 150, 200, 400, 600, 800,
			1000, 2000, 5000, 10000, 20000, 30000 };
	private int[] brights = new int[] { 25, 45, 45, 63, 63, 82, 82, 100, 100,
			118, 135, 183, 198, 221 };
	private Sensor mProximitySensor;
	
	
	private Timer mTimerReadPsensor = new Timer();
	private int mValue_ps_data;
    private String PS_DATA = "/sys/devices/soc.0/78b6000.i2c/i2c-0/0-0053/ps_data";
	
    private final static int PS_DATA_CHANGED = 0X0;
    private final static int END = 0X1144;
	
    private Sensor mAccelerimeter;
    private Sensor mGyroscope;

    Timer ClockTimer;
    
    private Handler mHandler;
    
    private boolean isBack = false;

    private Sensor mEcompassSensor;
    private ArrowView arrowView;
    private int[] orienValues;
    private float[] mValues;
    
    private boolean lightSensorAgingTestFail = false;
    private boolean proximitySensorAgingTestFail = false;
    private boolean accelerimeterAgingTestFail = false;
    private boolean gyroscopeAgingTestFail = false;
    private boolean ecompassSensorAgingTestFail = false;
    
    public static SensorAgingTestFragment newInstance() {
		return new SensorAgingTestFragment();
	}
	
	public SensorAgingTestFragment() {
		// TODO Auto-generated constructor stub
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		int layoutId = R.layout.txt_list;
		View view = inflater.inflate(layoutId, container, false);
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		
		txt_1 = (TextView) view.findViewById(R.id.txt_1);
		txt_2 = (TextView) view.findViewById(R.id.txt_2);
		txt_3 = (TextView) view.findViewById(R.id.txt_3);
		txt_4 = (TextView) view.findViewById(R.id.txt_4);
		txt_5 = (TextView) view.findViewById(R.id.txt_5);
		txt_6 = (TextView) view.findViewById(R.id.txt_6);
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
		mAccelerimeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		txt_5.setVisibility(View.GONE);    //陀螺仪感应器
		
		mEcompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mEcompassSensor = null;
		txt_6.setVisibility(View.GONE);    //方向传感器
		arrowView = new ArrowView(getActivity());
		arrowView.setVisibility(View.INVISIBLE);  //
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
				Gravity.LEFT|Gravity.BOTTOM);
		((FrameLayout)view.findViewById(R.id.arrowLayout)).addView(arrowView, lp);
//		getActivity().addContentView(arrowView, lp);
		
//		if (null == mLightSensor) {
//			Log.i("lvhongshan_LightSensor", "LightSensor is " + "null");
//		} else {
//			Log.i("lvhongshan_LightSensor", "LightSensor is " + "not null");
//		}

		txt_1.setText("Light Sensor");
		
		if (mHandler == null) {
			mHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case PS_DATA_CHANGED:
						if (!isBack) {
							txt_3.setText(getString(R.string.proximitysenor_info)+" 2:"
									+ mValue_ps_data);
						}
						break;
					case END:
						mCallback.onTestFinish(MainActivity.TEST_PASS);
						break;
					default:
						break;
					}
					super.handleMessage(msg);
				}
			};
		}
		
		ClockTimer = new Timer();
		ClockTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mHandler.sendEmptyMessage(END);
			}
		}, maxTimeLong);
		
		return view;
	}
	

	@Override
	public void onDestroy() {
		ResultsInformation.getResultsInformation(
				).addSensorAgingTestResult(
						lightSensorAgingTestFail, 
						proximitySensorAgingTestFail, 
						accelerimeterAgingTestFail, 
						gyroscopeAgingTestFail, 
						ecompassSensorAgingTestFail, 
						"");
		if (ClockTimer!=null) {
			ClockTimer.cancel();
			ClockTimer = null;
		}
		super.onDestroy();
		// CommonDrive.backlightControl(oldLight);
//		powerManager.setBacklightBrightness(oldLight);
	}

	@Override
	public void onPause() {
		super.onPause();
//		Log.d(TAG, "onPause");
		if (ClockTimer!=null) {
			ClockTimer.cancel();
			ClockTimer = null;
		}
		
		isBack = true;
		if (mLightSensor!=null) {
			mSensorManager.unregisterListener(mLightSensorListener);
		}
		if (mProximitySensor!=null) {
			mSensorManager.unregisterListener(mProximitySensorListener);
		}
		if (mTimerReadPsensor!=null) {
			mHandler.removeMessages(PS_DATA_CHANGED);
			mTimerReadPsensor.cancel();
		}
		if (mAccelerimeter!=null) {
			mSensorManager.unregisterListener(mAccelerimeterListener);
		}
		if (mGyroscope!=null) {
			mSensorManager.unregisterListener(mGyroscopeListener);
		}
		if (mEcompassSensor!=null) {
			mSensorManager.unregisterListener(mEcompassListener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		isBack = false;
//		if (mLightSensor == null) {
//			Toast.makeText(getActivity(), "LightSensor is null: ", 2000).show();
//		}
//		boolean bSucceed = mSensorManager.registerListener(
//				mLightSensorListener, mLightSensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
		if (mLightSensor!=null) {
			mSensorManager.registerListener(
					mLightSensorListener, mLightSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			lightSensorAgingTestFail = true;
			txt_1.setText("not found light sensor");
		}
//		boolean bSucceed = mSensorManager.registerListener(
//				mProximitySensorListener, mLightSensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
		if (mProximitySensor!=null) {
			mSensorManager.registerListener(
					mProximitySensorListener, mProximitySensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			txt_2.setText(getString(R.string.proximitysenor_info)+" 1:" + "unchanged");
			mTimerReadPsensor = new Timer();
			mTimerReadPsensor.schedule(new TimerReadPsensor(), 1, 100);
		} else {
			proximitySensorAgingTestFail = true;
			txt_2.setText("not found proximity sensor");
			txt_3.setText("");
		}
		
		if (mAccelerimeter!=null) {
			mSensorManager.registerListener(
					mAccelerimeterListener, mAccelerimeter,
					SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			accelerimeterAgingTestFail = true;
			txt_4.setText("not found accelerimeter");
		}
		
		if (mGyroscope!=null) {
			mSensorManager.registerListener(
					mGyroscopeListener, mGyroscope,
					SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			gyroscopeAgingTestFail = true;
			txt_5.setText("not found gyroscope");
		}
		
		if (mEcompassSensor!=null) {
			mSensorManager.registerListener(
					mEcompassListener, 
					mEcompassSensor, 
					SensorManager.SENSOR_DELAY_GAME);
			arrowView.setVisibility(View.VISIBLE);
			arrowView.setColor(Color.WHITE);
		} else {
			ecompassSensorAgingTestFail = true;
			txt_6.setText("not found ecompass sensor");
			arrowView.setVisibility(View.INVISIBLE);
			arrowView.setColor(0x88888888);
		}
		
//		if (!bSucceed) {
//			Toast.makeText(getActivity(), "registerListener is faild: ", 2000).show();
//		}
		
		if (ClockTimer!=null) {
			ClockTimer.cancel();
			ClockTimer = null;
		}
		ClockTimer = new Timer();
		ClockTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mHandler.sendEmptyMessage(END);
			}
		}, maxTimeLong);
	}

	private final SensorEventListener mLightSensorListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		public void onSensorChanged(SensorEvent event) {
			/*
			 * int degree = 0; try { degree =
			 * Integer.parseInt(CommonDrive.lightDegree().trim());
			 * Log.i("AmbientTest", "original data = " + degree); } catch
			 * (NumberFormatException e) { Log.i("AmbientTest",
			 * "original data is not number."); return; }
			 */

			float lux = event.values[0];
			int blDegree = 0;
			for (int i = 0; i < brights.length; i++) {
				if (i + 1 < brights.length) {
					if (lux >= luxes[i] && lux < luxes[i + 1]) {
						blDegree = brights[i];
						Log.i("AmbientTest", "blDegree = " + blDegree);
					}
				} else {
					if (lux >= luxes[brights.length - 1]) {
						blDegree = brights[brights.length - 1];
						Log.i("AmbientTest", "blDegree = " + blDegree);
					}
				}
			}
			// Add SensorCheck for MotionSensor by xiasiping 20140626 start
			if (SensorchangeTimes == 0L) {
				mValue_lux = lux;
			} else {
				float abs_mChange = Math.abs(lux - mValue_lux);
//				if (lux > 30 && abs_mChange > 10) {
//					btnPass.setEnabled(true);
//				} else if (lux <= 30 && lux >= 10 && abs_mChange > 5) {
//					btnPass.setEnabled(true);
//				} else if (lux < 10 && abs_mChange > 2) {
//					btnPass.setEnabled(true);
//				}
			}
			SensorchangeTimes++;
			// Add SensorCheck for MotionSensor by xiasiping 20140626 end

//			powerManager.setBacklightBrightness(blDegree);
			
			
			// CommonDrive.backlightControl(blDegree);
			txt_1.setText(getString(R.string.lux_data) + lux + "\n"
			// + getString(R.string.original_data) + degree + "\n"
					+ getString(R.string.backlight_data) + blDegree);
		}
	};

	
	private final SensorEventListener mProximitySensorListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		public void onSensorChanged(SensorEvent event) {
			float value = event.values[0];
			
			SensorchangeTimes++;
			
			txt_2.setText(getString(R.string.proximitysenor_info)+" 1:" + value);
		}
	};
	
	class TimerReadPsensor extends TimerTask {
        public void run() {
            int ps_data = -1;
            if (!"unknown".equals(readNode(PS_DATA))) {
                ps_data = Integer.parseInt(readNode(PS_DATA).trim());
            }
            mValue_ps_data = ps_data;
            mHandler.sendEmptyMessage(PS_DATA_CHANGED);
        }
    }
	
	
	private String readNode(String path){
        String procCurrentStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path), 256);
            try {
                procCurrentStr = reader.readLine();
            } finally {
                reader.close();
            }
            return procCurrentStr;
        } catch (IOException e) {
//            e.printStackTrace();
            return "unknown";
        }
    }
	
	private final SensorEventListener mAccelerimeterListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {

            // Gyroscope event.value has 3 equal value.
            synchronized (this) {
            	DecimalFormat decimalFormat = new DecimalFormat();
            	decimalFormat.setMaximumFractionDigits(2);
            	decimalFormat.setMinimumFractionDigits(2);
            	String value = "(" + decimalFormat.format(event.values[0]) + "," 
        				+ decimalFormat.format(event.values[1]) + ","
            			+ decimalFormat.format(event.values[2]) + ")";
            	txt_4.setText("Accelerimeter" + " : " + value);
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    };
    
    private final SensorEventListener mGyroscopeListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {

            // Gyroscope event.value has 3 equal value.
            synchronized (this) {
            	String value = "(\n" + event.values[0] + ",\n" 
        				+ event.values[1] + ",\n"
            			+ event.values[2] + "\n)";
            	txt_5.setText("Gyroscope" + " : " + value);
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    };
	
    private SensorEventListener mEcompassListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            mValues = event.values;
            Log.i(TAG, "mValues[0] = " + mValues[0]);
            if (arrowView != null) {
            	arrowView.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private class ArrowView extends View {

        private Paint mPaint = new Paint();
        private Path mPath = new Path();
        private boolean mAnimate;
        private long mNextTime;

        public ArrowView(Context context) {
            super(context);
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();

            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
//            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.FILL);
        }
        
        public void setColor(int color) {
            mPaint.setColor(color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int cx = w - 60;
            int cy = h - 60;

            canvas.save();
            canvas.translate(cx, cy);
            if (mValues != null) {
                canvas.rotate(-mValues[0]);
                canvas.drawPath(mPath, mPaint);
            }
            canvas.restore();
        }
    }
	
	void pass() {
    	if (mCallback != null) {
    		mCallback.onTestFinish(MainActivity.TEST_PASS);
		}

    }
	
    public FinishCallback getCallback() {
		return mCallback;
	}

	public void setCallback(FinishCallback callback) {
		mCallback = callback;
	}
}
