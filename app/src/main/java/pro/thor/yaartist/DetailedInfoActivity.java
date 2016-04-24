package pro.thor.yaartist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class DetailedInfoActivity extends ActionBarActivity {

    private static final String TAG = "DetailedInfoActivity";
    private ImageView imageView;
    private ProgressBar progressBar;

    private String artistName;
    private String bigPictureAddress;
    private String description;
    private String genre;

    private int albums;
    private int tracks;

    private boolean downloadError = false;

    IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(NetworkInfoLoader.isNetworkAvailable(context) && downloadError)
                artistPictureCheck((NetworkInfoLoader.PROGRAM_FOLDER+"/Big "+artistName+".jpg"),bigPictureAddress);
        }
    };

    NetworkInfoLoader networkInfoLoader = new NetworkInfoLoader() {
        @Override
        public boolean doJob(String localAddressData, HttpURLConnection connection)
        {
            return NetworkInfoLoader.doBitJob(localAddressData,connection);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);

        progressBar = (ProgressBar) findViewById(R.id.show_webProgress);
        progressBar.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.show_webPic);
        TextView descriptionText = (TextView) findViewById(R.id.show_web_description);
        TextView genresText = (TextView) findViewById(R.id.show_web_genres);
        TextView albumsTracksText = (TextView) findViewById(R.id.show_web_albums_tracks);

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            artistName = extras.getString("name");
            bigPictureAddress = extras.getString("bigPictureAddress");
            description = extras.getString("description");
            genre = extras.getString("genre");
            albums = extras.getInt("albums");
            tracks = extras.getInt("tracks");
        }

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar);
        if(toolbar !=null) setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(artistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set description
        descriptionText.setText(description);
        //set genres
        genresText.setText(genre);

        //set albums and tracks with proper name endings
        String albumNames =
                NetworkInfoLoader.properNameEnding(NetworkInfoLoader.handleNameEndings(albums), this, true);
        String trackNames =
                NetworkInfoLoader.properNameEnding(NetworkInfoLoader.handleNameEndings(tracks), this, false);
        albumsTracksText.setText(String.format("%d %s \u00B7 %d %s", albums, albumNames, tracks, trackNames));

        //try to set picture
        artistPictureCheck((NetworkInfoLoader.PROGRAM_FOLDER + "/Big " + artistName + ".jpg"), bigPictureAddress);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home) finish();

        if (id == R.id.action_exit)
        {
            finish();
            //closes only current activity :(
            android.os.Process.killProcess(android.os.Process.myPid());// we may use System.exit(0) as well?

            //return true to indicate that we have handled the action
            return true;
        }
        //return false to indicate that we haven't handled action;
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register Broadcast receiver
        this.registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister Broadcast receiver
        this.unregisterReceiver(receiver);
    }

/////OTHER TASKS//////////////////////////////////////////

    //check if file exist and attach image to imageView
    public void artistPictureCheck(String fileLocation, String bigPictureAddress)
    {
        File file = new File(fileLocation);
        if (file.exists() && file.length()!=0)
        {
            loadBitmap(imageView,fileLocation,progressBar);
        }
        else
        {
            NetworkInfoLoader.setAvatarWhileNoImage(progressBar,imageView);

            if(NetworkInfoLoader.isNetworkAvailable(this))
            {
                ArrayList data = new ArrayList(2);
                data.add(0,bigPictureAddress);
                data.add(1,fileLocation);
                loadBitmapFromNetwork(networkInfoLoader, data);
            }
            else
            {
                NetworkInfoLoader.EnableInternet(this);
                downloadError = true;
            }
        }
    }

    //initialize async task local bitmap loader------------------------------------------
    public void loadBitmap(ImageView imageView, String fileLocation, ProgressBar progressBar)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int reqSize;
        if(metrics.densityDpi>300)reqSize = 1000;
        else reqSize = 500;
        //Log.e(TAG, "Density: "+ metrics.densityDpi);

        BitmapWorkerTask task = new BitmapWorkerTask(imageView,reqSize,progressBar);
        task.execute(fileLocation);
    }

    //initialize async task network bitmap loader----------------------------------------
    public void loadBitmapFromNetwork(NetworkInfoLoader networkInfoLoader, ArrayList data)
    {
        NetworkWorkerTask task = new NetworkWorkerTask(networkInfoLoader) {
            @Override
            public void doSomethingAfterExecute(ArrayList result)
            {
                if(!(boolean)result.get(0))
                {
                    downloadError = true;
                }
                else loadBitmap(imageView,(String)result.get(1),progressBar);
            }
        };
        task.execute(data);
    }
}
