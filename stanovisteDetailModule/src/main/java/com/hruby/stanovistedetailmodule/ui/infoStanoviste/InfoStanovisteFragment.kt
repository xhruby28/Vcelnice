package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

class InfoStanovisteFragment : Fragment() {
    private var stanovisteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        Log.d("UlyStanovisteFragment", "stanoviste id: $stanovisteId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}