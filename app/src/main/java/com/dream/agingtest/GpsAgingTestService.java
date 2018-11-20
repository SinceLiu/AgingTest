package com.dream.agingtest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.dream.agingtest.BluetoothAgingTestService.MyBinder;

import android.R.color;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
//Modify for passButton clickable when locate success by songguangyu 20140220 start
import android.widget.Button;
//Modify for passButton clickable when locate success by songguangyu 20140220 end

public class GpsAgingTestService extends Service {
    private static final int MAX_SATELITE_COUNT = 50;
    private static final String TAG_GPS = "GPSTest";
    private static String strGpsFilePaht = "";
    private LocationManager m_mgr;
    private Location m_location;
    private GpsStatus m_gpsStatus;
    GpsInfo mGpsInfo[] = new GpsInfo[MAX_SATELITE_COUNT];
    private int mStateliteCount;
    boolean m_isGpsOpen = false;
    private boolean mStartLogGpsData = false;
    Timer st_timer ;
    private int mSecond = 0;
    st_TimerTask timerTask;
    //Modify for passButton clickable when locate success by songguangyu 20140220 start
    private int mLocatetime = 0;
    //Modify for passButton clickable when locate success by songguangyu 20140220 end
    //File gpsFile;
    
    private boolean isWorking = false;
    
    private final int START_DISCOVER = 1234;
    private final int LOCATED_FAIL = 1236;


	private long times = 0;
	
    private int color = MainActivity.textColorNormal;

	private long wrongTimes = 0;

    Handler hGpsHand = new Handler(){
        public void handleMessage(android.os.Message msg) {
//            mtv_Testtime.setText("" + mSecond);
        	switch (msg.what) {
			case START_DISCOVER:
				
				mLocatetime = 0;
				isWorking = false;
				
//				if (!m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//					openGPS();
//		        }
				times++;
				ResultsInformation.getResultsInformation(
        				).setGpsAgingTestCount(times);

	            color = MainActivity.textColorNormal;
	            
		        if (mDisplayTextCallback != null) {
		        	count = 0;
		            mDisplayTextCallback.onTextChanged("GPS:"
		            		+("ON")
		            		+", Found:" + count
	                		+", " +getString(R.string.gps_success)
		            		+", times:" + times
	                		+", wrongTimes:" + wrongTimes,
		            		color);
				}
				
		        registerScreenActionReceiver();
		        onResume();
		        
				break;
			case LOCATED_FAIL:
				Toast.makeText(GpsAgingTestService.this, 
            			R.string.gps_located_fail, Toast.LENGTH_SHORT).show();
            	count = 0;
				onPause();
            	unregisterScreenActionReceiver();
//                deleData();
//                closeGPS();
                

                color = MainActivity.textColorWrong;
            	wrongTimes++;
            	ResultsInformation.getResultsInformation(
        				).setGpsAgingTestWrongCount(wrongTimes);
            	
                if (mDisplayTextCallback != null) {
                    mDisplayTextCallback.onTextChanged("GPS:"
                    		+(m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)?"ON":"OFF")
//                    		+("OFF")
                    		+", Found:" + count
                    		+", " +getResources().getString(R.string.gps_located_fail)
                    		+", times:" + times
                    		+", wrongTimes:" + wrongTimes,
                    		color);
				}
                
                isWorking = false;
                hGpsHand.removeMessages(START_DISCOVER);
                hGpsHand.sendEmptyMessageDelayed(START_DISCOVER,  MainActivity.DELAY_TIME);
                break;
			default:
				break;
			}
        };
    };
    
    public static String ACTION_START_SERVICE = 
			"com.dream.agingtest.GpsAgingTestService.action.START_SERVICE";

    private DisplayTextCallback mDisplayTextCallback = null;
	public void setDisplayTextCallback(DisplayTextCallback displayTextCallback) {
		this.mDisplayTextCallback = displayTextCallback;
	}
	private int count = 0;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		ResultsInformation r = ResultsInformation.getResultsInformation();
		times = r.getGpsAgingTestCount();
		wrongTimes = r.getGpsAgingTestWrongCount();
		return new MyBinder();
	}
    
    public class MyBinder extends Binder {
		/**
		 * 获取当前Service的实例
		 * 
		 * @return
		 */
		public GpsAgingTestService getService() {
			return GpsAgingTestService.this;
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
        strGpsFilePaht="/data/GpsData.txt";
        initAllControl();
        //if(isSDcardexist()){
        boolean bdeleteFile = deleteGpsDataFile(strGpsFilePaht);
        Log.d(TAG_GPS,"The bdeleteFile = " + bdeleteFile);
        //}
        m_mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(m_mgr==null)
        {
            Log.i("lvhongshan_gps", "LocationManager is null");
            
            color = MainActivity.textColorWrong;
//        	wrongTimes++;
//        	ResultsInformation.getResultsInformation(
//    				).setGpsAgingTestWrongCount(wrongTimes);
//            
//            if (mDisplayTextCallback != null) {
//        		count = 0;
//                mDisplayTextCallback.onTextChanged("GPS:"
//                		+("OFF")
//                		+", Found:" + count
//                		+", times:" + times
//                		+", wrongTimes:" + wrongTimes,
//                		color);
//    		}
            if (mDisplayTextCallback != null) {
                mDisplayTextCallback.onTextChanged("GPS:"
                		+("FAIL"),
                		color);
    		}
        }
        else {
            Log.i("lvhongshan_gps", "LocationManager is not null");
//            if (!m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                m_isGpsOpen = true;
//                
//                openGPS();
//                
//            } else {
//    			times ++;
//    		}

            color = MainActivity.textColorNormal;
            
        	if (mDisplayTextCallback != null) {
        		count = 0;
                mDisplayTextCallback.onTextChanged("GPS:"
                		+("ON")
                		+", Found:" + count
                		+", times:" + times
                		+", wrongTimes:" + wrongTimes,
                		color);
    		}
            hGpsHand.sendEmptyMessage(START_DISCOVER);
        }
        
//        registerScreenActionReceiver();
//        onResume();
    }
    
    public void startDisplayText(){
    	if (mDisplayTextCallback != null) {
    		count = 0;
            mDisplayTextCallback.onTextChanged("GPS:"
            		+("ON")
            		+", Found:" + count
            		+", times:" + times
            		+", wrongTimes:" + wrongTimes,
            		MainActivity.textColorNormal);
		}
    }
    
    private void onResume() {
        m_mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
                locationListener);
        mSecond = 0;
        timerTask=new st_TimerTask();
        st_timer=new Timer();
        st_timer.schedule(timerTask, 100, 1000);

        /*if (!m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS provider is disable", 3000).show();
        }*/
        boolean bsucess = m_mgr.addGpsStatusListener(statusListener);

        Log.e(TAG_GPS, "Add the statusListner is" + bsucess);

        if (!bsucess) {
            Toast.makeText(this, R.string.gps_open_error, 3000).show();
        }
        bsucess = m_mgr.addNmeaListener(mNmeaListener);
        Log.e(TAG_GPS, "Add the statusListner is" + bsucess);
        if (!bsucess) {
            Toast.makeText(this, R.string.gps_open_error, 3000).show();
        }

        m_location = m_mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateWithNewLocation(m_location);

    }
    private void onPause() {
        // TODO Auto-generated method stub
        m_mgr.removeUpdates(locationListener);
        m_mgr.removeGpsStatusListener(statusListener);
        m_mgr.removeNmeaListener(mNmeaListener);
        if (st_timer!=null) {
    		st_timer.cancel();
    		st_timer=null;
		}
        /*if (m_isGpsOpen == true) {
            closeGPS();
        }
        mcd.CopyFile("/data/GpsData.txt", Environment.getExternalStorageDirectory()+"/GpsData.txt");*/
    }
    private void registerScreenActionReceiver() {    
	    final IntentFilter filter = new IntentFilter();    
	    filter.addAction(Intent.ACTION_USER_PRESENT);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);    
	    screenActionReceiver = new ScreenActionReceiver();
	    registerReceiver(screenActionReceiver, filter);    
	}
	private ScreenActionReceiver screenActionReceiver;
	private class ScreenActionReceiver extends BroadcastReceiver {    
	    
	    @Override    
	    public void onReceive(final Context context, final Intent intent) {    
	        // Do your action here    
	    	if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) { 
	    		// 锁屏
	    		onResume();
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) { 
            	// 解锁
            	onPause();
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
    	onPause();
        hGpsHand.removeMessages(START_DISCOVER);
        hGpsHand.removeMessages(LOCATED_FAIL);
    	unregisterScreenActionReceiver();
//        deleData();
//        if (m_isGpsOpen == true) {
//            closeGPS();
//        }
    	
    	if (st_timer!=null) {
    		st_timer.cancel();
    		st_timer=null;
		}
        super.onDestroy();
    }
    
    @Override
	public boolean onUnbind(Intent intent) {
    	hGpsHand.removeCallbacksAndMessages(null);
    	stopSelf();
		return super.onUnbind(intent);
	}
    
    public boolean deleteGpsDataFile(String filename){
        boolean bDelete = true;
        File file = new File(filename);
        if(file.exists()){
            bDelete = file.delete();
        }else{
            return false;
        }
        return bDelete;
    }
    class st_TimerTask extends TimerTask {

        public void run() {
            mSecond ++;
            if (mSecond >= 60*5) {
            	hGpsHand.removeMessages(LOCATED_FAIL);
                hGpsHand.sendEmptyMessage(LOCATED_FAIL);
			}
        }

    }

    private synchronized void setLogGpsData(boolean start) {
        mStartLogGpsData  = start;
    }

    private synchronized boolean getLogGpsData() {
        return mStartLogGpsData;
    }

    private void initAllControl() {

        for(int i = 0;i < MAX_SATELITE_COUNT;i ++){
            mGpsInfo[i] = new GpsInfo();
        }
    }

//    private void openGPS() {
//    	Log.d(TAG_GPS, "openGPS");
//        boolean enabled = true;
//        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//        LocationManager.NETWORK_PROVIDER, enabled);
//        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//        LocationManager.GPS_PROVIDER, enabled);
//
//		times ++;
//        if (mDisplayTextCallback != null) {
//        	count = 0;
//            mDisplayTextCallback.onTextChanged("GPS:"
//            		+("ON")
//            		+", Found:" + count
//            		+", times:" + times,
//            		MainActivity.textColorNormal);
//		}
//    }

//    private void closeGPS() {
//    	Log.d(TAG_GPS, "closeGPS");
//        boolean enabled = false;
//        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//        LocationManager.GPS_PROVIDER, enabled);
//        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//        LocationManager.NETWORK_PROVIDER, enabled);
//
//        if (mDisplayTextCallback != null) {
//            mDisplayTextCallback.onTextChanged("GPS:"
//            		+("OFF")
//            		+", Found:" + count
//            		+", times:" + times,
//            		MainActivity.textColorNormal);
//		}
//    }
    private final GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {

        public void onNmeaReceived(long timestamp, String nmea) {
            //if(isSDcardexist()){
            if(getLogGpsData()) {
                updateNmeaStatus(nmea);
                writeNeamDatainfile(nmea);
            }
            //}else{
                //Log.d(TAG_GPS,"The sdcard is not exist");
            //}
        }
    };
    private void updateNmeaStatus(String strNmea){
        Log.d(TAG_GPS,"GPS:data = " + strNmea);
    }
    private boolean isSDcardexist(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    private boolean writeNeamDatainfile(String strNmea){
            boolean bresult = true;
        try {
            File gpsFile = new File(strGpsFilePaht);
            FileWriter fileWriter = new FileWriter(gpsFile,true);

            boolean bcanWrite = gpsFile.canWrite();
            if (bcanWrite) {
                Log.i("lvhongshan_gps", "writeNeamDatainfile is success");
                fileWriter.append(strNmea + "\r\n");
                fileWriter.flush();
            }
            fileWriter.close();

        } catch (IOException e) {
            bresult = false;
            Log.e("LOG_TAG", e.getLocalizedMessage());
        } finally {
        }

        return bresult;
    }
 
    private GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            m_gpsStatus = m_mgr.getGpsStatus(null);
            switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                int nfixTime = m_gpsStatus.getTimeToFirstFix();
                setLogGpsData(true);
                Log.d(TAG_GPS, "GpsStatus.GPS_EVENT_FIRST_FIX the fix Time is " + nfixTime);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.d(TAG_GPS, "GpsStatus.GPS_EVENT_SATELLITE_STATUS");
                Iterable<GpsSatellite> allSatellites;
                allSatellites = m_gpsStatus.getSatellites();
                Iterator it = allSatellites.iterator();
                int iCount = 0;
                while (it.hasNext()) {
                    GpsSatellite satelite = (GpsSatellite) it.next();

                    mGpsInfo[iCount].prn = satelite.getPrn();
                    mGpsInfo[iCount].fAzimuth = satelite.getAzimuth();
                    mGpsInfo[iCount].fElevation = satelite.getElevation();
                    mGpsInfo[iCount].snr = satelite.getSnr();
                    mGpsInfo[iCount].iID = iCount;

                    iCount++;

//                    Log.d(TAG_GPS, "mGpsInfo[iCount].prn is " + mGpsInfo[iCount].prn);
//                    Log.d(TAG_GPS, "mGpsInfo[iCount].fAzimuth is " + mGpsInfo[iCount].fAzimuth);
//                    Log.d(TAG_GPS, "mGpsInfo[iCount].fElevation" + mGpsInfo[iCount].fElevation);
//                    Log.d(TAG_GPS, "mGpsInfo[iCount].snr" + mGpsInfo[iCount].snr);
//                    Log.d(TAG_GPS, "mGpsInfo[iCount].iID" + mGpsInfo[iCount].iID);

                }
                mStateliteCount = iCount;
                if (iCount > 0) {
                	mSecond = 0;
				}
                Log.d(TAG_GPS, "the mStateliteCount is" + mStateliteCount);
                setStateliteinfo(iCount);
                
                m_location = m_mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateWithNewLocation(m_location);
                
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                // Event sent when the GPS system has started.
                Log.d(TAG_GPS, "GpsStatus.GPS_EVENT_STARTED");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                // Event sent when the GPS system has stopped.
                Log.d(TAG_GPS, "GpsStatus.GPS_EVENT_STOPPED");
                break;
            default:
                break;
            }
        }
    };

    private void setStateliteinfo(int validsatelite) {
    	count = validsatelite;
        int ncount = 6;

        int bdIndex = 6;
        int gpsIndex = 0;
        for (int i = 0; i < validsatelite; i++) {
            if(mGpsInfo[i].prn < 32 && gpsIndex < ncount){
//                tvSatelite[gpsIndex].setText("ID:" + (gpsIndex + 1) + "  " + mStrSnr + mGpsInfo[i].snr
//                    + "  " + mStrPrn + mGpsInfo[i].prn + "\n" + mStrAzimuth + "  " +
//                    + mGpsInfo[i].fAzimuth + "  " + mStrElevation +
//                    + mGpsInfo[i].fElevation);
                gpsIndex ++;
            }else if(mGpsInfo[i].prn >= 32 && bdIndex < ncount * 2){
//                tvSatelite[bdIndex].setText("ID:" + (gpsIndex + 1) + "  " + mStrSnr + mGpsInfo[i].snr
//                    + "  " + mStrPrn + mGpsInfo[i].prn + "\n" + mStrAzimuth + "  " +
//                    + mGpsInfo[i].fAzimuth + "  " + mStrElevation +
//                    + mGpsInfo[i].fElevation);
                bdIndex ++;
            }
        }
        //Modify for passButton clickable when locate success by songguangyu 20140220 start
        if (validsatelite > 0 && !isWorking) {
        	isWorking = true;
        	Toast.makeText(this, R.string.gps_success, Toast.LENGTH_SHORT).show();
        	
            color = MainActivity.textColorNormal;
        	
        	if (mDisplayTextCallback != null) {
            	mDisplayTextCallback.onTextChanged("GPS:"
                		+(m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)?"ON":"OFF")
                		+", Found:" + count
                		+", " +getString(R.string.gps_success)
                		+", times:" + times
                		+", wrongTimes:" + wrongTimes,
                		color);
			}
		}
        if (validsatelite >= 4) {
            mLocatetime ++;
        }
        if (validsatelite >= 4 && mLocatetime <= 1) {
            Toast.makeText(this, R.string.gps_located, Toast.LENGTH_SHORT).show();
            
            
            onPause();
//        	unregisterScreenActionReceiver();
//            deleData();
//            closeGPS();
            
            color = MainActivity.textColorNormal;
            
            if (mDisplayTextCallback != null) {
                mDisplayTextCallback.onTextChanged("GPS:"
                		+(m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)?"ON":"OFF")
//                		+("OFF")
                		+", Found:" + count
                		+", " +getResources().getString(R.string.gps_located)
                		+", times:" + times
                		+", wrongTimes:" + wrongTimes,
                		color);
			}
            
            isWorking = false;
            hGpsHand.removeMessages(START_DISCOVER);
            hGpsHand.sendEmptyMessageDelayed(START_DISCOVER,  MainActivity.DELAY_TIME);
        }
        
        //Modify for passButton clickable when locate success by songguangyu 20140220 end
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            Log.d(TAG_GPS, "lvhongshan the onLocationChanged is exced");
        }

        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d(TAG_GPS, "lvhongshan the onProviderDisabled is exced");
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG_GPS, "lvhongshan the onProviderEnabled is exced");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG_GPS, "lvhongshan the onStatusChanged is exced");
        }
    };

    private void updateWithNewLocation(Location location) {
    }

    private class GpsInfo {
        int prn;
        int iID;
        private float fAzimuth;
        private float fElevation;
        private float snr;

        public GpsInfo() {
            prn = 0;
            iID = 0;
            fAzimuth = 0;
            fElevation = 0;
            snr = 0;
        }
    }

//    private void deleData(){
//        Log.i("xsp_delete_gps_data", "deleData() is start");
//        String str = "";
//        if(m_mgr.sendExtraCommand("gps", "delete_aiding_data", null)){
//            str = getString(R.string.del_success);
//        }else {
//            str = getString(R.string.del_fail);
//        }
//        Log.i("xsp_delete_gps_data", "deleData()  str is "+str);
//        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
//        toast.show();
//        Log.i("xsp_delete_gps_data", "deleData() is end");
//    }
}
