package com.hruby.stanovistedetailmodule.ui.ulyStanoviste.ulyDialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hruby.stanovistedetailmodule.R

class UlyCreateDialogFragment : DialogFragment() {
    internal lateinit var listener: UlCreateDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if(parentFragment is UlCreateDialogListener) {
            parentFragment as UlCreateDialogListener
        } else if (context is UlCreateDialogListener) {
            context
        } else {
            throw ClassCastException("$context must implement EditDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_uly_create_dialog, null)

        val textNumUl = view.findViewById<EditText>(R.id.uly_create_ul_number)
        val textDescUl = view.findViewById<EditText>(R.id.uly_create_ul_description)

        val index = arguments?.getInt("index") ?: -1  // Získání ID
        //textNumUl.setText(arguments?.getInt("cisloUlu").toString() ?: null)
        textDescUl.setText(arguments?.getString("popis") ?: "")

        builder.setView(view)
            .setTitle("Upravit stanoviště")
            .setPositiveButton("Uložit") { _, _ ->
                val newNumUl = textNumUl.text.toString()
                val newDescUl = textDescUl.text.toString()

                if (newNumUl.isEmpty()){
                    Toast.makeText(context, "Číslo úlu musí být vyplněné", Toast.LENGTH_SHORT).show()
                } else{
                    // Zavolání metody pro uložení změn
                    listener.onDialogSave(index, newNumUl.toInt(), newDescUl)
                    Log.d("EditDialog", "Saving: ID: $index, UlNum: $newNumUl, UlDesc: $newDescUl")
                }

                // Skrýt klávesnici
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
            }
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }


    interface UlCreateDialogListener {
        fun onDialogSave(index: Int, numUl: Int, descUl: String)
    }
}