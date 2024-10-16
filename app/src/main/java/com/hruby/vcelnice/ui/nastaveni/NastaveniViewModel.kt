package com.hruby.vcelnice.ui.nastaveni

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NastaveniViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Nastaveni Fragment"
    }
    val text: LiveData<String> = _text
}