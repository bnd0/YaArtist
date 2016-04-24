package pro.thor.yaartist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class NetworkInfoLoader
{
    private static final String TAG = "NetworkInfoLoader";

    //case for network connection
    public abstract boolean doJob(String localAddressData, HttpURLConnection connection);

    static final String JSON_ADDRESS =
            "http://cache-default06d.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json";
    static final String PROGRAM_FOLDER =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    static final String JSON_ADDRESS_ON_DEVICE = PROGRAM_FOLDER + "/jsonYandex.json";

    private static final int IO_BUFFER = 1024*8;

    //particular case for network connection (bitmap downloading)
    public static boolean doBitJob(String localAddressData, HttpURLConnection connection)
    {
        boolean result = false;
        try
        {
            InputStream is = new BufferedInputStream(connection.getInputStream(),IO_BUFFER);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            FileOutputStream out = new FileOutputStream(localAddressData);
            if (bitmap!=null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {result = true;}
            else {
                Log.e(TAG, "Download failed: " + localAddressData);}

            if (is != null) is.close();
        }
        catch (IOException e) {e.printStackTrace();}
        return result;
    }

    //establish network connection and do operation defined by particular case
    public boolean downloadSomething(String networkAddressData, String localAddressData)
    {
        boolean result = false;
        try
        {
            URL networkAddress = new URL(networkAddressData);

            HttpURLConnection conn = (HttpURLConnection) networkAddress.openConnection();
            conn.setReadTimeout(10000);// milliseconds
            conn.setConnectTimeout(15000);// milliseconds
            conn.setRequestMethod("GET");//default method
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            if (response == 200)
            {
                result = doJob(localAddressData, conn);
            }
            conn.disconnect();
        } catch (IOException e) {e.printStackTrace();}
        return result;
    }

    //check network state
    static boolean isNetworkAvailable(Context context)
    {
        boolean connected = false;
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) connected = true;
        return connected;
    }

    //ask user to turn on internet
    static void EnableInternet(Context context)
    {
        Toast.makeText(context, "Please enable network connection!", Toast.LENGTH_SHORT).show();
    }

    //start progress bar animation and attach temporary bitmap to image view
    static void setAvatarWhileNoImage(ProgressBar progressBar, ImageView imageView)
    {
        if(progressBar.getVisibility()== View.GONE) progressBar.setVisibility(View.VISIBLE);
        if(imageView!=null)imageView.setImageResource(R.drawable.avatar);
    }

    //russian language brr--------------------------------------------------------------------------
    static int handleNameEndings(int quantity)
    {
        int modQuantity = quantity;
        int chooser = 1; //...ов (default)
        //prepare value for checking, as we don't know how big it is
        while(quantity>99)
        {
            modQuantity = quantity%10;
            quantity=modQuantity;
        }
        //...ов
        if(modQuantity>4&&modQuantity<21)chooser = 1;
            //...ом
        else if(modQuantity==1||modQuantity%10==1)chooser = 2;
            //...a
        else if((modQuantity>1&&modQuantity<5)||(modQuantity%10>1&&modQuantity%10<5))chooser = 3;

        return chooser;
    }

    static String properNameEnding(int chooser, Context context, boolean wordType)
    {
        String name="";
        //if wordType true we have albums else tracks
        if(wordType)
            switch (chooser)
            {
                case 1: name = context.getResources().getString(R.string.artist_albums);
                    break;
                case 2: name = context.getResources().getString(R.string.artist_album_1);
                    break;
                case 3: name = context.getResources().getString(R.string.artist_album_2_4);
                    break;
                default: Log.e(TAG, "unexpected chooser value!");
                    break;
            }
        else
            switch (chooser)
            {
                case 1: name = context.getResources().getString(R.string.artist_tracks);
                    break;
                case 2: name = context.getResources().getString(R.string.artist_track_1);
                    break;
                case 3: name = context.getResources().getString(R.string.artist_track_2_4);
                    break;
                default: Log.e(TAG, "unexpected chooser value!");
                    break;
            }
        return name;
    }
    //-----------------------------------------------------------------------------------------brr//
}
