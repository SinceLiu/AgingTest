package com.dream.agingtest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WlanAgingTestService extends Service{
	private static final String TAG = "WlanAgingTestService";
	private WifiManager wm;
	IntentFilter intentFilter;

    private boolean is24g = false;
    private boolean is5g = false;
    private final int START_SCAN_WLAN = 123;
    private final int GET_SCAN_RESULTS = 124;
    private boolean isExit = false;
    List<ScanResult> scanResults=new ArrayList<ScanResult>();
	public static String ACTION_START_SERVICE = 
			"com.dream.agingtest.WlanAgingTestService.action.START_SERVICE";

	private DisplayTextCallback mDisplayTextCallback = null;
	public void setDisplayTextCallback(DisplayTextCallback displayTextCallback) {
		this.mDisplayTextCallback = displayTextCallback;
	}
	private int count = 0;
	
	private long times = 0;
	
	private int color = MainActivity.textColorNormal;
	
	private long wrongTimes = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		ResultsInformation r = ResultsInformation.getResultsInformation();
		times = r.getWlanAgingTestCount();
		wrongTimes = r.getWlanAgingTestWrongCount();
		return new MyBinder();
	}
	
	public class MyBinder extends Binder {
		/**
		 * 获取当前Service的实例
		 * 
		 * @return
		 */
		public WlanAgingTestService getService() {
			return WlanAgingTestService.this;
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
    
    @Override
	public void onCreate() {
    	super.onCreate();
        wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter=new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        this.registerReceiver(wifiReceiver,intentFilter);
        Log.i("progress", "onCreate enable="+wm.isWifiEnabled());
//        if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
//        	wm.setWifiEnabled(true);
//            Log.i("progress", "WifiManager.setWifiEnabled(true)");
//        }else if(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
//            //wm.startScan();
//            mHandler.removeMessages(START_SCAN_WLAN);
//            mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
//        }
        
        if (wm == null
        		|| wm.getWifiState() == WifiManager.WIFI_STATE_UNKNOWN) {
        	if (wm == null) {
            	Toast.makeText(WlanAgingTestService.this, 
            			"WIFI service isn't available", 
            			Toast.LENGTH_SHORT).show();
			}
            
        	color = MainActivity.textColorWrong;
        	
        	times++;
        	ResultsInformation.getResultsInformation(
    				).setWlanAgingTestCount(times);
        	wrongTimes++;
        	ResultsInformation.getResultsInformation(
    				).setWlanAgingTestWrongCount(wrongTimes, 
    						ResultsInformation.TYPE_OPEN_FAIL);
        	
//            if (mDisplayTextCallback != null) {
//                mDisplayTextCallback.onTextChanged("WLAN:"
//                		+("OFF")
//                		+", Found:" + count
//                		+", times:" + times
//                		+", wrongTimes:" + wrongTimes,
//                		MainActivity.textColorWrong);
//    		}
        	if (mDisplayTextCallback != null) {
                mDisplayTextCallback.onTextChanged("WLAN:"
                		+("FAIL"),
                		MainActivity.textColorWrong);
    		}
		} else {
			
			color = MainActivity.textColorNormal;
			
	        if (mDisplayTextCallback != null) {
	            mDisplayTextCallback.onTextChanged("WLAN:"
	            		+(wm.isWifiEnabled()?"ON":"OFF")
	            		+", Found:" + count
	            		+", times:" + times
                		+", wrongTimes:" + wrongTimes,
	            		MainActivity.textColorNormal);
			}

	        Log.d(TAG, ""+wm.getConnectionInfo().toString());
	        Log.d(TAG, ""+wm.getWifiState());
	        mHandler.removeMessages(START_SCAN_WLAN);
	        mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
	        
	        registerScreenActionReceiver();
		}
    }
    
    public void startDisplayText(){
    	if (mDisplayTextCallback != null) {
            mDisplayTextCallback.onTextChanged("WLAN:"
            		+(wm.isWifiEnabled()?"ON":"OFF")
            		+", Found:" + count
            		+", times:" + times
            		+", wrongTimes:" + wrongTimes,
            		MainActivity.textColorNormal);
		}
    }
    
    private BroadcastReceiver wifiReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
			Log.e("WlanAgingTestService","---action:"+action );
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                Log.i("progress", "WifiManager.WIFI_STATE_CHANGED_ACTION enable="+wm.isWifiEnabled());
                if (wm.isWifiEnabled()) {
                    //wm.startScan();
                    mHandler.removeMessages(START_SCAN_WLAN);
                    mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
//                    mHandler.sendMessageDelayed(mHandler.obtainMessage(START_SCAN_WLAN), 1000);
                }
//                else {
//                	wm.setWifiEnabled(true);
//                }
                
                if (mDisplayTextCallback != null) {
                    mDisplayTextCallback.onTextChanged("WLAN:"
                    		+(wm.isWifiEnabled()?"ON":"OFF")
                    		+", Found:" + count
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}

                Log.d(TAG, ""+wm.getWifiState());
            }
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.endsWith(action)) {
                scanResults=wm.getScanResults();
                Log.e("WlanAgingTestService","wifi :"+scanResults.toString());
                mHandler.removeMessages(GET_SCAN_RESULTS);
                mHandler.sendMessage(mHandler.obtainMessage(GET_SCAN_RESULTS));
                Log.i("progress", "WifiManager.SCAN_RESULTS_AVAILABLE_ACTION result="+scanResults.size());
            }
        }
    };
    
    private void registerScreenActionReceiver() {    
	    final IntentFilter filter = new IntentFilter();    
	    filter.addAction(Intent.ACTION_USER_PRESENT);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);    
	    registerReceiver(screenActionReceiver, filter);    
	}
	private BroadcastReceiver screenActionReceiver = new BroadcastReceiver(){    
	    
	    @Override    
	    public void onReceive(final Context context, final Intent intent) {    
	        // Do your action here    
	    	if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) { 
	    		// 锁屏
	    		isExit = true;
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) { 
            	// 解锁
            	isExit = false;
            	mHandler.removeMessages(START_SCAN_WLAN);
                mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
            }
	    }    
	    
	};
	private void unregisterScreenActionReceiver() {
		if (screenActionReceiver != null) {
			unregisterReceiver(screenActionReceiver);
			screenActionReceiver = null;
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(wifiReceiver);
		unregisterScreenActionReceiver();
		if (wm.isWifiEnabled()) {
			wm.setWifiEnabled(false);
			Log.i("progress", "onDestroy_WifiManager.setWifiEnabled(false)");
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		mHandler.removeCallbacksAndMessages(null);
		stopSelf();
		return super.onUnbind(intent);
	}
	
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
            case START_SCAN_WLAN:
//                sendEmptyMessageDelayed(START_SCAN_WLAN, 10*1000);
                if (isExit){
                    break;
                }
                if (!wm.isWifiEnabled()) {
                	if (wm.getWifiState() == WifiManager.WIFI_STATE_UNKNOWN) {
                		times++;
                    	ResultsInformation.getResultsInformation(
                				).setWlanAgingTestCount(times);
                    	wrongTimes++;
                    	ResultsInformation.getResultsInformation(
                				).setWlanAgingTestWrongCount(wrongTimes, 
                						ResultsInformation.TYPE_OPEN_FAIL);
					} 
                	wm.setWifiEnabled(true);
				} else {
					Log.i("progress", "startScan");
	                wm.startScan();
	                times++;
	                ResultsInformation.getResultsInformation(
	        				).setWlanAgingTestCount(times);

	                color = MainActivity.textColorNormal;
	                if (mDisplayTextCallback != null) {
	                    count = 0;
	                    mDisplayTextCallback.onTextChanged("WLAN:"
	                    		+(wm.isWifiEnabled()?"ON":"OFF")
	                    		+", Found:" + count
	                    		+", times:" + times
	                    		+", wrongTimes:" + wrongTimes,
	                    		color);
					}
				}
                break;
            case GET_SCAN_RESULTS:
                if (isExit){
                    break;
                }
                is5g = false;
                is24g = false;
                int size=scanResults.size();
                color = MainActivity.textColorNormal;
                if (size > 0) {
                	Toast.makeText(WlanAgingTestService.this, 
                			"WLAN:found "+size+" new points", 
                			Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(WlanAgingTestService.this, 
                			"WLAN:not found points", 
                			Toast.LENGTH_SHORT).show();
					color = MainActivity.textColorWrong;
		        	wrongTimes++;
		        	ResultsInformation.getResultsInformation(
	        				).setWlanAgingTestWrongCount(wrongTimes, 
	        						ResultsInformation.TYPE_NO_RESULTS);
				}
                
                if (mDisplayTextCallback != null) {
                    count = size;
                    mDisplayTextCallback.onTextChanged("WLAN:"
                    		+(wm.isWifiEnabled()?"ON":"OFF")
                    		+", Found:" + count
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}
                
                if (size>0) {
                    /*text_wifi_info.setText(getString(R.string.wifisettins_page)+"\n"+
                    scanResults.get(0).toString()
                    );*/
                	String str5g = getString(R.string.wifisettins_5g);
                	String str24g = getString(R.string.wifisettins_24g);
                    for (ScanResult sc : scanResults) {
                        if (sc.frequency < 5900 
                        		&& sc.frequency > 4900) {
//                            text_wifi_5g.setText(getString(R.string.wifisettins_5g) + "" + sc.SSID);
                            if (!is5g) {
                            	str5g += "\n"+ sc.SSID + ": " + sc.frequency;
							} else {
								str5g += ", \n"+ sc.SSID + ": " + sc.frequency;
							}
                            is5g = true;
                        }
                        if (sc.frequency < 2500 
                        		&& sc.frequency > 2400) {
                        	if (!is24g) {
                        		str24g += "\n"+ sc.SSID + ": " + sc.frequency;
							} else {
								str24g += ", \n"+ sc.SSID + ": " + sc.frequency;
							}
                            is24g = true;
                        }
                    }
                    Log.i("cit_wifi", "is 5g :[" + is5g + "] and is 2.4g :[" + is24g + "]");
                }
                removeMessages(START_SCAN_WLAN);
                sendEmptyMessageDelayed(START_SCAN_WLAN, MainActivity.DELAY_TIME);
                Log.d("START_SCAN_WLAN", ""+START_SCAN_WLAN);
                
                if (wm.isWifiEnabled()) {
        			wm.setWifiEnabled(false);
        			Log.i("progress", "setWifiEnabled(false)");
        		}
                
                break;
                /*Intent i = new Intent();
                i.setClassName("com.android.settings",
                "com.android.settings.wifi.WifiSettings");
                startActivity(i);*/
            }
        }
    };

}
