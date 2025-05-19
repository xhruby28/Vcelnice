package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.ZaznamKontrolyViewModelFactory
import com.hruby.ulydetailmodule.databinding.FragmentZaznamKontrolyUlBinding

class ZaznamKontrolyUlFragment : Fragment() {

    private var _binding: FragmentZaznamKontrolyUlBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ZaznamKontrolyAdapter
    private lateinit var viewModel: ZaznamKontrolyViewModel

    private val zaznamyKontrol: MutableList<ZaznamKontroly> = mutableListOf()

    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentZaznamKontrolyUlBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        recyclerView = binding.ulZaznamKontrolyRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = StanovisteDatabase.getDatabase(requireContext().applicationContext)
        val repository = ZaznamKontrolyRepository(db)
        val factory = ZaznamKontrolyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ZaznamKontrolyViewModel::class.java]

        adapter = ZaznamKontrolyAdapter(
            zaznamyKontrol,
            { zaznam, position ->
                showDeleteDialog(zaznam, position)
            },
            { zaznam ->
                openDetail(zaznam)
            }
        )
        
        recyclerView.adapter = adapter

        viewModel.getZaznamyByUlId(ulId).observe(viewLifecycleOwner) { zaznamy ->
            zaznamyKontrol.clear()
            zaznamyKontrol.addAll(zaznamy)
            adapter.notifyDataSetChanged()
        }

        binding.ulyStanovisteFab.setOnClickListener {
            // Otevření dialogu nebo activity pro přidání kontroly
            val action = ZaznamKontrolyUlFragmentDirections.actionNavZaznamUlToNavZaznamUlAdd(ulId)
            findNavController().navigate(action)
        }
    }

    private fun showDeleteDialog(zaznam: ZaznamKontroly, position: Int){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Smazat kontrolu")
            .setMessage("Opravdu chceš tuto kontrolu smazat?")
            .setPositiveButton("Ano") { dialog, _ ->
                viewModel.deleteZaznam(zaznam)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("Ne") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun openDetail(zaznam: ZaznamKontroly) {
        // TODO: Přesměrování na detail
        Toast.makeText(requireContext(), "Kliknuto: ${zaznam.typKontroly}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}