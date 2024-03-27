package com.example.progettoprogmobile


import org.junit.Before
import org.junit.Test
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.viewModel.RecensioneViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import com.google.android.gms.tasks.Tasks
import org.junit.runner.RunWith

import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = [34])
class RecensioneViewModelTest {

    private lateinit var viewModel: RecensioneViewModel
    private val mockDatabaseRef: DatabaseReference = mock(DatabaseReference::class.java)

    @Before
    fun setUp() {
        // Impostazione del mock del DatabaseReference
        `when`(mockDatabaseRef.child(anyString())).thenReturn(mockDatabaseRef)
        `when`(mockDatabaseRef.push()).thenReturn(mockDatabaseRef)
        `when`(mockDatabaseRef.key).thenReturn("fakeKey")

        // Inizializzazione del ViewModel con il DatabaseReference mockato
        viewModel = RecensioneViewModel(mockDatabaseRef)
    }

    @Test
    fun saveRecensioneTest() {
        val userId = "user123"
        val trackId = "track123"
        val artistId = "artist123"
        val commentContent = "bangerone"

        // Configura il mock per simulare la chiamata di successo
        `when`(mockDatabaseRef.setValue(any(Recensione::class.java))).thenReturn(Tasks.forResult(null))

        viewModel.saveRecensione(userId, trackId, artistId, commentContent)

        // Verifica che i metodi appropriati siano stati chiamati sul DatabaseReference
        verify(mockDatabaseRef).child("reviews")
        verify(mockDatabaseRef).setValue(any(Recensione::class.java))
    }
}
