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
import org.junit.Assert
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

    @Suppress("ConstPropertyName")
    companion object {
        private const val tag = "E2E-Test"
        private const val google_account_refusal_label_ru = "Продолжить без входа в аккаунт"
        private const val google_account_refusal_label_en = "No thanks"
        private val testDictionaryName = "test-weather-${System.currentTimeMillis()}"
    }

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
        try {
            testAddCards()
        } finally {
            testDeleteDictionary()
        }
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

        device.wait(Until.hasObject(By.text(google_account_refusal_label_ru)), 3_000)
        val noThanks = device.findObject(By.text(google_account_refusal_label_en))
            ?: device.findObject(By.text(google_account_refusal_label_ru))
        if (noThanks != null) {
            noThanks.click()
            device.waitForIdle(1_000)
        }

        Log.i(tag, "Wait short time for SIGN_OUT (desc) to appear (1-2 seconds).")
        val foundSignOutQuick = device.wait(Until.hasObject(By.desc("SIGN_OUT")), 2_000)

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
        val foundEditFields =
            device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 10_000)

        if (!foundEditFields) {
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
        device.findObjects(By.clazz("android.widget.EditText"))[0].text =
            BuildConfig.TEST_KEYCLOAK_USER

        if (device.findObjects(By.clazz("android.widget.EditText")).size != 2) {
            val foundAfterScroll = device.scrollUntilVisible(maxSwipes = 5) {
                device.findObjects(By.clazz("android.widget.EditText")).size == 2
            }
            if (!foundAfterScroll) {
                throw AssertionError("Can't find password text-box")
            }
        }
        Log.i(tag, "Type password = ${BuildConfig.TEST_KEYCLOAK_PASSWORD}")
        device.findObjects(By.clazz("android.widget.EditText"))[1].text =
            BuildConfig.TEST_KEYCLOAK_PASSWORD

        val loginSelector = By.text("Sign In")
        if (!device.hasObject(loginSelector)) {
            val foundAfterScroll =
                device.scrollUntilVisible(targetSelector = loginSelector, maxSwipes = 5)
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
        Log.i(tag, "CREATE DICTIONARY $testDictionaryName")
        val foundCreate = device.wait(Until.hasObject(By.text("CREATE")), 5_000)
        assertTrue("CREATE button not found", foundCreate)
        Log.i(tag, "Click CREATE button")
        device.findObject(By.text("CREATE")).click()

        device.wait(Until.hasObject(By.desc("SelectField1")), 5_000)
        device.findObject(By.desc("SelectField1")).click()

        device.wait(Until.hasObject(By.desc("SearchField1")), 5_000)
        device.findObject(By.desc("SearchField1")).click()

        device.type("en")

        device.wait(Until.hasObject(By.text("English")), 5_000)
        device.findObject(By.text("English")).click()

        device.wait(Until.hasObject(By.desc("SelectField2")), 5_000)
        device.findObject(By.desc("SelectField2")).click()

        device.wait(Until.hasObject(By.desc("SearchField2")), 5_000)
        device.findObject(By.desc("SearchField2")).click()

        device.type("ru")

        device.wait(Until.hasObject(By.text("Russian (русский)")), 5_000)
        device.findObject(By.text("Russian (русский)")).click()

        device.wait(Until.hasObject(By.desc("DictionaryName")), 5_000)
        device.findObject(By.desc("DictionaryName")).text = testDictionaryName

        device.wait(Until.hasObject(By.desc("AcceptedAnswers")), 5_000)
        device.findObject(By.desc("AcceptedAnswers")).text = "14"

        device.wait(Until.hasObject(By.text("SAVE")), 5_000)
        Log.i(tag, "Click SAVE button")
        device.findObject(By.text("SAVE")).click()

        val found = device.wait(Until.hasObject(By.text(testDictionaryName)), 5_000)
        assertTrue("New dictionary not found in the list", found)
    }

    private fun testDeleteDictionary() {
        Log.i(tag, "DELETE DICTIONARY $testDictionaryName")
        device.pressBack()
        val foundHome = device.wait(Until.hasObject(By.text("HOME")), 1_000)
        if (foundHome) {
            device.findObject(By.text("HOME")).click()
        } else {
            device.pressBack()
            device.wait(Until.hasObject(By.text("HOME")), 5_000)
            device.findObject(By.text("HOME")).click()
        }

        val dictionarySelector = By.text(testDictionaryName)
        device.wait(Until.hasObject(dictionarySelector), 5_000)
        if (!device.hasObject(dictionarySelector)) {
            val foundAfterScroll = device.scrollUntilVisible(
                targetSelector = dictionarySelector,
                maxSwipes = 5,
                fromTopToBottom = false,
            )
            if (!foundAfterScroll) {
                throw AssertionError("Can't find dictionary $testDictionaryName")
            }
        }
        device.findObject(dictionarySelector).click()
        device.waitForIdle(500)

        device.wait(Until.hasObject(By.text("DELETE")), 5_000)
        Log.i(tag, "Click DELETE button")
        device.findObject(By.text("DELETE")).click()

        val foundConfirm = device.wait(Until.hasObject(By.text("CONFIRM")), 5_000)
        if (foundConfirm) {
            device.findObject(By.text("CONFIRM")).click()
        } else {
            device.findObject(By.text("DELETE")).click()
            device.wait(Until.hasObject(By.text("CONFIRM")), 5_000)
            device.findObject(By.text("CONFIRM")).click()
        }

        device.wait(Until.gone(By.text(testDictionaryName)), 5_000)
        Assert.assertFalse(device.hasObject(By.text(testDictionaryName)))
    }

    private fun testAddCards() {
        device.wait(Until.hasObject(By.text(testDictionaryName)), 5_000)
        device.wait(Until.hasObject(By.text("CARDS")), 5_000)
        Log.i(tag, "Click CARDS button")
        device.findObject(By.text("CARDS")).click()

        device.wait(Until.hasObject(By.desc("CardWordName")), 5_000)
        device.findObject(By.desc("CardWordName")).click()

        device.type("weather")

        device.wait(Until.hasObject(By.text("ADD")), 5_000)
        device.findObject(By.text("ADD")).click()

        device.wait(Until.hasObject(By.desc("AddDialogWord")), 5_000)
        device.wait(Until.hasObject(By.desc("AddDialogTranslation")), 5_000)
        device.findObject(By.desc("AddDialogTranslation")).click()
        device.clearTextField()
        device.type("pogoda")

        val saveSelector = By.text("SAVE")
        if (!device.hasObject(saveSelector)) {
            device.pressBack()
        }
        val foundSave = device.wait(Until.hasObject(saveSelector), 1_000)
        if (foundSave) {
            device.findObject(saveSelector).click()
        } else {
            device.scrollUntilVisible(saveSelector)
            device.wait(Until.hasObject(saveSelector), 5_000)
            device.findObject(saveSelector).click()
        }
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
        maxSwipes: Int = 5,
        fromTopToBottom: Boolean = true,
    ): Boolean {
        return scrollUntilVisible(maxSwipes, fromTopToBottom) { this.hasObject(targetSelector) }
    }

    private fun UiDevice.scrollUntilVisible(
        maxSwipes: Int = 5,
        fromTopToBottom: Boolean = true,
        condition: () -> Boolean,
    ): Boolean {
        val screenWidth = this.displayWidth
        val screenHeight = this.displayHeight

        repeat(maxSwipes) { _ ->
            if (condition()) {
                return true
            }
            if (fromTopToBottom) {
                swipe(
                    /* startX = */ screenWidth / 2,   // fromX
                    /* startY = */ (screenHeight * 3) / 4,  // fromY
                    /* endX = */ screenWidth / 2,   // toX
                    /* endY = */ screenHeight / 4,  // toY
                    /* steps = */ 20
                )
            } else {
                swipe(
                    /* startX = */ screenWidth / 2,
                    /* startY = */ screenHeight / 4,
                    /* endX = */ screenWidth / 2,
                    /* endY = */ (screenHeight * 3) / 4,
                    /* steps = */ 20
                )
            }
            Thread.sleep(500)
        }
        return condition()
    }

    private fun UiDevice.clearTextField(times: Int = 50) {
        repeat(times) {
            pressKeyCode(KeyEvent.KEYCODE_DEL)
            Thread.sleep(50)
        }
    }

    private fun UiDevice.type(word: String) {
        for (char in word) {
            if (char.isLetter()) {
                val lowerChar = char.lowercaseChar()
                val keyCode = KeyEvent.KEYCODE_A + (lowerChar - 'a')
                if (char.isUpperCase()) {
                    pressKeyCode(KeyEvent.KEYCODE_SHIFT_LEFT)
                }
                pressKeyCode(keyCode)
            } else if (char.isDigit()) {
                val keyCode = KeyEvent.KEYCODE_0 + (char - '0')
                pressKeyCode(keyCode)
            } else if (char == ' ') {
                pressKeyCode(KeyEvent.KEYCODE_SPACE)
            }
            Thread.sleep(100)
        }
    }

}