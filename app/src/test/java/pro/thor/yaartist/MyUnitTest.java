package pro.thor.yaartist;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MyUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    //
    @Test
    public void artistClassTest()
    {
        int id = 0;
        int tracks = 0;
        int albums = 0;
        String name = "john doe";
        String[] genre = new String[]{"s","p","v"};
        String link = "www1";
        String description = "www2";
        String avatarSmall = "www3";
        String avatarBig = "www4";

        ArrayList<Artist> testArtist = new ArrayList<>();
        testArtist.add(new Artist(id, name, genre, tracks, albums, link, description, avatarSmall, avatarBig));
        assertEquals(1, testArtist.size());
        assertEquals("john doe", testArtist.get(0).getName());
        assertEquals("s, p, v",testArtist.get(0).getGenres());
        assertEquals("www1",testArtist.get(0).getLink());
        assertEquals("www2",testArtist.get(0).getDescription());
        assertEquals("www3",testArtist.get(0).getCoverSmall());
        assertEquals("www4",testArtist.get(0).getCoverBig());
    }

}