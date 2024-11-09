package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel

class InfoStanovisteFragment : Fragment() {
    private lateinit var viewModel: StanovisteViewModel
    private var stanovisteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stanovisteId = it.getInt("stanovisteId")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(StanovisteViewModel::class.java)

        stanovisteId?.let {
            viewModel.getStanovisteById(it).observe(viewLifecycleOwner, Observer { stanoviste ->
                // Zde nastavte UI s informacemi o stanovišti
                if (stanoviste != null) {
                    // Naplnění UI prvků daty ze stanoviste
                }
            })
        }
    }
}