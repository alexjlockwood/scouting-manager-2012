package com.cmu.scout.test;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Teams;

public class TeamDetailsFragment extends Fragment {

	private static final String TAG = "TeamDetailsFragment";
	private static final boolean DEBUG = true;
	
	private TextView text = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "--ON CREATE VIEW--");

		text = (TextView) inflater.inflate(R.layout.team_details_item, container, false);
		return text;
	}
	
	public void updateContent(Uri uri) {
		if (text != null) {
			Cursor cur = getActivity().getContentResolver().query(uri,  null, null, null, null);
			if (cur.moveToFirst()) {
				text.setText(cur.getString(cur.getColumnIndex(Teams.TEAM_NUM)));
			}
			cur.close();
		}
		
		// TODO: implement once the final details layout is implemented!
		
		// if the layout isn't null, this will update the current layout with
		// values specific to the team-specific uri.
	}
	
}
