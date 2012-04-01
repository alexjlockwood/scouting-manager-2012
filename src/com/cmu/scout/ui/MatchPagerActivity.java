package com.cmu.scout.ui;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cmu.scout.R;
import com.cmu.scout.fragment.MatchFragment;
import com.cmu.scout.fragment.MatchInputAutoFragment;
import com.cmu.scout.fragment.MatchInputGeneralFragment;
import com.cmu.scout.fragment.MatchInputTeleOpFragment;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class MatchPagerActivity extends FragmentActivity {
	
	private static final String TAG = "MatchPagerActivity";
	private static final boolean DEBUG = true;
		
	private int mTeamId = -1;	
	private int mMatchId = -1;
	private int mTeamNum = -1;
	private int mMatchNum = -1;

	public static final int MAX_SCORE = 999;
		
	private MatchFragmentAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.match_scout_pager);

		final Intent data = getIntent();
		
		/* this should NEVER be null */
		if (data != null) {
			// retrieve team/match information
			mTeamId = data.getIntExtra(DashboardActivity.INTENT_TEAM_ID, -1);
			mMatchId = data.getIntExtra(DashboardActivity.INTENT_MATCH_ID, -1);
			mTeamNum = data.getIntExtra(DashboardActivity.INTENT_TEAM_NUM, -1);
			mMatchNum = data.getIntExtra(DashboardActivity.INTENT_MATCH_NUM, -1);
			
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
	public void onPause() {
		super.onPause();
		Toast.makeText(this, R.string.save_match_successful, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("mTeamId", mTeamId);
		outState.putInt("mMatchId", mMatchId);
		outState.putInt("mTeamNum", mTeamNum);
		outState.putInt("mMatchNum", mMatchNum);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mTeamId = savedInstanceState.getInt("mTeamId");
		mMatchId = savedInstanceState.getInt("mMatchId");
		mTeamNum = savedInstanceState.getInt("mTeamNum");
		mMatchNum = savedInstanceState.getInt("mMatchNum");
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
	
    public int getTeamId() {
    	return mTeamId;
    }
    
    public int getMatchId() {
    	return mMatchId;
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
		case android.R.id.home:
            // go to home screen when app icon in action bar is clicked
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		case R.id.clear_data:
			clearScreen();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");
		
		MatchFragment fragAuto = ((MatchInputAutoFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_AUTO));
		MatchFragment fragTeleOp = ((MatchInputTeleOpFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_TELEOP));
		MatchFragment fragGeneral = ((MatchInputGeneralFragment) mAdapter.getFragment(MatchFragmentAdapter.POSITION_GENERAL));
		
		if (fragAuto != null) fragAuto.clearScreen();
		if (fragTeleOp != null) fragTeleOp.clearScreen();
		if (fragGeneral != null) fragGeneral.clearScreen();
		
		Toast.makeText(this, R.string.screen_reset, Toast.LENGTH_SHORT).show();
	}
	
	// handles clicks from this activity's attached fragments
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
		
		private static final String[] TITLES = new String[] { "Autonomous", "Tele-Op", "Other" };

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
	
	public void decCounter(View v){
		EditText et = (EditText)(v);
		int value = new Integer(et.getText().toString())-1;
		value = Math.max(0, value);
		et.setText(""+value);
	}
	/*
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
	}*/
}