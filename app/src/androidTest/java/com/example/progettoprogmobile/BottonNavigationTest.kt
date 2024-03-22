package com.example.progettoprogmobile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavigationTest {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var scenario: ActivityScenario<SecondActivity>

    @Before
    fun setup() {
        // Inizializzazione dell'activity con ActivityScenario
        scenario = ActivityScenario.launch(SecondActivity::class.java)
        scenario.onActivity { activity ->
            bottomNavigationView = activity.findViewById(R.id.bottomNavigationView)
        }
    }
    @Test
    fun testNavigationItemSelection() {
        val itemId = R.id.cerca

        // Simula la selezione dell'item
        scenario.onActivity {
            bottomNavigationView.selectedItemId = itemId
        }

        // Verifica che l'item selezionato sia effettivamente quello che ci aspettiamo
        assertEquals(itemId, bottomNavigationView.selectedItemId)
    }
}