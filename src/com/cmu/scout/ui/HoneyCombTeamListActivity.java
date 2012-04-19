package com.cmu.scout.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cmu.scout.R;
import com.cmu.scout.camera.BaseCameraActivity;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.Teams;

public class HoneyCombTeamListActivity extends BaseCameraActivity
		implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "HoneyCombTeamListActivity";
	private static final boolean DEBUG = true;
	
	private static final int TEAM_LIST_LOADER = 0x01;
	
	// camera intent request code
	private static final int ACTION_TAKE_PHOTO_CODE = 1;
	 
	private static final String PHOTO_PATH_STORAGE_KEY = "CurrentPhotoPath";
	private static final String TEAM_ID_STORAGE_KEY = "CurrentTeamId";
	
	private static final String CAMERA_ACTION = "android.hardware.camera";
	private static final String IMAGE_CAPTURE = "android.media.action.IMAGE_CAPTURE";
	private static final String MEDIA_SCANNER = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
	
	private String mCurrentPhotoPath;
	private String mCurrentPhotoName;
	private long mCurrentTeamId;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	public String mSelection = null;
	
	private TeamListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_list_view);
		
		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		
		// enable "up" navigation
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setActionBarTitle(getResources().getString(R.string.team_scouting_title));

		getSupportLoaderManager().initLoader(TEAM_LIST_LOADER, null, this);

		mAdapter = new TeamListAdapter(this, R.layout.team_list_row, null, 0);
		
		ListView lv = (ListView) findViewById(R.id.team_list);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				onTeamSelected((int)id);
			}
		});

		lv.setAdapter(mAdapter);
		registerForContextMenu(lv);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(PHOTO_PATH_STORAGE_KEY, mCurrentPhotoPath);
		outState.putLong(TEAM_ID_STORAGE_KEY, mCurrentTeamId);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mCurrentPhotoPath = savedInstanceState.getString(PHOTO_PATH_STORAGE_KEY);
		mCurrentTeamId = savedInstanceState.getLong(TEAM_ID_STORAGE_KEY);
	}

	public void onTeamSelected(int id) {
		final Intent data = new Intent(this, HoneyCombTeamInputActivity.class);
		final Uri uri = Teams.buildTeamIdUri("" + id);
		data.setData(uri);
		startActivity(data);
	}

	/**
	 * Setup menus
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.team_grid_options_menu, menu);
		
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
			search.setOnQueryTextListener(this);
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            // go to home screen when app icon in action bar is clicked
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		case R.id.menu_add_team:
			showDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		final boolean cameraAvailable = isCameraAvailable(this, CAMERA_ACTION)
				&& isIntentAvailable(this, IMAGE_CAPTURE);
		
		getMenuInflater().inflate(R.menu.team_grid_context_menu, menu);
		
		// add only if the device has the camera application installed
		menu.findItem(R.id.menu_take_picture).setEnabled(cameraAvailable);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menu_take_picture:
			mCurrentPhotoPath = null;
			mCurrentPhotoName = null;
			mCurrentTeamId = -1;
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_CODE, info.id);
			return true;
		case R.id.menu_view_picture:
			Intent intent = new Intent();  
			intent.setAction(android.content.Intent.ACTION_VIEW);
			
			Uri teamUri = Teams.buildTeamIdUri(""+info.id);
			Cursor cur = getContentResolver().query(teamUri, null, null, null, null);
			
			Uri uri = null;
			if (cur != null && cur.moveToFirst()) {
				String photo = cur.getString(cur.getColumnIndex(Teams.TEAM_PHOTO));
				uri = (!TextUtils.isEmpty(photo)) ? Uri.parse(photo) : null;
				cur.close();
			}
			
			if (uri != null) {
				intent.setDataAndType(uri, "image/*");  
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.menu_delete_team:
			showConfirmDeleteDialog(info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	
	
	private void setActionBarTitle(String title) {
		if (DEBUG) Log.v(TAG, "setActionBarTitle()");
		if (title != null) {
			getSupportActionBar().setTitle(title);
		}
	}

	@SuppressWarnings("unused")
	private void setActionBarSubtitle(String subtitle) {
		if (DEBUG) Log.v(TAG, "setActionBarSubtitle()");
		if (subtitle != null) {
			getSupportActionBar().setSubtitle(subtitle);
		}
	}

	/**
	 * Add-team dialog methods
	 */

	public void showDialog() {
		if (DEBUG) Log.v(TAG, "showDialog()");
		AddTeamDialog.newInstance().show(getSupportFragmentManager(), AddTeamDialog.TAG);
	}

	public void doPositiveClick(String teamName) {
		if (DEBUG) Log.v(TAG, "doPositiveClick()");
		
		if (TextUtils.isEmpty(teamName)) {
			Toast.makeText(this, "Invalid team number.", Toast.LENGTH_SHORT).show();
		} else {	
			final Cursor cur = getContentResolver().query(Teams.CONTENT_URI, new String[] { Teams.TEAM_NUM }, 
					Teams.TEAM_NUM + " = ? ", new String[] { ""+teamName }, null);
			
			if (cur != null && cur.moveToFirst()) {
				// user is attempting to insert duplicate team number into database
				Toast.makeText(this, R.string.duplicate_team_number, Toast.LENGTH_SHORT).show();
				cur.close();
			} else {
				// insert new team into database
				int teamNum = Integer.valueOf(teamName);
			
				ContentValues values = new ContentValues();
				values.put(Teams.TEAM_NUM, teamNum);
			
				getContentResolver().insert(Teams.CONTENT_URI, values);
			}
		}
	}
	
	public void doNegativeClick() {
		if (DEBUG) Log.v(TAG, "doNegativeClick()");
		/* Do nothing */
	}

	public static class AddTeamDialog extends DialogFragment {
		private static final String TAG = "AddTeamDialog";
		private static final boolean DEBUG = true;
		
		public static AddTeamDialog newInstance() {
			if (DEBUG) Log.v(TAG, "newInstance()");
			return new AddTeamDialog();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (DEBUG) Log.v(TAG, "onCreateDialog");

			LayoutInflater factory = LayoutInflater.from(getActivity());
			final View edit = factory.inflate(R.layout.add_team_edit_text, null);
			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.add_team_dialog_title)
					.setView(edit)
					.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String teamName = ((EditText) edit).getText().toString();
								((HoneyCombTeamListActivity) getActivity()).doPositiveClick(teamName);
							}
						})
					.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								((HoneyCombTeamListActivity) getActivity()).doNegativeClick();
							}
						}).create();
		}
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mSelection = !TextUtils.isEmpty(newText) ? newText : null;       
        getSupportLoaderManager().restartLoader(TEAM_LIST_LOADER, null, this);
        return true;
    }

    @Override 
    public boolean onQueryTextSubmit(String query) {
        // we don't care about this...
        return true;
    }

	/**
	 * Loader callback methods
	 */
	
	private static final String[] PROJECTION = new String[] { Teams._ID, Teams.TEAM_NUM, Teams.TEAM_PHOTO };
	private static final String DEFAULT_SORT = " COLLATE LOCALIZED ASC";
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(mSelection == null){
			return new CursorLoader(this, Teams.CONTENT_URI, PROJECTION, null, null, Teams.TEAM_NUM + DEFAULT_SORT);
		}
		
		return new CursorLoader(this, Teams.CONTENT_URI, PROJECTION, Teams.TEAM_NUM + " LIKE ?", new String[]{ mSelection + "%"}, Teams.TEAM_NUM + DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private static class TeamListAdapter extends ResourceCursorAdapter {
		
		private static final String TAG = "TeamListAdapter";
		private static final boolean DEBUG = false;
		
		public TeamListAdapter(Context context, int layout, Cursor cur, int flags) {
			super(context, layout, cur, flags);
		}
		
		@Override
		public void bindView(View view, Context ctx, Cursor cur) {
			if (DEBUG) Log.v(TAG, "bindView()");			

			// use ViewHolder pattern to reduce number of times we search the
			// View hierarchy with "findViewById"
			ViewHolder holder = (ViewHolder) view.getTag();
	        if (holder == null) {
	            holder = new ViewHolder();
	            
	            // initialize TextViews
	            holder.teamNum = (TextView) view.findViewById(R.id.team_list_row_number);
	            holder.teamPhoto = (ImageView) view.findViewById(R.id.team_list_row_photo);
	            
	            // initialize column indices
	            holder.teamNumCol = cur.getColumnIndexOrThrow(Teams.TEAM_NUM);
	            holder.teamPhotoCol = cur.getColumnIndexOrThrow(Teams.TEAM_PHOTO);
	            
	            view.setTag(holder);
	        }
	        	        
	        holder.teamNum.setText("" + cur.getInt(holder.teamNumCol));
	        
			String uri = cur.getString(holder.teamPhotoCol);		
			if (!TextUtils.isEmpty(uri)) {
				long photoId = Long.parseLong(Uri.parse(uri).getLastPathSegment());
				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(ctx.getContentResolver(), photoId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
				holder.teamPhoto.setImageBitmap(bitmap);
			} else {
				holder.teamPhoto.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_contact_picture));
			}
		}
				
		static class ViewHolder {
	        TextView teamNum;
	        ImageView teamPhoto;
	        int teamNumCol, teamPhotoCol;
	    }
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (DEBUG) Log.v(TAG, "ON ACTIVITY RESULT: " + requestCode + " " + resultCode);
    	switch (requestCode) {
    	case ACTION_TAKE_PHOTO_CODE:
    		if (resultCode == RESULT_OK) {
    			handleBigCameraPhoto();
    			break;
    		}
    	}
    }
    
	  private void dispatchTakePictureIntent(int actionCode, long teamId) {
		if (DEBUG) Log.v(TAG, "dispatchTakePictureIntent()");

		final Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		mCurrentPhotoPath = null;
		mCurrentPhotoName = null;
		mCurrentTeamId = teamId;

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
		}

		startActivityForResult(takePicture, actionCode);
	}
	
	private File createImageFile() throws IOException {
		if (DEBUG) Log.v(TAG, "createImageFile()");

		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
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

		if (isIntentAvailable(this, MEDIA_SCANNER)) {
			Intent mediaScanIntent = new Intent(MEDIA_SCANNER);		
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
			// TODO: FIX THIS LATER!
			int targetW = 512;
			int targetH = 512;

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
			
			Uri photoUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, mCurrentPhotoName + "_scaled.jpg", null));

			ContentValues values = new ContentValues();
			values.put(Teams.TEAM_PHOTO, photoUri.toString());

			getContentResolver().update(Teams.buildTeamIdUri("" + mCurrentTeamId), values, null, null);
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mAdapter.notifyDataSetChanged();
			galleryAddPic();
		}
	}

	public void showConfirmDeleteDialog(final long teamId) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.confirm_delete_team)
	           .setMessage(R.string.confirm_delete_team_message)
	           .setIcon(R.drawable.ic_dialog_alert_holo_light)
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        	   public void onClick(DialogInterface dialog, int id) {
	        		   Uri teamUri = Teams.buildTeamIdUri("" + teamId);
	        		   Uri teamMatchesUri = Matches.buildMatchTeamIdUri(""+teamId);
	        		   getContentResolver().delete(teamUri, null, null);
	        		   getContentResolver().delete(teamMatchesUri, null, null);
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
