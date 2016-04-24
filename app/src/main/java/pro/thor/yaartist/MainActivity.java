package pro.thor.yaartist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    Toolbar toolbar;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    MyAdapter mAdapter;
    ArrayList<Artist> artistsYandex = new ArrayList<>();
    ArrayList<Integer> failedArtists = new ArrayList<>();

    IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(NetworkInfoLoader.isNetworkAvailable(getApplicationContext()))
            {
                for (int key : failedArtists) {
                    mAdapter.notifyItemChanged(key);
                }
                failedArtists.clear();
            }
        }
    };

    Handler myHandler = new Handler(Looper.getMainLooper(),new UICallback());

    private final class UICallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg)
        {
            int position = msg.what;
            int storedPosition = failedArtists.indexOf(position);
            if(storedPosition == (-1)) failedArtists.add(position);
            else failedArtists.set(storedPosition, position);
            //Log.e("TEST: ", String.valueOf(failedArtists.size()));
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set toolbar
        toolbar = (Toolbar)findViewById(R.id.include_toolbar);
        if(toolbar!=null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getResources().getString(R.string.artist_list)); //replace standard (app name) toolbar title
        }
        new StartJSONHandler().execute(this); //start json handling in separate thread

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new MyAdapter(artistsYandex,this,myHandler);
        mRecyclerView.setAdapter(mAdapter);

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

        if (id == R.id.action_exit)
        {
            finish();
            //closes only current activity :(
            android.os.Process.killProcess(android.os.Process.myPid());

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
        this.registerReceiver(receiver,intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister Broadcast receiver
        this.unregisterReceiver(receiver);
    }

    ////////////////////////////////////////////////////////

    private class StartJSONHandler extends AsyncTask<Context, Void, ArrayList<Artist>>
    {

        @Override
        protected ArrayList<Artist> doInBackground(Context... contexts) {
            return downloadFile(contexts[0]);
        }

        protected void onPostExecute(ArrayList<Artist> result) {
            artistsYandex.addAll(0,result);
            //Log.e(TAG,String.valueOf(artistsYandex.size()));

            // update an adapter, here notifyDataSetChanged is appropriate
            mAdapter.notifyDataSetChanged();

        }

        protected ArrayList<Artist> downloadFile(Context context) {

            JSONHandler jsonHandler = new JSONHandler();
            ArrayList<Artist> artists = jsonHandler.ParseData(context);
            return artists;
        }
    }

}
