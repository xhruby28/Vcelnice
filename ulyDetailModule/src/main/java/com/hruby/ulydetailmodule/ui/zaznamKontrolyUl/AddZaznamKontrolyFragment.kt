package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
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

        val rootLayout = view.findViewById<View>(R.id.add_zaznam_kontroly_main_layout)

        setDateToCurrent()

        binding.addUlKontrolaDatePicker.setOnClickListener {
            showDatePickerDialog()
            hideKeyboard(view)
        }

        // switch Ne/Ano logika
        setSwitchLogic(binding.switchKontrolaProblemovyUl, binding.textswitchKontrolaProblemovyUl) { isChecked ->
            binding.editTextKontrolaPopisProblemu.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                resetAllProblemCheckBoxes()
            }
        }

        setSwitchLogic(binding.switchZaznamMatkaVidena, binding.textswitchZaznamMatkaVidena)
        setSwitchLogic(binding.plodZavrenySwitch, binding.plodZavrenySwitchText)
        setSwitchLogic(binding.plodOtevrenySwitch, binding.plodOtevrenySwitchText)
        setSwitchLogic(binding.plodMednikSwitch, binding.plodMednikSwitchText)
        setSwitchLogic(binding.plodTrubcinaSwitch, binding.plodTrubcinaSwitchText)
        setSwitchLogic(binding.plodMatecnikSwitch, binding.plodMatecnikSwitchText)

        binding.editTextKontrolaPopisProblemu.visibility =
            if (binding.switchKontrolaProblemovyUl.isChecked) View.VISIBLE else View.GONE

        binding.addUlKontrolaButtonSave.setOnClickListener {
            saveZaznam()
        }

        binding.addUlKontrolaButtonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.editTextKontrolaPopisKontroly.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.editTextKontrolaPopisKontroly.maxLines = 5
            } else {
                binding.editTextKontrolaPopisKontroly.maxLines = 2
            }
        }

        binding.checkMatkaOznacena.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !binding.switchZaznamMatkaVidena.isChecked) {
                binding.switchZaznamMatkaVidena.isChecked = true
                binding.textswitchZaznamMatkaVidena.text = "Ano"
            }
        }

        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                findEditTexts(view).forEach { editText ->
                    if (editText.isFocused) {
                        val outRect = Rect()
                        editText.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            editText.clearFocus()
                            hideKeyboard(editText)
                        }
                    }
                }
                view.performClick() // Přidání tohoto volání
            }
            false
        }
    }

    private fun setSwitchLogic(switch: SwitchMaterial, label: TextView, onChange: ((Boolean) -> Unit)? = null) {
        label.text = if (switch.isChecked) "Ano" else "Ne"
        switch.setOnCheckedChangeListener { _, isChecked ->
            label.text = if (isChecked) "Ano" else "Ne"
            onChange?.invoke(isChecked)
        }
    }

    private fun findEditTexts(view: View): List<EditText> {
        val editTexts = mutableListOf<EditText>()
        if (view is EditText) {
            editTexts.add(view)
        }

        // Rekursivní hledání ve všech podview
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childView = view.getChildAt(i)
                editTexts.addAll(findEditTexts(childView))
            }
        }
        return editTexts
    }

    private fun saveZaznam() {
        val zaznam = ZaznamKontroly(
            ulId = ulId,
            datum = selectedDate,
            zaznamText = binding.editTextKontrolaPopisKontroly.text.toString(),
            problemovyUl = binding.switchKontrolaProblemovyUl.isChecked,
            problemMatkaNeni = binding.editTextKontrolaMatkaNeni.isChecked,
            problemMatkaMednik = binding.editTextKontrolaMatkaMednik.isChecked,
            problemMatkaNeklade = binding.editTextKontrolaMatkaNeklade.isChecked,
            problemMatkaVybehliMatecnik = binding.editTextKontrolaVybehlyMatecnik.isChecked,
            problemMatkaZvapenatelyPlod = binding.editTextKontrolaZvapenatelyPlod.isChecked,
            problemMatkaNosema = binding.editTextKontrolaNosema.isChecked,
            problemMatkaLoupezOtevrena = binding.editTextKontrolaLoupezOtevrena.isChecked,
            problemMatkaLoupezSkryta = binding.editTextKontrolaLoupezSkryta.isChecked,
            problemMatkaTrubcice = binding.editTextKontrolaTrubcice.isChecked,
            problemMatkaJine = binding.editTextKontrolaJine.isChecked,
            problemText = if (binding.editTextKontrolaJine.isChecked) {
                binding.editTextKontrolaJineText.text.toString()
            } else null,
            matkaVidena = binding.switchZaznamMatkaVidena.isChecked,
            zasahMatkaOznacena = binding.checkMatkaOznacena.isChecked,
            zasahKrmeni = binding.checkKrmeni.isChecked,
            zasahVlozenaMrizka = binding.checkMrizkaVlozena.isChecked,
            zasahOdebranaMrizka = binding.checkMrizkaOdebrana.isChecked,
            zasahMedobrani = binding.checkMedobrani.isChecked,
            zasahPripravaNaKrmeni = binding.checkPripravaKrmeni.isChecked,
            zasahZrusenoSpojenim = binding.checkZrusenoSpojenim.isChecked,
            zasahVycisteneDno = binding.checkCisteniDno.isChecked,
            zasahOskrabaneSteny = binding.checkOskrAbaneSteny.isChecked,
            nastavekAkce = binding.addUlKontrolaEditTextMedobraniRamky.selectedItem?.toString(),
            mezistenyPlodiste = parseSpinnerValue(binding.mezistenyPlodisteSpinner),
            souskyPlodiste = parseSpinnerValue(binding.sousePlodisteSpinner),
            mezistenyMednik = parseSpinnerValue(binding.mezistenyMednikSpinner),
            souskyMednik = parseSpinnerValue(binding.souseMednikSpinner),
            plodZavreny = binding.plodZavrenySwitch.isChecked,
            plodOtevreny = binding.plodOtevrenySwitch.isChecked,
            plodVMedniku = binding.plodMednikSwitch.isChecked,
            plodTrubcina = binding.plodTrubcinaSwitch.isChecked,
            plodMatecnik = binding.plodMatecnikSwitch.isChecked,
            // Hodnocení a další hodnoty mohou být doplněny později podle dalších komponent layoutu
        )

        viewModel.insertZaznam(zaznam)
        Toast.makeText(requireContext(), "Záznam uložen", Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun resetAllProblemCheckBoxes() {
        val checkBoxes = listOf(
            binding.editTextKontrolaMatkaNeni,
            binding.editTextKontrolaMatkaMednik,
            binding.editTextKontrolaMatkaNeklade,
            binding.editTextKontrolaVybehlyMatecnik,
            binding.editTextKontrolaZvapenatelyPlod,
            binding.editTextKontrolaNosema,
            binding.editTextKontrolaLoupezOtevrena,
            binding.editTextKontrolaLoupezSkryta,
            binding.editTextKontrolaTrubcice,
            binding.editTextKontrolaJine
        )
        checkBoxes.forEach { it.isChecked = false }
        binding.editTextKontrolaJineText.setText("")
    }

    private fun parseSpinnerValue(spinner: Spinner): Int {
        val selected = spinner.selectedItem?.toString() ?: return 0
        return selected.toIntOrNull() ?: 0
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
        binding.addUlKontrolaDatePicker.setText(formattedDate)
    }

    // Funkce pro zobrazení DatePickerDialog
    @SuppressLint("DefaultLocale")
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
                selectedDate = selectedCalendar.timeInMillis
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.addUlKontrolaDatePicker.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}