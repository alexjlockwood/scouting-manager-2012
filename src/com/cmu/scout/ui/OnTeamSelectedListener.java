package com.cmu.scout.ui;

import android.net.Uri;

/** 
 * Interface that Fragment's can use for callbacks to the parent Activity 
 */
public interface OnTeamSelectedListener {
	public void onTeamSelected(Uri uri);
}
