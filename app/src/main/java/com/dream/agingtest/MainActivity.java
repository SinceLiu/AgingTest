package com.dream.agingtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.dream.agingtest.MicAgingTestService.MicTestMusicCallback;

import android.Manifest;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class MainActivity extends Activity {
	
	public final static int TEST_PASS = 1;
	public final static int TEST_FAIL = 0;
	
	public static final long DELAY_TIME_TEST = 5*1000;
	public static final long DELAY_TIME = 60*1000;
//	public static final long DELAY_TIME = DELAY_TIME_TEST;
	
	private long startTime = 0l;
	private long lastTime = 0l;
	
	FlashlightController flashlightController;
	private Camera mCamera = null;
	
	int curTypeIndex = 0;
	
	private boolean isStarted = false;
	
	private Timer mCountTimer;
	
	private View rootView = null;
	private TextView tvs[] = {null,null,null,null,null,null,null,null};
	private TextView tvTimeRecord = null;
	private TextView tvTimesCount = null;
	
	public static int textColorNormal = 0xff888888;
	public static int textColorWrong = 0xffff0000;
	
	private int testPoint = 13;

	private boolean isPause = false;
	private boolean isPlayingRecoding = false;
	private int timesFlashLightPlay = 0;
	private int wrongTimesFlashLightPlay = 0;
	private Timer vibrateTimer = null;
	private Vibrator vibrator = null;
	private int timesVibratePlay = 0;
	private int wrongTimesVibratePlay = 0;
	private int timesMusicPlay_incall = 0;
	private int timesMusicPlay_noraml = 0;
	private MediaPlayer mpBgMusic = null;
	private AudioManager am;
	private boolean isCallMusicSwitchOn = false;
	private Fragment f;
	private Intent mBatteryIntent;
	private Timer batteryTimer = null;
	
	private WlanAgingTestService mWlanAgingTestService;
	private ServiceConnection mWlanAgingTestServiceConnection;
	private void bindWlanAgingTestService() {
		mWlanAgingTestServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mWlanAgingTestService = null;
				mWlanAgingTestService.setDisplayTextCallback(null);
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				//返回一个MyService对象
				mWlanAgingTestService = ((WlanAgingTestService.MyBinder)service).getService();
				mWlanAgingTestService.setDisplayTextCallback(new DisplayTextCallback() {
					
					@Override
					public void onTextChanged(String text, int color) {
						// TODO Auto-generated method stub
						if (tvs[0] != null) {
							tvs[0].setText(text);
							tvs[0].setTextColor(color);
						}
					}
				});
				mWlanAgingTestService.startDisplayText();
			}
		};
		
		Intent intent = new Intent(WlanAgingTestService.ACTION_START_SERVICE);
		//5.0后service必须显性启动
		intent.setPackage(getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		
		if(!bindService(intent, mWlanAgingTestServiceConnection, Service.BIND_AUTO_CREATE))
		{
//			Utils.showToast(this, "服务绑定失败，请重试。", Toast.LENGTH_LONG);
//			finish();
		}
	}
	
	private BluetoothAgingTestService mBluetoothAgingTestService;
	private ServiceConnection mBluetoothAgingTestServiceConnection;
	private void bindBluetoothAgingTestService() {
		mBluetoothAgingTestServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mBluetoothAgingTestService = null;
				mBluetoothAgingTestService.setDisplayTextCallback(null);
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				//返回一个MyService对象
				mBluetoothAgingTestService = ((BluetoothAgingTestService.MyBinder)service).getService();
				mBluetoothAgingTestService.setDisplayTextCallback(new DisplayTextCallback() {
					
					@Override
					public void onTextChanged(String text, int color) {
						// TODO Auto-generated method stub
						if (tvs[1] != null) {
							tvs[1].setText(text);
							tvs[1].setTextColor(color);
						}
					}
				});
				mBluetoothAgingTestService.startDisplayText();
			}
		};
		
		Intent intent = new Intent(BluetoothAgingTestService.ACTION_START_SERVICE);
		//5.0后service必须显性启动
		intent.setPackage(getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		
		if(!bindService(intent, mBluetoothAgingTestServiceConnection, Service.BIND_AUTO_CREATE))
		{
//			Utils.showToast(this, "服务绑定失败，请重试。", Toast.LENGTH_LONG);
//			finish();
		}
	}
	
	private GpsAgingTestService mGpsAgingTestService;
	private ServiceConnection mGpsAgingTestServiceConnection;
	private void bindGpsAgingTestService() {
		mGpsAgingTestServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mGpsAgingTestService = null;
				mGpsAgingTestService.setDisplayTextCallback(null);
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				//返回一个MyService对象
				mGpsAgingTestService = ((GpsAgingTestService.MyBinder)service).getService();
				mGpsAgingTestService.setDisplayTextCallback(new DisplayTextCallback() {
					
					@Override
					public void onTextChanged(String text, int color) {
						// TODO Auto-generated method stub
						if (tvs[2] != null) {
							tvs[2].setText(text);
							tvs[2].setTextColor(color);
						}
					}
				});
				mGpsAgingTestService.startDisplayText();
			}
		};
		
		Intent intent = new Intent(GpsAgingTestService.ACTION_START_SERVICE);
		//5.0后service必须显性启动
		intent.setPackage(getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		
		if(!bindService(intent, mGpsAgingTestServiceConnection, Service.BIND_AUTO_CREATE))
		{
//			Utils.showToast(this, "服务绑定失败，请重试。", Toast.LENGTH_LONG);
//			finish();
		}
	}
	
	private MicAgingTestService mMicAgingTestService;
	private ServiceConnection mMicAgingTestServiceConnection;
	private void bindMicAgingTestService() {
		mMicAgingTestServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mMicAgingTestService = null;
				mMicAgingTestService.setDisplayTextCallback(null);
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				//返回一个MyService对象
				mMicAgingTestService = ((MicAgingTestService.MyBinder)service).getService();
				mMicAgingTestService.setDisplayTextCallback(new DisplayTextCallback() {
					
					@Override
					public void onTextChanged(String text, int color) {
						// TODO Auto-generated method stub
						if (tvs[3] != null) {
							tvs[3].setText(text);
							tvs[3].setTextColor(color);
						}
					}
				});
				mMicAgingTestService.setMicTestMusicCallback(new MicTestMusicCallback() {
					
					@Override
					public boolean onStart() {
						isPlayingRecoding = true;
						backgroundMusicPause();
						return isPause;
					}
					@Override
					public boolean onEnd() {
						isPlayingRecoding = false;
						backgroundMusicResume();
						return isPause;
					}
				});
				mMicAgingTestService.startDisplayText();
			}
		};
		
		Intent intent = new Intent(MicAgingTestService.ACTION_START_SERVICE);
		//5.0后service必须显性启动
		intent.setPackage(getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		
		if(!bindService(intent, mMicAgingTestServiceConnection, Service.BIND_AUTO_CREATE))
		{
//			Utils.showToast(this, "服务绑定失败，请重试。", Toast.LENGTH_LONG);
//			finish();
		}
	}
	
	private void bindService(){
		bindWlanAgingTestService();
		bindBluetoothAgingTestService();
		if (isGpsExisted()) {
			bindGpsAgingTestService();
		}
		bindMicAgingTestService();
	}
	
	private void unbindService(){
		if (mWlanAgingTestServiceConnection != null) {
			unbindService(mWlanAgingTestServiceConnection);
			mWlanAgingTestServiceConnection = null;
		}
		if (mBluetoothAgingTestServiceConnection != null) {
			unbindService(mBluetoothAgingTestServiceConnection);
			mBluetoothAgingTestServiceConnection = null;
		}
		if (mGpsAgingTestServiceConnection != null) {
			unbindService(mGpsAgingTestServiceConnection);
			mGpsAgingTestServiceConnection = null;
		}
		if (mMicAgingTestServiceConnection != null) {
			unbindService(mMicAgingTestServiceConnection);
			mMicAgingTestServiceConnection = null;
		}
	}
	
	private void resumeService(){
		bindWlanAgingTestService();
		bindBluetoothAgingTestService();
		if (isGpsExisted()
				&& isLocationOpen()) {
			bindGpsAgingTestService();
		}
		
//		bindMicAgingTestService();
//		isPlayingRecoding = false;
		if (mMicAgingTestService!=null) {
			mMicAgingTestService.resume();
		}
	}
	
	private void pauseService(){
		if (mWlanAgingTestServiceConnection != null) {
			unbindService(mWlanAgingTestServiceConnection);
			mWlanAgingTestServiceConnection = null;
		}
		if (mBluetoothAgingTestServiceConnection != null) {
			unbindService(mBluetoothAgingTestServiceConnection);
			mBluetoothAgingTestServiceConnection = null;
		}
		if (mGpsAgingTestServiceConnection != null) {
			unbindService(mGpsAgingTestServiceConnection);
			mGpsAgingTestServiceConnection = null;
		}
		
//		if (mMicAgingTestServiceConnection != null) {
//			unbindService(mMicAgingTestServiceConnection);
//			mMicAgingTestServiceConnection = null;
//		}
		if (mMicAgingTestService!=null) {
			mMicAgingTestService.pause();
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (isStarted) {
				ResultsInformation.getResultsInformation().setEndTime(
						System.currentTimeMillis());
				ResultsInformation.getResultsInformation().save(this);
			}
			
//			flashLightSwitch(false);
			vibrateSwitch(false);
			backgroundMusicSwitch(false);
			flashLightSwitch(false);
			batteryInfoSwitch(false);
			curTypeIndex = 0;
			unbindService();
			rootView = null;
			findViewById(R.id.btn_start).setEnabled(true);
			findViewById(R.id.btn_last_results).setEnabled(true);
			isStarted = false;
			
			if (mCountTimer != null) {
				mCountTimer.cancel();
			}
		}
		
//		if (keyCode == KeyEvent.KEYCODE_MENU
//				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
//				|| keyCode == KeyEvent.KEYCODE_SEARCH
//				|| keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
//			return true;
//		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		if (keyCode == KeyEvent.KEYCODE_MENU
//				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
//				|| keyCode == KeyEvent.KEYCODE_SEARCH
//				|| keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
//			return true;
//		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        
        initViews();
//        flashLightSwitch(true);
        
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            flashlightController = new FlashlightController(this);
		}
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		if (mpBgMusic!=null) {
			mpBgMusic.stop();
			mpBgMusic.release();
			mpBgMusic = null;
		}
		
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		isPause = true;
		unregisterBatteryReceiver();
		if (!isPlayingRecoding) {
			backgroundMusicPause();
		}
		
		if (mCountTimer != null) {
			mCountTimer.cancel();
		}
		
		vibrateSwitch(false);
		flashLightSwitch(false);
		batteryInfoSwitch(false);
		
		pauseService();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isPause = false;
		if (isStarted) {
			resumeService();
			
			if (curTypeIndex%testPoint == 2) {
				flashLightSwitch(true);
			}
			
			vibrateSwitch(true);
			batteryInfoSwitch(true);
			
			if (mCountTimer != null) {
				mCountTimer.cancel();
			}
			mCountTimer = new Timer();
			mCountTimer.schedule(new TimeRecordTask(), 0, 500);
			
			if (!isPlayingRecoding) {
				backgroundMusicResume();
			}
		}
        registerBatteryReceiver();
	}
	
	private boolean checkPermission() {
		boolean isAllPermissionGranted = true;
		if (Build.VERSION.SDK_INT >= 24){
//		if (Build.VERSION.SDK_INT >= 23){
			PackageManager pm = getPackageManager();
			List<String> permissionsUnGrantedList = new ArrayList<String>();
			try {
				String[] permissions = pm.getPackageInfo(
						getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
				for (int i = 0; i < permissions.length; i++) {
					int permission = ContextCompat.checkSelfPermission(
							this, 
							permissions[i]);
					if (permission != PackageManager.PERMISSION_GRANTED) {
						permissionsUnGrantedList.add(permissions[i]);
					}
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (permissionsUnGrantedList.size()>0) {
				isAllPermissionGranted = false;
				String[] permissions_un_granted = new String[permissionsUnGrantedList.size()];
				permissionsUnGrantedList.toArray(permissions_un_granted);

				Log.d("checkPermission", permissionsUnGrantedList.toString());
				
				if (permissions_un_granted!=null&&permissions_un_granted.length > 0) {
					 ActivityCompat.requestPermissions(this,
							 permissions_un_granted,
							 0);
				}
			}
		}
		return isAllPermissionGranted;
	}
	
	/**
	 * 
	 */
	private void initViews(){

		if (!ResultsInformation.hadSave(this)) {
			findViewById(R.id.btn_last_results).setVisibility(View.GONE);
		}
		
		findViewById(R.id.btn_start).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
//				arg0.setEnabled(false);
				if (!checkPermission()) {
					return;
				}
				if (isGpsExisted()&&!isLocationOpen()) {
					openLocationSettingsDialog();
				} else {
					arg0.setEnabled(false);
					findViewById(R.id.btn_last_results).setVisibility(View.VISIBLE);
					findViewById(R.id.btn_last_results).setEnabled(false);
					bindService();
					// TODO Auto-generated method stub
					timesFlashLightPlay = 0;
					wrongTimesFlashLightPlay = 0;
					timesVibratePlay = 0;
					wrongTimesVibratePlay = 0;
					vibrateSwitch(true);
					batteryInfoSwitch(true);
					timesMusicPlay_incall = 0;
					timesMusicPlay_noraml = 0;
					backgroundMusicSwitch(true);
					curTypeIndex = 0;
					
					isPause = false;
					isPlayingRecoding = false;
					
					startTime = System.currentTimeMillis();
					ResultsInformation.clear();
					ResultsInformation.getResultsInformation(
							).setStartTime(startTime).setEndTime(startTime);
					
					autoAgingTest();
					if (mCountTimer != null) {
						mCountTimer.cancel();
					}
					mCountTimer = new Timer();
					mCountTimer.schedule(new TimeRecordTask(), 0, 500);
					isStarted = true;
				}
			}
		});
        
		findViewById(R.id.btn_last_results).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ResultsActivity.class));
			}
		});
		
//        findViewById(R.id.btn_camera).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				curTypeIndex = 0;
//		        startCameraAgingTest(false);
//			}
//		});
//        
//        findViewById(R.id.btn_lcd).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				curTypeIndex = 1;
//				startLcdAgingTest(false);
//			}
//		});
//        
//        findViewById(R.id.btn_mp4).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				curTypeIndex = 2;
//				startMP4AgingTest(false);
//			}
//		});
//        
//        findViewById(R.id.btn_sensor).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				curTypeIndex = 3;
//				startSensorAgingTest(false);
//			}
//		});
	}
	
	
	private void openLocationSettingsDialog(){
		if (isGpsExisted()&&!isLocationOpen()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("请打开定位系统")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					openLocationSettings();
				}
			})
			.setNegativeButton("取消", null)
			.show();
		}
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
	
	private boolean isLocationOpen(){
		LocationManager locationManager = 
				(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if (locationManager == null) {
			return true;
		}
		
		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
			return true;
		}
		
		boolean isGPSOpen = locationManager.isProviderEnabled(
				LocationManager.GPS_PROVIDER);
		boolean isNetworkOpen = locationManager.isProviderEnabled(
				LocationManager.NETWORK_PROVIDER);
		
		return isGPSOpen || isNetworkOpen;
	}
	
	private void openLocationSettings(){
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, 0);
	}
	
	
	class TimeRecordTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(TIME_UPDATE);
		}
	}
	private final static int TIME_UPDATE = 0x1101;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case TIME_UPDATE:
				if (tvTimeRecord!=null) {
					long s = (System.currentTimeMillis()-startTime)/1000;
					long m = s/60;
					s = s%60;
					long h = m/60;
					m = m%60;
					if (h < 10) {
						tvTimeRecord.setText(new String().format("%02d:%02d:%02d",h,m,s));
					} else {
						tvTimeRecord.setText(new String().format("%d:%02d:%02d",h,m,s));
					}
				}
				break;

			default:
				break;
			}
		}
	};
	
	private void autoAgingTest(){
		Log.d("curTypeIndex", ""+curTypeIndex);
		long timelong = 0l;
		if (lastTime != 0) {
			long now = System.currentTimeMillis();
			timelong = now - lastTime;
			lastTime = now;
			Log.d("autoAgingTest", "timelong:"+timelong);
		} else {
			lastTime = startTime;
		}
		Log.d("autoAgingTest", "lastTime:"+lastTime);
		switch (curTypeIndex%testPoint) {
		case 0:
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			startCameraAgingTest(true);
			break;
		case 1:
//			new Handler().postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					flashLightSwitch(true);
//				}
//			}, 50);
			startLcdAgingTest(true);
			break;
		case 2:
			boolean isCallMusicSwitchOnT = isCallMusicSwitchOn;
			backgroundMusicSwitch(false);
			startMP4AgingTest(true);
			isCallMusicSwitchOn = isCallMusicSwitchOnT;
			flashLightSwitch(true);
			break;
		case 3:
			backgroundMusicSwitch(true);
			flashLightSwitch(false);
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			startSensorAgingTest(true);
			break;
		default:
			curTypeIndex = 0;
			rootView = null;
			break;
		}
		

		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				rootView = null;
				rootView = f.getView();

				
				if (rootView != null) {
					LinearLayout layout = new LinearLayout(MainActivity.this);
					layout.setOrientation(LinearLayout.VERTICAL);
					LayoutParams lp = new LayoutParams(
							LayoutParams.WRAP_CONTENT, 
							LayoutParams.WRAP_CONTENT, 
							Gravity.BOTTOM);
					((FrameLayout)rootView).addView(layout, lp);
					layout.setVisibility(View.GONE);

//					List<String> strings = new ArrayList<String>();
//					for (int i = 0; i < tvs.length; i++) {
//						if (tvs[i] != null) {
//							strings.add(i, tvs[i].getText().toString());
//							tvs[i] = null;
//						} else {
//							strings.add(i, "");
//						}
//					}
					
					for (int i = 0; i < tvs.length; i++) {
						if (tvs[i] == null) {
							tvs[i] = new TextView(MainActivity.this);
							tvs[i].setTextColor(textColorNormal);
						} else {
							if (tvs[i].getParent() != null) {
								((LinearLayout)tvs[i].getParent()).removeView(tvs[i]);
							}
						}
						layout.addView(tvs[i],lp);
					}
					
					if (tvTimeRecord == null) {
						tvTimeRecord = new TextView(MainActivity.this);
						tvTimeRecord.setText("00:00:00");
						tvTimeRecord.setTextColor(textColorNormal);
					} else {
						((FrameLayout)tvTimeRecord.getParent()).removeView(tvTimeRecord);
					}
					LayoutParams lpTimer = new LayoutParams(
							LayoutParams.WRAP_CONTENT, 
							LayoutParams.WRAP_CONTENT, 
							Gravity.TOP);
					((FrameLayout)rootView).addView(tvTimeRecord, lpTimer);
					tvTimeRecord.setVisibility(View.GONE);
					
					if (tvTimesCount == null) {
						tvTimesCount = new TextView(MainActivity.this);
						tvTimesCount.setText("0");
						tvTimesCount.setTextColor(textColorNormal);
					} else {
						if ((FrameLayout)tvTimesCount.getParent()!=null) {
							((FrameLayout)tvTimesCount.getParent()).removeView(tvTimesCount);
						}
					}
					LayoutParams lpTimes = new LayoutParams(
							LayoutParams.WRAP_CONTENT, 
							LayoutParams.WRAP_CONTENT, 
							Gravity.TOP|Gravity.END);
					((FrameLayout)rootView).addView(tvTimesCount, lpTimes);
					tvTimesCount.setVisibility(View.GONE);
					
//					for (int i = 0; i < 4; i++) {
//						Button btn = new Button(MainActivity.this);
//						btn.setText("1365165165156");
//						layout.addView(btn,lp);
//					}
				}
			}
		}, 100);
	}
	
	private void oneItemAgingTest(){
		Log.d("curTypeIndex", ""+curTypeIndex);
		switch (curTypeIndex%testPoint) {
		case 0:
			startCameraAgingTest(false);
			break;
		case 1:
			startLcdAgingTest(false);
			break;
		case 2:
			startMP4AgingTest(false);
			break;
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			startSensorAgingTest(false);
			break;
		default:
			curTypeIndex = 0;
			break;
		}
	}
	
	FinishCallback endCallback = new FinishCallback() {
		
		@Override
		public void onTestFinish(int tag) {
			// TODO Auto-generated method stub
			getFragmentManager().popBackStack();
			curTypeIndex+=testPoint;
			oneItemAgingTest();
		}
	};
	
	FinishCallback nextCallback = new FinishCallback() {
		
		@Override
		public void onTestFinish(int tag) {
			// TODO Auto-generated method stub
			getFragmentManager().popBackStack();
			System.gc();
			curTypeIndex++;
			autoAgingTest();
		}
	};
	
	CountChangeCallback countChangeCallback = new CountChangeCallback() {
		
		@Override
		public void onCountChange(int maxCount, int count, int color) {
			// TODO Auto-generated method stub
			int newCount = curTypeIndex/testPoint*maxCount + count;
			if (tvTimesCount==null) {
				tvTimesCount = new TextView(MainActivity.this);
				tvTimesCount.setTextColor(textColorNormal);
			}
			tvTimesCount.setTextColor(color);
			tvTimesCount.setText("times:"+newCount);
		}
	};
	
	private void flashLightSwitch(boolean isOpened){
		try {
			if (tvs[6] == null) {
	        	tvs[6] = new TextView(this);
	        	tvs[6].setTextColor(textColorNormal);
			}
			tvs[6].setText("FlashLight:"
            		+"times:" + timesFlashLightPlay
            		+", wrongTimes:" + wrongTimesFlashLightPlay
            		);
			
			boolean hasFlashLight = true;
			if (!getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA_FLASH)) {
				hasFlashLight = false;
			}
			final boolean t_hasFlashLight = hasFlashLight;
			if (!t_hasFlashLight){
				if (timesFlashLightPlay == 0 && wrongTimesFlashLightPlay == 0) {
        			tvs[6].setText("not found flash light");
            		tvs[6].setTextColor(textColorNormal);
				} else {
	        		tvs[6].append(", not found flash light");
	        		tvs[6].setTextColor(textColorWrong);
				}
        		return;
	        } else {
        		tvs[6].setTextColor(textColorNormal);
			}
			
			if (isOpened) {
				
				Log.d("flashLightSwitch", "on:"+mCamera);
				if (android.os.Build.VERSION.SDK_INT >= 23) {
					if (!flashlightController.setFlashlight(true)) {
						wrongTimesFlashLightPlay++;
						ResultsInformation.getResultsInformation(
								).setFlashLightAgingTestWrongCount(wrongTimesFlashLightPlay);
					}
				} else {
					if (mCamera == null) {
						mCamera = Camera.open();
					}
					if (mCamera == null) {
						wrongTimesFlashLightPlay++;
						ResultsInformation.getResultsInformation(
								).setFlashLightAgingTestWrongCount(wrongTimesFlashLightPlay);
					}
					
					Camera.Parameters mParameters = mCamera.getParameters();
					mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					mCamera.setParameters(mParameters);
				}
				
				tvs[6].post(new Runnable() {
					
					@Override
					public void run() {
						timesFlashLightPlay++;
						ResultsInformation.getResultsInformation(
								).setFlashLightAgingTestCount(timesFlashLightPlay);
						tvs[6].setText("FlashLight:"
			            		+"times:" + timesFlashLightPlay
			            		+", wrongTimes:" + wrongTimesFlashLightPlay
			            		);
			        	if (!t_hasFlashLight){
			        		if (timesFlashLightPlay == 0 && wrongTimesFlashLightPlay == 0) {
			        			tvs[6].setText("not found flash light");
			            		tvs[6].setTextColor(textColorNormal);
							} else {
				        		tvs[6].append(", not found flash light");
				        		tvs[6].setTextColor(textColorWrong);
							}
				        } else {
			        		tvs[6].setTextColor(textColorNormal);
						}
					}
				});
			} else {
				if (android.os.Build.VERSION.SDK_INT >= 23) {
					flashlightController.setFlashlight(false);
				} else {
					if (mCamera != null) {
						Log.d("flashLightSwitch", "off");
						Camera.Parameters mParameters = mCamera.getParameters();
						mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(mParameters);
						mCamera.release();
						mCamera = null;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("flashLightSwitch", "open fail");
		}
	}
	
	private void vibrateSwitch(boolean isOpened){
		Log.d("vibrateSwithc", "vibrateSwithc:"+isOpened);
		if (isOpened) {

			if (tvs[5] == null) {
	        	tvs[5] = new TextView(this);
	        	tvs[5].setTextColor(textColorNormal);
			}
			tvs[5].setText("Vibrate:"
            		+"times:" + timesVibratePlay
            		+", wrongTimes:" + wrongTimesVibratePlay
            		);
			
			if (vibrator == null) {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		        if (vibrator == null||!vibrator.hasVibrator()){
	        		if (timesVibratePlay == 0 && wrongTimesVibratePlay == 0) {
	        			tvs[5].setText("not found vibrator");
		        		tvs[5].setTextColor(textColorNormal);
					} else {
			            tvs[5].append(", not found vibrator");
		        		tvs[5].setTextColor(textColorWrong);
					}
		            return;
		        } else {
	        		tvs[5].setTextColor(textColorNormal);
				}
			} else if (!vibrator.hasVibrator()){
				if (timesVibratePlay == 0 && wrongTimesVibratePlay == 0) {
        			tvs[5].setText("not found vibrator");
	        		tvs[5].setTextColor(textColorNormal);
				} else {
		            tvs[5].append(", not found vibrator");
	        		tvs[5].setTextColor(textColorWrong);
				}
	            return;
			}
			
			vibrateTimer = new Timer();
			vibrateTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					if (vibrator != null) {
						timesVibratePlay++;
						ResultsInformation.getResultsInformation(
								).setVibrateAgingTestCount(timesVibratePlay);
						vibrator.vibrate(1000);
						tvs[5].post(new Runnable() {
							
							@Override
							public void run() {
								if (!vibrator.hasVibrator()){
									wrongTimesVibratePlay++;
									ResultsInformation.getResultsInformation(
											).setVibrateAgingTestWrongCount(wrongTimesVibratePlay);
						        }
					        	tvs[5].setText("Vibrate:"
					            		+"times:" + timesVibratePlay
					            		+", wrongTimes:" + wrongTimesVibratePlay
					            		);
					        	if (!vibrator.hasVibrator()){
					        		if (timesVibratePlay == 0 && wrongTimesVibratePlay == 0) {
					        			tvs[5].setText("not found vibrator");
						        		tvs[5].setTextColor(textColorNormal);
									} else {
							            tvs[5].append(", not found vibrator");
						        		tvs[5].setTextColor(textColorWrong);
									}
						        } else {
					        		tvs[5].setTextColor(textColorNormal);
								}
							}
						});
//				        vibrator.vibrate(new long[] { 0, 1000, 1000}, 0);
//				        vibrator.vibrate(new long[] { 0, 1000, 1000, 1000, 1000 }, -1);
					}
				}
			}, 0, DELAY_TIME);
		} else {
			if (vibrator != null) {
		        vibrator.cancel();
			}
			if (vibrateTimer != null) {
				vibrateTimer.cancel();
				vibrateTimer = null;
			}
		}
	}

	private float volumeScale = 1.0f;
//	private float volumeScale = 0.1f;
	private void backgroundMusicSwitch(boolean isOpened){
		if (isOpened) {
//			timesMusicPlay++;
			inCallMusicSwitch(!isCallMusicSwitchOn);

			if (isCallMusicSwitchOn) {
				timesMusicPlay_incall++;
				ResultsInformation.getResultsInformation(
						).setMusicPlayInCallAgingTestCount(timesMusicPlay_incall);
			} else {
				timesMusicPlay_noraml++;
				ResultsInformation.getResultsInformation(
						).setMusicPlayNoramlAgingTestCount(timesMusicPlay_noraml);
			}
			
			if (tvs[4] == null) {
	        	tvs[4] = new TextView(this);
	        	tvs[4].setTextColor(textColorNormal);
			}
			tvs[4].setText("Music:"+"now:"+(isCallMusicSwitchOn?"MODE_IN_CALL":"MODE_NORMAL")
					+ ", MODE_IN_CALL--times:" + timesMusicPlay_incall
            		+ ", MODE_NORMAL--times:" + timesMusicPlay_noraml);
			
//			if (mpBgMusic == null) {
//				mpBgMusic = MediaPlayer.create(MainActivity.this, R.raw.music);
//				mpBgMusic.setVolume(1.0f, 1.0f);
////				mpBgMusic.setLooping(true); 
//			}
			Log.e("MainActivity","111111create前 mpBgMusic: "+mpBgMusic);
			if (mpBgMusic!=null) {
				mpBgMusic.stop();
				mpBgMusic.release();
				mpBgMusic = null;
			}
			mpBgMusic = MediaPlayer.create(MainActivity.this, R.raw.music);
			Log.e("MainActivity","111111111create后 mpBgMusic: "+mpBgMusic);
            mpBgMusic.setVolume(1.0f, 1.0f);
			mpBgMusic.start();
			mpBgMusic.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
//					timesMusicPlay++;
					inCallMusicSwitch(!isCallMusicSwitchOn);
					if (isCallMusicSwitchOn) {
						timesMusicPlay_incall++;
						ResultsInformation.getResultsInformation(
								).setMusicPlayInCallAgingTestCount(timesMusicPlay_incall);
					} else {
						timesMusicPlay_noraml++;
						ResultsInformation.getResultsInformation(
								).setMusicPlayNoramlAgingTestCount(timesMusicPlay_noraml);
					}
					if (mpBgMusic!=null) {
						mpBgMusic.stop();
						mpBgMusic.release();
						mpBgMusic = null;
					}
					mpBgMusic = MediaPlayer.create(MainActivity.this, R.raw.music);
					mpBgMusic.setVolume(1.0f, 1.0f);
					mpBgMusic.start();
					mpBgMusic.setOnCompletionListener(this);
					tvs[4].post(new Runnable() {
						
						@Override
						public void run() {
							tvs[4].setText("Music:"
									+ "now:"+(isCallMusicSwitchOn?"MODE_IN_CALL":"MODE_NORMAL")
									+ ", MODE_IN_CALL--times:" + timesMusicPlay_incall
				            		+ ", MODE_NORMAL--times:" + timesMusicPlay_noraml);
						}
					});
				}
			});
			

//	        am.setStreamVolume(AudioManager.STREAM_MUSIC, 
//	        		(int) (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*volumeScale), 
//	        		0);
			
		} else {
			if (mpBgMusic != null) {
				mpBgMusic.stop();
				mpBgMusic.release();
				mpBgMusic = null;
				inCallMusicSwitch(false);
			}
		}
	}
	
	private void backgroundMusicPause(){
		if (mpBgMusic != null) {
			mpBgMusic.pause();
		}
		
		if (curTypeIndex%testPoint == 2) {
			((MP4AgingTestFragment)f).playerPause();
		}
	}
	private void backgroundMusicResume(){
//		if (mpBgMusic != null) {
//			mpBgMusic.start();
//		}
//		
//		if (curTypeIndex%testPoint == 2) {
//			((MP4AgingTestFragment)f).onPause();
//			((MP4AgingTestFragment)f).onResume();
//		}
		
		if (curTypeIndex%testPoint != 2) {
			if (mpBgMusic != null && !isPause) {
				mpBgMusic.start();
			}
		} else {
			((MP4AgingTestFragment)f).playerResume();
		} 
	}
	
	private void inCallMusicSwitch(boolean isOpened){
		if (isOpened) {
			am = (AudioManager) getSystemService(AUDIO_SERVICE);
	        am.setMode(AudioManager.MODE_IN_CALL);
		} else {
			am = (AudioManager) getSystemService(AUDIO_SERVICE);
			am.setMode(AudioManager.MODE_NORMAL);
		}
		isCallMusicSwitchOn = isOpened;
	}
    
	private void startCameraAgingTest(boolean isAuto){
		CameraAgingTestFragment fragment = CameraAgingTestFragment.newInstance();
		if (isAuto) {
			fragment.setCallback(nextCallback);
		} else {
			fragment.setCallback(endCallback);
		}
		fragment.setCountChangeCallback(countChangeCallback);
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_Layout, fragment, CameraAgingTestFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();

		f = fragment;
	}
	
	private void startLcdAgingTest(boolean isAuto){
		LcdAgingTestFragment fragment = LcdAgingTestFragment.newInstance();
		if (isAuto) {
			fragment.setCallback(nextCallback);
		} else {
			fragment.setCallback(endCallback);
		}
		fragment.setCountChangeCallback(countChangeCallback);
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_Layout, fragment, LcdAgingTestFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
		
		
		f = fragment;
	}
	
	private void startMP4AgingTest(boolean isAuto){
		MP4AgingTestFragment fragment = MP4AgingTestFragment.newInstance();
		Bundle b = new Bundle();
		b.putBoolean("isPlayingRecoding", isPlayingRecoding);
		fragment.setArguments(b);
		if (isAuto) {
			fragment.setCallback(nextCallback);
		} else {
			fragment.setCallback(endCallback);
		}
		fragment.setCountChangeCallback(countChangeCallback);
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_Layout, fragment, MP4AgingTestFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
		
		f = fragment;
	}
	
	private void startSensorAgingTest(boolean isAuto){
		SensorAgingTestFragment fragment = SensorAgingTestFragment.newInstance();
		if (isAuto) {
			fragment.setCallback(nextCallback);
		} else {
			fragment.setCallback(endCallback);
		}
		if (countChangeCallback!=null) {
			countChangeCallback.onCountChange(10, (curTypeIndex%13)-2, textColorNormal);
		}
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.main_Layout, fragment, SensorAgingTestFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
		
		f = fragment;
	}
	
	
	private void registerBatteryReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatteryReceiver, filter);
	}
	private void unregisterBatteryReceiver() {
		unregisterReceiver(mBatteryReceiver);
	}
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
	Runnable batteryInfoRunnable = new Runnable() {
		
		@Override
		public void run() {
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
				
				if (tvs[7] == null) {
		        	tvs[7] = new TextView(MainActivity.this);
		        	tvs[7].setTextColor(textColorNormal);
				}
				
				String displayString = "Battery:"+" status: " + statusStr
						+ ", temperature:" + df.format((float)temperature*0.1)+"℃"
						+ ", voltage:" + voltage +"mV";
				
				String current_t = current;
				if (!TextUtils.isEmpty(current)) {
					current_t = current;
				} else if (!TextUtils.isEmpty(current2)) {
					current_t = current2;
				}
				try {
					if (!TextUtils.isEmpty(current_t)) {
						int current_value = Integer.parseInt(current_t)/1000;
						displayString += ", current:" + current_value +"mA";
					}
				} catch (Exception e) {
					if (!TextUtils.isEmpty(current_t)) {
						displayString += ", current:" + current_t +"uA";
					}
				}
				
				if (!TextUtils.isEmpty(resistance_id)) {
					displayString += ", resistance id:" + resistance_id;
				}
				
				tvs[7].setText(displayString);
			}
		}
	};
}
