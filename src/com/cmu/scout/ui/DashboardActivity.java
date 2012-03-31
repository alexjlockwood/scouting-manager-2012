package com.cmu.scout.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;

public class DashboardActivity extends Activity {
	
	private static final String TAG = "DashboardActivity";
	private static final boolean DEBUG = true;
	
	// intent passed to team grid
	public static final String INTENT_CALL_FROM_TEAM = "call_from_team";
	
	private static final int DIALOG_START_MATCH = 1;
	
	public static final String INTENT_TEAM_ID = "scout_intent_team_id";
	public static final String INTENT_MATCH_ID = "scout_intent_match_id";
	public static final String INTENT_TEAM_NUM = "scout_intent_team_num";
	public static final String INTENT_MATCH_NUM = "scout_intent_match_num";

	
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
			showDialog(DIALOG_START_MATCH);
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
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_START_MATCH:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.start_match_scout_dialog, null);
			final EditText matchBox = (EditText) textEntryView.findViewById(R.id.ET_match_number); 
			final EditText teamBox = (EditText) textEntryView.findViewById(R.id.ET_team_number); 
			return new AlertDialog.Builder(DashboardActivity.this)
				/*.setIconAttribute(android.R.attr.alertDialogIcon)*/
				.setTitle(R.string.start_match_scout_title)
				.setView(textEntryView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String matchText = matchBox.getText().toString();
								String teamText = teamBox.getText().toString();
								
								if (TextUtils.isEmpty(matchText) || Integer.valueOf(matchText) <= 0) {
									Toast.makeText(DashboardActivity.this, R.string.invalid_user_input, Toast.LENGTH_SHORT).show();
									//matchBox.requestFocus();
									return;
								}
								
								if (TextUtils.isEmpty(teamText) || Integer.valueOf(teamText) <= 0) {
									Toast.makeText(DashboardActivity.this, R.string.invalid_user_input, Toast.LENGTH_SHORT).show();
									//teamBox.requestFocus();
									return;
								}
								
								int matchNum = Integer.valueOf(matchText);
								int teamNum = Integer.valueOf(teamText);
								
								int matchId = checkMatchBox(matchNum);
								int teamId = checkTeamBox(teamNum);
								
								if (DEBUG) {
									Log.v(TAG, "matchNum = " + matchNum);
									Log.v(TAG, "teamNum = " + teamNum);
									Log.v(TAG, "matchId = " + matchId);
									Log.v(TAG, "teamId = " + teamId);
								}
								
								final Uri queryUri = Matches.buildMatchIdTeamIdUri("" + matchId, "" + teamId);
								final Cursor cur = getContentResolver().query(queryUri, new String[] { TeamMatches.TEAM_ID, TeamMatches.MATCH_ID }, null, null, null);

								if (cur == null || !cur.moveToFirst()) {
									// then add new team-match to the table
									ContentValues values = new ContentValues();
									values.put(TeamMatches.MATCH_ID, matchId);
									values.put(TeamMatches.TEAM_ID, teamId);
									getContentResolver().insert(TeamMatches.CONTENT_URI, values);
								}
								
								final Intent launchingIntent = new Intent(getApplicationContext(), MatchPagerActivity.class);
								launchingIntent.putExtra(INTENT_MATCH_NUM, matchNum);
								launchingIntent.putExtra(INTENT_TEAM_NUM, teamNum);
								launchingIntent.putExtra(INTENT_MATCH_ID, matchId);
								launchingIntent.putExtra(INTENT_TEAM_ID, teamId);
								
								startActivity(launchingIntent);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).create();
		}
		return null;
	}
	
	
	// returns the match's id
	public int checkMatchBox(int matchNum) {
		Log.v(TAG, "start: "+matchNum);
		
		// search for matchNum in the database. create new match if it doesn't already exist.
		final Cursor matchNumCur = getContentResolver().query(Matches.CONTENT_URI, new String[] { Matches._ID, Matches.MATCH_NUM }, 
				Matches.MATCH_NUM + "=?", new String[] { ""+matchNum }, null);
		
		if (matchNumCur != null && matchNumCur.moveToFirst()) {
			Log.v(TAG, "match found");
			// fetch match id
			int matchId = matchNumCur.getInt(matchNumCur.getColumnIndex(Matches._ID));
			matchNumCur.close();
			return matchId;
		} else {
			Log.v(TAG, "match not found");
			// create new match
			ContentValues matchValues = new ContentValues();
			matchValues.put(Matches.MATCH_NUM, matchNum);
			return Integer.valueOf(getContentResolver().insert(Matches.CONTENT_URI, matchValues).getLastPathSegment());
		}
	}
	
	// returns the team's id
	public int checkTeamBox(int teamNum) {
		Log.v(TAG, "start: "+teamNum);
		
		// search for teamNum in the database. create new team if it doesn't already exist.
		final Cursor teamNumCur = getContentResolver().query(Teams.CONTENT_URI, new String[] { Teams._ID, Teams.TEAM_NUM }, 
				Teams.TEAM_NUM + "=?", new String[] { ""+teamNum }, null);
		
		if (teamNumCur != null && teamNumCur.moveToFirst()) {
			Log.v(TAG, "team found");
			// fetch team id
			int teamId = teamNumCur.getInt(teamNumCur.getColumnIndex(Teams._ID));
			teamNumCur.close();
			return teamId;
		} else {
			Log.v(TAG, "team not found");
			// create new team
			ContentValues teamValues = new ContentValues();
			teamValues.put(Teams.TEAM_NUM, teamNum);
			return Integer.valueOf(getContentResolver().insert(Teams.CONTENT_URI, teamValues).getLastPathSegment());
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
