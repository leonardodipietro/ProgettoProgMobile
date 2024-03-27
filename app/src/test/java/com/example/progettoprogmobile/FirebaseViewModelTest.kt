import android.app.Application
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.ArtistDataSource
import com.example.progettoprogmobile.model.Image
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = [34])
class FirebaseViewModelTest {

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockArtistDataSource: ArtistDataSource

    private lateinit var viewModel: FirebaseViewModel

    @Before
    fun setUp() {
        viewModel = FirebaseViewModel(mockApplication, mockArtistDataSource)
    }

    @Test
    fun retrieveArtistByIdTest() {
        val artistId = "uniqueArtistId"
        val expectedArtist = Artist("Test Artist Name", listOf("Genre"), artistId, listOf(Image("imageUrl")))

        `when`(mockArtistDataSource.retrieveArtistById(eq(artistId), any(), any())).thenAnswer { invocation ->
            val onCompleteCallback = invocation.arguments[1] as (Artist?) -> Unit
            onCompleteCallback(expectedArtist)
        }

        var artistResult: Artist? = null
        viewModel.retrieveArtistById(artistId) { artist ->
            artistResult = artist
        }

        assertNotNull(artistResult)
        assertEquals(expectedArtist.name, artistResult?.name)
    }
}