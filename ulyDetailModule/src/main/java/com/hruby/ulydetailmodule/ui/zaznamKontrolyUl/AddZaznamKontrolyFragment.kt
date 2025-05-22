package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.ZaznamKontrolyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentAddZaznamKontrolyBinding
import java.util.Calendar

class AddZaznamKontrolyFragment : Fragment() {

    private var _binding: FragmentAddZaznamKontrolyBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ZaznamKontrolyViewModel

    private var ulId: Int = -1
    private var selectedDate: Long = System.currentTimeMillis()

    private lateinit var spinnerTypKontroly: Spinner
    private lateinit var datePicker: EditText
    private lateinit var medobraniRamkyLayout: LinearLayout
    //private lateinit var medobraniCount: EditText
    private lateinit var medobraniCount: Spinner
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddZaznamKontrolyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulId = arguments?.getInt("ulId") ?: -1
        val db = StanovisteDatabase.getDatabase(requireContext().applicationContext)
        val repository = ZaznamKontrolyRepository(db)
        val factory = ZaznamKontrolyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ZaznamKontrolyViewModel::class.java]

        //spinnerTypKontroly = view.findViewById<Spinner>(R.id.add_ul_kontrola_spinnerTypKontroly)
        datePicker = view.findViewById<EditText>(R.id.add_ul_kontrola_datePicker)
        //medobraniRamkyLayout = view.findViewById<ConstraintLayout>(R.id.add_ul_kontrola_layoutMedobrani)
        //medobraniCount = view.findViewById<EditText>(R.id.add_ul_kontrola_editTextMedobraniRamky)
        medobraniCount = view.findViewById<Spinner>(R.id.add_ul_kontrola_editTextMedobraniRamky)
        buttonCancel = view.findViewById<Button>(R.id.add_ul_kontrola_buttonCancel)
        buttonSave = view.findViewById<Button>(R.id.add_ul_kontrola_buttonSave)

        // Nastavení výchozího data na aktuální čas
        setDateToCurrent()

        datePicker.setOnClickListener {
            showDatePickerDialog()
            hideKeyboard(view)
        }

        /*spinnerTypKontroly.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                if (selectedItem == "Medobraní") {
                    medobraniRamkyLayout.visibility = View.VISIBLE
                } else {
                    medobraniRamkyLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }*/

        buttonSave.setOnClickListener {
            saveZaznam()
        }

        buttonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setDateToCurrent() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDate = calendar.timeInMillis

        val formattedDate = String.format(
            "%02d-%02d-%04d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
        datePicker.setText(formattedDate)
    }

    // Funkce pro zobrazení DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDate = selectedCalendar.timeInMillis // <-- ukládáme timestamp
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                datePicker.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveZaznam() {
        /*val agresivita = seekBarAgresivita.progress
        val mezolitostPlodu = seekBarPlod.progress
        val silaVcelstva = seekBarSilaVcelstva.progress
        val stavebniPud = seekBarStavebniPud.progress

        var medobraniRamky = 0
        if (spinnerTypKontroly.selectedItem.toString() == "Medobraní") {
            medobraniRamky = medobraniCount.selectedItem.toString().toIntOrNull() ?: -1
        }



        val prumer = (agresivita + mezolitostPlodu + silaVcelstva + stavebniPud) / 4.0f

        val zaznam = ZaznamKontroly(
            ulId = ulId,
            datum = selectedDate,
            typKontroly = spinnerTypKontroly.selectedItem.toString(),
            zaznamText = editTextZaznamText.text.toString(),
            stavZasob = editTextStavZasob.text.toString(),
            medobrani = spinnerTypKontroly.selectedItem.toString() == "Medobraní",
            medobraniRamky = medobraniRamky,
            hodnoceni = prumer,
            agresivita = agresivita,
            mezolitostPlodu = mezolitostPlodu,
            silaVcelstva = silaVcelstva,
            stavebniPud = stavebniPud
        )*/
        if (spinnerTypKontroly.selectedItem.toString() == "-------") {
            Toast.makeText(requireContext(), "Vyberte typ kontroly..", Toast.LENGTH_SHORT).show()
        } else {
            //viewModel.insertZaznam(zaznam)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}