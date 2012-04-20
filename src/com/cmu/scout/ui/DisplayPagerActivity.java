package com.cmu.scout.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.cmu.scout.R;
import com.cmu.scout.fragment.DisplayAutoFragment;
import com.cmu.scout.fragment.DisplayMainFragment;
import com.cmu.scout.fragment.DisplayScoutOneFragment;
import com.cmu.scout.fragment.DisplayScoutTwoFragment;
import com.cmu.scout.fragment.DisplayTeleOpFragment;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class DisplayPagerActivity extends SherlockFragmentActivity 
		implements OnTeamSelectedListener {
	
//	private static final String TAG = "DisplayPagerActivity";
//	private static final boolean DEBUG = true;
		
	private DisplayFragmentAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.display_teams_pager);
		
	    // enable "up" navigation
		final ActionBar actionBar = getSupportActionBar();	 
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.display_title);
		}
		
		mAdapter = new DisplayFragmentAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.display_pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TabPageIndicator)findViewById(R.id.display_indicator);
		mIndicator.setViewPager(mPager);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			// go to home screen when app icon in action bar is clicked
			Intent intent = new Intent(this, DashboardActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(intent);
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onTeamEdit(Uri uri) {
		final Intent data = new Intent(this, HoneyCombTeamInputActivity.class);
		data.setData(uri);
		startActivity(data);
	}
	
	@Override
	public void onTeamSelected(Uri uri) {
		final Intent i = new Intent(this, DisplayPagerMatchesActivity.class);
		i.setData(uri);
		startActivity(i);
	}
	
	public static class DisplayFragmentAdapter extends FragmentPagerAdapter 
			implements TitleProvider {
		
//		private static final String TAG = "DisplayFragmentAdapter";
//		private static final boolean DEBUG = false;
		
		public static final int POSITION_MAIN = 0;
		public static final int POSITION_AUTO = 1;
		public static final int POSITION_TELEOP = 2;
		public static final int POSITION_SCOUT_ONE = 3;
		public static final int POSITION_SCOUT_TWO = 4;
		
		private static final String[] TITLES = new String[] { 
			"Main", 
			"Autonomous", 
			"TeleOp", 
			"Team-scout (1)", 
			"Team-scout (2)" 
		};

		private int mCount = TITLES.length;

		public DisplayFragmentAdapter(FragmentManager fm) {		
			super(fm);
//			if (DEBUG) Log.v(TAG, "DisplayFragmentAdapter()");
		}

		@Override
		public Fragment getItem(int position) {
//			if (DEBUG) Log.v(TAG, "getItem()");
			switch (position) {
			case POSITION_MAIN: return DisplayMainFragment.newInstance();
			case POSITION_AUTO: return DisplayAutoFragment.newInstance();
			case POSITION_TELEOP: return DisplayTeleOpFragment.newInstance();
			case POSITION_SCOUT_ONE: return DisplayScoutOneFragment.newInstance();
			case POSITION_SCOUT_TWO: return DisplayScoutTwoFragment.newInstance();
			}
			return null;
		}

		@Override
		public int getCount() {
//			if (DEBUG) Log.v(TAG, "getCount()");
			return mCount;
		}
		
		@Override
		public String getTitle(int position) {
//			if (DEBUG) Log.v(TAG, "getTitle()");
			return TITLES[position % TITLES.length].toUpperCase();
		}
	}	
}