package com.example.maps3djava;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.maps3dcommon.R;
import com.example.maps3djava.hellomap.HelloMapActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HelloMapActivityTest {
    @Test
    public void testMapIsDisplayed() {
        ActivityScenario.launch(HelloMapActivity.class);
        onView(withId(R.id.map3dView)).check(matches(isDisplayed()));
    }
}