package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.hruby.vcelnice.R
import java.util.Locale

class EditDialogFragment : DialogFragment(), MapFragment.OnLocationSelectedListener {

    internal lateinit var listener: EditDialogListener
    private lateinit var editTextLocationUrl: EditText
    private var fragmentContainer: FrameLayout? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface.
        listener = if(parentFragment is EditDialogListener) {
            parentFragment as EditDialogListener
        } else if (context is EditDialogListener) {
            context
        } else {
            throw ClassCastException("$context must implement EditDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_stanoviste_edit_dialog, null)

        // Získání prvků z layoutu
        val editTextName = view.findViewById<EditText>(R.id.edit_text_name)
        val editTextLastCheck = view.findViewById<EditText>(R.id.edit_text_last_check)
        editTextLocationUrl = view.findViewById<EditText>(R.id.edit_text_location_url)
        val btnPickLocation = view.findViewById<Button>(R.id.button_select_map)
        val locationSpinner = view.findViewById<Spinner>(R.id.spinner_mode)

        // Nastavení výchozích hodnot
        val index = arguments?.getInt("index") ?: -1  // Získání ID
        editTextName.setText(arguments?.getString("name") ?: "")
        editTextLastCheck.setText(arguments?.getString("lastCheck") ?: "")
        editTextLocationUrl.setText(arguments?.getString("locationUrl") ?: "")

        // Skrytí fragment_containeru na začátku
        fragmentContainer = view.findViewById(R.id.fragment_container)
        fragmentContainer?.visibility = View.GONE


        // Listener pro výběr z rozbalovacího seznamu
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Zvoleno "Zadat URL"
                        editTextLocationUrl.visibility = View.VISIBLE
                        btnPickLocation.visibility = View.GONE
                    }
                    1 -> {
                        // Zvoleno "Vybrat na mapě"
                        editTextLocationUrl.visibility = View.GONE
                        btnPickLocation.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nic nezvoleno, výchozí je "Zadat URL"
                editTextLocationUrl.visibility = View.VISIBLE
                btnPickLocation.visibility = View.GONE
            }
        }

        // Kliknutí na "Vybrat na mapě" otevře mapu
        btnPickLocation.setOnClickListener {
            openMap()
        }

        builder.setView(view)
            .setTitle("Upravit stanoviště")
            .setPositiveButton("Uložit") { _, _ ->
                val newName = editTextName.text.toString()
                val newLastCheck = editTextLastCheck.text.toString()
                val newLocationUrl = editTextLocationUrl.text.toString()

                // Zavolání metody pro uložení změn
                listener.onDialogSave(index, newName, newLastCheck, newLocationUrl)

                // Skrýt klávesnici
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                Log.d("EditDialog", "Saving: ID: $index, Name: $newName, Last Check: $newLastCheck, Location URL: $newLocationUrl")
            }
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }

    // Otevření mapy jako child fragment
    private fun openMap() {
        // Zobrazení fragment_containeru
        fragmentContainer?.visibility = View.VISIBLE
        val mapFragment = MapFragment()

        // Nastavení listeneru pro MapFragment
        mapFragment.setOnLocationSelectedListener(this)

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    // Implementace metody z MapFragment
    override fun onLocationSelected(latLng: LatLng) {
        // Použití NumberFormat k zajištění formátování s tečkami
        val latitude = String.format(Locale.US, "%.6f", latLng.latitude)
        val longitude = String.format(Locale.US, "%.6f", latLng.longitude)
        val locationUrl = "https://maps.google.com/?q=$latitude,$longitude"
        editTextLocationUrl.setText(locationUrl)
        fragmentContainer?.visibility = View.GONE
    }

    interface EditDialogListener {
        fun onDialogSave(index: Int, name: String, lastCheck: String, locationUrl: String)
    }
}