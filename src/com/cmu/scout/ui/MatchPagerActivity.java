package com.cmu.scout.ui;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.cmu.scout.R;
import com.cmu.scout.fragment.MatchFragment;
import com.cmu.scout.fragment.MatchInputAutoFragment;
import com.cmu.scout.fragment.MatchInputGeneralFragment;
import com.cmu.scout.fragment.MatchInputTeleOpFragment;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class MatchPagerActivity extends FragmentActivity {
	
	private static final String TAG = "MatchPagerActivity";
	private static final boolean DEBUG = true;
		
	private int mTeamId = -1;	
	private int mTeamNum = -1;
	private int mMatchNum = -1;

	public static final int MAX_SCORE = 999;
		
	private MatchFragmentAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;
	
	private static final String[] SUMMARY_PROJ = {
		Teams.SUMMARY_AUTO_NUM_ATTEMPT,
		Teams.SUMMARY_AUTO_NUM_SCORED,
		Teams.SUMMARY_AUTO_NUM_POINTS,
		Teams.SUMMARY_NUM_ATTEMPT,
		Teams.SUMMARY_NUM_SCORED,
		Teams.SUMMARY_NUM_POINTS,
		Teams.SUMMARY_NUM_WINS,
		Teams.SUMMARY_NUM_LOSSES,
		Teams.SUMMARY_TOTAL_SCORE
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.match_scout_pager);
		
		if (DEBUG) {
			FragmentManager.enableDebugLogging(true);
		}
		
		// set result to cancelled in case user backs out, finishes activity, etc.
		setResult(Activity.RESULT_CANCELED, getIntent());
		
		final Intent intent = getIntent();
		if (intent != null) {
			mTeamId = intent.getIntExtra(TeamGridActivity.INTENT_TEAM_ID, -1);
			mMatchNum = intent.getIntExtra(TeamGridActivity.INTENT_MATCH_ID, -1);
			
			final Uri teamsUri = Teams.buildTeamIdUri("" + mTeamId);
			final Cursor teamCur = getContentResolver().query(teamsUri, new String[] { Teams.TEAM_NUM }, null, null, null);
			
			if (teamCur != null && teamCur.moveToFirst()) {
				mTeamNum = teamCur.getInt(teamCur.getColumnIndex(Teams.TEAM_NUM));
			}
			
			setActionBarTitle(getResources().getString(R.string.match_scouting_title));
			setActionBarSubtitle("Team " + mTeamNum + ", Match " + mMatchNum);
		}
		
		mAdapter = new MatchFragmentAdapter(getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.match_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(MatchFragmentAdapter.NUM_TITLES);
		
		mIndicator = (TabPageIndicator) findViewById(R.id.match_indicator);
		mIndicator.setViewPager(mPager);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("mTeamId", mTeamId);
		outState.putInt("mTeamNum", mTeamNum);
		outState.putInt("mMatchNum", mMatchNum);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mTeamId = savedInstanceState.getInt("mTeamId");
		mTeamNum = savedInstanceState.getInt("mTeamNum");
		mMatchNum = savedInstanceState.getInt("mMatchNum");
	}
	
	public int getCurrentTeamId() {
		return mTeamId;
	}
	
	public int getCurrentMatchNum() {
		return mMatchNum;
	}
	
	public static class MatchPickerDialog extends DialogFragment {
		private static final String TAG = "EnterMatchDialog";
		private static final boolean DEBUG = true;
		
		public static MatchPickerDialog newInstance() {
			return new MatchPickerDialog();
		}
		
		@Override
		public void onCancel(DialogInterface dialog) {
			// finish Activity if user "backs out" of Dialog
			((MatchPagerActivity) getActivity()).finish();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (DEBUG) Log.v(TAG, "onCreateDialog");

			LayoutInflater factory = LayoutInflater.from(getActivity());
			
			final NumberPicker matchPicker = (NumberPicker) factory.inflate(R.layout.match_scout_number_picker, null);
			matchPicker.setMinValue(1);
			matchPicker.setMaxValue(500);
			matchPicker.setValue(((MatchPagerActivity)getActivity()).getCurrentMatchNum());
			matchPicker.setWrapSelectorWheel(false);
			matchPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
			
			return new AlertDialog.Builder(getActivity())
				.setTitle("Match number:")
				.setView(matchPicker)
				.setPositiveButton(R.string.set,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int matchNumber = matchPicker.getValue();
							((MatchPagerActivity) getActivity()).setMatchNumber(matchNumber);
						}
					})
				.create();
		}
	}
	
	public void showDialog() {
		if (DEBUG) Log.v(TAG, "showDialog()");
		MatchPickerDialog.newInstance().show(getSupportFragmentManager(), MatchPickerDialog.TAG);
	}
	
    private void setActionBarTitle(String title) {
    	if (DEBUG) Log.v(TAG, "setActionBarTitle()");
    	if (title != null) {
        	final ActionBar actionBar = getActionBar();
        	actionBar.setTitle(title);
        }
    }

    private void setActionBarSubtitle(String subtitle) {
    	if (DEBUG) Log.v(TAG, "setActionBarSubtitle()");
    	final ActionBar actionBar = getActionBar();
    	actionBar.setSubtitle(subtitle);
    }
    
	private void setMatchNumber(int matchNumber) {
		if (DEBUG) Log.v(TAG, "setMatchNumber()");
		
		mMatchNum = matchNumber;
		setActionBarSubtitle("Match " + mMatchNum);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.match_input_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DEBUG) Log.v(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case R.id.edit_match_number:
			showDialog();
			return true;
		case R.id.clear_data:
			clearScreen();
			return true;
		case R.id.bt_cancel:
			showConfirmExitDialog();
			return true;
		case R.id.bt_save:
			saveData();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void saveData() {
		if (DEBUG) Log.v(TAG, "saveData()");
		
		if (mMatchNum < 1) {
			// this should never happen!
			Toast.makeText(this, R.string.invalid_match_number, Toast.LENGTH_SHORT).show();
			return;
		}
		
		MatchFragment fragAuto = ((MatchInputAutoFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_AUTO));
		MatchFragment fragTeleOp = ((MatchInputTeleOpFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_TELEOP));
		MatchFragment fragGeneral = ((MatchInputGeneralFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_GENERAL));
		
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!

		// TODO: figure out if the life-cycle will ever cause this to be null!
		// TODO: CHECK AGAINST THIS!!!!!
		
		ContentValues autoData = null;
		ContentValues teleOpData = null;
		ContentValues generalData = null;
		
		if (fragAuto != null) {
			autoData = fragAuto.getData();
			if (autoData == null) {
				Toast.makeText(this, R.string.invalid_user_input, Toast.LENGTH_SHORT).show();
				mPager.setCurrentItem(MatchFragmentAdapter.POSITION_AUTO);
				
				int box = ((MatchInputAutoFragment) fragAuto).getFaultyEditTextBox();
				((MatchInputAutoFragment) fragAuto).selectFaultyEditTextBox(box);
				((MatchInputAutoFragment) fragAuto).resetFaultyEditTextBox();
				
				return;
			}
		}
		
		if (fragTeleOp != null) {
			teleOpData = fragTeleOp.getData();
			if (teleOpData == null) {
				Toast.makeText(this, R.string.invalid_user_input, Toast.LENGTH_SHORT).show();
				mPager.setCurrentItem(MatchFragmentAdapter.POSITION_TELEOP);
				
				int box = ((MatchInputTeleOpFragment) fragTeleOp).getFaultyEditTextBox();
				((MatchInputTeleOpFragment) fragTeleOp).selectFaultyEditTextBox(box);
				((MatchInputTeleOpFragment) fragTeleOp).resetFaultyEditTextBox();
				
				return;
			}
		}
		
		if (fragGeneral != null) {
			generalData = fragGeneral.getData();
			if (generalData == null) {
				mPager.setCurrentItem(MatchFragmentAdapter.POSITION_GENERAL);
				return;
			}
		}	
		
		// insert new match into match tables if it doesn't already exist
		final Cursor matchCur = getContentResolver().query(Matches.CONTENT_URI, null, Matches.MATCH_NUM + " = ?", new String[] { "" + mMatchNum }, null);
			
		// get the referenced matchId
		Uri matchUri; int matchId;	
		if (matchCur == null || !matchCur.moveToFirst()) {			
			ContentValues matchData = new ContentValues();
			matchData.put(Matches.MATCH_NUM, mMatchNum);
			matchUri = getContentResolver().insert(Matches.CONTENT_URI, matchData);
			if (DEBUG) Log.v(TAG, "matchUri: " + matchUri.toString());
			matchId = Integer.valueOf(matchUri.getLastPathSegment());
			if (DEBUG) Log.v(TAG, "matchId: "+matchId);
		} else {
			matchId = matchCur.getInt(matchCur.getColumnIndex(Matches._ID));
			Log.v(TAG, ""+matchId);
		}
		
		autoData.put(TeamMatches.TEAM_ID, mTeamId);
		teleOpData.put(TeamMatches.TEAM_ID, mTeamId);
		generalData.put(TeamMatches.TEAM_ID, mTeamId);
		
		autoData.put(TeamMatches.MATCH_ID, matchId);
		teleOpData.put(TeamMatches.MATCH_ID, matchId);
		generalData.put(TeamMatches.MATCH_ID, matchId);
		
		// get already existing cummulative data
		final Uri summaryUri = Teams.buildTeamIdUri(""+mTeamId);
		final Cursor summaryCur = getContentResolver().query(summaryUri, SUMMARY_PROJ, null, null, null);
		
		ContentValues summaryData = new ContentValues();
		if (summaryCur != null && summaryCur.moveToFirst()) {
			int summaryAutoNumScored = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_SCORED));
			int summaryAutoNumAttempt = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_ATTEMPT));
			int summaryAutoNumPoints = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_POINTS));
			int summaryNumScored = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_SCORED));
			int summaryNumAttempt = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_ATTEMPT));
			int summaryNumPoints = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_POINTS));
			int summaryNumWins = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_WINS));
			int summaryNumLosses = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_LOSSES));
			int summaryTotalScore = summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_TOTAL_SCORE));
			
			// get auto cumulative data
			summaryAutoNumScored += autoData.getAsInteger(Teams.SUMMARY_AUTO_NUM_SCORED);
			summaryAutoNumAttempt += autoData.getAsInteger(Teams.SUMMARY_AUTO_NUM_ATTEMPT);
			summaryAutoNumPoints += autoData.getAsInteger(Teams.SUMMARY_AUTO_NUM_POINTS);
			
			// get teleop cumulative data
			summaryNumScored += teleOpData.getAsInteger(Teams.SUMMARY_NUM_SCORED);
			summaryNumAttempt += teleOpData.getAsInteger(Teams.SUMMARY_NUM_ATTEMPT);
			summaryNumPoints += teleOpData.getAsInteger(Teams.SUMMARY_NUM_POINTS);
				
			// get general cumulative data
			summaryNumWins += generalData.getAsInteger(Teams.SUMMARY_NUM_WINS);
			summaryNumLosses += generalData.getAsInteger(Teams.SUMMARY_NUM_LOSSES);
			summaryTotalScore += generalData.getAsInteger(Teams.SUMMARY_TOTAL_SCORE);
			
			// remove auto cumulative data
			autoData.remove(Teams.SUMMARY_AUTO_NUM_SCORED);
			autoData.remove(Teams.SUMMARY_AUTO_NUM_ATTEMPT);
			autoData.remove(Teams.SUMMARY_AUTO_NUM_POINTS);
			
			// remove teleop cumulative data
			teleOpData.remove(Teams.SUMMARY_NUM_SCORED);
			teleOpData.remove(Teams.SUMMARY_NUM_ATTEMPT);
			teleOpData.remove(Teams.SUMMARY_NUM_POINTS);
			
			// remove general cumulative data
			generalData.remove(Teams.SUMMARY_NUM_WINS);
			generalData.remove(Teams.SUMMARY_NUM_LOSSES);
			generalData.remove(Teams.SUMMARY_TOTAL_SCORE);
						
			// put auto cumulative data
			summaryData.put(Teams.SUMMARY_AUTO_NUM_SCORED, summaryAutoNumScored);
			summaryData.put(Teams.SUMMARY_AUTO_NUM_ATTEMPT, summaryAutoNumAttempt);
			summaryData.put(Teams.SUMMARY_AUTO_NUM_POINTS, summaryAutoNumPoints);
			
			// put teleop cumulative data
			summaryData.put(Teams.SUMMARY_NUM_SCORED, summaryNumScored);
			summaryData.put(Teams.SUMMARY_NUM_ATTEMPT, summaryNumAttempt);
			summaryData.put(Teams.SUMMARY_NUM_POINTS, summaryNumPoints);
			
			// put general cumulative data
			summaryData.put(Teams.SUMMARY_NUM_WINS, summaryNumWins);
			summaryData.put(Teams.SUMMARY_NUM_LOSSES, summaryNumLosses);
			summaryData.put(Teams.SUMMARY_TOTAL_SCORE, summaryTotalScore);
		}
		
		Log.v(TAG, autoData.toString());
		
		// check if a TeamMatch record already exists
		final Uri queryUri = Matches.buildMatchIdTeamIdUri("" + matchId, "" + mTeamId);
		final Cursor cur = getContentResolver().query(queryUri, new String[] { TeamMatches.TEAM_ID, TeamMatches.MATCH_ID }, null, null, null);
		
		final Uri insertUri = Matches.buildMatchTeamIdUri(""+mTeamId);
		final Uri updateUri = Matches.buildMatchIdTeamIdUri(""+matchId, ""+mTeamId);
		
		if (cur != null && cur.moveToFirst()) {
			// update the existing record
			autoData.remove(TeamMatches.TEAM_ID);
			teleOpData.remove(TeamMatches.TEAM_ID);
			generalData.remove(TeamMatches.TEAM_ID);
			
			autoData.remove(TeamMatches.MATCH_ID);
			teleOpData.remove(TeamMatches.MATCH_ID);
			generalData.remove(TeamMatches.MATCH_ID);
			
			getContentResolver().update(updateUri, autoData, null, null);
			getContentResolver().update(updateUri, teleOpData, null, null);
			getContentResolver().update(updateUri, generalData, null, null);
			getContentResolver().update(summaryUri, summaryData, null, null);
			Toast.makeText(this, R.string.save_update_successful, Toast.LENGTH_SHORT).show();
		} else {
			// insert the new record, then update
			getContentResolver().insert(insertUri, autoData);
			getContentResolver().update(updateUri, teleOpData, null, null);
			getContentResolver().update(updateUri, generalData, null, null);
			getContentResolver().update(summaryUri, summaryData, null, null);
			Toast.makeText(this, R.string.save_insert_successful, Toast.LENGTH_SHORT).show();
		}
				
		final Intent i = new Intent();
		i.putExtra(TeamGridActivity.INTENT_MATCH_ID, mMatchNum);
		setResult(RESULT_OK, i);
		finish();
	}
	
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");
		
		MatchFragment fragAuto = ((MatchInputAutoFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_AUTO));
		MatchFragment fragTeleOp = ((MatchInputTeleOpFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_TELEOP));
		MatchFragment fragGeneral = ((MatchInputGeneralFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_GENERAL));
		
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!

		// TODO: figure out if the life-cycle will ever cause this to be null!
		
		if (fragAuto != null) fragAuto.clearScreen();
		if (fragTeleOp != null) fragTeleOp.clearScreen();
		if (fragGeneral != null) fragGeneral.clearScreen();
		
		Toast.makeText(this, R.string.screen_reset, Toast.LENGTH_SHORT).show();
	}
	
	public void onClickHandler(View v) {
		if (DEBUG) Log.v(TAG, "onClickHandler()");
		
		MatchFragment frag = null;
		final int viewId = v.getId();
		int pos = -1;
		
		switch (viewId) {
		case R.id.BT_Auto_Shots_Hit_High:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Auto_Shots_Miss_High:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Auto_Shots_Hit_Med:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Auto_Shots_Miss_Med:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Auto_Shots_Hit_Low:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Auto_Shots_Miss_Low:
			pos = MatchFragmentAdapter.POSITION_AUTO;
			break;
		case R.id.BT_Shots_Hit_High:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.BT_Shots_Miss_High:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.BT_Shots_Hit_Med:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.BT_Shots_Miss_Med:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.BT_Shots_Hit_Low:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.BT_Shots_Miss_Low:
			pos = MatchFragmentAdapter.POSITION_TELEOP;
			break;
		case R.id.TBT_Balance:
			pos = MatchFragmentAdapter.POSITION_GENERAL;
			break;
		}
		
		frag = mAdapter.getFragment(pos);
		frag.updateDisplay(viewId);
	}
	
	public static class MatchFragmentAdapter extends FragmentPagerAdapter 
			implements TitleProvider {
		
		private static final String TAG = "MatchFragmentAdapter";
		private static final boolean DEBUG = false;
		
		public static final int POSITION_AUTO = 0;
		public static final int POSITION_TELEOP = 1;
		public static final int POSITION_GENERAL = 2;
		
		private Map<Integer, WeakReference<MatchFragment>> mPageReferenceMap 
					= new HashMap<Integer, WeakReference<MatchFragment>>();
		
		private static final String[] TITLES = new String[] { "Autonomous", "Tele-Op", "General" };

		public static final int NUM_TITLES = TITLES.length;

		public MatchFragmentAdapter(FragmentManager fm) {		
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (DEBUG) Log.v(TAG, "getItem()");
			
			MatchFragment result = null;
			
			switch (position) {
			case POSITION_AUTO:				
				result = MatchInputAutoFragment.newInstance();
				break;
			case POSITION_TELEOP: 
				result = MatchInputTeleOpFragment.newInstance();
				break;
			case POSITION_GENERAL:
				result = MatchInputGeneralFragment.newInstance();			
				break;
			}
			mPageReferenceMap.put(position, new WeakReference<MatchFragment>(result));
			return result;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		    super.destroyItem(container, position, object);
		    mPageReferenceMap.remove(position);
		}

		@Override
		public int getCount() {
			if (DEBUG) Log.v(TAG, "getCount()");
			return NUM_TITLES;
		}
		
		@Override
		public String getTitle(int position) {
			if (DEBUG) Log.v(TAG, "getTitle()");
			return TITLES[position % NUM_TITLES].toUpperCase();
		}
		
		public MatchFragment getFragment(int position) {
			WeakReference<MatchFragment> weakRef = mPageReferenceMap.get(position);
			return (weakRef != null) ? weakRef.get() : null;
		}
	}
	
	@Override
	public void onBackPressed() {
		showConfirmExitDialog();
	}
	
	public void showConfirmExitDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.confirm_exit)
	           .setMessage(R.string.confirm_exit_message)
	           .setIcon(R.drawable.ic_dialog_alert_holo_light)
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        	   public void onClick(DialogInterface dialog, int id) {
	        		   MatchPagerActivity.this.setResult(Activity.RESULT_CANCELED);
	        		   MatchPagerActivity.this.finish();
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
}