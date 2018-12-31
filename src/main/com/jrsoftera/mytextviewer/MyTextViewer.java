/*
 * Copyright (C) 2018 See Author's File.
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.ScrollView.LayoutParams;
import android.widget.FrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
//import java.io.File;
//import java.io.ByteArrayOutputStream;

public class MyTextViewer extends TabActivity implements TabHost.TabContentFactory
{
	//private static final String SHARED_FILE_NAME = "shared.png";
	//private static final int WIDTH = 200;
    //private static final int HEIGHT = 200;
    //private static final int STRIDE = 256;

	private String editCont;

	private static final int RequestCode = 1;

	private static final int NEWQUERY = Menu.FIRST;

	private static final int REMOVE = Menu.FIRST + 1;

	private Cursor returnCur;

	private String name;

	private String size;

	private static TabHost tabHost;

	private String tag;

	private int tabCount;

	//private static int mBitmapView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        tabHost = getTabHost();
        //LayoutInflater.from(this).inflate(R.layout.tabs1, getTabHost().getTabContentView(), true);
        /*tabHost.addTab(tabHost.newTabSpec("tab1")
					   .setIndicator(name+"%n"+size)
					   .setContent(this));*/
        /*tabHost.addTab(tabHost.newTabSpec("tab2")
					   .setIndicator("tab2")
					   .setContent(R.id.view3));*/
		/*tabHost.addTab(tabHost.newTabSpec("tab3")
		 .setIndicator("Bitmap")
		 .setContent(mBitmapView));*/
    }
	@Override
	public View createTabContent(String p1)
	{
		tag = p1;
		final ScrollView scrl = new ScrollView(this);
		final LayoutParams scrPrms = new LayoutParams(
			LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		scrl.setLayoutParams(scrPrms);
		TextView editv = new EditText(this);
		editv.setPadding(4, 4, 4, 4);
		editv.setBackgroundColor(0x4a2530aa);
		editv.setTextSize(12.f);
		editv.setText(tag+"\n"+editCont);
		scrl.addView(editv);
		return scrl;
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        getMenuInflater().inflate(R.menu.action_bar_share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent());

		MenuItem newTab = menu.add(0,NEWQUERY, 0, "Query");
		newTab.setIcon(android.R.drawable.ic_menu_search);
		MenuItem remove = menu.add(0,REMOVE, 0, "Remove");
		remove.setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId() ) {
			case NEWQUERY:
				Intent query = new Intent(this, FileSelector.class);
				//query.setAction(Intent.ACTION_PICK);
				//query.addCategory(Intent.CATEGORY_OPENABLE);
				//query.setType("text/plain");
				startActivityForResult(query,RequestCode);
				return true;
			case REMOVE:
				tabHost.clearAllTabs();
				tabCount = -1;
				if (BuildConfig.DEBUG) {
				Log.d("MyTextViewer","onOptionsItemClick getCurrentTabTag: "+tabHost.getCurrentTabTag());
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK && requestCode == RequestCode) {
			if ( null != data) {
				final Uri uri = data.getData();
				final ContentResolver cr = getContentResolver();
				cr.getPersistedUriPermissions();
				String datype = cr.getType(uri);
				/*final File file = new File(uri.getPath());
				String name = file.getName();*/
			try {
                    returnCur = cr.query(uri, null, null, null, null, null);
                int nameIndex = returnCur.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				int sizeIndex = returnCur.getColumnIndex(OpenableColumns.SIZE);
               if (returnCur.moveToFirst()) {
				name = returnCur.getString(nameIndex) ;
				size = Long.toString( returnCur.getLong(sizeIndex));
                } else {
				if (BuildConfig.DEBUG)
                 Log.e("MyTextViewer","onActivityResult"+"Cann't move to first.." );
                }
				} catch (CursorIndexOutOfBoundsException e){
					if (BuildConfig.DEBUG)
					Log.e("MyTextViewer","returnCur: ", e);
				} finally {
					returnCur.close();
				}
				if (BuildConfig.DEBUG)
					Log.e("MyTextViewer","onActivityResult: "+datype);
				if (datype.equals("text/plain") || datype.equals("application/x-java")) {
					InputStream in = null;
					BufferedReader bRder = null;
					tabCount++;
					String tabCountTag = Integer.toString(tabCount);
					try
					{
						 in = cr.openInputStream(uri);
						bRder = new BufferedReader( new InputStreamReader(in) );
						StringBuilder sb = new StringBuilder(); String line;
						while ( (line = bRder.readLine()) != null ) {
							sb.append(line);
						}
						editCont = sb.toString();
					} catch (IOException e) {
						if (BuildConfig.DEBUG)
						Log.e("MyTextViewer","onActivityResult: Cautch IoException: ",e);
						//e.printStackTrace();
					} finally {
						try
						{
							if (in != null)
								in.close();
							if (bRder != null)
								bRder.close();
						}
						catch (IOException e)
						{
							if (BuildConfig.DEBUG)
							Log.e("MyTextViewer","onActivityResult: close() IoException: ",e);
							//e.printStackTrace();
						}
					}
					tabHost.addTab(tabHost.newTabSpec(tabCountTag).setIndicator(name+"\n"+size).setContent(this));
					if (BuildConfig.DEBUG){
					Log.d("MyTextViewer","onActivityResult addTab() getCurrentTabTag: "+tag);
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    /**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(getFileStreamPath("shared.png"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }

    /**
     * Copies a private raw resource content to a publicly readable
     * file such that the latter can be shared with other applications.
     */
    /**private void copyPrivRawResToPubAccesFile() {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getResources().openRawResource(R.raw.robot);
            outputStream = openFileOutput(SHARED_FILE_NAME,
										  Context.MODE_WORLD_READABLE | Context.MODE_APPEND);
            byte[] buffer = new byte[1024];
            int length = 0;
            try {
                while ((length = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException ioe) {*/
               // /* ignore */}
        	//} catch (FileNotFoundException fnfe) {
            /* ignore */
        //} finally {
            //try {
                //inputStream.close();
            //} catch (IOException ioe) {
				/* ignore *///}
            //try {
                //outputStream.close();
            //} catch (IOException ioe) {
			//	/* ignore */}
        	/*}
    	}*/

//    private static int[] createColors() {
//        int[] colors = new int[STRIDE * HEIGHT];
//        for (int y = 0; y < HEIGHT; y++) {
//            for (int x = 0; x < WIDTH; x++) {
//                int r = x * 255 / (WIDTH - 1);
//                int g = y * 255 / (HEIGHT - 1);
//                int b = 255 - Math.min(r, g);
//                int a = Math.max(r, g);
//                colors[y * STRIDE + x] = (a << 24) | (r << 16) | (g << 8) | b;
//            }
//        }
//        return colors;
//    }

//    private static class SampleView extends View {
//        private Bitmap[] mBitmaps;
//        private Bitmap[] mJPEG;
//        private Bitmap[] mPNG;
//        private int[]    mColors;
//        private Paint    mPaint;
//
//        private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
//                                    int quality) {
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            src.compress(format, quality, os);
//
//            byte[] array = os.toByteArray();
//            return BitmapFactory.decodeByteArray(array, 0, array.length);
//        }
//
//        public SampleView(Context context) {
//            super(context);
//            setFocusable(true);
//
//            mColors = createColors();
//            int[] colors = mColors;
//
//            mBitmaps = new Bitmap[6];
//            // these three are initialized with colors[]
//            mBitmaps[0] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT,
//                                              Bitmap.Config.ARGB_8888);
//            mBitmaps[1] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT,
//                                              Bitmap.Config.RGB_565);
//            mBitmaps[2] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT,
//                                              Bitmap.Config.ARGB_4444);
//
//            // these three will have their colors set later
//            mBitmaps[3] = Bitmap.createBitmap(WIDTH, HEIGHT,
//                                              Bitmap.Config.ARGB_8888);
//            mBitmaps[4] = Bitmap.createBitmap(WIDTH, HEIGHT,
//                                              Bitmap.Config.RGB_565);
//            mBitmaps[5] = Bitmap.createBitmap(WIDTH, HEIGHT,
//                                              Bitmap.Config.ARGB_4444);
//            for (int i = 3; i <= 5; i++) {
//                mBitmaps[i].setPixels(colors, 0, STRIDE, 0, 0, WIDTH, HEIGHT);
//            }
//
//            mPaint = new Paint();
//            mPaint.setDither(true);
//
//            // now encode/decode using JPEG and PNG
//            mJPEG = new Bitmap[mBitmaps.length];
//            mPNG = new Bitmap[mBitmaps.length];
//            for (int i = 0; i < mBitmaps.length; i++) {
//                mJPEG[i] = codec(mBitmaps[i], Bitmap.CompressFormat.JPEG, 80);
//                mPNG[i] = codec(mBitmaps[i], Bitmap.CompressFormat.PNG, 0);
//            }
//        }
//
//        @Override protected void onDraw(Canvas canvas) {
//            canvas.drawColor(Color.WHITE);
//
//            for (int i = 0; i < mBitmaps.length; i++) {
//                canvas.drawBitmap(mBitmaps[i], 0, 0, null);
//                canvas.drawBitmap(mJPEG[i], 80, 0, null);
//                canvas.drawBitmap(mPNG[i], 160, 0, null);
//                canvas.translate(0, mBitmaps[i].getHeight());
//            }
//
//            // draw the color array directly, w/o craeting a bitmap object
//            canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT,
//                              true, null);
//            canvas.translate(0, HEIGHT);
//            canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT,
//                              false, mPaint);
//        }
//    }
}
