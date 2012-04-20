package com.cmu.scout.camera;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cmu.scout.R;

public class BaseCameraActivity extends SherlockFragmentActivity {
	
	private static final String TAG = "BaseCameraActivity";
	//private static final boolean DEBUG = true;
	
	public AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}
	
	public static boolean isCameraAvailable(Context context, String action) {
		return context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	// check if camera application is installed!!!!
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	public String getAlbumName() {
//		if (DEBUG) Log.v(TAG, "getAlbumName()");

		return getString(R.string.album_name);
	}
	
	public File getAlbumDir() {
//		if (DEBUG) Log.v(TAG, "getAlbumDir()");

		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.e(TAG, "Failed to create directory.");
						return null;
					}
				}
			}
		} else {
			Log.e(TAG, "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}
}
