package com.cmu.scout.ui;

import android.text.TextUtils;
import android.widget.SearchView.OnQueryTextListener;

import com.actionbarsherlock.view.Menu;
import com.cmu.scout.R;

public class TeamGridActivity extends BaseTeamGridActivity 
		implements OnQueryTextListener {

	private static final String TAG = "TeamGridActivity";
	private static final boolean DEBUG = true;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.team_grid_options_menu, menu);
		
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
			//search.setOnQueryTextListener(this);
		//}
		return super.onCreateOptionsMenu(menu);
	}

	
	@Override
	public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mSelection = !TextUtils.isEmpty(newText) ? newText : null;       
        super.restartLoader();
        return true;
    }

    @Override 
    public boolean onQueryTextSubmit(String query) {
        // we don't care about this...
        return true;
    }
}
