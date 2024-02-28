package com.example.progettoprogmobile.utils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class SharedDataComment: ViewModel() {


    private val _commentrecyclervisibility = MutableLiveData<Boolean>()
    val commentrecyclervisibility: LiveData<Boolean> =_commentrecyclervisibility
    fun hideComments() {
        _commentrecyclervisibility.value = false
    }
    fun setCommentsVisibility(isVisible: Boolean) {
        _commentrecyclervisibility.postValue(isVisible)
    }

}