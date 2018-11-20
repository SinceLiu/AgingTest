package com.dream.agingtest;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResultsActivity extends Activity{
	
	private LinearLayout results_Layout;
	private TextView tv_Battery;

    private boolean hasCamcorderMic = true;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.results);
        
        
        results_Layout = (LinearLayout) findViewById(R.id.results_Layout);
        
        ResultsInformation resultsInformation = 
        		ResultsInformation.getResultsInformation().getLast(this);;
        
        //测试时间
        long s = (resultsInformation.getEndTime()-resultsInformation.getStartTime())/1000;
		long m = s/60;
		s = s%60;
		long h = m/60;
		m = m%60;
        TextView tv_timeLong = new TextView(this);
		if (h < 10) {
			tv_timeLong.setText(new String().format("%02d:%02d:%02d",h,m,s));
		} else {
			tv_timeLong.setText(new String().format("%d:%02d:%02d",h,m,s));
		}
        results_Layout.addView(tv_timeLong);
        
        TextView tv_time = new TextView(this);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        calendar.setTimeInMillis(resultsInformation.getStartTime());
    	String startTimeStr = sdf.format(calendar.getTime());
    	calendar.setTimeInMillis(resultsInformation.getEndTime());
    	String endTimeStr = sdf.format(calendar.getTime());
        tv_time.setText(" from " + startTimeStr + " to " + endTimeStr);
        results_Layout.addView(tv_time);
        
        int i = 0;
        
        //摄像头测试记录
        TextView tv_Camera = new TextView(this);
        i++;
        tv_Camera.setText("\n"+i+".Camera: test times:" + resultsInformation.getCameraAgingTestCount()
        		+ ", wrong times:" + resultsInformation.getCameraAgingTestWrongCount());
        if (resultsInformation.getCameraAgingTestWrongCount()>0) {
        	tv_Camera.setTextColor(MainActivity.textColorWrong);
		}
        results_Layout.addView(tv_Camera);
		
        //屏幕测试记录
        TextView tv_Lcd = new TextView(this);
//        tv_Lcd.setText("\n2.LCD: test times:" + resultsInformation.getLcdAgingTestCount());
        i++;
        tv_Lcd.setText("\n"+i+".Screen: test times:" + resultsInformation.getLcdAgingTestCount());
        results_Layout.addView(tv_Lcd);
        
        //mp4测试记录
        TextView tv_MP4 = new TextView(this);
        i++;
        tv_MP4.setText("\n"+i+".MP4: test times:" + resultsInformation.getMp4AgingTestCount()
        		+ ", wrong times:" + resultsInformation.getMp4AgingTestWrongCount());
        if (resultsInformation.getMp4AgingTestWrongCount()>0) {
        	tv_MP4.setTextColor(MainActivity.textColorWrong);
		}
        results_Layout.addView(tv_MP4);
        
        
        //传感器测试记录
        long[] SensorResults = resultsInformation.getSensorAgingTestCount();
        TextView tv_SensorSum = new TextView(this);
        i++;
        tv_SensorSum.setText("\n"+i+".Sensor: test times:" + SensorResults[0]);
        results_Layout.addView(tv_SensorSum);
        //光感传感器测试记录
        TextView tv_LightSensor = new TextView(this);
        tv_LightSensor.setText("\t LightSensor: test pass times:" + SensorResults[1]);
        if (SensorResults[1] != SensorResults[0]) {
        	if (SensorResults[1] == 0) {
        		tv_LightSensor.setText("\t LightSensor: not found");
			} else {
	        	tv_LightSensor.setTextColor(MainActivity.textColorWrong);
			}
		}
        results_Layout.addView(tv_LightSensor);
        //距离传感器测试记录
        TextView tv_ProximitySensor = new TextView(this);
        tv_ProximitySensor.setText("\t ProximitySensor: test pass times:" + SensorResults[2]);
        if (SensorResults[2] != SensorResults[0]) {
        	if (SensorResults[2] == 0) {
        		tv_ProximitySensor.setText("\t ProximitySensor: not found");
			} else {
	        	tv_ProximitySensor.setTextColor(MainActivity.textColorWrong);
			}
		}
        results_Layout.addView(tv_ProximitySensor);
        //重力传感器测试记录
        TextView tv_Accelerimeter = new TextView(this);
        tv_Accelerimeter.setText("\t Accelerimeter: test pass times:" + SensorResults[3]);
        if (SensorResults[3] != SensorResults[0]) {
        	if (SensorResults[3] == 0) {
        		tv_Accelerimeter.setText("\t Accelerimeter: not found");
			} else {
	        	tv_Accelerimeter.setTextColor(MainActivity.textColorWrong);
			}
		}
        results_Layout.addView(tv_Accelerimeter);
//        //陀螺仪测试记录
//        TextView tv_Gyroscope = new TextView(this);
//        tv_Gyroscope.setText("\t Gyroscope: test pass times:" + SensorResults[4]);
//        if (SensorResults[4] != SensorResults[0]) {
//        	if (SensorResults[4] == 0) {
//        		tv_Gyroscope.setText("\t Gyroscope: not found");
//			} else {
//	        	tv_Gyroscope.setTextColor(MainActivity.textColorWrong);
//			}
//		}
//        results_Layout.addView(tv_Gyroscope);
//        //方向传感器测试记录
//        TextView tv_EcompassSensor = new TextView(this);
//        tv_EcompassSensor.setText("\t EcompassSensor: test pass times:" + SensorResults[5]);
//        if (SensorResults[5] != SensorResults[0]) {
//        	if (SensorResults[5] == 0) {
//        		tv_EcompassSensor.setText("\t EcompassSensor: not found");
//			} else {
//	        	tv_EcompassSensor.setTextColor(MainActivity.textColorWrong);
//			}
//		}
//        results_Layout.addView(tv_EcompassSensor);
        
        
        //wlan测试记录
        TextView tv_Wlan = new TextView(this);
        i++;
        tv_Wlan.setText("\n"+i+".WLAN: test times:" + resultsInformation.getWlanAgingTestCount()
        		+ ", wrong times:" + resultsInformation.getWlanAgingTestWrongCount()
        		+ ", open fail:" + resultsInformation.getWlanAgingTestWrongCountOpenFail()
        		+ ", no results:" + resultsInformation.getWlanAgingTestWrongCountNoResults()
        		);
        if (resultsInformation.getWlanAgingTestWrongCount()>0) {
        	tv_Wlan.setTextColor(MainActivity.textColorWrong);
		}
        results_Layout.addView(tv_Wlan);
      
        //Bluetooth测试记录
        TextView tv_Bluetooth = new TextView(this);
        i++;
        tv_Bluetooth.setText("\n"+i+".Bluetooth: test times:" + resultsInformation.getBluetoothAgingTestCount()
        		+ ", wrong times:" + resultsInformation.getBluetoothAgingTestWrongCount()
        		+ ", open fail:" + resultsInformation.getBluetoothAgingTestWrongCountOpenFail()
        		+ ", no results:" + resultsInformation.getBluetoothAgingTestWrongCountNoResults()
        		);
        if (resultsInformation.getBluetoothAgingTestWrongCount()>0) {
        	tv_Bluetooth.setTextColor(MainActivity.textColorWrong);
		}
        results_Layout.addView(tv_Bluetooth);
        
        /*//GPS测试记录
        TextView tv_GPS = new TextView(this);
        if (isGpsExisted()
        		|| resultsInformation.getGpsAgingTestCount() > 0) {
            i++;
            tv_GPS.setText("\n"+i+".GPS: test times:" + resultsInformation.getGpsAgingTestCount()
            		+ ", wrong times:" + resultsInformation.getGpsAgingTestWrongCount());
            if (resultsInformation.getGpsAgingTestWrongCount()>0) {
            	tv_GPS.setTextColor(MainActivity.textColorWrong);
    		}
		} else {
            i++;
			tv_GPS.setText("\n"+i+".GPS: not found");
		}
        results_Layout.addView(tv_GPS);*/
      
        
        //Mic测试记录
        TextView tv_MIC = new TextView(this);
        i++;
        tv_MIC.setText("\n"+i+".MIC:" );
        results_Layout.addView(tv_MIC);
        
        if(hasCamcorderMic) {
        	TextView tv_CamcorderMic = new TextView(this);
            tv_CamcorderMic.setText("\t Camcorder MIC: test times:" + resultsInformation.getCamcorderMicAgingTestCount()
            		+ ", wrong times:" + resultsInformation.getCamcorderMicAgingTestWrongCount());
            if (resultsInformation.getCamcorderMicAgingTestWrongCount()>0) {
            	tv_CamcorderMic.setTextColor(MainActivity.textColorWrong);
    		}
            results_Layout.addView(tv_CamcorderMic);
        }
        
        TextView tv_NormalMic = new TextView(this);
        tv_NormalMic.setText("\t Normal MIC: test times:" + resultsInformation.getNormalMicAgingTestCount()
        		+ ", wrong times:" + resultsInformation.getNormalMicAgingTestWrongCount());
        if (resultsInformation.getNormalMicAgingTestWrongCount()>0) {
        	tv_NormalMic.setTextColor(MainActivity.textColorWrong);
		}
        results_Layout.addView(tv_NormalMic);
        
        
        //闪光灯测试记录
        boolean hasFlashLight = true;
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH)) {
			hasFlashLight = false;
		}
        TextView tv_FlashLight = new TextView(this);
        if (hasFlashLight
        		|| resultsInformation.getFlashLightAgingTestCount() > 0) {
        	i++;
        	tv_FlashLight.setText("\n"+i+".Flash Light: test times:" + resultsInformation.getFlashLightAgingTestCount()
            		+ ", wrong times:" + resultsInformation.getFlashLightAgingTestWrongCount());
            if (resultsInformation.getFlashLightAgingTestWrongCount()>0) {
            	tv_FlashLight.setTextColor(MainActivity.textColorWrong);
    		}
		} else {
			i++;
			tv_FlashLight.setText("\n"+i+".Flash Light: not found");
		}
        results_Layout.addView(tv_FlashLight);
        
        //马达测试记录
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        TextView tv_Vibrator = new TextView(this);
        if ((vibrator!=null&&vibrator.hasVibrator())
        		|| resultsInformation.getVibrateAgingTestCount() > 0) {
        	i++;
        	tv_Vibrator.setText("\n"+i+".Vibrator: test times:" + resultsInformation.getVibrateAgingTestCount()
            		+ ", wrong times:" + resultsInformation.getVibrateAgingTestWrongCount());
            if (resultsInformation.getVibrateAgingTestWrongCount()>0) {
            	tv_Vibrator.setTextColor(MainActivity.textColorWrong);
    		}
		} else {
			i++;
			tv_Vibrator.setText("\n"+i+".Vibrator: not found");
		}
        results_Layout.addView(tv_Vibrator);
        
        //MusicPlay测试记录
        TextView tv_MusicPlay = new TextView(this);
        i++;
        tv_MusicPlay.setText("\n"+i+".Music:" );
        results_Layout.addView(tv_MusicPlay);
        
        TextView tv_MusicPlayInCall = new TextView(this);
        tv_MusicPlayInCall.setText("\t InCall: test times:"
        		+ resultsInformation.getMusicPlayInCallAgingTestCount());
        results_Layout.addView(tv_MusicPlayInCall);
        
        TextView tv_MusicPlayNoraml = new TextView(this);
        tv_MusicPlayNoraml.setText("\t Noraml: test times:"
        		+ resultsInformation.getMusicPlayNoramlAgingTestCount());
        results_Layout.addView(tv_MusicPlayNoraml);
        
        //电池信息
        tv_Battery = new TextView(this);
        results_Layout.addView(tv_Battery);
	}
	
	private boolean isGpsExisted() {
		LocationManager locationManager = 
				(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if (locationManager == null
				|| locationManager.getProvider(LocationManager.GPS_PROVIDER) == null
				|| !getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			
			Log.d("isGpsExisted", "locationManager:"+locationManager);
			if (locationManager!=null) {
				Log.d("isGpsExisted", "getProvider:"
						+locationManager.getProvider(LocationManager.GPS_PROVIDER));
			}
			Log.d("isGpsExisted", "hasSystemFeature:"
					+getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));
			
			return false;
		}
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
			Log.d("isGpsExisted", "FEATURE_LOCATION:"+false);
			return false;
		}
		
		
//		return true;
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerBatteryReceiver();
	}
	
	@Override
	protected void onPause() {
		unregisterBatteryReceiver();
		super.onPause();
	}
	
	private void registerBatteryReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatteryReceiver, filter);
		batteryInfoSwitch(true);
	}
	private void unregisterBatteryReceiver() {
		batteryInfoSwitch(false);
		unregisterReceiver(mBatteryReceiver);
	}
	private Intent mBatteryIntent;
	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				mBatteryIntent = new Intent(intent);
			}
		}
	};
	
	private Timer batteryTimer;
	private void batteryInfoSwitch(boolean isOpened){
		if (isOpened) {
			if (batteryTimer != null) {
				batteryTimer.cancel();
				batteryTimer = null;
			}
			batteryTimer = new Timer();
			batteryTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					mHandler.post(batteryInfoRunnable);
				}
			}, 0, 500);
		} else {
			if (batteryTimer != null) {
				batteryTimer.cancel();
				batteryTimer = null;
			}
		}
	}
	private Handler mHandler = new Handler();
	Runnable batteryInfoRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (tv_Battery != null&&mBatteryIntent!=null) {
				Bundle bundle = mBatteryIntent.getExtras();
				if (bundle!=null) {
					int status = bundle.getInt(BatteryManager.EXTRA_STATUS);
					String statusStr = "full";
					switch (status) {
					case BatteryManager.BATTERY_STATUS_FULL:
						statusStr = "full";
						break;
					case BatteryManager.BATTERY_STATUS_CHARGING:
						statusStr = "charging";
						break;
					default:
						statusStr = "not charging";
						break;
					}
					int temperature = bundle.getInt(BatteryManager.EXTRA_TEMPERATURE);
					int voltage = bundle.getInt(BatteryManager.EXTRA_VOLTAGE);
					DecimalFormat df = new DecimalFormat(".00");
					String resistance_id = BatteryInfoUtil.readFiles(
							BatteryInfoUtil.PATH_RESISTANCE);
					String current = BatteryInfoUtil.readFiles(
							BatteryInfoUtil.PATH_CURRENT);
					String current2 = BatteryInfoUtil.readFiles(
							BatteryInfoUtil.PATH_CURRENT2);
					

//					Log.i("-----","++++++++++++++++++++++");
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_CAPACITY);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_CURRENT);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_CURRENT2);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_HVDCP3);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_RESISTANCE);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_STATUS);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_TEMP);
//					BatteryInfoUtil.readFiles(
//							BatteryInfoUtil.PATH_VOLTAGE);
//					Log.i("-----","====================");
					
					String displayString = "\n12.Battery:"
							+ "\n\t status: " + statusStr
							+ "\n\t temperature:" + df.format((float)temperature*0.1)+"℃"
							+ "\n\t voltage:" + voltage +"mV";
					
					String current_t = current;
					if (!TextUtils.isEmpty(current)) {
						current_t = current;
					} else if (!TextUtils.isEmpty(current2)) {
						current_t = current2;
					}
					try {
						if (!TextUtils.isEmpty(current_t)) {
							int current_value = Integer.parseInt(current_t)/1000;
							displayString += "\n\t current:" + current_value +"mA";
						}
					} catch (Exception e) {
						if (!TextUtils.isEmpty(current_t)) {
							displayString += "\n\t current:" + current_t +"uA";
						}
					}
					
					if (!TextUtils.isEmpty(resistance_id)) {
						displayString += "\n\t resistance id:" + resistance_id;
					}
					
					tv_Battery.setText(displayString);
				}
			}
		}
	};
}
