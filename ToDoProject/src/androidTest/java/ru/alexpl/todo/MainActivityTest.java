package ru.alexpl.todo;

import android.content.CursorLoader;
import android.test.ActivityInstrumentationTestCase2;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.*;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getActivity();
	}

	@Override
	public void tearDown() throws Exception {
		DB.instance = null;
		super.tearDown();
	}

	public void testSpinnersOnStart() {
		onView(withId(R.id.folderSpinner)).check(matches(not(isDisplayed())));
		onView(withId(R.id.impSpinner)).check(matches(not(isDisplayed())));

		onView(withId(R.id.IVRotator)).perform(click());

		onView(withId(R.id.folderSpinner)).check(matches(isDisplayed()));
		onView(withId(R.id.impSpinner)).check(matches(isDisplayed()));

		onView(withId(R.id.IVRotator)).perform(click());

		onView(withId(R.id.folderSpinner)).check(matches(not(isDisplayed())));
		onView(withId(R.id.impSpinner)).check(matches(not(isDisplayed())));
	}

	public void testAddTask() {
		onView(withId(R.id.ETTextInput)).perform(typeText("test"));
		onView(withId(R.id.IVAdd)).perform(click());
	}

	public void testOfTest() {
		onData(allOf(is(instanceOf(CursorLoader.class))))
				.inAdapterView(withContentDescription("listView"))
				.atPosition(1)
				.perform(swipeRight());
	}
}