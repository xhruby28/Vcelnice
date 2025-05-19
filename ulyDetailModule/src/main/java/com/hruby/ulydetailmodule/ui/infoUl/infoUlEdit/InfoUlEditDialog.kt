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
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hruby.databasemodule.data.Uly
import com.hruby.ulydetailmodule.R

class InfoUlEditDialog : DialogFragment() {
    internal lateinit var listener: EditUlEditDialogListener

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

        Log.d("InfoUlEditDialog", "UlId: $ulIndex, StanovisteId? $ulStanovisteIndex, UlNum: $getUlNumber, UlDesc: $getUlDesc")

        ulCislo.setText(getUlNumber?.toString() ?: "")
        ulPopis.setText(getUlDesc ?: "")

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

                val updatedUl = ul?.copy(
                    cisloUlu = newCislo,
                    popis = newPopis
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface EditUlEditDialogListener {
        fun onUlUpdate(updatedStanoviste: Uly)
    }
}