package com.example.progettoprogmobile.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SharedEditTextVisibilityManager {

    private val _editTextVisibility = MutableLiveData<Boolean>()
    val editTextVisibility: LiveData<Boolean> = _editTextVisibility

    /*fun showEditText() { Si canna probabilmente
        _editTextVisibility.value = true
    }*/

    fun hideEditText() {
        _editTextVisibility.value = false
    }
    fun setEditTextVisibility(isVisible: Boolean) {
        _editTextVisibility.postValue(isVisible)
    }

}