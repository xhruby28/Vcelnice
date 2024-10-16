package com.hruby.vcelnice.ui.stanoviste

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hruby.vcelnice.R
import com.hruby.vcelnice.databinding.FragmentStanovisteBinding
import com.hruby.vcelnice.ui.stanoviste.dialogs.EditDialogFragment

class StanovisteFragment : Fragment(), EditDialogFragment.EditDialogListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StanovisteRecycleViewAdapter
    private var stanovisteList: MutableList<Stanoviste> = mutableListOf()

    private var _binding: FragmentStanovisteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStanovisteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        if(stanovisteList.isEmpty()){

        }
        stanovisteList.add(Stanoviste("Stanoviste1","2024-09-28", "https://maps.google.com/?q=49.1951,16.6075", "V pořádku", 0))
        stanovisteList.add(Stanoviste("Stanoviste2","2024-03-24", "https://maps.google.com/?q=49.1951,16.6075", "Problém s úlem č.13", 0))

        adapter = StanovisteRecycleViewAdapter(stanovisteList)
        recyclerView.adapter = adapter
    }

    override fun onDialogSave(id: Int, name: String, lastCheck: String, locationUrl: String) {
        Log.d("EditDialog", "Saving: ID: $id, Name: $name, Last Check: $lastCheck, Location URL: $locationUrl")
        if (id in stanovisteList.indices) {
            val existingStanoviste = stanovisteList[id]
            existingStanoviste.name = name
            existingStanoviste.lastCheck = lastCheck
            existingStanoviste.locationUrl = locationUrl
            adapter.notifyItemChanged(id) // Informuj adaptér
        } else {
            Log.e("EditDialog", "Invalid ID: $id")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}