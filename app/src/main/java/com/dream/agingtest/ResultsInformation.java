package com.dream.agingtest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;

public class ResultsInformation {

	public static ResultsInformation mResultsInformation;
	
	private long startTime;
	private long endTime;
	
	public static final int TYPE_OPEN_FAIL = 0;
	public static final int TYPE_NO_RESULTS = 1;

	//摄像头测试记录
	private long mCameraAgingTestCount;
	private long mCameraAgingTestWrongCount;
	private List<String> mCameraAgingTestResults;
	
	//屏幕测试记录
	private long mLcdAgingTestCount;
	
	//mp4测试记录
	private long mMp4AgingTestCount;
	private long mMp4AgingTestWrongCount;
	private List<String> mMp4AgingTestResults;
	
	//传感器测试记录
	private long mSensorAgingTestCount;
	private long mLightSensorAgingTestCount;
	private long mProximitySensorAgingTestCount;
	private long mAccelerimeterAgingTestCount;
	private long mGyroscopeAgingTestCount;
	private long mEcompassSensorAgingTestCount;
	
	
	//wlan测试记录
	private long mWlanAgingTestCount;
	private long mWlanAgingTestWrongCount;
	private long mWlanAgingTestWrongCount_openFail;
	private long mWlanAgingTestWrongCount_noResults;
	
	//Bluetooth测试记录
	private long mBluetoothAgingTestCount;
	private long mBluetoothAgingTestWrongCount;
	private long mBluetoothAgingTestWrongCount_openFail;
	private long mBluetoothAgingTestWrongCount_noResults;
	
	//GPS测试记录
	private long mGpsAgingTestCount;
	private long mGpsAgingTestWrongCount;
	
	//Mic测试记录
	private long mCamcorderMicAgingTestCount;
	private long mCamcorderMicAgingTestWrongCount;
	private long mNormalMicAgingTestCount;
	private long mNormalMicAgingTestWrongCount;
	
	//闪光灯测试记录
	private long mFlashLightAgingTestCount;
	private long mFlashLightAgingTestWrongCount;
	
	//马达测试记录
	private long mVibrateAgingTestCount;
	private long mVibrateAgingTestWrongCount;
	
	//喇叭测试记录
	private long mMusicPlayInCallAgingTestCount;
	private long mMusicPlayNoramlAgingTestCount;
	
	public static ResultsInformation getResultsInformation(){
		if (mResultsInformation==null) {
			mResultsInformation = new ResultsInformation();
		}
		return mResultsInformation;
	}
	
	public ResultsInformation() {
		startTime = 0;
		endTime = 0;

		mCameraAgingTestCount = 0;
		mCameraAgingTestWrongCount = 0;
		mCameraAgingTestResults = new ArrayList<String>();
		
		mLcdAgingTestCount = 0;
		
		mMp4AgingTestCount = 0;
		mMp4AgingTestResults = new ArrayList<String>();
		
		mSensorAgingTestCount = 0;
		mLightSensorAgingTestCount = 0;
		mProximitySensorAgingTestCount = 0;
		mAccelerimeterAgingTestCount = 0;
		mGyroscopeAgingTestCount = 0;
		mEcompassSensorAgingTestCount = 0;
		
		
		mWlanAgingTestCount = 0;
		mWlanAgingTestWrongCount = 0;
		mWlanAgingTestWrongCount_openFail = 0;
		mWlanAgingTestWrongCount_noResults = 0;
		
		mBluetoothAgingTestCount = 0;
		mBluetoothAgingTestWrongCount = 0;
		mBluetoothAgingTestWrongCount_openFail = 0;
		mBluetoothAgingTestWrongCount_noResults = 0;
		
		mGpsAgingTestCount = 0;
		mGpsAgingTestWrongCount = 0;
		
		mCamcorderMicAgingTestCount = 0;
		mCamcorderMicAgingTestWrongCount = 0;
		mNormalMicAgingTestCount = 0;
		mNormalMicAgingTestWrongCount = 0;
		
		mFlashLightAgingTestCount = 0;
		mFlashLightAgingTestWrongCount = 0;
		
		mVibrateAgingTestCount = 0;
		mVibrateAgingTestWrongCount = 0;
		
		mMusicPlayInCallAgingTestCount = 0;
		mMusicPlayNoramlAgingTestCount = 0;
	}
	
	public static void clear(){
		mResultsInformation = null;
	}

	public static boolean hadSave(Context context){
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				"AgingTest", Service.MODE_PRIVATE);
		return mSharedPreferences.getLong("startTime", 0) > 0;
	}
	public void save(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				"AgingTest", Service.MODE_PRIVATE);
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		
        edit.putLong("startTime", startTime);
        edit.putLong("endTime", endTime);

        edit.putLong("mCameraAgingTestCount", mCameraAgingTestCount);
        edit.putLong("mCameraAgingTestWrongCount", mCameraAgingTestWrongCount);
        edit.putString("mCameraAgingTestResults", 
        		new JSONArray(mCameraAgingTestResults).toString());

        edit.putLong("mLcdAgingTestCount", mLcdAgingTestCount);
		
        edit.putLong("mMp4AgingTestCount", mMp4AgingTestCount);
        edit.putString("mMp4AgingTestResults", 
        		new JSONArray(mMp4AgingTestResults).toString());
		
        edit.putLong("mSensorAgingTestCount", mSensorAgingTestCount);
        edit.putLong("mLightSensorAgingTestCount", mLightSensorAgingTestCount);
        edit.putLong("mProximitySensorAgingTestCount", mProximitySensorAgingTestCount);
        edit.putLong("mAccelerimeterAgingTestCount", mAccelerimeterAgingTestCount);
        edit.putLong("mGyroscopeAgingTestCount", mGyroscopeAgingTestCount);
        edit.putLong("mEcompassSensorAgingTestCount", mEcompassSensorAgingTestCount);
		
        
        edit.putLong("mWlanAgingTestCount", mWlanAgingTestCount);
        edit.putLong("mWlanAgingTestWrongCount", mWlanAgingTestWrongCount);
        edit.putLong("mWlanAgingTestWrongCount_openFail", mWlanAgingTestWrongCount_openFail);
        edit.putLong("mWlanAgingTestWrongCount_noResults", mWlanAgingTestWrongCount_noResults);

        edit.putLong("mBluetoothAgingTestCount", mBluetoothAgingTestCount);
        edit.putLong("mBluetoothAgingTestWrongCount", mBluetoothAgingTestWrongCount);
        edit.putLong("mBluetoothAgingTestWrongCount_openFail", mBluetoothAgingTestWrongCount_openFail);
        edit.putLong("mBluetoothAgingTestWrongCount_noResults", mBluetoothAgingTestWrongCount_noResults);

        edit.putLong("mGpsAgingTestCount", mGpsAgingTestCount);
        edit.putLong("mGpsAgingTestWrongCount", mGpsAgingTestWrongCount);

        edit.putLong("mCamcorderMicAgingTestCount", mCamcorderMicAgingTestCount);
        edit.putLong("mCamcorderMicAgingTestWrongCount", mCamcorderMicAgingTestWrongCount);
        edit.putLong("mNormalMicAgingTestCount", mNormalMicAgingTestCount);
        edit.putLong("mNormalMicAgingTestWrongCount", mNormalMicAgingTestWrongCount);

        edit.putLong("mFlashLightAgingTestCount", mFlashLightAgingTestCount);
        edit.putLong("mFlashLightAgingTestWrongCount", mFlashLightAgingTestWrongCount);

        edit.putLong("mVibrateAgingTestCount", mVibrateAgingTestCount);
        edit.putLong("mVibrateAgingTestWrongCount", mVibrateAgingTestWrongCount);

        edit.putLong("mMusicPlayInCallAgingTestCount", mMusicPlayInCallAgingTestCount);
        edit.putLong("mMusicPlayNoramlAgingTestCount", mMusicPlayNoramlAgingTestCount);
		edit.commit();
	}
	public ResultsInformation getLast(Context context) {
		if (mResultsInformation==null) {
			mResultsInformation = new ResultsInformation();
		}
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				"AgingTest", Service.MODE_PRIVATE);
		
		startTime = mSharedPreferences.getLong("startTime", startTime);
		endTime = mSharedPreferences.getLong("endTime", endTime);

		mCameraAgingTestCount = mSharedPreferences.getLong("mCameraAgingTestCount", mCameraAgingTestCount);
		mCameraAgingTestWrongCount = mSharedPreferences.getLong("mCameraAgingTestWrongCount", mCameraAgingTestWrongCount);
		JSONArray JSONArray_CameraAgingTestResults;
		try {
			JSONArray_CameraAgingTestResults = new JSONArray(
					mSharedPreferences.getString("mCameraAgingTestResults",""));
			for (int i = 0; i < JSONArray_CameraAgingTestResults.length(); i++) {
				mCameraAgingTestResults.add(JSONArray_CameraAgingTestResults.getString(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mCameraAgingTestResults = new ArrayList<String>();
		}

        mLcdAgingTestCount = mSharedPreferences.getLong("mLcdAgingTestCount", mLcdAgingTestCount);
		
        mMp4AgingTestCount = mSharedPreferences.getLong("mMp4AgingTestCount", mMp4AgingTestCount);
        JSONArray JSONArray_Mp4AgingTestResults;
		try {
			JSONArray_Mp4AgingTestResults = new JSONArray(
					mSharedPreferences.getString("mMp4AgingTestResults",""));
			for (int i = 0; i < JSONArray_Mp4AgingTestResults.length(); i++) {
				mMp4AgingTestResults.add(JSONArray_Mp4AgingTestResults.getString(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mMp4AgingTestResults = new ArrayList<String>();
		}
		
        mSensorAgingTestCount = mSharedPreferences.getLong("mSensorAgingTestCount", mSensorAgingTestCount);
        mLightSensorAgingTestCount = mSharedPreferences.getLong("mLightSensorAgingTestCount", mLightSensorAgingTestCount);
        mProximitySensorAgingTestCount = mSharedPreferences.getLong("mProximitySensorAgingTestCount", mProximitySensorAgingTestCount);
        mAccelerimeterAgingTestCount = mSharedPreferences.getLong("mAccelerimeterAgingTestCount", mAccelerimeterAgingTestCount);
        mGyroscopeAgingTestCount = mSharedPreferences.getLong("mGyroscopeAgingTestCount", mGyroscopeAgingTestCount);
        mEcompassSensorAgingTestCount = mSharedPreferences.getLong("mEcompassSensorAgingTestCount", mEcompassSensorAgingTestCount);
		
        
        mWlanAgingTestCount = mSharedPreferences.getLong("mWlanAgingTestCount", mWlanAgingTestCount);
        mWlanAgingTestWrongCount = mSharedPreferences.getLong("mWlanAgingTestWrongCount", mWlanAgingTestWrongCount);
        mWlanAgingTestWrongCount_openFail = mSharedPreferences.getLong("mWlanAgingTestWrongCount_openFail", mWlanAgingTestWrongCount_openFail);
        mWlanAgingTestWrongCount_noResults = mSharedPreferences.getLong("mWlanAgingTestWrongCount_noResults", mWlanAgingTestWrongCount_noResults);

        mBluetoothAgingTestCount = mSharedPreferences.getLong("mBluetoothAgingTestCount", mBluetoothAgingTestCount);
        mBluetoothAgingTestWrongCount = mSharedPreferences.getLong("mBluetoothAgingTestWrongCount", mBluetoothAgingTestWrongCount);
        mBluetoothAgingTestWrongCount_openFail = mSharedPreferences.getLong("mBluetoothAgingTestWrongCount_openFail", mBluetoothAgingTestWrongCount_openFail);
        mBluetoothAgingTestWrongCount_noResults = mSharedPreferences.getLong("mBluetoothAgingTestWrongCount_noResults", mBluetoothAgingTestWrongCount_noResults);

        mGpsAgingTestCount = mSharedPreferences.getLong("mGpsAgingTestCount", mGpsAgingTestCount);
        mGpsAgingTestWrongCount = mSharedPreferences.getLong("mGpsAgingTestWrongCount", mGpsAgingTestWrongCount);

        mCamcorderMicAgingTestCount = mSharedPreferences.getLong("mCamcorderMicAgingTestCount", mCamcorderMicAgingTestCount);
        mCamcorderMicAgingTestWrongCount = mSharedPreferences.getLong("mCamcorderMicAgingTestWrongCount", mCamcorderMicAgingTestWrongCount);
        mNormalMicAgingTestCount = mSharedPreferences.getLong("mNormalMicAgingTestCount", mNormalMicAgingTestCount);
        mNormalMicAgingTestWrongCount = mSharedPreferences.getLong("mNormalMicAgingTestWrongCount", mNormalMicAgingTestWrongCount);

        mFlashLightAgingTestCount = mSharedPreferences.getLong("mFlashLightAgingTestCount", mFlashLightAgingTestCount);
        mFlashLightAgingTestWrongCount = mSharedPreferences.getLong("mFlashLightAgingTestWrongCount", mFlashLightAgingTestWrongCount);

        mVibrateAgingTestCount = mSharedPreferences.getLong("mVibrateAgingTestCount", mVibrateAgingTestCount);
        mVibrateAgingTestWrongCount = mSharedPreferences.getLong("mVibrateAgingTestWrongCount", mVibrateAgingTestWrongCount);

        mMusicPlayInCallAgingTestCount = mSharedPreferences.getLong("mMusicPlayInCallAgingTestCount", mMusicPlayInCallAgingTestCount);
        mMusicPlayNoramlAgingTestCount = mSharedPreferences.getLong("mMusicPlayNoramlAgingTestCount", mMusicPlayNoramlAgingTestCount);
        

		return mResultsInformation;
	}
	
	public long getStartTime() {
		return startTime;
	}
	public ResultsInformation setStartTime(long startTime) {
		this.startTime = startTime;
		return mResultsInformation;
	}

	public long getEndTime() {
		return endTime;
	}
	public ResultsInformation setEndTime(long endTime) {
		this.endTime = endTime;
		return mResultsInformation;
	}

	
	public List<String> getCameraAgingTestResults() {
		return mCameraAgingTestResults;
	}
	public void addCameraAgingTestResult(boolean fail, String CameraAgingTestResult) {
//		mCameraAgingTestResults.add(CameraAgingTestResult);
		mCameraAgingTestCount++;
		if (fail) {
			mCameraAgingTestWrongCount++;
		}
	}
	public long getCameraAgingTestCount() {
		return mCameraAgingTestCount;
	}
	public long getCameraAgingTestWrongCount() {
		return mCameraAgingTestWrongCount;
	}
	
	public void addLcdAgingTestResult(boolean fail, String LcdTestResult) {
		mLcdAgingTestCount++;
	}
	public long getLcdAgingTestCount() {
		return mLcdAgingTestCount;
	}
	
	public List<String> getMp4AgingTestResults() {
		return mMp4AgingTestResults;
	}
	public void addMp4AgingTestResult(boolean fail, String Mp4TestResult) {
//		mMp4AgingTestResults.add(Mp4TestResult);
		mMp4AgingTestCount++;
		if (fail) {
			mMp4AgingTestWrongCount++;
		}
	}
	public long getMp4AgingTestCount() {
		return mMp4AgingTestCount;
	}
	public long getMp4AgingTestWrongCount() {
		return mMp4AgingTestWrongCount;
	}
	
	public void addSensorAgingTestResult(
			boolean lightSensorAgingTestFail, 
			boolean proximitySensorAgingTestFail, 
			boolean accelerimeterAgingTestFail, 
			boolean gyroscopeAgingTestFail, 
			boolean ecompassSensorAgingTestFail, 
			String SensorTestResult) {
		mSensorAgingTestCount++;
		if (!lightSensorAgingTestFail) {
			mLightSensorAgingTestCount++;
		}
		if (!proximitySensorAgingTestFail) {
			mProximitySensorAgingTestCount++;
		}
		if (!accelerimeterAgingTestFail) {
			mAccelerimeterAgingTestCount++;
		}
		if (!gyroscopeAgingTestFail) {
			mGyroscopeAgingTestCount++;
		}
		if (!ecompassSensorAgingTestFail) {
			mEcompassSensorAgingTestCount++;
		}
	}
	public long[] getSensorAgingTestCount() {
		return new long[]{
				mSensorAgingTestCount, 
				mLightSensorAgingTestCount,
				mProximitySensorAgingTestCount,
				mAccelerimeterAgingTestCount,
				mGyroscopeAgingTestCount,
				mEcompassSensorAgingTestCount};
	}
	
	
	
	
	public long getWlanAgingTestCount() {
		return mWlanAgingTestCount;
	}
	public void setWlanAgingTestCount(long wlanAgingTestCount) {
		mWlanAgingTestCount = wlanAgingTestCount;
	}
	public long getWlanAgingTestWrongCount() {
		return mWlanAgingTestWrongCount;
	}
	public long getWlanAgingTestWrongCountOpenFail() {
		return mWlanAgingTestWrongCount_openFail;
	}
	public long getWlanAgingTestWrongCountNoResults() {
		return mWlanAgingTestWrongCount_noResults;
	}
	public void setWlanAgingTestWrongCount(long wlanAgingTestWrongCount, int type) {
		mWlanAgingTestWrongCount = wlanAgingTestWrongCount;
		switch (type) {
		case TYPE_OPEN_FAIL:
			mWlanAgingTestWrongCount_openFail++;
			break;
		case TYPE_NO_RESULTS:
			mWlanAgingTestWrongCount_noResults++;
			break;
		default:
			break;
		}
	}

	public long getBluetoothAgingTestCount() {
		return mBluetoothAgingTestCount;
	}
	public void setBluetoothAgingTestCount(long bluetoothAgingTestCount) {
		mBluetoothAgingTestCount = bluetoothAgingTestCount;
	}
	public long getBluetoothAgingTestWrongCount() {
		return mBluetoothAgingTestWrongCount;
	}
	public long getBluetoothAgingTestWrongCountOpenFail() {
		return mBluetoothAgingTestWrongCount_openFail;
	}
	public long getBluetoothAgingTestWrongCountNoResults() {
		return mBluetoothAgingTestWrongCount_noResults;
	}
	public void setBluetoothAgingTestWrongCount(long bluetoothAgingTestWrongCount, int type) {
		mBluetoothAgingTestWrongCount = bluetoothAgingTestWrongCount;
		switch (type) {
		case TYPE_OPEN_FAIL:
			mBluetoothAgingTestWrongCount_openFail++;
			break;
		case TYPE_NO_RESULTS:
			mBluetoothAgingTestWrongCount_noResults++;
			break;
		default:
			break;
		}
	}

	public long getGpsAgingTestCount() {
		return mGpsAgingTestCount;
	}
	public void setGpsAgingTestCount(long gpsAgingTestCount) {
		mGpsAgingTestCount = gpsAgingTestCount;
	}
	public long getGpsAgingTestWrongCount() {
		return mGpsAgingTestWrongCount;
	}
	public void setGpsAgingTestWrongCount(long gpsAgingTestWrongCount) {
		mGpsAgingTestWrongCount = gpsAgingTestWrongCount;
	}

	public long getCamcorderMicAgingTestCount() {
		return mCamcorderMicAgingTestCount;
	}
	public void setCamcorderMicAgingTestCount(long camcorderMicAgingTestCount) {
		mCamcorderMicAgingTestCount = camcorderMicAgingTestCount;
	}
	public long getCamcorderMicAgingTestWrongCount() {
		return mCamcorderMicAgingTestWrongCount;
	}
	public void setCamcorderMicAgingTestWrongCount(long camcorderMicAgingTestWrongCount) {
		mCamcorderMicAgingTestWrongCount = camcorderMicAgingTestWrongCount;
	}

	public long getNormalMicAgingTestCount() {
		return mNormalMicAgingTestCount;
	}
	public void setNormalMicAgingTestCount(long normalMicAgingTestCount) {
		mNormalMicAgingTestCount = normalMicAgingTestCount;
	}
	public long getNormalMicAgingTestWrongCount() {
		return mNormalMicAgingTestWrongCount;
	}
	public void setNormalMicAgingTestWrongCount(long normalMicAgingTestWrongCount) {
		mNormalMicAgingTestWrongCount = normalMicAgingTestWrongCount;
	}
	
	

	public long getFlashLightAgingTestCount() {
		return mFlashLightAgingTestCount;
	}
	public void setFlashLightAgingTestCount(long flashLightAgingTestCount) {
		mFlashLightAgingTestCount = flashLightAgingTestCount;
	}
	public long getFlashLightAgingTestWrongCount() {
		return mFlashLightAgingTestWrongCount;
	}
	public void setFlashLightAgingTestWrongCount(long flashLightAgingTestWrongCount) {
		mFlashLightAgingTestWrongCount = flashLightAgingTestWrongCount;
	}

	public long getVibrateAgingTestCount() {
		return mVibrateAgingTestCount;
	}
	public void setVibrateAgingTestCount(long vibrateAgingTestCount) {
		mVibrateAgingTestCount = vibrateAgingTestCount;
	}
	public long getVibrateAgingTestWrongCount() {
		return mVibrateAgingTestWrongCount;
	}
	public void setVibrateAgingTestWrongCount(long vibrateAgingTestWrongCount) {
		mVibrateAgingTestWrongCount = vibrateAgingTestWrongCount;
	}
	
	public long getMusicPlayInCallAgingTestCount() {
		return mMusicPlayInCallAgingTestCount;
	}
	public void setMusicPlayInCallAgingTestCount(long musicPlayInCallAgingTestCount) {
		mMusicPlayInCallAgingTestCount = musicPlayInCallAgingTestCount;
	}
	public long getMusicPlayNoramlAgingTestCount() {
		return mMusicPlayNoramlAgingTestCount;
	}
	public void setMusicPlayNoramlAgingTestCount(long musicPlayNoramlAgingTestCount) {
		mMusicPlayNoramlAgingTestCount = musicPlayNoramlAgingTestCount;
	}
}
