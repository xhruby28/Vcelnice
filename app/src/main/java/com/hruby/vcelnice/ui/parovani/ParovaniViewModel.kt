package com.hruby.vcelnice.ui.parovani

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ParovaniViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is parovani Fragment"
    }
    val text: LiveData<String> = _text
}