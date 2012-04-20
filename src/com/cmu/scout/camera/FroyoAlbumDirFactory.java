package com.cmu.scout.camera;

import java.io.File;

import android.annotation.TargetApi;
import android.os.Environment;

@TargetApi(8)
public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

	@Override
	public File getAlbumStorageDir(String albumName) {
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}
}
