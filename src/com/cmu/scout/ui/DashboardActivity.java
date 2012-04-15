package com.cmu.scout.ui;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;

// TODO:
// 1) Organize DashboardActivity. Make use of an AsyncTask instead of a Thread just to be safe.
// 2) Fix database/provider... some inserts/updates don't notify the correct Uris on content changes.
// 3) Fix "export data" work flow. This might be better in a preferences menu or something... not a
//    dashboard home button.
// 4) Optimize match-scout UI. This is probably not the best way to do it. Ensure the user knows
//    that they can click the EditText button.
// 5) Port app to phones. Most of the app makes use of compatability library, so probably not too difficult.
//    might require making new layouts though... current layouts are probably not compatible with
//    multiple screens.
// 6) Debug the database.
// 7) Allow user to "import" a csv file into the app's internal database (must make use of Intent Filters).
// 8) The "share" data button shouldn't export data to external storage... it should save it to
//    internal storage instead.

public class DashboardActivity extends SherlockActivity implements Runnable {

	private static final String TAG = "DashboardActivity";
	private static final boolean DEBUG = true;

	// intent passed to team grid
	public static final String INTENT_CALL_FROM_TEAM = "call_from_team";

	private static final int DIALOG_START_MATCH = 1;
	private static final int DIALOG_EXPORT_DATA = 2;
	
	private static final int WRITE_SUCCESS = 1;
	private static final int WRITE_FAIL = 0;
	private static final int WRITE_EMPTY = 2;
	private static final int WRITE_NO_ACCESS = 3;
	//private static final int MSG_DONE = 0;
	private static final int MSG_TEAM = 1;
	private static final int MSG_MATCH = 2;
	private static final int MSG_INC = 3;

	public static final String INTENT_TEAM_ID = "scout_intent_team_id";
	public static final String INTENT_MATCH_ID = "scout_intent_match_id";
	public static final String INTENT_TEAM_NUM = "scout_intent_team_num";
	public static final String INTENT_MATCH_NUM = "scout_intent_match_num";

	private boolean shareCall = false;

	private static final String[] projectionTeam = {
		Teams.TEAM_NUM,	
		Teams.TEAM_NAME,
		Teams.FRIDAY_RANK,
		Teams.DRIVE_SYSTEM,
		Teams.WHEELS,
		Teams.HAS_AUTONOMOUS,
		Teams.PREFERRED_START,
		Teams.HAS_KINECT,
		Teams.CAN_CROSS,
		Teams.CAN_PUSH_DOWN_BRIDGE,
		Teams.STRATEGY,
		Teams.COMMENTS,
		//Teams.SUMMARY_AUTO_NUM_SCORED,
		//Teams.SUMMARY_AUTO_NUM_ATTEMPT,
		//Teams.SUMMARY_AUTO_NUM_POINTS,
		//Teams.SUMMARY_NUM_SCORED,
		//Teams.SUMMARY_NUM_ATTEMPT,
		//Teams.SUMMARY_NUM_POINTS,
		//Teams.SUMMARY_TOTAL_SCORE,
		//Teams.SUMMARY_NUM_WINS,
		//Teams.SUMMARY_NUM_LOSSES
	};

	private static final String[] projectionMatch = {
		TeamMatches.MATCH_ID,
		TeamMatches.TEAM_ID,
		TeamMatches.AUTO_NUM_SCORED_HIGH,
		TeamMatches.AUTO_NUM_ATTEMPT_HIGH,
		TeamMatches.AUTO_NUM_SCORED_MED,
		TeamMatches.AUTO_NUM_ATTEMPT_MED,
		TeamMatches.AUTO_NUM_SCORED_LOW,
		TeamMatches.AUTO_NUM_ATTEMPT_LOW,
		TeamMatches.NUM_SCORED_HIGH,
		TeamMatches.NUM_ATTEMPT_HIGH,
		TeamMatches.NUM_SCORED_MED,
		TeamMatches.NUM_ATTEMPT_MED,
		TeamMatches.NUM_SCORED_LOW,
		TeamMatches.NUM_ATTEMPT_LOW,
		TeamMatches.NUM_BALANCED,
		TeamMatches.HOW_CROSS,
		TeamMatches.PICK_UP_BALLS,
		TeamMatches.SPEED,
		TeamMatches.AGILITY,
		TeamMatches.STRATEGY,
		TeamMatches.PENALTY_RISK,
		TeamMatches.WHICH_ALLIANCE,
		TeamMatches.FINAL_SCORE,
		TeamMatches.WIN_MATCH			
	};

	private ProgressDialog pd;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.dashboard_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.v(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case R.id.share_database:
			shareCall = true;
			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				Cursor teamsCur = getContentResolver().query(Teams.CONTENT_URI, new String[] { Teams._ID }, null, null, null);
				if (teamsCur == null || !teamsCur.moveToFirst()) {
					Toast.makeText(DashboardActivity.this, "No data to share.", Toast.LENGTH_SHORT).show();
				} else {
					teamsCur.close();
					
					pd = new ProgressDialog(this);
					pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					pd.setTitle("Export data");
					pd.setMessage("Writing data...");
					pd.setCancelable(false);
					pd.show();

					Thread thread = new Thread(this);
					thread.start();
				}
			} else {
				Toast.makeText(DashboardActivity.this, "This device does not support this feature.", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
	}

	public void onClickHandler(View v) {
		switch (v.getId()) {
		case R.id.dashboard_teams:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				Intent teamData = new Intent(getApplicationContext(), TeamGridActivity.class);
				teamData.putExtra(INTENT_CALL_FROM_TEAM, true);
				startActivity(teamData);
				break;	
			} else {
				Intent teamData = new Intent(getApplicationContext(), BaseTeamGridActivity.class);
				teamData.putExtra(INTENT_CALL_FROM_TEAM, true);
				startActivity(teamData);
				break;
			}
		case R.id.dashboard_match:
			showDialog(DIALOG_START_MATCH);
			break;
		case R.id.dashboard_display:
			boolean isTablet = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	        
			if (isTablet) {
				startActivity(new Intent(getApplicationContext(), DisplayPagerActivity.class));
			} else {
				Toast.makeText(DashboardActivity.this, "This feature is currently only supported on tablets.", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.dashboard_transfer:
			Toast.makeText(this,"Coming soon!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.dashboard_manage:			
			shareCall = false;
			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				Cursor teamsCur = getContentResolver().query(Teams.CONTENT_URI, new String[] { Teams._ID }, null, null, null);
				if (teamsCur == null || !teamsCur.moveToFirst()) {
					Toast.makeText(DashboardActivity.this, "No data to export.", Toast.LENGTH_SHORT).show();
				} else {
					teamsCur.close();
					showDialog(DIALOG_EXPORT_DATA);
				}
			} else {
				Toast.makeText(DashboardActivity.this, "This device does not support this feature.", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private static final String SCOUTING_MANAGER_DIR = "scouting_manager";
	private static final String TEAM_SCOUT_DATA_FILE = "team_scouting_data.csv";		
	private static final String MATCH_SCOUT_DATA_FILE = "match_scouting_data.csv";
	
	private int output_team(File root){
		Cursor teamsCur = getContentResolver().query(Teams.CONTENT_URI, projectionTeam, null, null, Teams.TEAM_NUM + " ASC");
		int numCols = teamsCur.getColumnCount();
		Message msg = new Message();
		msg.what = MSG_TEAM;
		msg.arg1 = teamsCur.getCount();
		handler.sendMessage(msg);
		try{
			if(teamsCur != null && teamsCur.moveToFirst()){
				File dir = new File (root, "/" + SCOUTING_MANAGER_DIR);
				dir.mkdirs();
				File out = new File(dir, TEAM_SCOUT_DATA_FILE);
				FileWriter writer = new FileWriter(out);

				for(int i = 0;i<numCols;i++){
					writer.append(projectionTeam[i]);
					if (i+1 != numCols) writer.append(',');
					else writer.append('\n');
				}

				do {
					for (int i=0; i<numCols; i++) {
						String s = teamsCur.getString(i);
						if(s == null || s.equals("-1")){
							//do nothing
						}
						else if(i == teamsCur.getColumnIndex(Teams.TEAM_NAME)){
					        // sanitize data input
							s = s.replace("\"", "\"\"");
					        writer.append("\""+s+"\"");
						}
						else if(i == teamsCur.getColumnIndex(Teams.DRIVE_SYSTEM)){
							writer.append(getResources().getStringArray(R.array.drive_option)[new Integer(s)+1]);
						}
						else if(i == teamsCur.getColumnIndex(Teams.WHEELS)){
							writer.append(getResources().getStringArray(R.array.wheel_option)[new Integer(s)+1]);
						}
						else if(i == teamsCur.getColumnIndex(Teams.STRATEGY)){
							writer.append(getResources().getStringArray(R.array.strategy_option)[new Integer(s)+1]);
						}
						else if(i == teamsCur.getColumnIndex(Teams.COMMENTS)){
					        // sanitize data input
							s = s.replace("\"", "\"\"");
					        writer.append("\""+s+"\"");
						}

						else if(i == teamsCur.getColumnIndex(Teams.PREFERRED_START)){

							if(s.contains("1")){
								writer.append("L");
							}
							if(s.contains("2")){
								writer.append("M");
							}
							if(s.contains("3")){
								writer.append("R");
							}
						}
						else if(i == teamsCur.getColumnIndex(Teams.HAS_AUTONOMOUS)||i == teamsCur.getColumnIndex(Teams.HAS_KINECT)
								||i == teamsCur.getColumnIndex(Teams.CAN_CROSS)||i == teamsCur.getColumnIndex(Teams.CAN_PUSH_DOWN_BRIDGE)){
							int value = new Integer(s);
							String st = "No Value";
							switch(value){
							case 0:
								st = "No";
								break;
							case 1:
								st = "Yes";
							}
							writer.append(st);
						}

						else{
							writer.append(teamsCur.getString(i));
						}

						if (i+1 != numCols) writer.append(',');
						else writer.append('\n');
					}
					handler.sendEmptyMessage(MSG_INC);
					//Thread.sleep(20);
				} while (teamsCur.moveToNext());
				teamsCur.close();
				writer.flush();
				writer.close();		
			}
			else{
				return WRITE_EMPTY;
			}
		}catch(Exception e){return WRITE_FAIL;}
		return WRITE_SUCCESS;
	}		

	private static final String[] cross_option={"No Atmp","Barrier","Bridge","Both"};
	private static final String[] pick_ball_option={"Feeder","Floor","Both"};
	private static final String[] rate_option={"Poor","Fair","Good","Great"};
	private static final String[] strategy_option={"Offense","Defense","Neutual"};
	private static final String[] risk_option={"Low","Medium","High"};

	private int output_match(File root){
		// map "_id"s to "team_num"s
		// (this is necessary because the team_matches table references the team's "_id" in the teams table, 
		// but we need the team's number)
		Map<Integer,Integer> teamIds = new HashMap<Integer, Integer>();
		Map<Integer,String> teamNames = new HashMap<Integer, String>();
		Cursor teams = getContentResolver().query(Teams.CONTENT_URI, new String[]{Teams._ID,Teams.TEAM_NUM,Teams.TEAM_NAME}, null, null, Teams.TEAM_NUM + " ASC");
		Cursor matches = getContentResolver().query(Matches.CONTENT_URI, null, null, null, Matches.MATCH_NUM + " ASC");
		Cursor cur = getContentResolver().query(TeamMatches.CONTENT_URI, projectionMatch, null, null, TeamMatches.MATCH_ID + " ASC");

		Message msg = new Message();
		msg.what = MSG_MATCH;
		msg.arg1 = teams.getCount()+matches.getCount()+cur.getCount();
		handler.sendMessage(msg);

		if (teams != null && teams.moveToFirst()) {
			do {
				teamIds.put(teams.getInt(teams.getColumnIndex(Teams._ID)), 
						teams.getInt(teams.getColumnIndex(Teams.TEAM_NUM)));
				String name = teams.getString(teams.getColumnIndex(Teams.TEAM_NAME));
				if (name==null) name = "";
				teamNames.put(teams.getInt(teams.getColumnIndex(Teams._ID)), name);
				handler.sendEmptyMessage(MSG_INC);
			} while (teams.moveToNext());
			teams.close();
		}



		// map "_id"s to "match_num"s
		// (this is necessary because the team_matches table references the match's "_id" in the matches table, 
		// but we need the match's number)
		Map<Integer,Integer> matchIds = new HashMap<Integer, Integer>();

		if (matches != null && matches.moveToFirst()) {
			do {
				matchIds.put(matches.getInt(matches.getColumnIndex(Matches._ID)), 
						matches.getInt(matches.getColumnIndex(Matches.MATCH_NUM)));
				handler.sendEmptyMessage(MSG_INC);
			} while (matches.moveToNext());		
			matches.close();
		} 


		try{
			int numCols = cur.getColumnCount();
			if(cur != null && cur.moveToFirst()){

				
				File dir = new File (root, "/" + SCOUTING_MANAGER_DIR);
				dir.mkdirs();
				File out = new File(dir, MATCH_SCOUT_DATA_FILE);
				
				FileWriter writer = new FileWriter(out);
				writer.append("match_number,team_number,team_name,");
				for(int i = 2;i<numCols;i++){//skip match id, team id
					writer.append(projectionMatch[i]);
					if (i+1 != numCols) writer.append(',');
					else writer.append('\n');
				}

				do{
					for(int i=0;i<numCols;i++){
						String s = cur.getString(i);
						if(s==null|| s.equals("-1")){
							//do nothing
						}
						else if(i == cur.getColumnIndex(TeamMatches.MATCH_ID)){
							writer.append(matchIds.get(new Integer(s))+"");
						}
						else if(i == cur.getColumnIndex(TeamMatches.TEAM_ID)){
							writer.append(teamIds.get(new Integer(s))+",");
							writer.append(teamNames.get(new Integer(s))+"");
						}
						else if(i == cur.getColumnIndex(TeamMatches.NUM_BALANCED)){
							if(s.equals("0")) writer.append("No Atmp");
							else writer.append(s);
						}
						else if(i == cur.getColumnIndex(TeamMatches.HOW_CROSS)){
							writer.append(cross_option[new Integer(s)]);
						}
						else if(i == cur.getColumnIndex(TeamMatches.PICK_UP_BALLS)){
							writer.append(pick_ball_option[new Integer(s)]);
						}
						else if(i == cur.getColumnIndex(TeamMatches.AGILITY)
								||i == cur.getColumnIndex(TeamMatches.SPEED)){
							writer.append(rate_option[new Integer(s)]);
						}
						else if(i == cur.getColumnIndex(TeamMatches.STRATEGY)){
							writer.append(strategy_option[new Integer(s)]);
						}
						else if(i == cur.getColumnIndex(TeamMatches.PENALTY_RISK)){
							writer.append(risk_option[new Integer(s)]);
						}
						else if(i == cur.getColumnIndex(TeamMatches.WHICH_ALLIANCE)){
							String color = (new Integer(s)==0)?"Blue":"Red";
							writer.append(color);
						}
						else if(i == cur.getColumnIndex(TeamMatches.WIN_MATCH)){
							String result = (new Integer(s)==0)?"Lost":"Win";
							writer.append(result);
						}
						else{
							writer.append(s);
						}

						if (i+1 != numCols) writer.append(',');
						else writer.append('\n');
					}
					handler.sendEmptyMessage(MSG_INC);
				}while(cur.moveToNext());
				cur.close();
				writer.flush();
				writer.close();
			}
			else{
				return WRITE_EMPTY;
			}
		}catch(Exception e){return WRITE_FAIL;}

		return WRITE_SUCCESS;
	}


	@Override
	public void run() {
		File root = Environment.getExternalStorageDirectory();
		Message msg = new Message();
		msg.what=0;
		if (root.canWrite()){
			int teamRes = output_team(root);
			int matchRes = output_match(root);
			if (teamRes == WRITE_FAIL || matchRes == WRITE_FAIL) {
				msg.arg1 = WRITE_FAIL;
			} else if (teamRes == WRITE_SUCCESS || matchRes == WRITE_SUCCESS) {
				msg.arg1 = WRITE_SUCCESS;
			} else {
				msg.arg1 = WRITE_EMPTY;
			}
			//if(teamRes == WRITE_SUCCESS){
				//msg.arg1 = output_match(root);
			//}
			//else{
				//msg.arg1 = teamRes;
			//}
		}
		else{
			msg.arg1 = WRITE_NO_ACCESS;
		}
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_TEAM:
				pd.setMessage("Writing team-scout data...");
				pd.setMax(msg.arg1);
				pd.setProgress(0);
				break;

			case MSG_MATCH:
				pd.setMessage("Writing match-scout data...");
				pd.setMax(msg.arg1);
				pd.setProgress(0);
				break;

			case MSG_INC:
				pd.incrementProgressBy(1);
				break;

			default:
				pd.dismiss();
				if (shareCall){
					shareCall = false;
					switch(msg.arg1){
					case WRITE_SUCCESS:
						Toast.makeText(DashboardActivity.this, "Data written to external storage.", Toast.LENGTH_SHORT).show();
						callChooser();
						break;
					case WRITE_EMPTY:
						//Toast.makeText(DashboardActivity.this, "No data to write.", Toast.LENGTH_SHORT).show();
						break;
					case WRITE_FAIL:
						Toast.makeText(DashboardActivity.this, "Error writing data.", Toast.LENGTH_SHORT).show();
						break;
					}
					return;
				}

				switch(msg.arg1){
				case WRITE_SUCCESS:
					Toast.makeText(DashboardActivity.this, "Data written to external storage.", Toast.LENGTH_SHORT).show();
					break;
				case WRITE_EMPTY:
					//Toast.makeText(DashboardActivity.this, "No data to write.", Toast.LENGTH_SHORT).show();
					break;
				case WRITE_FAIL:
					Toast.makeText(DashboardActivity.this, "Error writing data.", Toast.LENGTH_SHORT).show();
					break;
				}

			}
		}
	};
	
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
						cur.close();
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
		case DIALOG_EXPORT_DATA:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false)
			.setTitle(R.string.confirm_export_title)
			.setMessage(R.string.confirm_export_message)
			.setIcon(R.drawable.ic_dialog_alert_holo_light)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					pd = new ProgressDialog(DashboardActivity.this);
					pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					pd.setTitle("Export data");
					pd.setMessage("Writing data...");
					pd.setCancelable(false);
					pd.show();

					Thread thread = new Thread(DashboardActivity.this);
					thread.start();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		return null;
	}


	// returns the match's id
	public int checkMatchBox(int matchNum) {
		// search for matchNum in the database. create new match if it doesn't already exist.
		final Cursor matchNumCur = getContentResolver().query(Matches.CONTENT_URI, new String[] { Matches._ID, Matches.MATCH_NUM }, 
				Matches.MATCH_NUM + "=?", new String[] { ""+matchNum }, null);

		if (matchNumCur != null && matchNumCur.moveToFirst()) {
			// fetch match id
			int matchId = matchNumCur.getInt(matchNumCur.getColumnIndex(Matches._ID));
			matchNumCur.close();
			return matchId;
		} else {
			// create new match
			ContentValues matchValues = new ContentValues();
			matchValues.put(Matches.MATCH_NUM, matchNum);
			return Integer.valueOf(getContentResolver().insert(Matches.CONTENT_URI, matchValues).getLastPathSegment());
		}
	}

	// returns the team's id
	public int checkTeamBox(int teamNum) {

		// search for teamNum in the database. create new team if it doesn't already exist.
		final Cursor teamNumCur = getContentResolver().query(Teams.CONTENT_URI, new String[] { Teams._ID, Teams.TEAM_NUM }, 
				Teams.TEAM_NUM + "=?", new String[] { ""+teamNum }, null);

		if (teamNumCur != null && teamNumCur.moveToFirst()) {
			// fetch team id
			int teamId = teamNumCur.getInt(teamNumCur.getColumnIndex(Teams._ID));
			teamNumCur.close();
			return teamId;
		} else {
			// create new team
			ContentValues teamValues = new ContentValues();
			teamValues.put(Teams.TEAM_NUM, teamNum);
			return Integer.valueOf(getContentResolver().insert(Teams.CONTENT_URI, teamValues).getLastPathSegment());
		}
	}

	private void callChooser(){
		Cursor teams = getContentResolver().query(Teams.CONTENT_URI, new String[]{Teams._ID,Teams.TEAM_NUM,Teams.TEAM_NAME}, null, null, null);
		Cursor teamMatches = getContentResolver().query(TeamMatches.CONTENT_URI, null, null, null, null);

		boolean exportTeams = (teams != null && teams.moveToFirst());
		boolean exportTeamMatches = (teamMatches != null && teams.moveToFirst());
		teams.close();
		teamMatches.close();
		
		final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

		emailIntent.setType("text/csv");
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		File dir = new File (Environment.getExternalStorageDirectory(), "/" + SCOUTING_MANAGER_DIR);
		dir.mkdirs();
		
		File matchFile = new File(dir, MATCH_SCOUT_DATA_FILE);
		File teamFile = new File(dir, TEAM_SCOUT_DATA_FILE);

		if (teamFile.isFile()){
			Uri matchUri = Uri.fromFile(matchFile);
			Uri teamUri = Uri.fromFile(teamFile);

			ArrayList<Uri> csvUris = new ArrayList<Uri>();
			if (exportTeams) csvUris.add(teamUri);
			if (matchFile.isFile() && exportTeamMatches) csvUris.add(matchUri);

			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Scouting Manager - scouting data");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "See attachments...");
			emailIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, csvUris);

			startActivity(Intent.createChooser(emailIntent, "Share scouting data..."));
		} else {
			Toast.makeText(this, "No data to share.", Toast.LENGTH_SHORT).show();
		}
	}
	/*
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
	}*/
}
