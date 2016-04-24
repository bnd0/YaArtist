package pro.thor.yaartist;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private static final String TAG = "MyAdapter";
    static ArrayList<Artist> artists;
    Context myAdapterContext;
    Handler myHandler;

    //initialize networkInfoLoader and set what to do when connection established
    NetworkInfoLoader networkInfoLoader = new NetworkInfoLoader()
    {
        @Override
        public boolean doJob(String localAddressData, HttpURLConnection connection)
        {
            return NetworkInfoLoader.doBitJob(localAddressData,connection);
        }
    };

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView artistNameTextView;
        public TextView genresTextView;
        public TextView badgeText;
        public ImageView mImageView;
        public ProgressBar progressBar;

        public ViewHolder(View v)
        {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailedInfoIntent(getPosition(), v.getContext());
                }
            });

            artistNameTextView = (TextView)v.findViewById(R.id.list_item_text);
            mImageView = (ImageView) v.findViewById(R.id.list_item_icon);
            badgeText = (TextView) v.findViewById(R.id.list_item_badge);
            progressBar = (ProgressBar) v.findViewById(R.id.list_item_progress);
            genresTextView = (TextView) v.findViewById(R.id.list_item_genre);
        }

    }

    public static void DetailedInfoIntent (int position, Context context)
    {
        Intent detailedInfo = new Intent(context, DetailedInfoActivity.class);
        detailedInfo.putExtra("name", artists.get(position).getName());
        detailedInfo.putExtra("bigPictureAddress", artists.get(position).getCoverBig());
        detailedInfo.putExtra("description",artists.get(position).getDescription());
        detailedInfo.putExtra("genre",artists.get(position).getGenres());
        detailedInfo.putExtra("albums",artists.get(position).getAlbums());
        detailedInfo.putExtra("tracks",artists.get(position).getTracks());
        context.startActivity(detailedInfo);
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<Artist> artists, Context myAdapterContext, Handler myHandler)
    {
        MyAdapter.artists = artists;
        this.myAdapterContext = myAdapterContext;
        this.myHandler = myHandler;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_artist_custom, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // get element data from your dataset at this position and
        // add this data to view items
        String artistGenres = artists.get(position).getGenres();
        String artistName = artists.get(position).getName();
        String artistPicSmall = artists.get(position).getCoverSmall();
        String artistPicSmallLocal = NetworkInfoLoader.PROGRAM_FOLDER + "/" + artistName + ".jpg";

        int artistAlbums = artists.get(position).getAlbums();
        String albumsText =
                NetworkInfoLoader.properNameEnding(NetworkInfoLoader.handleNameEndings(artistAlbums),myAdapterContext,true);

        int artistTracks = artists.get(position).getTracks();
        String tracksText =
                NetworkInfoLoader.properNameEnding(NetworkInfoLoader.handleNameEndings(artistTracks), myAdapterContext, false);

        holder.artistNameTextView.setText(artistName);
        holder.genresTextView.setText(artistGenres);
        holder.badgeText.setText(String.format("%d %s, %d %s",artistAlbums, albumsText, artistTracks, tracksText));

        //check if file already downloaded and attach it to view or start downloading
        File imageFile = new File(artistPicSmallLocal);
        if(imageFile.exists() && imageFile.length()!=0)
        {
            //Log.e(TAG, imageFile.length() + " : " + position);
            //attach bitmap to imageView
            loadBitmap(holder.mImageView,artistPicSmallLocal,holder.progressBar);
        }
        else
        {
            //start progress bar animation and attach temporary bitmap to image view
            NetworkInfoLoader.setAvatarWhileNoImage(holder.progressBar,holder.mImageView);
            //if network connection is enabled start downloading
            if(NetworkInfoLoader.isNetworkAvailable(myAdapterContext))
            {
                //prepare information for loadBitmapFromNetwork
                ArrayList data = new ArrayList(3);
                data.add(artistPicSmall);
                data.add(artistPicSmallLocal);
                data.add(position);

                loadBitmapFromNetwork(networkInfoLoader,data);
            }

            else
            {
                //send toast message to user to enable network connection
                NetworkInfoLoader.EnableInternet(myAdapterContext);
                //send message to handler in main activity
                myHandler.sendEmptyMessage(position);
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return artists.size();
    }

    //------------------------------------------------------------------------------adapter end

    //initialize async task local bitmap loader------------------------------------------------
    public void loadBitmap(ImageView imageView, String fileLocation, ProgressBar progressBar)
    {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView,151,progressBar);
        task.execute(fileLocation);
    }

    //initialize async task network bitmap loader----------------------------------------------
    public void loadBitmapFromNetwork(NetworkInfoLoader networkInfoLoader, ArrayList data)
    {
        NetworkWorkerTask task = new NetworkWorkerTask(networkInfoLoader) {
            @Override
            public void doSomethingAfterExecute(ArrayList result)
            {
                //reload element of recycler view if download succeed
                if((boolean)result.get(0)) notifyItemChanged((int)result.get(2));
                else
                {
                    //send message to handler in main activity
                    myHandler.sendEmptyMessage((int)result.get(2));
                }
            }
        };
        task.execute(data);
    }
}