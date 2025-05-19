package com.hruby.ulydetailmodule.ui.namereneHodnotyUl

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentNamereneHodnotyUlGraphsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NamereneHodnotyUlFragmentGraphs : Fragment() {
    private lateinit var lineChart: LineChart
    private lateinit var spinnerVelicina: Spinner
    private lateinit var spinnerCasovyUsek: Spinner
    private lateinit var textViewAnalyza: TextView

    private lateinit var datePicker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>>
    private var isDatePickerInitialized = false

    private var mereneHodnoty: MutableList<MereneHodnoty> = mutableListOf()

    private lateinit var ulyViewModel: UlyViewModel
    private var _binding: FragmentNamereneHodnotyUlGraphsBinding? = null
    private val binding get() = _binding!!

    private var ulId: Int = -1
    private var stanovisteId: Int = -1

    private var customStart: Long = 0
    private var customEnd: Long = 0
    private var isCustomRangeSelected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNamereneHodnotyUlGraphsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        lineChart = binding.lineChart
        spinnerVelicina = binding.spinnerVelicina
        spinnerCasovyUsek = binding.spinnerCasovyUsek
        //textViewAnalyza = binding.textViewAnalyza

        val isDarkTheme = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val textColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        val scrollView = binding.root.findViewById<ScrollView>(R.id.graphsScrollView)

        lineChart.legend.textColor = textColor
        lineChart.xAxis.textColor = textColor
        lineChart.axisLeft.textColor = textColor
        lineChart.axisRight.textColor = textColor
        lineChart.axisRight.isEnabled = false

        // Načti data z ViewModelu
        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)
        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]

        ulyViewModel.getUlWithOthersByStanovisteId(ulId, stanovisteId).observe(viewLifecycleOwner) { ul ->
            mereneHodnoty.clear()
            Log.d("NamereneHodnotyUlFragment", "Data loaded: ${ul.mereneHodnoty.size} items")
            if (ul.mereneHodnoty.isEmpty()) {
                Log.d("NamereneHodnotyUlFragment", "No measured data available.")
            } else {
                mereneHodnoty.addAll(ul.mereneHodnoty)
            }
            //updateChart()
        }

        spinnerCasovyUsek.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val casovyUsek = spinnerCasovyUsek.selectedItem.toString()
                setupFilter(casovyUsek, mereneHodnoty)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerVelicina.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val casovyUsek = spinnerCasovyUsek.selectedItem.toString()
                setupFilter(casovyUsek, mereneHodnoty)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lineChart.apply {
            setOnTouchListener { v, event ->
                scrollView?.requestDisallowInterceptTouchEvent(true)
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    scrollView?.requestDisallowInterceptTouchEvent(false)
                    performClick()
                }
                false
            }
        }

        prepareDatePicker()

        return root
    }

    private fun prepareDatePicker() {
        datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Vyberte časové období")
            .build()

        isDatePickerInitialized = true

        // Listener přidáš už teď
        datePicker.addOnPositiveButtonClickListener { selection ->
            isCustomRangeSelected = true
            val startMillis = selection.first ?: return@addOnPositiveButtonClickListener
            val endMillis = selection.second ?: return@addOnPositiveButtonClickListener
            customStart = startMillis / 1000
            customEnd = endMillis / 1000
            val filtered = mereneHodnoty.filter { it.datum in customStart..customEnd }
            updateChart(filtered)
            updateDateRangeText(customStart, customEnd)
        }
    }

    private fun updateChart(data: List<MereneHodnoty>) {
        val selectedVelicina = spinnerVelicina.selectedItem.toString()
        val jednotka: String
        val lineDataSets = mutableListOf<ILineDataSet>()

        when (selectedVelicina) {
            "Teplota a vlhkost modulu" -> {
                val tempEntries = data.mapNotNull {
                    it.teplotaModul?.let { v -> Entry(it.datum.toFloat(), v) }
                }
                val humEntries = data.mapNotNull {
                    it.vlhkostModul?.let { v -> Entry(it.datum.toFloat(), v) }
                }

                val tempDataSet = LineDataSet(tempEntries, "Teplota modulu [°C]").apply {
                    color = Color.RED
                    setDrawCircles(false)
                    setDrawCircles(true)
                    circleRadius = 2f
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                }

                val humDataSet = LineDataSet(humEntries, "Vlhkost modulu [%]").apply {
                    color = Color.BLUE
                    setDrawCircles(true)
                    circleRadius = 2f
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.RIGHT
                }

                lineChart.axisLeft.apply {
                    textColor = Color.RED
                    textSize = 12f
                    setDrawGridLines(true)
                    setDrawLabels(true)
                }

                lineChart.axisRight.apply {
                    isEnabled = true
                    textColor = Color.BLUE
                    textSize = 12f
                    setDrawGridLines(false)
                    setDrawLabels(true)
                }

                lineDataSets.add(tempDataSet)
                lineDataSets.add(humDataSet)
                jednotka = ""
                lineChart.axisRight.isEnabled = true
            }

            // Ostatní případy jako doteď (např. "Teplota v úlu", "Váha úlu" ...)
            else -> {
                lineChart.axisRight.isEnabled = false
                val entries = data.mapNotNull {
                    val value = when (selectedVelicina) {
                        "Teplota v úlu" -> it.teplotaUl
                        "Váha úlu" -> it.hmotnost
                        "Frekvence v úlu" -> it.frekvence
                        else -> null
                    }
                    value?.let { v -> Entry(it.datum.toFloat(), v) }
                }

                jednotka = when (selectedVelicina) {
                    "Teplota v úlu" -> "°C"
                    "Váha úlu" -> "kg"
                    "Frekvence v úlu" -> "Hz"
                    else -> ""
                }

                val dataSet = LineDataSet(entries, "$selectedVelicina [$jednotka]").apply {
                    color = Color.YELLOW
                    setDrawCircles(true)
                    circleRadius = 2f
                    setDrawValues(false)
                    setDrawHighlightIndicators(true)
                }

                lineChart.axisLeft.apply {
                    textColor = Color.YELLOW
                    textSize = 12f
                    setDrawGridLines(true)
                    setDrawLabels(true)
                }
                lineDataSets.add(dataSet)
            }
        }

        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    return sdf.format(Date(value.toLong() * 1000))
                }
            }
            granularity = 3600f
        }

        // MarkerView
        val markerView = CustomMarkerView(requireContext(), R.layout.custom_marker_view)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        lineChart.highlightValues(null)
        lineChart.data = LineData(lineDataSets)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun setupFilter(timeRange: String, data: List<MereneHodnoty>) {
        if (timeRange != "Vlastní rozmezí") {
            isCustomRangeSelected = false
        }
        filterByTimeRange(data, timeRange) { filteredData, start, end ->
            updateChart(filteredData)
            updateDateRangeText(start, end)
        }
    }

    private fun filterByTimeRange(
        data: List<MereneHodnoty>,
        timeRange: String,
        onFiltered: (List<MereneHodnoty>, Long, Long) -> Unit
    ) {
        val now = System.currentTimeMillis() / 1000
        when (timeRange) {
            "Poslední den" -> {
                val start = now - TimeUnit.DAYS.toSeconds(1)
                onFiltered(data.filter { it.datum in start..now }, start, now)
            }
            "Poslední týden" -> {
                val start = now - TimeUnit.DAYS.toSeconds(7)
                onFiltered(data.filter { it.datum in start..now }, start, now)
            }
            "Poslední měsíc" -> {
                val start = now - TimeUnit.DAYS.toSeconds(30)
                onFiltered(data.filter { it.datum in start..now }, start, now)
            }
            "Poslední rok" -> {
                val start = now - TimeUnit.DAYS.toSeconds(365)
                onFiltered(data.filter { it.datum in start..now }, start, now)
            }
            "Celé období" -> {
                val start = data.minOfOrNull { it.datum } ?: now
                val end = data.maxOfOrNull { it.datum } ?: now
                onFiltered(data, start, end)
            }
            "Vlastní rozmezí" -> {
                if (isCustomRangeSelected && customStart != 0L && customEnd != 0L) {
                    onFiltered(data.filter { it.datum in customStart..customEnd }, customStart, customEnd)
                } else {
                    showDateRangePicker()
                }
            }
            else -> onFiltered(data, now, now)
        }
    }

    // Funkce pro analýzu stavu úlu
    private fun analyzeHiveState(data: List<MereneHodnoty>): String {
        val avgTemp = data.map { it.teplotaUl }.average()
        val avgVaha = data.map { it.hmotnost }.average()
        val avgFreq = data.map { it.frekvence }.average()

        return if (avgTemp in 30.0..36.0 && avgVaha > 10.0 && avgFreq > 200.0) {
            "Úl vypadá v pořádku (teplota, váha i frekvence jsou v normě)."
        } else {
            "Pozor! Některé hodnoty se odchylují od normálu."
        }
    }

    // Funkce pro zobrazení pickeru pro vlastní časové období
    private fun showDateRangePicker() {
        if (!isDatePickerInitialized) prepareDatePicker()
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun updateDateRangeText(start: Long, end: Long) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val startStr = sdf.format(Date(start * 1000))
        val endStr = sdf.format(Date(end * 1000))
        binding.textViewRozsah.text = "Zobrazené období: $startStr – $endStr"
    }

    private fun formatUnixTimestamp(seconds: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+ (používáme Instant a DateTimeFormatter)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            formatter.format(Instant.ofEpochSecond(seconds))
        } else {
            // API 24–25 (používáme SimpleDateFormat)
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            sdf.format(Date(seconds * 1000))  // Konverze na milisekundy pro SimpleDateFormat
        }
    }
}