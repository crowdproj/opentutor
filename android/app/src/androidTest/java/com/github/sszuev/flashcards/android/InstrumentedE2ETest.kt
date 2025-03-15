package com.github.sszuev.flashcards.android

import android.util.Log
import android.view.KeyEvent
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
import androidx.test.uiautomator.BySelector
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
    fun test() {
        testSignIn()
        testCreateDictionary()
    }

    private fun testSignIn() {
        val onLoginScreen = try {
            onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))
            true
        } catch (_: NoMatchingViewException) {
            false
        }

        if (!onLoginScreen) {
            Log.i(
                tag,
                "No SIGN IN / SIGN UP -> possibly in MainActivity? We'll skip to sign out logic."
            )
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
            Log.i(
                tag,
                "Signed out, now definitely on login screen -> click SIGN IN / SIGN UP again."
            )
            onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))
                .perform(click())
        }

        Log.i(tag, "Wait for Keycloak login page (res=username, password)")
        val foundUsername = device.wait(Until.hasObject(By.res("username")), 10_000)
        if (!foundUsername) {
            Log.w(
                tag,
                "No Keycloak form. Possibly the user got in w/o login again? Checking SIGN_OUT."
            )
            val foundSignOut = device.hasObject(By.desc("SIGN_OUT"))
            if (!foundSignOut) {
                throw AssertionError("Neither Keycloak form nor SIGN_OUT found => unknown state.")
            } else {
                Log.i(
                    tag,
                    "Well, SIGN_OUT is found => user is in. Test ends successfully, but no real login was performed."
                )
                return
            }
        }

        Log.i(tag, "Type username = ${BuildConfig.TEST_KEYCLOAK_USER}")
        device.findObject(By.res("username")).text = BuildConfig.TEST_KEYCLOAK_USER

        val passwdSelector = By.res("password")
        if (!device.hasObject(passwdSelector)) {
            val foundAfterScroll = device.scrollUntilVisible(passwdSelector, maxSwipes = 5)
            if (!foundAfterScroll) {
                throw AssertionError("Can't find password text-box")
            }
        }
        Log.i(tag, "Type password = ${BuildConfig.TEST_KEYCLOAK_PASSWORD}")
        device.findObject(passwdSelector).text = BuildConfig.TEST_KEYCLOAK_PASSWORD

        val loginSelector = By.res("kc-login")
        if (!device.hasObject(loginSelector)) {
            val foundAfterScroll = device.scrollUntilVisible(loginSelector, maxSwipes = 5)
            if (!foundAfterScroll) {
                throw AssertionError("Can't find log-in button")
            }
        }
        Log.i(tag, "Push LOG IN (kc-login)")
        device.findObject(loginSelector).click()

        val pkgName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val foundOurApp = device.wait(Until.hasObject(By.pkg(pkgName)), 10_000)
        assertTrue("Did not return to $pkgName after Keycloak login", foundOurApp)

        device.waitForIdle(2000)

        val foundSignOut = device.wait(Until.hasObject(By.desc("SIGN_OUT")), 10_000)
        assertTrue("SIGN OUT not found, login might have failed", foundSignOut)

        Log.i(tag, "=== E2E sign-in scenario completed successfully! ===")
    }

    private fun testCreateDictionary() {
        val foundCreate = device.wait(Until.hasObject(By.text("CREATE")), 5_000)
        assertTrue("CREATE button not found", foundCreate)
        Log.i(tag, "Click CREATE button")
        device.findObject(By.text("CREATE")).click()

        device.wait(Until.hasObject(By.desc("SelectField1")), 3_000)
        device.findObject(By.desc("SelectField1")).click()

        device.wait(Until.hasObject(By.desc("SearchField1")), 3_000)
        device.findObject(By.desc("SearchField1")).click()

        device.pressKeyCode(KeyEvent.KEYCODE_E)
        device.pressKeyCode(KeyEvent.KEYCODE_N)

        device.wait(Until.hasObject(By.text("English")), 3_000)
        device.findObject(By.text("English")).click()

        device.wait(Until.hasObject(By.desc("SelectField2")), 3_000)
        device.findObject(By.desc("SelectField2")).click()

        device.wait(Until.hasObject(By.desc("SearchField2")), 3_000)
        device.findObject(By.desc("SearchField2")).click()

        device.pressKeyCode(KeyEvent.KEYCODE_R)
        device.pressKeyCode(KeyEvent.KEYCODE_U)

        device.wait(Until.hasObject(By.text("Russian (русский)")), 3_000)
        device.findObject(By.text("Russian (русский)")).click()

        device.wait(Until.hasObject(By.desc("DictionaryName")), 3_000)
        device.findObject(By.desc("DictionaryName")).text = testDictionaryName

        device.wait(Until.hasObject(By.desc("AcceptedAnswers")), 3_000)
        device.findObject(By.desc("AcceptedAnswers")).text = "14"

        device.wait(Until.hasObject(By.text("SAVE")), 3_000)
        Log.i(tag, "Click SAVE button")
        device.findObject(By.text("SAVE")).click()
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

    private fun UiDevice.scrollUntilVisible(
        targetSelector: BySelector,
        maxSwipes: Int = 5
    ): Boolean {
        val screenWidth = this.displayWidth
        val screenHeight = this.displayHeight

        repeat(maxSwipes) { attempt ->
            if (this.hasObject(targetSelector)) {
                return true
            }
            swipe(
                screenWidth / 2,   // fromX
                (screenHeight * 3) / 4,  // fromY
                screenWidth / 2,   // toX
                screenHeight / 4,  // toY
                20
            )
            Thread.sleep(500)
        }
        return this.hasObject(targetSelector)
    }

    companion object TestData {
        private val testDictionaryName = "test-weather-${System.currentTimeMillis()}"
    }
}