package com.hruby.vcelnice.ui.stanoviste

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StanovisteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is stanoviste Fragment"
    }
    val text: LiveData<String> = _text
}