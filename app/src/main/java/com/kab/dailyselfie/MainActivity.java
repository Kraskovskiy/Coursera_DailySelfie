package com.kab.dailyselfie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    ActionBar mActionBar;
    private AlarmManagerBroadcastReceiver mAlarm;
    private Uri mUri;
    SharedPreferences mSettings;
    private static final String TAG = "DailySelfie";
    private String[] mFileStrings;
    private String[] mFileName;
    private Bitmap[] mFileBitmap;
    private File[] listFile;
    ListView listView;
    ImageView iv;
    LoaderThumbnailsAsyncTask mLoaderThumb;
    ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        creatActionBar();

        mAlarm = new AlarmManagerBroadcastReceiver();
        iv =(ImageView) findViewById(R.id.imageView);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv.setImageBitmap(null);
                iv.setVisibility(View.INVISIBLE);
            }
        });

        mSettings = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
        mSettings.edit().putBoolean("FIRST_RUN", true).commit();

        pBar = (ProgressBar) findViewById(R.id.pBar);

        cancelRepeatingTimer(getApplicationContext()); //restart Timer
        startRepeatingTimer(getApplicationContext()); //~ 5 min

        createListView();
   }

    @Override
    public void onResume() {
       createListView();
        super.onResume();

    }

    public void creatActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.menu_save:
                getSelfie();

                 return true;


            default:
                return false;
        }
    }


    public void startRepeatingTimer(Context context) {

        if (mAlarm != null) {
            mAlarm.SetAlarm(context);
        }
    }

    public void cancelRepeatingTimer(Context context) {

        if (mAlarm != null) {
            mAlarm.CancelAlarm(context);
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void getSelfie() {
        if (checkCameraHardware(getApplicationContext())) {

            if (Build.VERSION.SDK_INT < 16) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_CAMERA_BUTTON);
                i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAMERA));
                sendOrderedBroadcast(i, null);
            } else {

                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mUri = getOutputMediaFileUri();

                if (mUri == null) {

                    mUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "DCIM/Camera");
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    Log.i(TAG, Environment.getExternalStorageDirectory().getPath() + "DCIM/Camera");
                    startActivityForResult(takePhotoIntent, 0);

                } else {
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

                    startActivityForResult(takePhotoIntent, 0);
                }
            }


        } else {
            Toast.makeText(getApplicationContext(), "Camera Error", Toast.LENGTH_LONG).show();
        }
    }


    private Uri getOutputMediaFileUri() {

        if (isExternalStorageAvaible()) {

              File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera");

            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
                Log.e(TAG, "create directory");
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;

            mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

            return Uri.fromFile(mediaFile);
        } else {
            Toast.makeText(getApplicationContext(),"Need External Storage",Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(mUri);
            sendBroadcast(mediaScanIntent);
            Log.i(TAG, "onActivityResult");
        } else if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult_RESULT_CANCELED");
        }
    }

    public void createListView(){

        mLoaderThumb=new LoaderThumbnailsAsyncTask();
        mLoaderThumb.execute();

    }


    public class LoaderThumbnailsAsyncTask extends AsyncTask<Void, Integer, Bitmap[]> {


        @Override
        protected void onPreExecute() {
           pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Bitmap... result) {

            pBar.setVisibility(View.INVISIBLE);
            if (result.length>0) {
                createListViewAsync();
            }
        }

        @Override
        protected Bitmap[] doInBackground(Void... parameter) {

            return createListView();
        }


        public Bitmap[] createListView() {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera");
            if (!file.exists()) {
                file.mkdirs();
                Log.e(TAG, "create directory");
                if (!file.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                }
            }
            int length = 0;
            if (file.isDirectory()) {
                listFile = file.listFiles();
                mFileStrings = new String[listFile.length];
                mFileName = new String[listFile.length];
                mFileBitmap = new Bitmap[listFile.length];

                for (int i = 0; i < listFile.length; i++) {
                    mFileStrings[i] = listFile[i].getAbsolutePath();

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                    Bitmap bitmap = BitmapFactory.decodeFile(mFileStrings[i], bmOptions);
                    // bitmap = Bitmap.createScaledBitmap(bitmap,50,50,true);
                    mFileBitmap[i] = bitmap;

                    mFileName[i] = listFile[i].getName();
                    mFileName[i] = mFileName[i].substring(4, 8) + "/" + mFileName[i].substring(8, 10)
                            + "/" + mFileName[i].substring(10, 12) + "_" + mFileName[i].substring(13, 15)
                            + ":" + mFileName[i].substring(15, 17) + ":" + mFileName[i].substring(17, 19);

                }
                return mFileBitmap;
            }

            return null;
        }
    }

    public void createListViewAsync()
    {
        CustomListAdapter adapter = new CustomListAdapter(this, mFileName, mFileBitmap);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                iv.setVisibility(View.VISIBLE);
                iv.setImageBitmap(mFileBitmap[+position]);
            }
        });
    }


    @Override
    public void onBackPressed() {

        if (iv.getVisibility() != View.VISIBLE) {
            finish();
        }
        else {
            iv.setImageBitmap(null);
            iv.setVisibility(View.INVISIBLE);
            return;
        }

        super.onBackPressed();
    }

    private boolean isExternalStorageAvaible(){
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

}
