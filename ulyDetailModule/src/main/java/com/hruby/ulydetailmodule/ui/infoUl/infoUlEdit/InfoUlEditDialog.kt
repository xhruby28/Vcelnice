package com.hruby.ulydetailmodule.ui.infoUl.infoUlEdit

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hruby.databasemodule.data.Uly
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentAddZaznamKontrolyBinding
import com.hruby.ulydetailmodule.databinding.FragmentInfoUlEditDialogBinding
import java.util.Calendar

class InfoUlEditDialog : DialogFragment() {
    internal lateinit var listener: EditUlEditDialogListener

    private var matkaRokPick: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface.
        listener = if(parentFragment is EditUlEditDialogListener) {
            parentFragment as EditUlEditDialogListener
        } else if (context is EditUlEditDialogListener) {
            context
        } else {
            throw ClassCastException("$context must implement EditUlEditDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_info_ul_edit_dialog, null)

        val rootLayout = view.findViewById<View>(R.id.editUlDialogRoot)

        val ulCislo = view.findViewById<EditText>(R.id.editTextCisloUlu)
        val ulPopis = view.findViewById<EditText>(R.id.editTextPopisUlu)
        val ulMatkaBarva = view.findViewById<Spinner>(R.id.ul_edit_info_barva_spinner)
        val ulMatkaRok = view.findViewById<EditText>(R.id.ul_edit_info_rok_edit_text)
        val ulMatkaKridla = view.findViewById<SwitchMaterial>(R.id.ul_edit_info_kridla_switch)
        val ulMatkaKridlaText = view.findViewById<TextView>(R.id.ul_edit_info_kridla_switch_text)

        // Nastaveni hodnot
        val ul: Uly? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("ul", Uly::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("ul") as? Uly
        }

        val ulIndex = ul?.id
        val ulStanovisteIndex = ul?.stanovisteId
        val getUlNumber = ul?.cisloUlu
        val getUlDesc = ul?.popis
        val barvy = resources.getStringArray(R.array.barva_oznaceni_kralovny)

        Log.d("InfoUlEditDialog", "UlId: $ulIndex, StanovisteId? $ulStanovisteIndex, UlNum: $getUlNumber, UlDesc: $getUlDesc")

        ulCislo.setText(getUlNumber?.toString() ?: "")
        ulPopis.setText(getUlDesc ?: "")
        ul?.matkaBarva?.let { barva ->
            val index = barvy.indexOf(barva)
            if (index >= 0) ulMatkaBarva.setSelection(index)
        }
        ulMatkaRok.setText(ul?.matkaRok ?: "")
        ulMatkaKridla.isChecked = ul?.matkaKridla == true
        ulMatkaKridlaText.text = if (ulMatkaKridla.isChecked) "Ano" else "Ne"

        /*ulMatkaBarva.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Vybrána položka "- - - - - -", nedělej nic
                    return
                }

                val selectedBarva = barvy[position]  // Tady bude např. "Modrá" pro position=1

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                // Mapa barev podle indexů od 1 (protože pozice 0 je placeholder)
                val barvaRokMap = mapOf(
                    "Modrá" to 1,
                    "Bílá" to 2,
                    "Žlutá" to 3,
                    "Červená" to 4,
                    "Zelená" to 5
                )

                barvaRokMap[selectedBarva]?.let { barvaOffset ->
                    if ((currentYear % 5) == barvaOffset) {
                        ulMatkaRok.setText(currentYear.toString())
                        matkaRokPick = currentYear
                    } else {
                        // Můžeš třeba smazat pole pokud neodpovídá barvě
                        ulMatkaRok.setText("")
                        matkaRokPick = -1
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }*/

        ulMatkaKridla.setOnCheckedChangeListener { _, isChecked ->
            ulMatkaKridlaText.text = if (isChecked) "Ano" else "Ne"
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

        ulMatkaRok.setOnClickListener {
            showYearPickerDialog(ulMatkaRok)
        }

        builder.setView(view)
            .setTitle("Upravit úl")
            .setPositiveButton("Uložit", null)
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newCislo = ulCislo.text.toString().toIntOrNull()
                val newPopis = ulPopis.text.toString()

                if (newCislo == null) {
                    ulCislo.error = "Zadej platné číslo"
                    return@setOnClickListener
                }

                val barva = ulMatkaBarva.selectedItem?.toString() ?: ""
                val rok = ulMatkaRok.text?.toString() ?: ""
                val zk = if (ulMatkaKridla.isChecked) "křídla zastřižená" else "křídla nezastřižená"

                val oznaceni = if (barva != "- - - - -" && rok.isNotEmpty()) {
                    "$barva, $rok, $zk"
                } else {
                    null
                }

                val updatedUl = ul?.copy(
                    cisloUlu = newCislo,
                    popis = newPopis,
                    matkaBarva = if (barva != "- - - - -") barva else null,
                    matkaRok = rok.ifEmpty { null },
                    matkaKridla = ulMatkaKridla.isChecked,
                    matkaOznaceni = oznaceni
                )

                if (updatedUl != null) {
                    listener.onUlUpdate(updatedUl)
                    Toast.makeText(requireContext(), "Ukládám...", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        return dialog
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

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showYearPickerDialog(editText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.year_picker)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        numberPicker.minValue = 2000
        numberPicker.maxValue = currentYear + 5
        numberPicker.value = currentYear

        AlertDialog.Builder(requireContext())
            .setTitle("Vyberte rok značení matky")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = numberPicker.value
                matkaRokPick = selectedYear
                editText.setText(selectedYear.toString())
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface EditUlEditDialogListener {
        fun onUlUpdate(updatedStanoviste: Uly)
    }
}