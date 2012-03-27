package com.cmu.scout.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cmu.scout.R;
import com.cmu.scout.camera.BaseCameraActivity;
import com.cmu.scout.provider.ScoutContract.Teams;

public class TeamInputActivity extends BaseCameraActivity 
		implements PopupMenu.OnMenuItemClickListener {

	private static final String TAG = "TeamInputActivity";
	private static final boolean DEBUG = true;
	
	private static final int ACTION_TAKE_PHOTO_CODE = 1;
	
	private static final String PHOTO_PATH_STORAGE_KEY = "CurrentPhotoPath";
	private static final String TEAM_ID_STORAGE_KEY = "CurrentTeamId";
	private static final String TEAM_NUM_STORAGE_KEY = "CurrentTeamNum";
	private static final String CAMERA_ACTION = "android.hardware.camera";
	private String mCurrentPhotoPath;
	private String mCurrentPhotoName;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	private int mTeamId;
	private int mTeamNum;
	
	private ImageView mContact;
	
	private Spinner mDrive;
	private Spinner mWheel;
	private Spinner mStrategy;
	
	private EditText mTeamName;
	private EditText mComment;
	
	private ToggleButton mToggleAuto;
	private ToggleButton mToggleKinect; 
	private ToggleButton mToggleBarrier; 
	private ToggleButton mToggleBridge;
	
	private CheckBox mCheckLeft;
	private CheckBox mCheckMiddle;
	private CheckBox mCheckRight;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(PHOTO_PATH_STORAGE_KEY, mCurrentPhotoPath);
		outState.putLong(TEAM_ID_STORAGE_KEY, mTeamId);
		outState.putInt(TEAM_NUM_STORAGE_KEY, mTeamNum);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mCurrentPhotoPath = savedInstanceState.getString(PHOTO_PATH_STORAGE_KEY);
		mTeamId = savedInstanceState.getInt(TEAM_ID_STORAGE_KEY);
		mTeamNum = savedInstanceState.getInt(TEAM_NUM_STORAGE_KEY);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_input_main);
		
		// enable "up" navigation
		final ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		findViews();
		
		final Intent intent = getIntent();
		if (intent != null) {
			loadInfo(intent.getData());
		}	
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveData();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadInfo(getIntent().getData());
	}
	
	private void loadInfo(Uri teamUri) {	
		Cursor cur = getContentResolver().query(teamUri, null, null, null, null);
		if (cur != null && cur.moveToFirst()) {
			mTeamId = cur.getInt(cur.getColumnIndex(Teams._ID));
			mTeamNum = cur.getInt(cur.getColumnIndex(Teams.TEAM_NUM));
			final ActionBar actionBar = getActionBar();
			if (actionBar != null) {
				actionBar.setTitle(R.string.team_scouting_title);
				actionBar.setSubtitle("Team " + mTeamNum);
			}
		}
		
		loadContactPicture();
		
		mTeamName.setText(cur.getString(cur.getColumnIndex(Teams.TEAM_NAME)));
		mComment.setText(cur.getString(cur.getColumnIndex(Teams.COMMENTS)));
		mDrive.setSelection(cur.getInt(cur.getColumnIndex(Teams.DRIVE_SYSTEM))+1);
		mWheel.setSelection(cur.getInt(cur.getColumnIndex(Teams.WHEELS))+1);
		mStrategy.setSelection(cur.getInt(cur.getColumnIndex(Teams.STRATEGY))+1);
		mToggleAuto.setChecked(cur.getInt(cur.getColumnIndex(Teams.HAS_AUTONOMOUS))>0);
		mToggleKinect.setChecked(cur.getInt(cur.getColumnIndex(Teams.HAS_KINECT))>0);
		mToggleBarrier.setChecked(cur.getInt(cur.getColumnIndex(Teams.CAN_CROSS))>0);
		mToggleBridge.setChecked(cur.getInt(cur.getColumnIndex(Teams.CAN_PUSH_DOWN_BRIDGE))>0);
		
		String s = "" + cur.getInt(cur.getColumnIndex(Teams.PREFERRED_START));
		mCheckLeft.setChecked(s.charAt(0) == '1');
		mCheckMiddle.setChecked(s.contains("2"));
		mCheckRight.setChecked(s.contains("3"));
	}

	/**
	 * Setup menus
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.team_input_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_data:
			clearData();
			return true;
		case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, TeamGridActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		//case R.id.bt_cancel:
			//showConfirmExitDialog();
			//return true;
		//case R.id.bt_save:
			//saveData();
			//finish();
			//return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void findViews(){
		final View parent = findViewById(R.id.Team_Input_Screen);
		
		mContact = (ImageView) parent.findViewById(R.id.IV_Team_Picture);
		
		mDrive = (Spinner) parent.findViewById(R.id.SP_Drive);
		mWheel = (Spinner) parent.findViewById(R.id.SP_Wheel);
		mStrategy = (Spinner) parent.findViewById(R.id.SP_Strategy);
		
		mTeamName = (EditText) parent.findViewById(R.id.ET_Team_Name);
		mComment = (EditText) parent.findViewById(R.id.ET_Team_Comment);
		
		mToggleAuto = (ToggleButton) parent.findViewById(R.id.TBT_Team_Auto);
		mToggleKinect = (ToggleButton) parent.findViewById(R.id.TBT_Team_Kinect);
		mToggleBarrier = (ToggleButton) parent.findViewById(R.id.TBT_Team_Barrier);
		mToggleBridge = (ToggleButton) parent.findViewById(R.id.TBT_Team_Bridge);
		
		mCheckLeft = (CheckBox) parent.findViewById(R.id.CB_Left);
		mCheckMiddle = (CheckBox) parent.findViewById(R.id.CB_Middle);
		mCheckRight = (CheckBox) parent.findViewById(R.id.CB_Right);
	}

	private void saveData(){	
		int drive_n = mDrive.getSelectedItemPosition()-1;
		int wheel_n = mWheel.getSelectedItemPosition()-1;
		int strategy_n = mStrategy.getSelectedItemPosition()-1;
		int auto = (mToggleAuto.isChecked()) ? 1:0;
		int kinect = (mToggleKinect.isChecked()) ? 1:0;
		int barrier = (mToggleBarrier.isChecked()) ? 1:0;
		int bridge = (mToggleBridge.isChecked()) ? 1:0;
		
		String position = "";
		if (mCheckLeft.isChecked()) position += "1";
		if (mCheckMiddle.isChecked()) position += "2";
		if (mCheckRight.isChecked()) position += "3";
		int p_n = (!position.isEmpty()) ? new Integer(position) : 0;
		
		final Uri uri = Teams.buildTeamIdUri(""+mTeamId);
		
		ContentValues values = new ContentValues();
		values.put(Teams.TEAM_NUM, mTeamNum);
		values.put(Teams.TEAM_NAME, mTeamName.getText().toString());
		values.put(Teams.COMMENTS, mComment.getText().toString());
		values.put(Teams.DRIVE_SYSTEM, drive_n);
		values.put(Teams.WHEELS, wheel_n);
		values.put(Teams.STRATEGY, strategy_n);
		values.put(Teams.HAS_AUTONOMOUS, auto);
		values.put(Teams.HAS_KINECT,kinect);
		values.put(Teams.CAN_CROSS,barrier);
		values.put(Teams.CAN_PUSH_DOWN_BRIDGE, bridge);
		values.put(Teams.PREFERRED_START, p_n);
		
		// update the existing record
		// should always have data in database at this point
		getContentResolver().update(uri, values, null, null);
		//Toast.makeText(this, R.string.save_insert_successful, Toast.LENGTH_SHORT).show();
	}
	
	private void clearData(){
		mTeamName.setText("");
		mComment.setText("");
		mToggleAuto.setChecked(false);
		mToggleKinect.setChecked(false);
		mToggleBarrier.setChecked(false);
		mToggleBridge.setChecked(false);
		mCheckLeft.setChecked(false);
		mCheckMiddle.setChecked(false);
		mCheckRight.setChecked(false);
		mDrive.setSelection(0);
		mWheel.setSelection(0);
		mStrategy.setSelection(0);
	}
	
	// bypass any recreation when screen rotate
	// save operations for efficiency
	@Override
	public void onConfigurationChanged(Configuration newconfig){
		super.onConfigurationChanged(newconfig);
	}
	
	// popup menu for take photo
    public void onPhotoClick(View view){
    	Log.v(TAG, "onPhotoClick()");
    	PopupMenu popup = new PopupMenu(this, view);		
    	
    	final boolean cameraAvailable = isCameraAvailable(this, CAMERA_ACTION)
				&& isIntentAvailable(this, "android.media.action.IMAGE_CAPTURE");
		
    	popup.getMenuInflater().inflate(R.menu.team_photo_popup_menu, popup.getMenu());
		
    	// add only if the device has the camera application installed
    	popup.setOnMenuItemClickListener(this);
    	popup.getMenu().findItem(R.id.team_take_photo).setEnabled(cameraAvailable);
    	popup.show();
    }
    
    @Override
    public boolean onMenuItemClick(MenuItem item) {   
    	Log.v(TAG, "onMenuItemClick()");
    	switch (item.getItemId()) {
    	case R.id.team_take_photo:
			mCurrentPhotoPath = null;
			mCurrentPhotoName = null;
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_CODE, mTeamId);
    		break;
    	case R.id.team_choose_from_gallary: 
    		// TODO: implement this
    		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.team_delete_picture:
    		ContentValues updateValues = new ContentValues();
    		updateValues.put(Teams.TEAM_PHOTO, "");
    		showConfirmDeletePictureDialog(updateValues, (long)mTeamId);
    		break;
    	}
    	return false;
    }
    
	private void dispatchTakePictureIntent(int actionCode, int teamId) {
		if (DEBUG) Log.v(TAG, "dispatchTakePictureIntent()");

		final Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		mCurrentPhotoPath = null;
		mCurrentPhotoName = null;
		mTeamId = teamId;

		switch (actionCode) {
		case ACTION_TAKE_PHOTO_CODE:
			File f = null;
			try {
				f = createImageFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
				mCurrentPhotoName = null;
			}
			break;
		default:
			break;
		}

		startActivityForResult(takePicture, actionCode);
	}
	
	private File createImageFile() throws IOException {
		if (DEBUG) Log.v(TAG, "createImageFile()");

		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		mCurrentPhotoName = imageFileName;
		return imageF;
	}
	
	private void handleBigCameraPhoto() {
		if (DEBUG) Log.v(TAG, "handleBigCameraPhoto()");

		if (mCurrentPhotoPath != null) {
			scaleBitmap();
		}
	}

	private void galleryAddPic() {
		if (DEBUG) Log.v(TAG, "galleryAddPic()");

		if (isIntentAvailable(this,
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE")) {
			Intent mediaScanIntent = new Intent(
					"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
			Uri contentUri = Uri.fromFile(f);
			mediaScanIntent.setData(contentUri);
			this.sendBroadcast(mediaScanIntent);
		}
	}
	
	private void scaleBitmap() {
		new ScaleBitmapTask().execute();
	}
	
	// TODO: THIS IS A PRETTY STUPID WAY TO IMPLEMENT THIS... FIX LATER!!!
	private class ScaleBitmapTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... urls) {
			/* Get the size of the Button */
			int targetW = 512; // button.getWidth();
			int targetH = 512; // button.getHeight();

			/* Get the size of the image */
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			//bmOptions.inSampleSize = 4;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
			
			/* Figure out which way needs to be reduced less */
			int scaleFactor = 1;
			if ((targetW > 0) || (targetH > 0)) {
				scaleFactor = Math.min(photoW / targetW, photoH / targetH);
			}
			
			/* Set bitmap options to scale the image decode target */
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			/* Decode the JPEG file into a Bitmap */
			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			
			final Uri photoUri = Uri.parse(MediaStore.Images.Media.insertImage(
					getContentResolver(), bitmap,
					mCurrentPhotoName + "_scaled.jpg", null));

			final Uri updateUri = Teams.buildTeamIdUri(""+mTeamId);
			
			ContentValues values = new ContentValues();
			values.put(Teams.TEAM_PHOTO, photoUri.toString());

			getContentResolver().update(updateUri, values, null, null);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			galleryAddPic();
			loadContactPicture();
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		switch (requestCode) {
    		case ACTION_TAKE_PHOTO_CODE:
    			handleBigCameraPhoto();
    			break;
    		}
    	}
    }

    private void loadContactPicture() {
    	Log.v(TAG, "loadContactPicture()");
    	
    	final Uri teamUri = Teams.buildTeamIdUri("" + mTeamId);
    	final String[] proj = { Teams.TEAM_PHOTO };
    	final Cursor cur = getContentResolver().query(teamUri, proj, null, null, null);
    	
    	if (cur != null && cur.moveToFirst()) {
			final String photoUri = cur.getString(cur.getColumnIndex(Teams.TEAM_PHOTO));
			
			if (photoUri != null) {
				long photoId = Long.parseLong(Uri.parse(photoUri).getLastPathSegment());
				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
						getContentResolver(), photoId,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				mContact.setImageBitmap(bitmap);
			} else {
				mContact.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_picture));
			}
    	}
    }
    /*
	@Override
	public void onBackPressed() {
		showConfirmExitDialog();
	}*/
	
	public void showConfirmExitDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.confirm_exit)
	           .setMessage(R.string.confirm_exit_message)
	           .setIcon(R.drawable.ic_dialog_alert_holo_light)
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        	   public void onClick(DialogInterface dialog, int id) {
	        		   TeamInputActivity.this.setResult(Activity.RESULT_CANCELED);
	        		   TeamInputActivity.this.finish();
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
	
	public void showConfirmDeletePictureDialog(final ContentValues updateValues, final long teamId) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.confirm_delete_picture)
	           .setMessage(R.string.confirm_delete_picture_message)
	           .setIcon(R.drawable.ic_dialog_alert_holo_light)
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        	   public void onClick(DialogInterface dialog, int id) {
	        		   final Uri teamUri = Teams.buildTeamIdUri("" + teamId);
	        		   getContentResolver().update(teamUri, updateValues, null, null);
	        		   mContact.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_picture));
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
