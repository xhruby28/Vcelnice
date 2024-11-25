package com.hruby.vcelnice.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Aplikace je ve vývoji, jedná se o prázdný fragment. Stránka která je v tuto chvíli funkční je Stanoviště"
    }
    val text: LiveData<String> = _text
}