package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.viewModel.MereneHodnotyViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.sharedresources.helpers.BluetoothHelper
import com.hruby.sharedresources.helpers.ImageHelper
import com.hruby.sharedresources.helpers.PermissionHelper
import com.hruby.sharedresources.helpers.WiFiHelper
import com.hruby.stanovistedetailmodule.databinding.FragmentInfoStanovisteBinding
import com.hruby.stanovistedetailmodule.R
import com.hruby.stanovistedetailmodule.ui.infoStanoviste.editDialog.EditStanovisteDialogFragment
import com.squareup.picasso.Picasso
import java.io.File

class InfoStanovisteFragment : Fragment(), EditStanovisteDialogFragment.EditStanovisteDialogListener {
    private var _binding: FragmentInfoStanovisteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InfoStanovisteViewModel
    private var stanovisteId: Int = -1

    private var isStanovisteInfoFabMenuOpen = false

    private var currentPhotoUri: Uri? = null
    private var previousImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        if (stanovisteId != -1) {
            // Inicializace ViewModelu s ID stanoviště
            val factory = InfoStanovisteViewModelFactory(requireContext(), stanovisteId)
            viewModel = ViewModelProvider(this, factory)[InfoStanovisteViewModel::class.java]
        } else {
            throw IllegalArgumentException("StanovisteId není validní.")
        }

        Log.d("UlyStanovisteFragment", "stanoviste id: $stanovisteId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoStanovisteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.stanoviste.observe(viewLifecycleOwner) { stanoviste ->
            val fab: FloatingActionButton = view.findViewById(R.id.stanoviste_detail_info_fragment_fab)

            infoStanovisteFragmentText()

            fab.setOnClickListener {
                toggleStanovisteInfoFabMenu(fab, stanoviste)
            }

            binding.stanovisteDetailInfoFragmentFabSync.setOnClickListener {
                if (stanoviste.maMAC) {
                    checkPermissions()
                } else {
                    Toast.makeText(
                        context,
                        "Stanoviště není propojené, propojení lze vytvořit pomocí UPRAVIT",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.stanovisteDetailInfoFragmentFabEdit.setOnClickListener {
                val dialog = EditStanovisteDialogFragment()
                val bundle = Bundle()
                bundle.putSerializable("stanoviste", stanoviste)
                dialog.arguments = bundle
                dialog.show(childFragmentManager, "EditStanovisteDialogFragment")
            }

            binding.stanovisteDetailInfoFragmentFabPic.setOnClickListener {
                    showImageSelectionDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Funkce pro fungování fragmentu a jeho částí
    private fun infoStanovisteFragmentText(){
        viewModel.stanoviste.observe(viewLifecycleOwner) { stanoviste ->
            binding.stanovisteDetailInfoFragmentNazevStanoviste.text = stanoviste.name
            binding.stanovisteDetailInfoFragmentStanovisteLastCheck.text = stanoviste.lastCheck
            binding.stanovisteDetailInfoFragmentStanovisteLastState.text = stanoviste.lastState
            binding.stanovisteDetailInfoFragmentDetailStanoviste.text = stanoviste.description

            val stringURL: String = stanoviste.locationUrl.toString()
            val arrayGPS: List<String> = stringURL.split("=")

            var gpsCoordinates = if (arrayGPS.size > 1 && arrayGPS[1].isNotEmpty()) {
                arrayGPS[1]
            } else {
                "NULL, NULL"
            }

            binding.stanovisteDetailInfoFragmentStanovisteGps.text = gpsCoordinates

            if (stanoviste.maMAC) {
                binding.stanovisteDetailInfoFragmentStanovisteMaMac.text = stanoviste.siteMAC
            } else {
                binding.stanovisteDetailInfoFragmentStanovisteMaMac.text = String.format("Není spárované")
            }

            viewModel.ulyCount.observe(viewLifecycleOwner) { count ->
                binding.stanovisteDetailInfoFragmentStanovistePocetUlu.text = String.format("$count")
            }

            binding.stanovisteDetailInfoFragmentStanovisteGps.setOnClickListener {
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(stanoviste.locationUrl))
                mapIntent.setPackage("com.google.android.apps.maps")
                it.context.startActivity(mapIntent)
            }

//            TODO("Zde přidat další informace stanoviště pro Detailní infon např. notifikace, problemy v úlech")
//            TODO("Při načítání odkazu z databáze to problikává, po znovu")
            if (stanoviste.imagePath != previousImagePath) {
                previousImagePath = stanoviste.imagePath

            }
            loadImage(stanoviste.imagePath)
        }
    }

    private fun loadImage(imagePath: String?) {
        imagePath?.let { path ->

            val file = File(requireContext().filesDir, path)

            if (file.exists()) {
                Log.d("InfoStanovisteFragment", "Loading image from: $path")
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                Picasso.get()
                    .load(uri)
                    .fit() // přizpůsobí velikosti ImageView
                    .centerInside() // zachová celý obrázek uvnitř
                    .placeholder(com.hruby.sharedresources.R.drawable.ic_launcher_background)
                    .into(binding.stanovisteDetailInfoFragmentImage)

            } else {
                Log.e("InfoStanovisteFragment", "File does not exist at: $path")
            }
        }
    }

    private fun toggleStanovisteInfoFabMenu(fab: FloatingActionButton, stanoviste: Stanoviste) {
        if (isStanovisteInfoFabMenuOpen) {
            fab.setImageResource(com.hruby.sharedresources.R.drawable.arrow_upward_24dp)
            binding.stanovisteDetailInfoFragmentFabEdit.visibility = View.GONE
            binding.stanovisteDetailInfoFragmentFabSync.visibility = View.GONE
            binding.stanovisteDetailInfoFragmentFabPic.visibility = View.GONE
        } else {
            fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.stanovisteDetailInfoFragmentFabEdit.visibility = View.VISIBLE
            if (stanoviste.maMAC) {
                binding.stanovisteDetailInfoFragmentFabSync.visibility = View.VISIBLE
            } else {
                binding.stanovisteDetailInfoFragmentFabSync.visibility = View.GONE
            }
            binding.stanovisteDetailInfoFragmentFabPic.visibility = View.VISIBLE
        }
        isStanovisteInfoFabMenuOpen = !isStanovisteInfoFabMenuOpen
    }

    override fun onStanovisteUpdated(updatedStanoviste: Stanoviste) {
            viewModel.updateStanoviste(updatedStanoviste)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            showImageSelectionDialog()
        } else {
            Toast.makeText(context, "Oprávnění byla zamítnuta", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val newPath = ImageHelper.saveImageToInternalStorage(requireContext(), it)
            updateStanovisteImage(newPath)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                val newPath = ImageHelper.saveImageToInternalStorage(requireContext(), uri)
                updateStanovisteImage(newPath)
            }
        }
    }

    // Funkce pro zobrazení dialogu pro výběr obrázku
    private fun showImageSelectionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Vyberte akci")
            .setItems(arrayOf("Vybrat foto z uložiště", "Vyfotit stanoviště")) { _, which ->
                when (which) {
                    0 -> {
                        if (PermissionHelper.hasPermissions(requireContext(), PermissionHelper.storagePermissionsRead)) {
                            ImageHelper.pickImageFromGallery(galleryLauncher)
                        } else {
                            PermissionHelper.requestPermissions(permissionLauncher, PermissionHelper.storagePermissionsRead)
                        }
                    }
                    1 -> {
                        if (PermissionHelper.hasPermissions(requireContext(), PermissionHelper.cameraPermissions)) {
                            currentPhotoUri = ImageHelper.captureImage(requireContext())
                            currentPhotoUri?.let {
                                cameraLauncher.launch(it)
                            } ?: Toast.makeText(context, "Chyba při přípravě fotoaparátu.", Toast.LENGTH_SHORT).show()
                        } else {
                            PermissionHelper.requestPermissions(permissionLauncher, PermissionHelper.cameraPermissions)
                        }
                    }
                }
            }
            .setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }
            .show()
    }

    // Funkce pro aktualizaci obrázku pro stanoviště v databázi
    private fun updateStanovisteImage(imagePath: String?) {
        imagePath?.let { newImagePath ->
            viewModel.stanoviste.value?.let { stanoviste ->
                // Smazání starého obrázku, pokud existuje
                stanoviste.imagePath?.let { oldImagePath ->
                    ImageHelper.deleteImageFromInternalStorage(oldImagePath)
                }
                // Aktualizace cesty v databázi s relativní cestou
                stanoviste.imagePath = "images/${File(newImagePath).name}"
                viewModel.updateStanoviste(stanoviste)
            }

            val file = File(requireContext().filesDir, newImagePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                Picasso.get()
                    .load(uri)
                    .fit() // přizpůsobí velikosti ImageView
                    .centerInside() // zachová celý obrázek uvnitř
                    .placeholder(com.hruby.sharedresources.R.drawable.ic_launcher_background)
                    .into(binding.stanovisteDetailInfoFragmentImage)
            } else {
                Log.e("InfoStanovisteFragment", "File does not exist at: $newImagePath")
            }
        }
    }

    // Bluetooth logika
    private val bluetoothAndWiFiPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val requiredPermissions = PermissionHelper.bluetoothPermissions + PermissionHelper.wifiPermissions
            val grantedPermissions = permissions.filterValues { it }.keys

            if (requiredPermissions.all { it in grantedPermissions }) {
                synchronizeWithBTDevice()
            } else {
                Toast.makeText(context, "Nedostatečná oprávnění pro Bluetooth nebo WiFi", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermissions() {
        val permissions = PermissionHelper.bluetoothPermissions  + PermissionHelper.wifiPermissions

        // Pokud je pro Bluetooth nějaké oprávnění potřeba, zkontrolujte ho
        if (permissions.isNotEmpty()) {
            val missingPermissions = permissions.filter {
                ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                bluetoothAndWiFiPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                synchronizeWithBTDevice()
            }
        } else {
            synchronizeWithBTDevice()
        }
    }

    private fun synchronizeWithBTDevice(){
        var stanovisteMAC = viewModel.stanoviste.value?.siteMAC.toString()
        val bluetoothEnabled = BluetoothHelper.isBluetoothEnabled()
        val wifiEnabled = WiFiHelper.isWifiEnabled(requireContext())

        if (wifiEnabled && !bluetoothEnabled){
            Toast.makeText(requireContext(), "Zapněte Bluetooth pro synchronizaci.", Toast.LENGTH_SHORT).show()
        } else if (!wifiEnabled && bluetoothEnabled){
            Toast.makeText(requireContext(), "Zapněte WiFi pro synchronizaci", Toast.LENGTH_SHORT).show()
        } else {
            BluetoothHelper.connectToDevice(
                context = requireContext(),
                macAddress = stanovisteMAC,
                onConnected = {
                    // Kód, který se spustí po úspěšném připojení
                    Log.d("Activity", "Device connected, ready to send sync command")
                    WiFiHelper.writeMACForWifi(stanovisteMAC)
                    BluetoothHelper.sendCommand("SEND_TEL",requireContext())
                },
                onError = { error ->
                    // Kód pro ošetření chyby při připojování
                    Log.e("Activity", "Error: $error")
                }
            )

            binding.stanovisteDetailInfoFragmentFab.setImageResource(com.hruby.sharedresources.R.drawable.arrow_upward_24dp)
            binding.stanovisteDetailInfoFragmentFabEdit.visibility = View.GONE
            binding.stanovisteDetailInfoFragmentFabSync.visibility = View.GONE
            binding.stanovisteDetailInfoFragmentFabPic.visibility = View.GONE
            isStanovisteInfoFabMenuOpen = !isStanovisteInfoFabMenuOpen
        }
    }
}