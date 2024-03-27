package com.example.progettoprogmobile
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
@RunWith(AndroidJUnit4::class)
class ArtistaSelezionatoFragmentTest {

    @Test
    fun testImageIsDisplayedOnFragmentLaunch() {
        // Lancia il fragment in un container di test
        launchFragmentInContainer<ArtistaSelezionato>()

        // Verifica che l'ImageView sia visualizzato. Questo verifica che l'immagine (di default o caricata) sia presente
        onView(withId(R.id.imageartistaselezionato)).check(matches(isDisplayed()))
        onView(withId(R.id.imageartistaselezionato)).check(matches(withDrawable(R.drawable.imgcantante)))

    }
}

fun withDrawable(resourceId: Int): Matcher<View> {
    return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with drawable from resource id: ")
            description.appendValue(resourceId)
        }

        override fun matchesSafely(imageView: ImageView): Boolean {
            if (resourceId < 0) {
                return imageView.drawable == null
            }
            val expectedDrawable = imageView.context.getDrawable(resourceId) ?: return false
            return imageView.drawable.constantState == expectedDrawable.constantState
        }
    }
}