package com.cmu.scout.test;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	private static final String TAG = "TabListener";
	private static final boolean DEBUG = true;

	private final Activity mActivity;
	private final String mTag;
	private final Class<T> mClass;
	private final Bundle mArgs;
	private Fragment mFragment;

	public TabListener(Activity activity, String tag, Class<T> clz) {
		this(activity, tag, clz, null);
	}

	public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
		mArgs = args;

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state. If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
		if (mFragment != null && !mFragment.isDetached()) {
			FragmentTransaction ft = mActivity.getFragmentManager()
					.beginTransaction();
			ft.detach(mFragment);
			ft.commit();
		}
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (DEBUG)
			Log.v(TAG, "onTabSelected()");

		if (mFragment == null) {
			mFragment = Fragment
					.instantiate(mActivity, mClass.getName(), mArgs);
			ft.add(android.R.id.content, mFragment, mTag);
		} else {
			ft.attach(mFragment);
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (DEBUG)
			Log.v(TAG, "onTabUnselected()");

		if (mFragment != null) {
			ft.detach(mFragment);
		}
	}

	// TODO: figure out onTabReselected
	// TODO: consider orientation changes, etc.
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		if (DEBUG)
			Log.v(TAG, "onTabReselected()");

		// Toast.makeText(mActivity, "Reselected!",
		// Toast.LENGTH_SHORT).show();
	}

	/*
	bar.addTab(bar
			.newTab()
			.setText("General")
			.setTabListener(
					new TabListener<DisplayGeneralFragment>(this,
							"general", DisplayGeneralFragment.class)));

	bar.addTab(bar
			.newTab()
			.setText("Offense")
			.setTabListener(
					new TabListener<DisplayOffenseFragment>(this,
							"offense", DisplayOffenseFragment.class)));
	 */
}