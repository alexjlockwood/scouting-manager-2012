package com.cmu.scout.fragment;

import android.support.v4.app.Fragment;

public abstract class MatchFragment extends Fragment {
	
	abstract public void updateDisplay(int viewId);
	
	abstract public void clearScreen();
	
	abstract public void loadData();
	
	abstract public void saveData();
}
