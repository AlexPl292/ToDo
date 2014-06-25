package ru.alexpl.todo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@LargeTest
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getActivity();
		Log.d("aDebug", "ip");
		//onView(withId(R.id.folderSpinner)).check(matches(not(isDisplayed())));
	}

	/*public void tearDown() throws Exception {
		Log.d("aDebug", "tear");
		//onView(withId(R.id.folderSpinner)).check(matches((isDisplayed())));
		super.tearDown();
	}*/

	public void testRotator() {
		/*onView(withId(R.id.folderSpinner)).check(matches(not(isDisplayed())));
		onView(withId(R.id.impSpinner)).check(matches(not(isDisplayed())));*/
		onView(withId(R.id.testV)).check(matches(not(isDisplayed())));

		onView(withId(R.id.IVRotator)).perform(click());

		/*onView(withId(R.id.folderSpinner)).check(matches(isDisplayed()));
		onView(withId(R.id.impSpinner)).check(matches(isDisplayed()));*/
		onView(withId(R.id.testV)).check(matches(isDisplayed()));

		//onView(withId(R.id.IVRotator)).perform(click());
	}

	public void testMy() {
		Log.d("aDebug", "test");
		//onView(withId(R.id.ETTextInput)).perform(typeText("Test"));
		//onView(withId(R.id.IVAdd)).perform(click());
	}
}