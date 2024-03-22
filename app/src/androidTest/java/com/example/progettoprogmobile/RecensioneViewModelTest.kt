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

import org.mockito.Mockito.*

class RecensioneViewModelTest {

    private lateinit var viewModel: RecensioneViewModel
    private val mockDatabaseRef: DatabaseReference = mock(DatabaseReference::class.java)
    private val mockFirebaseDb: FirebaseDatabase = mock(FirebaseDatabase::class.java)

    @Before
    fun setUp() {
        `when`(mockFirebaseDb.reference).thenReturn(mockDatabaseRef)
        `when`(mockDatabaseRef.child(anyString())).thenReturn(mockDatabaseRef)
        `when`(mockDatabaseRef.push()).thenReturn(mockDatabaseRef)
        `when`(mockDatabaseRef.key).thenReturn("fakeKey")
        // Crea l'istanza di RecensioneViewModel
        viewModel = RecensioneViewModel().apply {

            initForTesting(mockDatabaseRef)
        }
    }

    @Test
    fun saveRecensioneTest() {
        val userId = "user123"
        val trackId = "track123"
        val artistId = "artist123"
        val commentContent = "bangerone"

        // Configura il mock per comportarsi come previsto quando setValue() è chiamato
        `when`(mockDatabaseRef.setValue(any())).thenReturn(Tasks.forResult<Void>(null))


        viewModel.saveRecensione(userId, trackId, artistId, commentContent)

        // Verifica che push() e setValue() siano stati chiamati come previsto
        verify(mockDatabaseRef).push()
        verify(mockDatabaseRef).setValue(any(Recensione::class.java))
    }
}