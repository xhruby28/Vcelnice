package com.hruby.ulydetailmodule.ui.namereneHodnotyUl

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentNamereneHodnotyUlBinding
import kotlin.math.roundToInt
import kotlin.random.Random

class NamereneHodnotyUlFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NamereneHodnotyUlRecycleViewAdapter
    private lateinit var ulyViewModel: UlyViewModel

    private val mereneHodnoty: MutableList<MereneHodnoty> = mutableListOf()

    private var _binding: FragmentNamereneHodnotyUlBinding? = null
    private val binding get() = _binding!!

    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNamereneHodnotyUlBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        recyclerView = view.findViewById(R.id.merene_hodnoty_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)

        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]

        adapter = NamereneHodnotyUlRecycleViewAdapter(mereneHodnoty)
        recyclerView.adapter = adapter

        ulyViewModel.getUlWithOthersByStanovisteId(ulId,stanovisteId).observe(viewLifecycleOwner){ ul ->
            mereneHodnoty.clear()
            Log.d("NamereneHodnotyUlFragment", "Data loaded: ${ul.mereneHodnoty.size} items")
            if (ul.mereneHodnoty.isEmpty()) {
                Log.d("NamereneHodnotyUlFragment", "No measured data available.")
            } else {
                mereneHodnoty.addAll(ul.mereneHodnoty)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun generateRandomMereneHodnoty(ulId: Int): MereneHodnoty {
        fun randomWithTwoDecimals(min: Float, max: Float): Float {
            return ((Random.nextFloat() * (max - min) + min) * 100).roundToInt() / 100f
        }

        val threeMonthsInMillis = 90L * 24 * 60 * 60 * 1000
        return MereneHodnoty(
            ulId = ulId,
            datum = System.currentTimeMillis() - Random.nextLong(0, threeMonthsInMillis), // Náhodný čas za poslední 3 měsíce
            hmotnost = randomWithTwoDecimals(0f, 50f), // Náhodná hmotnost mezi 0 a 50 kg
            teplotaUl = randomWithTwoDecimals(10f, 50f), // Teplota mezi 10 a 50 °C
            vlhkostModul = randomWithTwoDecimals(0f, 100f), // Vlhkost mezi 0 a 100 %
            teplotaModul = randomWithTwoDecimals(-15f, 45f), // Teplota mezi 15 a 45 °C
            frekvence = randomWithTwoDecimals(0f, 1000f), // Náhodná hodnota
            gyroX = randomWithTwoDecimals(-1f, 1f), // Gyro hodnoty mezi -1 a 1
            gyroY = randomWithTwoDecimals(-1f, 1f),
            gyroZ = randomWithTwoDecimals(-1f, 1f),
            accelX = randomWithTwoDecimals(-10f, 10f), // Akcelerace mezi -10 a 10
            accelY = randomWithTwoDecimals(-10f, 10f),
            accelZ = randomWithTwoDecimals(-10f, 10f)
        )
    }
}