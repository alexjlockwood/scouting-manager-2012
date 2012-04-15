package com.cmu.scout.fragment;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class MatchFragment extends SherlockFragment {
	
	abstract public void updateDisplay(int viewId);
	
	abstract public void clearScreen();
	
	abstract public void loadData();
	
	abstract public void saveData();
}
