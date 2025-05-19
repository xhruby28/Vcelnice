package com.hruby.ulydetailmodule.ui.infoUl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.ulydetailmodule.databinding.FragmentInfoUlBinding
import com.hruby.ulydetailmodule.ui.infoUl.infoUlEdit.InfoUlEditDialog

class InfoUlFragment : Fragment(), InfoUlEditDialog.EditUlEditDialogListener {
    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    private var _binding: FragmentInfoUlBinding? = null
    private val binding get() = _binding!!

    private lateinit var ulyViewModel: UlyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)
        Log.d("InfoUlFragment", "stanoviste id: $stanovisteId, Ul ID: $ulId")

        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)
        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoUlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulyViewModel.getUlWithOthersByStanovisteId(ulId, stanovisteId).observe(viewLifecycleOwner) { ulWithOthers ->
            val ul = ulWithOthers.ul

            // Název úlu
            binding.ulDetailInfoFragmentNazev.text = "Úl č. ${ul.cisloUlu ?: "?"}"

            // Popis
            binding.ulDetailInfoFragmentPopis.text = ul.popis ?: "Bez popisu"

            // MAC adresa
            if (ul.maMAC) {
                binding.ulDetailInfoFragmentMac.text = ul.macAddress ?: "Neuvedeno"
            } else {
                binding.ulDetailInfoFragmentMac.visibility = View.GONE
                binding.ulDetailInfoFragmentMacText.visibility = View.GONE
            }

            // Hodnocení
            /*val ratingLayout = binding.root.findViewById<LinearLayout>(R.id.rating_layout)
            val showRatings = ul.hodnoceni > 0f || ul.agresivita > 0 || ul.mezolitostPlodu > 0 || ul.silaVcelstva > 0 || ul.stavebniPud > 0 || !ul.stavZasob.isNullOrEmpty()

            if (showRatings) {
                ratingLayout.visibility = View.VISIBLE
                binding.ratingHodnoceni.rating = ul.hodnoceni
                binding.ratingAgresivita.rating = ul.agresivita.toFloat()
                binding.ratingMezolitostPlodu.rating = ul.mezolitostPlodu.toFloat()
                binding.ratingSilaVcelstva.rating = ul.silaVcelstva.toFloat()
                binding.ratingStavebniPud.rating = ul.stavebniPud.toFloat()
                binding.textStavZasob.text = "Stav zásob: ${ul.stavZasob ?: "neuvedeno"}"
            } else {
                ratingLayout.visibility = View.GONE
            }*/

            binding.ulDetailInfoFragmentFab.setOnClickListener {
                    val dialog = InfoUlEditDialog()
                    val bundle = Bundle()
                    bundle.putSerializable("ul", ul)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, "InfoUlEditDialog")
            }
        }
    }

    override fun onUlUpdate(updatedStanoviste: Uly) {
        ulyViewModel.updateUl(updatedStanoviste)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}