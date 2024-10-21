package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hruby.vcelnice.R

class EditDialogFragment : DialogFragment() {

    internal lateinit var listener: EditDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface.
        if (parentFragment is EditDialogListener) {
            listener = parentFragment as EditDialogListener
        } else if (context is EditDialogListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement EditDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        // Inflate a custom layout for the dialog
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_stanoviste_edit_dialog, null)

        // Get the data from the arguments
        val index = arguments?.getInt("index") ?: -1  // Získání ID
        val name = arguments?.getString("name") ?: ""
        val lastCheck = arguments?.getString("lastCheck") ?: ""
        val locationUrl = arguments?.getString("locationUrl") ?: ""

        // Populate dialog fields
        val editTextName = view.findViewById<EditText>(R.id.edit_text_name)
        val editTextLastCheck = view.findViewById<EditText>(R.id.edit_text_last_check)
        val editTextLocationUrl = view.findViewById<EditText>(R.id.edit_text_location_url)

        editTextName.setText(name)
        editTextLastCheck.setText(lastCheck)
        editTextLocationUrl.setText(locationUrl)

        builder.setView(view)
            .setTitle("Upravit stanoviště")
            .setPositiveButton("Uložit") { _, _ ->
                // Handle saving the edited data
                val newName = editTextName.text.toString()
                val newLastCheck = editTextLastCheck.text.toString()
                val newLocationUrl = editTextLocationUrl.text.toString()

                // Zavolání metody pro uložení změn
                // Zde zkontroluj, jestli listener není null
                listener.onDialogSave(index, newName, newLastCheck, newLocationUrl)

                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                Log.d("EditDialog", "Saving: ID: $index, Name: $newName, Last Check: $newLastCheck, Location URL: $newLocationUrl")
            }
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }

    // Interface for communicating with the parent fragment/activity
    interface EditDialogListener {
        fun onDialogSave(index: Int, name: String, lastCheck: String, locationUrl: String)
    }
}