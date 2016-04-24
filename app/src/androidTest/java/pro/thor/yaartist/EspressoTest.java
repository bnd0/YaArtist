package pro.thor.yaartist;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {

    //@Rule
    //public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Rule
    public IntentsTestRule<MainActivity> mIntentRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void recycledTest()
    {
        //check toolbar title
        onView(withText("Исполнители")).check(matches(ViewMatchers.isDisplayed()));
        //confirm element of RecyclerView with "Tove Lo" exist
        onView(withId(R.id.my_recycler_view)).check(matches(hasDescendant(withText("Tove Lo"))));
        //find 0 element in RecyclerView and click it
        onView(withId(R.id.my_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //define that DetailedInfoActivity was called
        intended(hasComponent(hasShortClassName(".DetailedInfoActivity")));

        //define that intent has extra resources--------------
        intended(hasExtraWithKey("name"));
        intended(hasExtraWithKey("bigPictureAddress"));
        intended(hasExtraWithKey("description"));
        //---------------------------------------------------//

        //check if toolbar title == "Tove Lo"
        onView(withText("Tove Lo")).check(matches(ViewMatchers.isDisplayed()));
        //go back to caller activity (MainActivity)
        Espresso.pressBack();
        //check toolbar title
        onView(withText("Исполнители")).check(matches(ViewMatchers.isDisplayed()));
    }

}
