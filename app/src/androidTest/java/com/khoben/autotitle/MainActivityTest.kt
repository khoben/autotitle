package com.khoben.autotitle

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.khoben.autotitle.huawei.ui.activity.MainActivity
import org.hamcrest.CoreMatchers.*
import org.junit.runner.RunWith
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.ui.activity.VideoEditActivity
import org.junit.*


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val testRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun checkIfActivityIsLaunched() {
        testRule.launchActivity(null)
        onView(withId(R.id.logo_title)).check(matches(isDisplayed()))
        onView(withId(R.id.welcome_title)).check(matches(isDisplayed()))
    }

    @Test
    fun checkIfFileIntentStartedWhenClickOnLoadButton() {
        Intents.init()
        testRule.launchActivity(Intent())
        onView(withId(R.id.filestore_load_button)).perform(click())
        intended(hasAction(equalTo(Intent.ACTION_GET_CONTENT)))
        Intents.release()
    }

    @Test
    fun checkIfVideoIntentStartedWhenClickOnTakeVideoButton() {
        Intents.init()
        testRule.launchActivity(Intent())
        onView(withId(R.id.camera_capture_button)).perform(click())
        intended(hasAction(equalTo(MediaStore.ACTION_VIDEO_CAPTURE)))
        Intents.release()
    }

    @Test
    fun checkIfWrongUriVideoLoad() {
        Intents.init()
        testRule.launchActivity(Intent())
        val testUri = Uri.parse("/wronguri")
        val successIntent = Intent().apply {
            data = testUri
        }
        intending(hasAction(equalTo(Intent.ACTION_GET_CONTENT)))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, successIntent))

        onView(withId(R.id.filestore_load_button)).perform(click())
        // toast with error
        onView(withText(R.string.error_while_opening_file)).inRoot(
            withDecorView(
                not(
                    `is`(
                        testRule.activity.window.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))
        Intents.release()
    }

    @Test
    fun checkIfGoodUriVideoLoad() {
        Intents.init()
        testRule.launchActivity(Intent())
        val testUri = Uri.parse("android.resource://" + testRule.activity.packageName + "/" + R.raw.test_video)
        val successIntent = Intent().apply {
            data = testUri
        }
        intending(hasAction(equalTo(Intent.ACTION_GET_CONTENT)))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, successIntent))

        onView(withId(R.id.filestore_load_button)).perform(click())
        // start video edit
        intended(hasComponent(VideoEditActivity::class.java.name))
        Intents.release()
    }
}