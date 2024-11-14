package com.hruby.stanovistedetailmodule.ui.ulyStanoviste

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.navmodule.Navigator
import com.hruby.stanovistedetailmodule.databinding.FragmentUlyStanovisteBinding
import com.hruby.stanovistedetailmodule.R
import com.hruby.stanovistedetailmodule.ui.ulyStanoviste.ulyDialogs.UlyCreateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UlyStanovisteFragment : Fragment(), UlyCreateDialogFragment.UlCreateDialogListener{
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UlyRecycleViewAdapter
    private lateinit var ulyViewModel: UlyViewModel
    private val ulyList: MutableList<Uly> = mutableListOf()

    @Inject
    lateinit var navigator: Navigator

    private var _binding: FragmentUlyStanovisteBinding? = null
    private val binding get() = _binding!!

    private var stanovisteId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUlyStanovisteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("UlyStanovisteFragment", "onViewCreated called")
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)
        Log.d("UlyStanovisteFragment", "stanoviste id: $stanovisteId")

        recyclerView = view.findViewById(R.id.uly_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = view.findViewById(R.id.uly_stanoviste_fab)

        fab.setOnLongClickListener {
            Snackbar.make(view, "Přidat úl nepatřící pod SmartHive Network", Snackbar.LENGTH_SHORT).show()
            true
        }
        fab.setOnClickListener {
            openUlAddDialog()
        }

        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)
        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]

        // Pozorování na změny v LiveData
        val stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)
        ulyViewModel.getUlyByStanovisteId(stanovisteId).observe(viewLifecycleOwner){ uly ->
            ulyList.clear()
            ulyList.addAll(uly)
            adapter.notifyDataSetChanged()
        }

        adapter = UlyRecycleViewAdapter(
            ulyList,
            { uly, position ->
//                TODO("Při kliknutí na možnost EDIT bude uživatel přesměrován na UlDetailModule - Úprava," +
//                        "kde bude kompletní seznam věcí, které uživatel může změnit")
                showUlyEditDialog(uly, position)
            },
            { uly, position ->
                showUlyDeleteDialog(uly, position)
            },
            { ulId ->
//                TODO("Při vybrání úlu bude uživatel přesměrován na UlDetailModule, ked bude kompletní informace k úlu," +
//                        "naměřené hodnoty, jeho MAC pokud je to SmartHive, grafy naměřených hodnot," +
//                        "tento modul bude mít také svůj vlastní navigační graf, u toho grafu bude vidět číslo úlu a název stanoviště tak," +
//                        "jak je to teď u stnaoviště")
                onUlSelected(ulId)
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUlyEditDialog(uly: Uly, position: Int){
//        TODO("Implementace v příštích verzích.. nestíhám ")
    }

    private fun showUlyDeleteDialog(uly: Uly, position: Int){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Potvrzení smazání")
            .setMessage("Opravdu chcete smazat úl číslo ${uly.cisloUlu}?")
            .setPositiveButton("Ano") { dialog, _ ->
                // Volání funkce pro smazání
                ulyViewModel.deleteUl(uly)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("Ne") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun openUlAddDialog() {
        val dialog = UlyCreateDialogFragment()
        val bundle = Bundle()
        bundle.putInt("index", -1)  // Přidání argumentů
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "UlyCreateDialogFragment")
    }

    override fun onDialogSave(index: Int, numUl: Int, descUl: String){
        if (index in ulyList.indices){
            val existingUl = ulyList[index]
            existingUl.cisloUlu = numUl
            existingUl.popis = descUl

            ulyViewModel.updateUl(existingUl)
            adapter.notifyItemChanged(index)
        } else{
            val newUl = Uly(
                stanovisteId = stanovisteId,
                cisloUlu = numUl,
                popis = descUl
            )
            ulyViewModel.insertUl(newUl)
            adapter.notifyItemChanged(index)
        }
    }

    private fun onUlSelected(ulId: Int) {
        navigator.openUlDetail(ulId)
    }
}