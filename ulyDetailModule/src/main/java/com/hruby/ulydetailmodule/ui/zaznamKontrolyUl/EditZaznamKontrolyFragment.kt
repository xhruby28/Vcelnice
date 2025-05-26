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
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.ZaznamKontrolyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentAddZaznamKontrolyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditZaznamKontrolyFragment : Fragment() {

    private var _binding: FragmentAddZaznamKontrolyBinding? = null
    private val binding get() = _binding!!

    private lateinit var zaznam: ZaznamKontroly

    private lateinit var viewModel: ZaznamKontrolyViewModel

    private var zaznamId: Int = -1
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

        zaznamId = arguments?.getInt("zaznamId") ?: -1
        ulId = arguments?.getInt("ulId") ?: -1

        val db = StanovisteDatabase.getDatabase(requireContext().applicationContext)
        val repository = ZaznamKontrolyRepository(db)
        val factory = ZaznamKontrolyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ZaznamKontrolyViewModel::class.java]

        val rootLayout = view.findViewById<View>(R.id.add_zaznam_kontroly_main_layout)

        viewModel.getZaznamByIdAndUlId(zaznamId, ulId).observe(viewLifecycleOwner) { zaznamOld ->
            if (zaznamOld != null) {
                zaznam = zaznamOld
                naplnFormular(zaznam, view)
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

    private fun naplnFormular(zaznam: ZaznamKontroly, view: View) {
        // Datum
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        binding.addUlKontrolaDatePicker.setText(dateFormat.format(Date(zaznam.datum)))
        selectedDate = zaznam.datum

        binding.addUlKontrolaDatePicker.setOnClickListener {
            showDatePickerDialog()
            hideKeyboard(view)
        }

        // Popis
        binding.editTextKontrolaPopisKontroly.setText(zaznam.zaznamText ?: "")
        binding.editTextKontrolaPopisKontroly.setOnFocusChangeListener { _, hasFocus ->
            binding.editTextKontrolaPopisKontroly.maxLines = if (hasFocus) 5 else 2
        }

        // Switch pro problémový úl
        binding.switchKontrolaProblemovyUl.isChecked = zaznam.problemovyUl
        setSwitchLogic(binding.switchKontrolaProblemovyUl, binding.textswitchKontrolaProblemovyUl) { isChecked ->
            binding.editTextKontrolaPopisProblemu.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                resetAllProblemCheckBoxes()
            }
        }
        binding.editTextKontrolaPopisProblemu.visibility =
            if (zaznam.problemovyUl) View.VISIBLE else View.GONE

        // Problémy
        binding.editTextKontrolaMatkaNeni.isChecked = zaznam.problemMatkaNeni == true
        binding.editTextKontrolaMatkaMednik.isChecked = zaznam.problemMatkaMednik == true
        binding.editTextKontrolaMatkaNeklade.isChecked = zaznam.problemMatkaNeklade == true
        binding.editTextKontrolaVybehlyMatecnik.isChecked = zaznam.problemMatkaVybehliMatecnik == true
        binding.editTextKontrolaZvapenatelyPlod.isChecked = zaznam.problemMatkaZvapenatelyPlod == true
        binding.editTextKontrolaNosema.isChecked = zaznam.problemMatkaNosema == true
        binding.editTextKontrolaLoupezOtevrena.isChecked = zaznam.problemMatkaLoupezOtevrena == true
        binding.editTextKontrolaLoupezSkryta.isChecked = zaznam.problemMatkaLoupezSkryta == true
        binding.editTextKontrolaTrubcice.isChecked = zaznam.problemMatkaTrubcice == true
        binding.editTextKontrolaJine.isChecked = zaznam.problemMatkaJine == true
        binding.editTextKontrolaJineText.setText(zaznam.problemText ?: "")

        // Matka
        binding.switchZaznamMatkaVidena.isChecked = zaznam.matkaVidena
        setSwitchLogic(binding.switchZaznamMatkaVidena, binding.textswitchZaznamMatkaVidena)

        // Logika označení matky
        binding.checkMatkaOznacena.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !binding.switchZaznamMatkaVidena.isChecked) {
                binding.switchZaznamMatkaVidena.isChecked = true
                binding.textswitchZaznamMatkaVidena.text = "Ano"
            }
        }

        // Zásahy
        binding.checkMatkaOznacena.isChecked = zaznam.zasahMatkaOznacena
        binding.checkKrmeni.isChecked = zaznam.zasahKrmeni
        binding.checkPripravaKrmeni.isChecked = zaznam.zasahPripravaNaKrmeni
        binding.checkMrizkaVlozena.isChecked = zaznam.zasahVlozenaMrizka
        binding.checkMrizkaOdebrana.isChecked = zaznam.zasahOdebranaMrizka
        binding.checkMedobrani.isChecked = zaznam.zasahMedobrani
        binding.checkZrusenoSpojenim.isChecked = zaznam.zasahZrusenoSpojenim
        binding.checkCisteniDno.isChecked = zaznam.zasahVycisteneDno
        binding.checkOskrAbaneSteny.isChecked = zaznam.zasahOskrabaneSteny

        // Spinner pro nástavek
        zaznam.nastavekAkce?.let { akce ->
            val adapter = binding.addUlKontrolaEditTextMedobraniRamky.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == akce) {
                    binding.addUlKontrolaEditTextMedobraniRamky.setSelection(i)
                    break
                }
            }
        }

        // Mezistěny a souše
        binding.mezistenyPlodisteSpinner.setSelection(zaznam.mezistenyPlodiste + 10)
        binding.sousePlodisteSpinner.setSelection(zaznam.souskyPlodiste + 10)
        binding.mezistenyMednikSpinner.setSelection(zaznam.mezistenyMednik + 10)
        binding.souseMednikSpinner.setSelection(zaznam.souskyMednik + 10)

        // Plod
        binding.plodZavrenySwitch.isChecked = zaznam.plodZavreny
        setSwitchLogic(binding.plodZavrenySwitch, binding.plodZavrenySwitchText)
        binding.plodOtevrenySwitch.isChecked = zaznam.plodOtevreny
        setSwitchLogic(binding.plodOtevrenySwitch, binding.plodOtevrenySwitchText)
        binding.plodMednikSwitch.isChecked = zaznam.plodVMedniku
        setSwitchLogic(binding.plodMednikSwitch, binding.plodMednikSwitchText)
        binding.plodTrubcinaSwitch.isChecked = zaznam.plodTrubcina
        setSwitchLogic(binding.plodTrubcinaSwitch, binding.plodTrubcinaSwitchText)
        binding.plodMatecnikSwitch.isChecked = zaznam.plodMatecnik
        setSwitchLogic(binding.plodMatecnikSwitch, binding.plodMatecnikSwitchText)

        // Tlačítka
        binding.addUlKontrolaButtonSave.setOnClickListener {
            saveZaznam()
        }
        binding.addUlKontrolaButtonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
                }
                selectedDate = selectedCalendar.timeInMillis
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.addUlKontrolaDatePicker.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun saveZaznam() {
        val updated = zaznam.copy(
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
            problemText = if (binding.editTextKontrolaJine.isChecked) binding.editTextKontrolaJineText.text.toString() else null,

            matkaVidena = binding.switchZaznamMatkaVidena.isChecked,
            zasahMatkaOznacena = binding.checkMatkaOznacena.isChecked,
            zasahKrmeni = binding.checkKrmeni.isChecked,
            zasahPripravaNaKrmeni = binding.checkPripravaKrmeni.isChecked,
            zasahVlozenaMrizka = binding.checkMrizkaVlozena.isChecked,
            zasahOdebranaMrizka = binding.checkMrizkaOdebrana.isChecked,
            zasahMedobrani = binding.checkMedobrani.isChecked,
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
            plodMatecnik = binding.plodMatecnikSwitch.isChecked
        )

        viewModel.updateZaznam(updated)
        Toast.makeText(requireContext(), "Záznam upraven", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun parseSpinnerValue(spinner: Spinner): Int {
        val selected = spinner.selectedItem?.toString() ?: return 0
        return selected.toIntOrNull() ?: 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}