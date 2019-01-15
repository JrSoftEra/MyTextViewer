/*
 * Copyright (C) 2018 See Author's File
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrsoftera.mytextviewer;

import java.io.File;
import android.app.Activity;
import android.app.ListActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Environment;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.*;
import android.widget.Button;
import android.support.v4.content.FileProvider;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.text.format.DateFormat;
//import com.jrsoftera.mytextviewer.FileSelector.*;
import java.io.IOException;
//import android.support.mediacompat.*;

public class FileSelector extends ListActivity {
	private File mExternRootDir;
	private File mExternDir;
	private File[] mExternFiles;
	private String[] mNames;
	private String[] mExternFilePath;
	//private CharSequence[] mDates;
	private String[] mDates;
	//private ArrayAdapter<String> mArrayAdapter;
		private ListView fileListVw;

		private FileListAdapter mfileListAdapter;

		private static final int ADD = Menu.FIRST;

		private static final int FOLD = Menu.FIRST + 1;

		private static final int FILE = Menu.FIRST + 2;

		private static final int DELETE = Menu.FIRST + 3;

		private static final int SETTING = Menu.FIRST + 4;

		private static final int SORT = SETTING + 1;

	private static final int SORT_ALPHA = SETTING + 2;

	private DateFormat mdateForm;

	private boolean newFolder = false;
	private boolean newFile = false;

	public static final int KEY1DIALOG = 0;

	public static final int KEY2DIALOG = 1;
protected void onCreate(Bundle b) {
    super.onCreate(b);
	getWindow().requestFeature(Window.FEATURE_OPTIONS_PANEL);
	//final TextView mpthTvw = new TextView(this);
	fileListVw = getListView();
	//fileListVw.setTextFilterEnabled(true);
	mExternRootDir = Environment.getExternalStorageDirectory();
	mExternDir = new File(mExternRootDir,"");
	mdateForm = new DateFormat();
	mfileListAdapter = new FileListAdapter(this);
	setFileList(mExternDir);
	setResult(RESULT_CANCELED, null );
	//fileListVw.setAdapter(mfileListAdapter);
	//setListAdapter(mArrayAdapter)
}
@Override
public void onBackPressed(){
	File parentFileDir = mExternDir.getParentFile();
	String mPath = parentFileDir.getAbsolutePath();
	if (mPath.equals(mExternRootDir.getParent()) ){
		finish();
	}
	setFileList(parentFileDir);
	if (BuildConfig.DEBUG)
	Log.d("FileSelector","onBackPressed() parentFileDir path: " + mPath );
}
// Begin include FileListAdapter
	public class FileListAdapter extends BaseAdapter
		{
			private File isDir;
	class ViewHolder {
		TextView text1;
		TextView text2;
		ImageView mIcon;
		}
	private LayoutInflater mInflater;
	private Bitmap iconFold;
	private Bitmap iconFile;
	private Bitmap iconFile2;
	public FileListAdapter(Context cntxt) {
		mInflater = LayoutInflater.from(cntxt);
		iconFold = BitmapFactory.decodeResource(cntxt.getResources(), R.drawable.ic_folder);
		iconFile = BitmapFactory.decodeResource(cntxt.getResources(), R.drawable.ic_file_icon_1);
			iconFile2 = BitmapFactory.decodeResource(cntxt.getResources(), R.drawable.ic_file_icon_2);
		}
	@Override
	public int getCount() {
		Log.i("FileSelector","BaseAdapter getCount(): " + mExternFilePath.length );
		return mExternFilePath.length;
	}
	@Override
	public Object getItem(int p1) { return p1;
		}
	@Override
	public long getItemId(int p1) { return p1;
		}
	@Override
	public View getView(int p1, View p2, ViewGroup p3) {
		ViewHolder holder;
	  if (p2 == null) {
		p2 = mInflater.inflate(R.layout.file_list_layout, null);
		holder = new ViewHolder();
		holder.text1 = (TextView) p2.findViewById(R.id.text1);
		holder.text2 = (TextView) p2.findViewById(R.id.text2);
		holder.mIcon = (ImageView) p2.findViewById(R.id.icon);
		p2.setTag(holder);
	  } else {
		//Log.i("BaseAdapter","getView() is not null" );
		holder = (ViewHolder) p2.getTag();
			}
		holder.text1.setText( mNames[p1] );
		holder.text2.setText( mDates[p1]  );
		isDir = new File( mExternFilePath[p1] );
		//Log.i("BaseAdapter","getView()"+"mExternslPath[p1]: "+ mExternFilePath[p1].toString() );
		holder.mIcon.setImageBitmap( getBitmap(p1) );//(isDir.isDirectory()) == true ? iconFold : iconFile );
		return p2;
		}
  protected Bitmap getBitmap(int p) {
		if (isDir.isDirectory()) {
				return iconFold;
		} else if( mNames[p].endsWith(".java") || mNames[p].endsWith(".txt")){    //("[*.pdf]")) {
				return iconFile2;
		} else {
				return iconFile;
		}
      }
}//end include FileListAdapter
private void setFileList(File inDir){
	if (mExternFiles != null) {
		if (BuildConfig.DEBUG)
		Log.i("FileSelector","setFileList mExternFiles not null length: " + mExternFiles.length );
		mExternFiles = null;
	}
	mExternDir = inDir;
	mExternFiles = mExternDir.listFiles();
	//Collections.sort(mExternFiles);
	int len = mExternFiles.length;
	mDates = new String[len];
	mNames = new String[len];
	mExternFilePath = new String[len];
	for(int i = 0 ; i < len ; i++) {
		mExternFilePath[i] = mExternFiles[i].getAbsolutePath();
		mNames[i] = mExternFiles[i].getName();
		mDates[i] = (mdateForm.format("yyyy-MM-dd_HH:mm:ss",mExternFiles[i].lastModified())).toString();
		}
	if (BuildConfig.DEBUG)
	Log.d("FileSelector", "setFileList() mExternFiles.length: " + len );
	//setResult(Activity.RESULT_CANCELED, null);
	/*if (mfileListAdapter != null){
		Log.i("FileSelector","setFileList setArrayAdapter allready exist");
		mfileListAdapter = null;
	}*/
	/**mfileListAdapter = new FileListAdapter(this);*/
	fileListVw.setAdapter(mfileListAdapter);
	}
	private Uri fileUri;
@Override
public void onListItemClick(ListView adaptView, View v, int position, long id) {
	File clickedFile = new File( mExternFilePath[position] );
	if (clickedFile.isDirectory()) {
		mExternDir = clickedFile;
		if (BuildConfig.DEBUG)
		Log.d("FileSelector","onListItemClick() selected file is directory position: " + mExternFilePath[position] );
			setFileList(mExternDir);
		} else {
		final Intent mResultIntent = new Intent();//"com.jrsoftera.mytextviewer.ACTION_RETURN_FILE");
		if(BuildConfig.DEBUG)
			Log.d("FileSelector","onListItemClick() mExternFilepath: " + mExternFilePath[position]);
	try { fileUri = FileProvider.getUriForFile(this,
		"com.jrsoftera.mytextviewer.MyTextViewer.fileprovider", clickedFile); 
		  } catch (IllegalArgumentException e) {
		Log.e("FileSelector", "Cann't getUriforFile:", e); 
		}
	if (fileUri != null) {
		mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		mResultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
		FileSelector.this.setResult(Activity.RESULT_OK, mResultIntent);
		}
		else {
		mResultIntent.setDataAndType(null, "");
		FileSelector.this.setResult(Activity.RESULT_CANCELED, mResultIntent);
		}
		if (BuildConfig.DEBUG) {
		Log.d("FileSelector","getUriForFile() fileUri: " + fileUri );
		}
		finish();
		}
}

@Override
public boolean onCreatePanelMenu(int featureId, Menu menu)
{
		Menu item = menu.addSubMenu(Menu.NONE, ADD, 0, "Add New")
		.setIcon(android.R.drawable.ic_menu_add);
		MenuItem itmFold = item.add(Menu.NONE, FOLD, 0, "Folder");
		itmFold.setIcon(R.drawable.ic_folder);
		MenuItem itmFile = item.add(Menu.NONE, FILE, 0, "File");
		itmFile.setIcon(R.drawable.ic_file_icon_2);
		MenuItem item2 = menu.add(Menu.NONE, DELETE, 0, "Delete");
		item2.setIcon(android.R.drawable.ic_menu_delete);
		Menu sub = menu.addSubMenu(Menu.NONE, SORT, 0, "Sort").setIcon(
		android.R.drawable.ic_menu_sort_by_size);
		MenuItem itmSort = sub.add(Menu.NONE, SORT_ALPHA, 0, "Alphabetically");
		itmSort.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		Menu sub2 = menu.addSubMenu("Settings");
		MenuItem item3 = sub2.add(0, SETTING, 0, "Settings");
		item3.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreatePanelMenu(featureId, menu);
}

@Override
public boolean onMenuItemSelected(int featureId, MenuItem item)
{
		switch (item.getItemId()) {
				case FOLD:
					    newFolder = true;
						SaveNameDialog svname = new SaveNameDialog(this);
						svname.show();
						return true;
				case FILE:
					    newFile = true;
						SaveNameDialog saveName = new SaveNameDialog(this);
						saveName.show();
						return true;
				case DELETE:
					    break;
				case SORT_ALPHA:
					/*Collections.sort(mExternFiles);
					    return true;*/
					    break;
				case SETTING:
					    break;
		}
		return super.onMenuItemSelected(featureId, item);
}
// Begin include SaveNameDialog
		protected class SaveNameDialog extends Dialog
		{
				//private static final int edit_filename = 1;
				//private static final int save_button = 2;
				private String textName;
			protected SaveNameDialog(Context c) {
					super(c);
			}
				private static final String TAG = "SaveNameDialog";
			@Override
			protected void onCreate(Bundle savedInstanceState)
			{
					super.onCreate(savedInstanceState);
					Context dctxt = getContext();
					LinearLayout lnr = new LinearLayout(dctxt);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.FILL_PARENT , LayoutParams.WRAP_CONTENT);
					lnr.setLayoutParams(lp);
					lnr.setOrientation(LinearLayout.VERTICAL);
					final EditText editName = new EditText(dctxt);
					//editName.setId(edit_filename);
					editName.setLayoutParams(new LinearLayout.LayoutParams(
																			 LayoutParams.MATCH_PARENT , LayoutParams.WRAP_CONTENT) );
					lnr.addView(editName);
					final Button saveButton = new Button(dctxt);
					saveButton.setLayoutParams(new LinearLayout.LayoutParams(
																				 LayoutParams.MATCH_PARENT , LayoutParams.WRAP_CONTENT) );
					//saveButton.setId(save_button);
					saveButton.setText("Save");
				    
				saveButton.setOnClickListener(new View.OnClickListener() {
						//private File newFile;
						@Override
						public void onClick(View p1)
						{
							textName = editName.getText().toString();
							if (!textName.isEmpty()) {
								if(newFolder) {
									//createFolder();
									File newFold = new File(mExternDir,textName);
									if(!newFold.exists()){
										//newFold.setExecutable(true);
										//newFold.setReadable(true);
										//newFold.setWritable(true);
										if(newFold.mkdir()){
										showDialog(FileSelector.this.KEY1DIALOG);
										} else {
											if (BuildConfig.DEBUG)
											Log.d("FileSelector","Folder creation failed..");
										}
									}
									if (newFold.exists()){
										dismissDialog(FileSelector.this.KEY1DIALOG);
									}
									newFolder = false;
									dismiss();
								} else if(newFile) {
									//createFile();
									File newfile = new File(mExternDir,textName);
									try
									{
										//newFile.setExecutable(true);
										//newFile.setReadable(true);
										//newFile.setWritable(true);
										if (newfile.createNewFile()){
										showDialog(FileSelector.this.KEY2DIALOG);
										}
									}
									catch (IOException e)
									{
										if (BuildConfig.DEBUG)
											Log.d("FileSelector","onMenuSelected Creating file failed: ",e);
									}
									if(newfile.exists()){
										dismissDialog(FileSelector.this.KEY2DIALOG);
									}
									newFile = false;
									dismiss();
								}
								if (BuildConfig.DEBUG)
								Log.d(TAG,"new string name created"+textName);
								} else {
								if (BuildConfig.DEBUG)
								Log.d(TAG, "edit filename failed to create..");
								//textFileName = editName.getText().toString();
								}//hide();
						}	
					});
					lnr.addView(saveButton);
					setContentView(lnr);
			}
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id) {
			case KEY1DIALOG:
		ProgressDialog progrsDai= new ProgressDialog(this);
		progrsDai.setMessage("New Folder created..Please wait while loading..");
		getListView().invalidateViews();
		    return progrsDai;
			case KEY2DIALOG:
		ProgressDialog progrsDia2= new ProgressDialog(this);
		progrsDia2.setMessage("New File created..Please wait while loading..");
		getListView().invalidateViews();
		    return progrsDia2;
		}
		return super.onCreateDialog(id);
	}// End include SaveNameDialog
	
}
