package com.example.kanjuice;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.kanjuice.activities.JuiceMenuActivity;
import com.example.kanjuice.activities.UserInputActivity;
import com.example.kanjuice.models.Juice;
import com.example.kanjuice.models.JuiceItem;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class juiceMenuActivityTest {

    @Rule
    public final IntentsTestRule<JuiceMenuActivity> rule = new IntentsTestRule<JuiceMenuActivity>(JuiceMenuActivity.class, true, true);

    @Test
    public void testOnClickLaunchesCardSwipeActivity() {

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(0).onChildView(withId(R.id.juice_frame)).perform(click());

        intended(hasComponent(UserInputActivity.class.getName()));

        ArrayList<JuiceItem> selectedJuiceItems = new ArrayList<>();
        selectedJuiceItems.add(new JuiceItem("Water Melon",R.drawable.watermelon,R.string.watermelon_kan,false));

        JuiceItem[] selectedJuicesArray = new JuiceItem[selectedJuiceItems.size()];
        selectedJuiceItems.toArray(selectedJuicesArray);
        intended(hasExtra("juices",selectedJuicesArray));
    }


    @Test
    public void testOnLongClickAndSelectingTwoJuicesLaunchesCardSwipeActivity() {

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(0).onChildView(withId(R.id.juice_frame)).perform(longClick());
        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(0).onChildView(withId(R.id.juice_frame)).onChildView(withId(R.id.multi_select_layout)).onChildView(withId(R.id.two)).perform(click());
        onView(allOf(withId(R.id.order), withParent(withId(R.id.action_button_layout)))).perform(click());

        intended(hasComponent(UserInputActivity.class.getName()));

        List<JuiceItem> selectedJuiceItems = new ArrayList<>();
        JuiceItem juiceItem = new JuiceItem("Water Melon",R.drawable.watermelon,R.string.watermelon_kan,false);
        juiceItem.selectedQuantity = 2;
        selectedJuiceItems.add(juiceItem);

        JuiceItem[] selectedJuicesArray = new JuiceItem[selectedJuiceItems.size()];
        selectedJuiceItems.toArray(selectedJuicesArray);
        intended(hasExtra("juices",selectedJuicesArray));
    }
}
