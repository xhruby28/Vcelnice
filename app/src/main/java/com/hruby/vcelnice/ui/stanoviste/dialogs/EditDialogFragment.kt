package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.hruby.vcelnice.R
import java.util.Calendar
import java.util.Locale

class EditDialogFragment : DialogFragment(), MapFragment.OnLocationSelectedListener {

    internal lateinit var listener: EditDialogListener
    private lateinit var editTextLocationUrl: EditText
    private lateinit var editTextLastCheck: EditText
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
//        TODO("Tato funkce bude v následující verzi (pravděpodobně až v lednu 2025) využívaná jenom pro vytvoření stanoviště" +
//                "a úprava bude nahrazena komplexnejší verzí, kdy aplikace uživatele" +
//                "přesune přímo do StanovisteDetailModule a zde bude moc editovat hodnoty")
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_stanoviste_edit_dialog, null)

        // Získání prvků z layoutu
        val editTextName = view.findViewById<EditText>(R.id.edit_text_name)
        editTextLastCheck = view.findViewById<EditText>(R.id.edit_text_last_check)
        editTextLocationUrl = view.findViewById<EditText>(R.id.edit_text_location_url)
        val btnPickLocation = view.findViewById<Button>(R.id.button_select_map)
        val locationSpinner = view.findViewById<Spinner>(R.id.spinner_mode)

        // Nastavení výchozích hodnot
        val index = arguments?.getInt("index") ?: -1  // Získání ID
        editTextName.setText(arguments?.getString("name") ?: "")
        editTextLastCheck.setText(arguments?.getString("lastCheck") ?: "")
        editTextLocationUrl.setText(arguments?.getString("locationUrl") ?: "")
        var macAdress = arguments?.getString("macAddress") ?: ""
        val maMac = requireArguments().getBoolean("maMac")

        // Otevření kalendáře při kliknutí na editTextLastCheck
        editTextLastCheck.setOnClickListener {
            showDatePickerDialog()
            hideKeyboard(view)
        }

        // Skrytí fragment_containeru na začátku
        fragmentContainer = view.findViewById(R.id.fragment_container)
        fragmentContainer?.visibility = View.GONE


        // Listener pro výběr z rozbalovacího seznamu
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Zvoleno "Vybrat na mapě"
                        editTextLocationUrl.visibility = View.GONE
                        btnPickLocation.visibility = View.VISIBLE
                    }
                    1 -> {
                        // Zvoleno "Zadat URL"
                        editTextLocationUrl.visibility = View.VISIBLE
                        btnPickLocation.visibility = View.GONE
                        fragmentContainer?.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nic nezvoleno, výchozí je "Vybrat na mapě"
                editTextLocationUrl.visibility = View.GONE
                btnPickLocation.visibility = View.VISIBLE
            }
        }



        // Kliknutí na "Vybrat na mapě" otevře mapu
        btnPickLocation.setOnClickListener {
            hideKeyboard(view)
            openMap()
        }

        builder.setView(view)
            .setTitle("Upravit stanoviště")
            .setPositiveButton("Uložit") { _, _ ->
                val newName = editTextName.text.toString()
                val newLastCheck = editTextLastCheck.text.toString()
                val newLocationUrl = editTextLocationUrl.text.toString()

                // Zavolání metody pro uložení změn
                listener.onDialogSave(maMac, macAdress, index, newName, newLastCheck, newLocationUrl)

                // Skrýt klávesnici
                hideKeyboard(view)
                Log.d("EditDialog", "Saving: maMAC: $maMac, MAC: $macAdress, ID: $index, Name: $newName, Last Check: $newLastCheck, Location URL: $newLocationUrl")
            }
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }

    // Otevření mapy jako child fragment
    private fun openMap() {
        Toast.makeText(context, "Vyberte místo na mapě dlouhým stisknutím", Toast.LENGTH_SHORT).show()

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
        //fragmentContainer?.visibility = View.GONE
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Zobrazit vybrané datum ve formátu DD-MM-YYYY
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                editTextLastCheck.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    interface EditDialogListener {
        fun onDialogSave(maMac: Boolean, macAddress: String, index: Int, name: String, lastCheck: String, locationUrl: String)
    }
}