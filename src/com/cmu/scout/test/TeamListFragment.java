package com.cmu.scout.test;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Teams;

public class TeamListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "TeamListFragment";
	private static final boolean DEBUG = true;
	
	private OnTeamSelectedListener teamSelectedListener;
	
	private static final int TEAM_LIST_LOADER = 0x01;
	
	// TODO: Change this to display a more complex list item in the future?
	private SimpleCursorAdapter mAdapter;

	private static final String[] bindFrom = { Teams.TEAM_NUM };
	private static final int[] bindTo = { R.id.team_name };
	
	public interface OnTeamSelectedListener {
		public void onTeamSelected(Uri uri);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "--ON CREATE VIEW--");
		
		// inflate custom layout (with additional empty-list display)
		return inflater.inflate(R.layout.team_listview, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.v(TAG, "-ON ACTIVITY CREATED-");

		getLoaderManager().initLoader(TEAM_LIST_LOADER, null, this);
		
		mAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.team_listview_item, null, bindFrom, bindTo, 0);
		
		setListAdapter(mAdapter);
	}
	
	/**
	 * "SupportActivity" required in order to get actionbarsherlock
	 * to work... don't ask me why.
	 */
	//@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "-ON ATTACH-");

		try {
			teamSelectedListener = (OnTeamSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTeamSelectedListener");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (DEBUG) Log.v(TAG, "ON LIST ITEM CLICK");

		final Uri uri = Teams.buildTeamIdUri(String.valueOf(id));
		teamSelectedListener.onTeamSelected(uri);
		
		// TODO: Implement once ContentProvider is finished		
		// This will build the appropriate URI and pass it to the TeamDetailsActivity
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String [] projection = { Teams._ID, Teams.TEAM_NUM };
		
		// TODO: This won't work until ContentProvider is at least partially
		// implemented!
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				Teams.CONTENT_URI, projection, null, null, 
				Teams.TEAM_NUM + " COLLATE LOCALIZED ASC");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}	
}
