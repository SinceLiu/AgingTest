package com.dream.agingtest;

import com.dream.agingtest.WlanAgingTestService.MyBinder;

import android.R.color;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothAgingTestService extends Service{
    private static final String TAG = "BluetoothAgingTestService";
    private BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice device=null;
    IntentFilter intentFilter;
    private boolean isExit = false;
    
    private boolean isDiscoverying = false;

	public static String ACTION_START_SERVICE = 
			"com.dream.agingtest.BluetoothAgingTestService.action.START_SERVICE";
    
	private final int START_DISCOVER = 123;
	
	private int deviceCount = 0;
	
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
		times = r.getBluetoothAgingTestCount();
		wrongTimes = r.getBluetoothAgingTestWrongCount();
		return new MyBinder();
	}
	
	public class MyBinder extends Binder {
		/**
		 * 获取当前Service的实例
		 * 
		 * @return
		 */
		public BluetoothAgingTestService getService() {
			return BluetoothAgingTestService.this;
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (mBluetoothAdapter == null) {
        	Toast.makeText(BluetoothAgingTestService.this, 
        			"Bluetooth service isn't available", 
        			Toast.LENGTH_SHORT).show();
        	
        	color = MainActivity.textColorWrong;
        	
        	times++;
			ResultsInformation.getResultsInformation(
    				).setBluetoothAgingTestCount(times);
        	wrongTimes++;
        	ResultsInformation.getResultsInformation(
    				).setBluetoothAgingTestWrongCount(wrongTimes, 
    						ResultsInformation.TYPE_OPEN_FAIL);
        	
//        	if (mDisplayTextCallback != null) {
//            	mDisplayTextCallback.onTextChanged("Bluetooth:"
//                		+("OFF")
//                		+", Found:" + count
//                		+", times:" + times
//                		+", wrongTimes:" + wrongTimes,
//                		MainActivity.textColorWrong);
//			}
        	if (mDisplayTextCallback != null) {
            	mDisplayTextCallback.onTextChanged("Bluetooth:"
                		+("FAIL"),
                		MainActivity.textColorWrong);
			}
		} else {

	        intentFilter=new IntentFilter();
	        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
	        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
	        this.registerReceiver(blueToothReceiver,intentFilter);
			
			color = MainActivity.textColorNormal;
			
			if (mDisplayTextCallback != null) {
				mDisplayTextCallback.onTextChanged("Bluetooth:"
	            		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
	            		+", Found:" + count
	            		+", times:" + times
	            		+", wrongTimes:" + wrongTimes,
                		MainActivity.textColorNormal);
			}
			

	        mHandler.removeMessages(START_DISCOVER);
	        mHandler.sendMessage(mHandler.obtainMessage(START_DISCOVER));
	        
	        registerScreenActionReceiver();
		}
        
    }
    
    public void startDisplayText(){
    	if (mBluetoothAdapter == null) {
    		color = MainActivity.textColorWrong;
    		if (mDisplayTextCallback != null) {
            	mDisplayTextCallback.onTextChanged("Bluetooth:"
                		+("OFF")
                		+", Found:" + count
                		+", times:" + times
                		+", wrongTimes:" + wrongTimes,
                		color);
			}
		} else {
			if (mDisplayTextCallback != null) {
				mDisplayTextCallback.onTextChanged("Bluetooth:"
	            		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
	            		+", Found:" + count
	            		+", times:" + times
	            		+", wrongTimes:" + wrongTimes,
                		MainActivity.textColorNormal);
			}
		}
    }
    
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
	    		if (mBluetoothAdapter.isDiscovering()) {
	                mBluetoothAdapter.cancelDiscovery();
	            }
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) { 
            	// 解锁
            	isExit = false;
            	mHandler.removeMessages(START_DISCOVER);
                mHandler.sendMessage(mHandler.obtainMessage(START_DISCOVER));
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
    	unregisterReceiver(blueToothReceiver);
    	unregisterScreenActionReceiver();
    	if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    	if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            mBluetoothAdapter.disable();
        }
        super.onDestroy();
    }
    
    @Override
	public boolean onUnbind(Intent intent) {
		mHandler.removeCallbacksAndMessages(null);
    	stopSelf();
		return super.onUnbind(intent);
	}
    
    private BroadcastReceiver blueToothReceiver=new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    Log.i("progress", "BluetoothAdapter.getState()==true");
                    mHandler.removeMessages(START_DISCOVER);
                    mHandler.sendMessage(mHandler.obtainMessage(START_DISCOVER));
                }
                
                if (mDisplayTextCallback != null) {
                    mDisplayTextCallback.onTextChanged("Bluetooth:"
                    		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
                    		+", Found:" + count
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                text_bluetooth_info.setText(getString(R.string.bluetooth_setingpage)+"\n"+"Address="+device.getAddress()+"\n"+
//                                "name ="+device.getName());
//                mBluetoothAdapter.cancelDiscovery();
//                Toast.makeText(BluetoothAgingTestService.this, "find new divece name="+device.getAddress(), Toast.LENGTH_SHORT).show();
//                mHandler.removeMessages(START_DISCOVER);
//                mHandler.sendMessage(mHandler.obtainMessage(START_DISCOVER));
                if (device != null) {
                	deviceCount++;
                	color = MainActivity.textColorNormal;
				}
                if (mDisplayTextCallback != null) {
                    count = deviceCount;
                    mDisplayTextCallback.onTextChanged("Bluetooth:"
                    		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
                    		+", Found:" + count
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}
                Log.i("progress", "BluetoothDevice.ACTION_FOUND device="+device.getAddress());
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)
            		&& isDiscoverying) {
            	isDiscoverying = false;
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter.disable();
                mHandler.removeMessages(START_DISCOVER);
                mHandler.sendEmptyMessageDelayed(START_DISCOVER, MainActivity.DELAY_TIME);
                Log.i("progress", "BluetoothDevice.ACTION_DISCOVERY_FINISHED");
                
                color = MainActivity.textColorNormal;
                if (deviceCount > 0) {
                	Toast.makeText(BluetoothAgingTestService.this, 
                			"Buletooth:found "+deviceCount+" new diveces", 
                			Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(BluetoothAgingTestService.this, 
                			"Buletooth:not found divece", 
                			Toast.LENGTH_SHORT).show();
					color = MainActivity.textColorWrong;
		        	wrongTimes++;
		        	ResultsInformation.getResultsInformation(
		    				).setBluetoothAgingTestWrongCount(wrongTimes, 
            						ResultsInformation.TYPE_NO_RESULTS);
				}
                
                if (mDisplayTextCallback != null) {
                    count = deviceCount;
                    mDisplayTextCallback.onTextChanged("Bluetooth:"
                    		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
                    		+", Found:" + count
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}
                
                deviceCount = 0;
            }
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
			case START_DISCOVER:
				Log.d("START_DISCOVER", ""+START_DISCOVER);
				if (device!=null) {
//	                passButton.setEnabled(true);
	            }
	            
	            if (isExit) {
					break;
				}

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	            if (mBluetoothAdapter==null
	            		||mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
	                
	                if (mBluetoothAdapter==null) {
	                	times++;
	        			ResultsInformation.getResultsInformation(
	            				).setBluetoothAgingTestCount(times);
	                	wrongTimes++;
	                	ResultsInformation.getResultsInformation(
	            				).setBluetoothAgingTestWrongCount(wrongTimes, 
	            						ResultsInformation.TYPE_OPEN_FAIL);
					} else {
		                mBluetoothAdapter.enable();
					}
	                
	            }else if (!mBluetoothAdapter.isDiscovering()) {
	                mBluetoothAdapter.startDiscovery();
	                isDiscoverying = true;
					times++;
					ResultsInformation.getResultsInformation(
	        				).setBluetoothAgingTestCount(times);
					color = MainActivity.textColorNormal;
	                if (mDisplayTextCallback != null) {
	                    count = 0;
	                    mDisplayTextCallback.onTextChanged("Bluetooth:"
	                    		+(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON?"ON":"OFF")
	                    		+", Found:" + count
	                    		+", times:" + times
	                    		+", wrongTimes:" + wrongTimes,
	                    		color);
					}
	            }
				break;

			default:
				break;
			}
            
        }
    };
}
