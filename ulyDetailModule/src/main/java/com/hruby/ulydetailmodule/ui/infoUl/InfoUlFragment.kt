package com.hruby.ulydetailmodule.ui.infoUl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentInfoUlBinding
import com.hruby.ulydetailmodule.databinding.FragmentNamereneHodnotyUlBinding

class InfoUlFragment : Fragment() {
    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    private var _binding: FragmentInfoUlBinding? = null
    private val binding get() = _binding!!

    private lateinit var ulyViewModel: UlyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoUlBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)
        Log.d("InfoUlFragment", "stanoviste id: $stanovisteId, Ul ID: $ulId")

        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)

        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]

        ulyViewModel.getUlWithOthersByStanovisteId(ulId,stanovisteId).observe(viewLifecycleOwner) {ul ->
            Log.d("InfoUlFragment", "Ul MAC: ${ul.ul.macAddress}")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}