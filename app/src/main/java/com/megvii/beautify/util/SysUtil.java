package com.megvii.beautify.util;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by binghezhouke on 15-8-12.
 */
public class SysUtil {
	
	public static String API_KEY;
	public static String API_SECRET;

	public static HashMap<String, byte[]> featureMap = new HashMap<String, byte[]>();//key:name, value:feature

	public static int getNativeMemoryInfo() {
		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);


		return memoryInfo.nativePss;
	}

	public static boolean checkCameraHasPerm() {
		Camera mCamera=null;
		try {
			mCamera=Camera.open(0);
		}catch (Exception e){

		}

		if (mCamera==null){
			Log.i("xie","xie camera null");
			return false;
		}else{
			Log.i("xie","xie camera not null");
			mCamera.release();
			return true;
		}

	}

	public static void getAppDetailSettingIntent(Activity context){
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(Build.VERSION.SDK_INT >= 9){
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			intent.setData(Uri.fromParts("package", context.getPackageName(), null));
		} else if(Build.VERSION.SDK_INT <= 8){
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
			intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
		}
		context.startActivity(intent);
	}

}
