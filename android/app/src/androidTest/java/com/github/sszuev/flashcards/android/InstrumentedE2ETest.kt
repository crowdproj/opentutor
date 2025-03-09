package com.github.sszuev.flashcards.android

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class InstrumentedE2ETest {

    private val tag = "E2E-Test"

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.setCompressedLayoutHierarchy(false)
    }

    @Test
    fun test1_SignInFlow() {
        val onLoginScreen = try {
            onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))
            true
        } catch (_: NoMatchingViewException) {
            false
        }

        if (!onLoginScreen) {
            Log.i(tag, "No SIGN IN / SIGN UP -> possibly in MainActivity? We'll skip to sign out logic.")
            trySignOutIfAvailable()
        }

        Log.i(tag, "Click SIGN IN / SIGN UP (via Espresso)")
        onView(withId(R.id.loginButton))
            .check(matches(isDisplayed()))
            .perform(click())

        Log.i(tag, "Wait short time for SIGN_OUT (desc) to appear (1-2 seconds).")
        val foundSignOutQuick = device.wait(Until.hasObject(By.desc("SIGN_OUT")), 2000)

        if (foundSignOutQuick) {
            Log.i(tag, "We landed in MainActivity without Keycloak form -> sign out first.")
            device.findObject(By.desc("SIGN_OUT")).click()
            device.wait(Until.hasObject(By.text("SIGN IN / SIGN UP")), 5_000)
            Log.i(tag, "Signed out, now definitely on login screen -> click SIGN IN / SIGN UP again.")
            onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))
                .perform(click())
        }

        Log.i(tag, "Wait for Keycloak login page (res=username, password)")
        val foundUsername = device.wait(Until.hasObject(By.res("username")), 10_000)
        if (!foundUsername) {
            Log.w(tag, "No Keycloak form. Possibly the user got in w/o login again? Checking SIGN_OUT.")
            val foundSignOut = device.hasObject(By.desc("SIGN_OUT"))
            if (!foundSignOut) {
                throw AssertionError("Neither Keycloak form nor SIGN_OUT found => unknown state.")
            } else {
                Log.i(tag, "Well, SIGN_OUT is found => user is in. Test ends successfully, but no real login was performed.")
                return
            }
        }

        Log.i(tag, "Type username = ${BuildConfig.TEST_KEYCLOAK_USER}")
        device.findObject(By.res("username")).text = BuildConfig.TEST_KEYCLOAK_USER

        Log.i(tag, "Type password = ${BuildConfig.TEST_KEYCLOAK_PASSWORD}")
        device.findObject(By.res("password")).text = BuildConfig.TEST_KEYCLOAK_PASSWORD

        Log.i(tag, "Push LOG IN (kc-login)")
        device.findObject(By.res("kc-login")).click()

        val pkgName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val foundOurApp = device.wait(Until.hasObject(By.pkg(pkgName)), 10_000)
        assertTrue("Did not return to $pkgName after Keycloak login", foundOurApp)

        device.waitForIdle(2000)

        val foundSignOut = device.wait(Until.hasObject(By.desc("SIGN_OUT")), 10_000)
        assertTrue("SIGN OUT not found, login might have failed", foundSignOut)

        Log.i(tag, "=== E2E sign-in scenario completed successfully! ===")
    }

    private fun trySignOutIfAvailable() {
        val foundSignOut = device.hasObject(By.desc("SIGN_OUT"))
        if (foundSignOut) {
            Log.i(tag, "trySignOutIfAvailable -> found SIGN_OUT, performing sign out")
            device.findObject(By.desc("SIGN_OUT")).click()
            device.wait(Until.hasObject(By.text("SIGN IN / SIGN UP")), 5_000)
        } else {
            Log.i(tag, "trySignOutIfAvailable -> no SIGN_OUT.")
        }
    }
}