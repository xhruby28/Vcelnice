package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hruby.stanovistedetailmodule.databinding.FragmentInfoStanovisteBinding

class InfoStanovisteFragment : Fragment() {
    private var _binding: FragmentInfoStanovisteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InfoStanovisteViewModel
    private var stanovisteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        if (stanovisteId != -1) {
            // Inicializace ViewModelu s ID stanoviště
            val factory = InfoStanovisteViewModelFactory(requireContext(), stanovisteId)
            viewModel = ViewModelProvider(this, factory)[InfoStanovisteViewModel::class.java]
        } else {
            throw IllegalArgumentException("StanovisteId není validní.")
        }

        Log.d("UlyStanovisteFragment", "stanoviste id: $stanovisteId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoStanovisteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sledujte data z ViewModelu a aktualizujte UI
        viewModel.ulyCount.observe(viewLifecycleOwner) { count ->
            //binding.textViewUlyCount.text = "Počet úlů: $count"
        }

        viewModel.stanoviste.observe(viewLifecycleOwner) { stanoviste ->
            binding.stanovisteDetailInfoFragmentNazevStanoviste.text = stanoviste.name
            binding.stanovisteDetailInfoFragmentStanovisteMaMac.text = stanoviste.siteMAC
            //TODO("Zde přidat další změny pro Detailní info pro stanoviště")
            //binding.stanovisteDetailInfoFragmentImage.src
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}