package com.hruby.stanovistedetailmodule.ui.infoStanoviste.editDialog

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.sharedresources.helpers.BluetoothHelper
import com.hruby.sharedresources.helpers.PermissionHelper
import com.hruby.stanovistedetailmodule.R
import com.skydoves.expandablelayout.ExpandableLayout
import java.util.Locale

class EditStanovisteDialogFragment : DialogFragment(), StanovisteEditMapFragment.OnLocationSelectedListener  {

    internal lateinit var listener: EditStanovisteDialogListener
    private lateinit var editTextLocationUrl: EditText
    private var fragmentContainer: FrameLayout? = null

    private lateinit var connectDevice: Button
    private lateinit var connectionReset: Button

    private var maMac: Boolean? = false
    private var macAdress: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface.
        listener = if(parentFragment is EditStanovisteDialogListener) {
            parentFragment as EditStanovisteDialogListener
        } else if (context is EditStanovisteDialogListener) {
            context
        } else {
            throw ClassCastException("$context must implement EditStanovisteDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_stanoviste, null)

        val ex2_main = view.findViewById<LinearLayout>(R.id.expandable_sms_main)

        val ex1 = view.findViewById<ExpandableLayout>(R.id.expandable_info)
        val ex2 = view.findViewById<ExpandableLayout>(R.id.expandable_sms)
        val ex3 = view.findViewById<ExpandableLayout>(R.id.expandable_others)

        val infoLayout = ex1.secondLayout
        val smsLayout = ex2.secondLayout
        val otherLayout = ex3.secondLayout

        val rootLayout = view.findViewById<View>(R.id.editStanovisteDialogRoot)

        // Info layout
        val stanovisteName = infoLayout.findViewById<EditText>(R.id.edit_stanoviste_editTextName)
        val stanovisteDesc = infoLayout.findViewById<EditText>(R.id.edit_stanoviste_editTextDescription)
        val locationSpinner = infoLayout.findViewById<Spinner>(R.id.edit_stanoviste_location_spinner)
        val btnPickLocation = infoLayout.findViewById<Button>(R.id.edit_stanoviste_button_select_map)
        editTextLocationUrl = infoLayout.findViewById<EditText>(R.id.edit_stanoviste_text_location_url)

        // SMS layout
        val switchSms = smsLayout.findViewById<SwitchMaterial>(R.id.switchSmsNotifications)
        val spinnerCountryCode = smsLayout.findViewById<Spinner>(R.id.spinnerCountryCode)
        val editPhone = smsLayout.findViewById<EditText>(R.id.editTextPhoneNumber)
        val switchSimPin = smsLayout.findViewById<SwitchMaterial>(R.id.switchSimPin)
        val editPin = smsLayout.findViewById<EditText>(R.id.editTextPin)

        // Other Layout
        val isConnected = otherLayout.findViewById<TextView>(R.id.tvPairingStatus)
        connectDevice = otherLayout.findViewById<Button>(R.id.btnPairDevice)
        connectionReset = otherLayout.findViewById<Button>(R.id.btnResetConnection)

        // Nastaveni hodnot
        val stanoviste: Stanoviste? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("stanoviste", Stanoviste::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("stanoviste") as? Stanoviste
        }

        // Info
        val index = stanoviste?.id
        val getStanovisteName = stanoviste?.name
        val getStanovisteDesc = stanoviste?.description
        val getStanovisteLocation = stanoviste?.locationUrl
        stanovisteName.setText(getStanovisteName)
        stanovisteDesc.setText(getStanovisteDesc)
        editTextLocationUrl.setText(getStanovisteLocation)

        // SMS
        val smsNotifications = stanoviste?.notificationsEnabled
        switchSms.isChecked = smsNotifications == true

        val getPhoneNumber = stanoviste?.notificationPhoneNumber
        getPhoneNumber?.let { phoneFull ->
            if (phoneFull.length >= 4) {
                val countryCode = phoneFull.take(4)
                val phoneNumber = phoneFull.drop(4)
                val countryCodes = resources.getStringArray(R.array.country_codes)
                val prefixPosition = countryCodes.indexOf(countryCode)

                if (prefixPosition >= 0) {
                    spinnerCountryCode.setSelection(prefixPosition)
                }

                editPhone.setText(phoneNumber)
            }
        }

        val isPinEnabled = stanoviste?.isPin
        switchSimPin.isChecked = isPinEnabled == true

        val getPin = stanoviste?.hashedPin
        if (!getPin.isNullOrEmpty()) {
            editPin.setText(getPin)
        }

        // Others
        maMac = stanoviste?.maMAC
        macAdress = stanoviste?.siteMAC

        if (maMac == true) {
            ex2_main.visibility = View.VISIBLE
            isConnected.text = "Stanoviště je spárované"
            connectDevice.visibility = View.GONE
            connectionReset.visibility = View.VISIBLE
            maMac = true
        } else {
            ex2_main.visibility = View.GONE
            isConnected.text = "Stanoviště není spárované"
            connectDevice.visibility = View.VISIBLE
            connectionReset.visibility = View.GONE
            maMac = false
        }

        switchSms?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spinnerCountryCode.visibility = View.VISIBLE
                editPhone.visibility = View.VISIBLE
                switchSimPin.visibility = View.VISIBLE
                if(switchSimPin.isChecked){
                    editPin.visibility = View.VISIBLE
                }
            } else {
                spinnerCountryCode.visibility = View.GONE
                editPhone.visibility = View.GONE
                switchSimPin.visibility = View.GONE
                editPin.visibility = View.GONE
            }
        }

        switchSimPin?.setOnCheckedChangeListener { _, isChecked ->
            editPin.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        ex1.parentLayout.setOnClickListener {
            ex1.toggleLayout()
            ex1.requestLayout()
        }

        ex2.parentLayout.setOnClickListener {
            ex2.toggleLayout()
            if(switchSms.isChecked){
                spinnerCountryCode.visibility = View.VISIBLE
                editPhone.visibility = View.VISIBLE
                switchSimPin.visibility = View.VISIBLE
                if(switchSimPin.isChecked){
                    editPin.visibility = View.VISIBLE
                } else {
                    editPin.visibility = View.GONE
                }
            } else {
                spinnerCountryCode.visibility = View.GONE
                editPhone.visibility = View.GONE
                switchSimPin.visibility = View.GONE
                editPin.visibility = View.GONE
            }
            ex2.requestLayout()
        }

        ex3.parentLayout.setOnClickListener {
            ex3.toggleLayout()
            ex3.requestLayout()
        }

        // Skrytí fragment_containeru na začátku
        fragmentContainer = infoLayout.findViewById(R.id.edit_stanoviste_fragment_container)
        fragmentContainer?.visibility = View.VISIBLE

        // Listener pro výběr z rozbalovacího seznamu
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // Zvoleno "Vybrat na mapě"
                        editTextLocationUrl.visibility = View.GONE
                        btnPickLocation.visibility = View.VISIBLE
                        fragmentContainer?.visibility = View.VISIBLE
                        ex2.collapse()
                        ex3.collapse()
                    }
                    1 -> {
                        // Zvoleno "Zadat URL"
                        editTextLocationUrl.visibility = View.VISIBLE
                        btnPickLocation.visibility = View.GONE
                        fragmentContainer?.visibility = View.GONE
                        ex2.collapse()
                        ex3.collapse()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nic nezvoleno, výchozí je "Vybrat na mapě"
                editTextLocationUrl.visibility = View.GONE
                btnPickLocation.visibility = View.VISIBLE
            }
        }

        stanovisteDesc.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                stanovisteDesc.maxLines = 8
            } else {
                stanovisteDesc.maxLines = 2
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

        // Kliknutí na "Vybrat na mapě" otevře mapu
        btnPickLocation.setOnClickListener {
            hideKeyboard(view)
            openMap()
        }

        connectDevice.setOnClickListener {
            val bluetoothEnabled = BluetoothHelper.isBluetoothEnabled()
            if (bluetoothEnabled) {
                checkBluetoothPermissions()
            } else {
                Toast.makeText(requireContext(), "Zapněte Bluetooth pro párování.", Toast.LENGTH_SHORT).show()
            }
        }

        connectionReset.setOnClickListener {
            maMac = false
            macAdress = null
            connectDevice.visibility = View.VISIBLE
            connectionReset.visibility = View.GONE
            isConnected.text = "Stanoviště není spárované"
            Toast.makeText(requireContext(), "Párování resetováno", Toast.LENGTH_SHORT).show()
        }

        builder.setView(view)
            .setTitle("Upravit stanoviště")
            .setPositiveButton("Uložit", null)
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newName = stanovisteName.text.toString()
                val newDesc = stanovisteDesc.text.toString()
                val newLocation = editTextLocationUrl.text.toString()
                val newSmsNotification = switchSms.isChecked
                val prefix = spinnerCountryCode.selectedItem.toString()
                val phoneNumber = editPhone.text.toString().trim()
                var phone = prefix + phoneNumber
                var simPin = switchSimPin.isChecked
                var pin = editPin.text.toString().trim()

                if (newSmsNotification) {
                    if (phoneNumber.length != 9 || !phoneNumber.matches(Regex("\\d+"))) {
                        Toast.makeText(requireContext(), "Telefonní číslo musí mít 9 číslic.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                } else {
                    phone = ""
                }

                if (simPin) {
                    if (pin.length !in 4..8) {
                        Toast.makeText(requireContext(), "PIN musí mít mezi 4 a 8 znaky.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                } else {
                    pin = ""
                }

                if (!newSmsNotification){
                    simPin = false;
                    pin = ""
                }

                val updatedStanoviste = Stanoviste(
                    id = index ?: -1,
                    name = newName,
                    maMAC = maMac == true,
                    siteMAC = macAdress,
                    lastCheck = stanoviste?.lastCheck,
                    locationUrl = newLocation,
                    lastState = stanoviste?.lastState,
                    imagePath = stanoviste?.imagePath,
                    notificationsEnabled = newSmsNotification,
                    notificationPhoneNumber = phone,
                    isPin = simPin,
                    hashedPin = pin,
                    description = newDesc,
                )

                listener.onStanovisteUpdated(updatedStanoviste)
                Toast.makeText(requireContext(), "Ukládám...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        return dialog
    }

    // Otevření mapy jako child fragment
    private fun openMap() {
        Toast.makeText(context, "Vyberte místo na mapě dlouhým stisknutím", Toast.LENGTH_SHORT).show()

        // Zobrazení fragment_containeru
        fragmentContainer?.visibility = View.VISIBLE
        val mapFragment = StanovisteEditMapFragment()

        // Nastavení listeneru pro MapFragment
        mapFragment.setOnLocationSelectedListener(this)

        childFragmentManager.beginTransaction()
            .replace(R.id.edit_stanoviste_fragment_container, mapFragment)
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

    private // Skrytí klávesnice
    fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val requiredPermissions = PermissionHelper.bluetoothPermissions.toSet()
            val grantedPermissions = permissions.filterValues { it }.keys

            if (requiredPermissions.all { it in grantedPermissions }) {
                requestBluetoothDevices()
            } else {
                Toast.makeText(context, "Bluetooth oprávnění nebyla udělena", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkBluetoothPermissions() {
        val bluetoothPermissions = PermissionHelper.bluetoothPermissions

        if (bluetoothPermissions.isNotEmpty()) {
            val missingPermissions = bluetoothPermissions.filter {
                ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                requestBluetoothDevices()
            }
        } else {
            requestBluetoothDevices()
        }
    }

    private fun requestBluetoothDevices() {
        BluetoothHelper.requestBluetoothDevices(
            context = requireContext(),
            fragmentManager = childFragmentManager,
            onDeviceSelected = { newMacAddress ->
                if (BluetoothHelper.isEsp32Device(newMacAddress)) {
                    maMac = true
                    connectDevice.visibility = View.GONE
                    connectionReset.visibility = View.VISIBLE
                    macAdress = newMacAddress
                } else {
                    Toast.makeText(context, "Neplatné ESP32 zařízení", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface EditStanovisteDialogListener {
        fun onStanovisteUpdated(updatedStanoviste: Stanoviste)
    }
}