package com.example.snippets.java;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import com.example.snippets.java.MapActivity; // Implicitly imported if in same package but good to be explicit or if names collide

@RunWith(AndroidJUnit4.class)
public class MapActivityTest {

    @Test
    public void mapActivityLaunches() {
        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class)) {
            // Check if map view is displayed
            // R.id.map is in com.example.snippets.common.R
            onView(withId(com.example.snippets.common.R.id.map)).check(matches(isDisplayed()));
        }
    }
}
