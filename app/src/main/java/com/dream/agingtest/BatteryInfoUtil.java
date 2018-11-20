package com.dream.agingtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;

public class BatteryInfoUtil {

	private static String TAG = "BatteryInfoUtil";
	
	public static final String PATH_STATUS = "/sys/class/power_supply/battery/status";
	public static final String PATH_CAPACITY = "/sys/class/power_supply/battery/capacity";
	public static final String PATH_TEMP = "/sys/class/power_supply/battery/temp";
	public static final String PATH_RESISTANCE = "/sys/class/power_supply/battery/resistance_id";
	public static final String PATH_VOLTAGE = "/sys/class/power_supply/battery/voltage_now";
	public static final String PATH_CURRENT = "/sys/class/power_supply/battery/flash_current_max";
	public static final String PATH_CURRENT2 = "/sys/class/power_supply/battery/current_now";
	public static final String PATH_HVDCP3 = "/sys/class/power_supply/battery/allow_hvdcp3";
	
	public static String readFiles(String path){
		File f = new File(path);
		if(!f.exists()){
			Log.i(TAG,path+" no exists");
			return null;
		}
		String str = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	str += tempString;
            }
            reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			str = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			str = null;
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return str;
	}
}
