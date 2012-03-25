package com.cmu.scout.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;

public class DashboardActivity extends Activity {
	
	// intent passed to team grid
	public static final String INTENT_CALL_FROM_TEAM = "call_from_team";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
	}
	
	public void onClickHandler(View v) {
		switch (v.getId()) {
		case R.id.dashboard_teams:
			Intent teamData = new Intent(getApplicationContext(), TeamGridActivity.class);
			teamData.putExtra(INTENT_CALL_FROM_TEAM, true);
			startActivity(teamData);
			break;
		case R.id.dashboard_match:
			//Intent matchData = new Intent(getApplicationContext(), TeamGridActivity.class);
			//matchData.putExtra(INTENT_CALL_FROM_TEAM, false);
			//startActivity(matchData);
			Intent i = new Intent(getApplicationContext(), MatchPagerActivity.class);
			startActivity(i);
			break;
		case R.id.dashboard_display:
			startActivity(new Intent(getApplicationContext(), DisplayPagerActivity.class));
			break;
		case R.id.dashboard_transfer:
			Toast.makeText(DashboardActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.dashboard_manage:
			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				new ExportDataTask().execute();
			} else {
				Toast.makeText(DashboardActivity.this, "No SD card present!", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private class ExportDataTask extends AsyncTask<String, String, String> {
		
		@Override
		protected String doInBackground(String... urls) {
			try {
				File root = Environment.getExternalStorageDirectory();
				if (root.canWrite()){
					
					// map "_id"s to "team_num"s
					// (this is necessary because the team_matches table references the team's "_id" in the teams table, 
					// but we need the team's number)
					Map<Integer,Integer> teamIds = new HashMap<Integer, Integer>();
					// TODO: include a projection as the second argument... we don't need all of the columns!
					Cursor teams = getContentResolver().query(Teams.CONTENT_URI, null, null, null, Teams.TEAM_NUM + " ASC");
					
					if (teams != null && teams.moveToFirst()) {
						do {
							teamIds.put(teams.getInt(teams.getColumnIndex(Teams._ID)), 
									teams.getInt(teams.getColumnIndex(Teams.TEAM_NUM)));
						} while (teams.moveToNext());
					} else {
						return "No teams to export!";
					}
					
					// map "_id"s to "match_num"s
					// (this is necessary because the team_matches table references the match's "_id" in the matches table, 
					// but we need the match's number)
					Map<Integer,Integer> matchIds = new HashMap<Integer, Integer>();
					Cursor matches = getContentResolver().query(Matches.CONTENT_URI, null, null, null, Matches.MATCH_NUM + " ASC");
					
					if (matches != null && matches.moveToFirst()) {
						do {
							matchIds.put(matches.getInt(matches.getColumnIndex(Matches._ID)), 
									matches.getInt(matches.getColumnIndex(Matches.MATCH_NUM)));
						} while (matches.moveToNext());		
					} else {
						return "No matches to export!";
					}
					
					// TODO: include a projection as the second argument... we don't need all of the columns!
					Cursor teamMatches = getContentResolver().query(TeamMatches.CONTENT_URI, null, null, null, TeamMatches.MATCH_ID + " ASC");
					int numCols = teamMatches.getColumnCount();
					
					if (teamMatches != null && teamMatches.moveToFirst()) {
						File path = new File(root, "scouting_data.csv");
						FileWriter writer = new FileWriter(path);
						
						// TODO: write header columns to the top of the file
						
						// TODO: write data to file similar to how the girls manage their excel spreadsheet
						
						// TODO: need some sort of mapping of int values to strings for almost 
						// all of the columns (i.e. drive, wheels, etc.)
						
						do {
							for (int i=0; i<numCols; i++) {				
								writer.append(teamMatches.getString(i));
								if (i+1 != numCols) writer.append(',');
								else writer.append('\n');
							}
						} while (teamMatches.moveToNext());
						
						writer.flush();
						writer.close();
					}
				}
			} catch (IOException e) {
				Log.e("DashboardActivity", "Could not write file " + e.getMessage());
				e.printStackTrace();
			}
			return "Export successful";
		}
		
		@Override
		protected void onPostExecute(String result) {
	        Toast.makeText(DashboardActivity.this, result, Toast.LENGTH_SHORT).show();
	    }
	}
}
