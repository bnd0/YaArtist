package pro.thor.yaartist;

import android.util.Log;

public class Artist
{
    private int tracks;
    private int albums;
    private int id;

    private String name;
    private String[] genres;
    private String link;
    private String description;
    private String coverSmall;
    private String coverBig;

    public Artist(int id, String name, String[] genres, int tracks, int albums,
                  String link, String description, String coverSmall, String coverBig)
    {
        //integers
        this.id = id;
        this.tracks = tracks;
        this.albums = albums;
        //strings
        this.name = name;
        this.genres = genres;
        this.description = description;
        //urls in strings
        this.link = link;
        this.coverSmall = coverSmall;
        this.coverBig = coverBig;
    }

    public String getName() {return name;}
    public String getDescription() {return description;}
    public String getGenres()
    {
        String allGenres = "";
        int i = 0;
        for (String genre : genres)
        {
            allGenres = allGenres + genre;
            if (i<(genres.length-1))
            {
                allGenres = allGenres + ", ";
                i++;
            }
        }
        //Log.e("Artist class",allGenres);
        return allGenres;
    }
    public String getLink() {return link;} //for future use
    public String getCoverSmall() {return coverSmall;}
    public String getCoverBig() {return coverBig;}
    public int getYaId() {return id;} //for future use
    public int getTracks() {return tracks;}
    public int getAlbums() {return albums;}


}
