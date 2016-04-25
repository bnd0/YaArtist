package pro.thor.yaartist;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class JSONHandler {
    private static final String TAG = "JSON handler";

    private ArrayList<Artist> artists = new ArrayList<Artist>();
    String flatJsonYandex;
    NetworkInfoLoader networkInfoLoader = new NetworkInfoLoader()
    {
        @Override
        public boolean doJob(String localAddressData, HttpURLConnection connection)
        {
            boolean result = false;
            try
            {
                StringBuilder sb = new StringBuilder();
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
                reader.close();

                flatJsonYandex = sb.toString();
                result = true;

                FileOutputStream outputStream = new FileOutputStream(localAddressData);
                outputStream.write(flatJsonYandex.getBytes());
                outputStream.close();
            } catch (Exception e) {e.printStackTrace();}
            return result;
        }
    };

    //method that finds or downloads file and parses JSON-----------------------------------------
    public ArrayList<Artist> ParseData(Context context, Handler handler) {

        File jsonYandex = new File(NetworkInfoLoader.JSON_ADDRESS_ON_DEVICE);

        if(!jsonYandex.exists()||jsonYandex.length()==0)
        {
            if(NetworkInfoLoader.isNetworkAvailable(context))
            {
                String networkAddressData = NetworkInfoLoader.JSON_ADDRESS;
                String localAddressData = NetworkInfoLoader.JSON_ADDRESS_ON_DEVICE;

                //start downloading
                if(networkInfoLoader.downloadSomething(networkAddressData, localAddressData))
                    actualParsing(flatJsonYandex);
                //if fails send message (to retry when network available)
                else handler.sendEmptyMessage(-1);
            }
            else
            {
                //send message (to retry when network available)
                handler.sendEmptyMessage(-1);
            }
        }

        else
        {
            //read file-----------------------------------------------------------
            FileReader fr = null;
            StringBuilder sb = new StringBuilder();

            try {
                fr = new FileReader(jsonYandex);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "File wasn't found, something went wrong");
            }


            if (fr != null) {
                BufferedReader br = new BufferedReader(fr);
                String nextLine;
                try {
                    while ((nextLine = br.readLine()) != null) {
                        sb.append(nextLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                flatJsonYandex = sb.toString();
                actualParsing(flatJsonYandex);
            }
        }
        //----------------------------------------------------------------------read file

        return artists;
    }

    //parse JSON and add result to an ArrayList--------------------------------------
    private void actualParsing(String flatJsonYandex)
    {
        try {
            String name;
            String[] genre = null;
            String avatarSmall; //300*300px
            String avatarBig; //1000*1000px
            String link;
            String description;
            int tracks;
            int albums;
            int id;

            JSONArray arr = new JSONArray(flatJsonYandex);

            for (int i = 0; i < arr.length(); i++) {

                if (!arr.getJSONObject(i).isNull("name"))
                    name = arr.getJSONObject(i).getString("name"); //artist's name
                else name = "no name";

                //artist's genre-----------------------------------------------------------------------------------//
                int genresLength = arr.getJSONObject(i).getJSONArray("genres").length(); // replacement for shorter code
                if (genresLength == 0) {
                    genre = new String[]{"YaTrash"};//defined proper genre
                    //genre[0] = "YaTrash";
                } else {
                    genre = new String[genresLength];
                    for (int j = 0; j < genresLength; j++) {
                        genre[j] = arr.getJSONObject(i).getJSONArray("genres").getString(j);//artist's genres
                    }
                }

                //---------------------------------------------------------------------------------------------------//

                if (!arr.getJSONObject(i).isNull("description")) {
                    description = arr.getJSONObject(i).getString("description");//artist's description
                    description = description.substring(0, 1).toUpperCase() + description.substring(1);
                } else description = "no description";

                if (!arr.getJSONObject(i).isNull("link"))
                    link = arr.getJSONObject(i).getString("link");//artist's site
                else link = "no link";

                if (!arr.getJSONObject(i).getJSONObject("cover").isNull("small"))
                    avatarSmall = arr.getJSONObject(i).getJSONObject("cover").getString("small");//artist's image small
                else avatarSmall = "no small picture";

                if (!arr.getJSONObject(i).getJSONObject("cover").isNull("big"))
                    avatarBig = arr.getJSONObject(i).getJSONObject("cover").getString("big");//artist's image big
                else avatarBig = "no big picture";

                if (!arr.getJSONObject(i).isNull("id"))
                    id = arr.getJSONObject(i).getInt("id");//artist's id
                else id = 0;
                if (!arr.getJSONObject(i).isNull("tracks"))
                    tracks = arr.getJSONObject(i).getInt("tracks");//artist's tracks
                else tracks = 0;
                if (!arr.getJSONObject(i).isNull("albums"))
                    albums = arr.getJSONObject(i).getInt("albums");//artist's albums
                else albums = 0;

                artists.add(new Artist(id, name, genre, tracks, albums, link, description, avatarSmall, avatarBig));
                //Log.e(TAG, artists.get(i).getGenres());
            }

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON");
            e.printStackTrace();
        }
    }

}